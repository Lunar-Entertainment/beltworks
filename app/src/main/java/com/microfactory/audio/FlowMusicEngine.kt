package com.microfactory.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.tanh

/**
 * Procedural ambient/flow synthesizer soundtrack using Android AudioTrack.
 * Generates warm chord pads, pentatonic marimba-style arpeggios, and subtle conveyor pulse
 * with zero external audio assets.
 */
class FlowMusicEngine {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHUNK_SAMPLES = 2048
        private const val TWO_PI = 2.0 * PI

        // 96 BPM -> 1 beat = 0.625s, 8th-note arp step = 0.3125s (~13781 samples)
        private const val STEP_SAMPLES = (SAMPLE_RATE * 0.3125).toInt()
        private const val STEPS_PER_BAR = 8
        private const val NUM_BARS = 4

        // D Dorian / C Major 9th flow progression: Dm9 -> G13 -> Cmaj9 -> Am7
        private val CHORDS = arrayOf(
            doubleArrayOf(73.42, 174.61, 220.00, 261.63, 329.63), // Dm9
            doubleArrayOf(98.00, 174.61, 246.94, 293.66, 329.63), // G13
            doubleArrayOf(65.41, 164.81, 196.00, 246.94, 293.66), // Cmaj9
            doubleArrayOf(110.00, 164.81, 196.00, 261.63, 329.63) // Am7
        )

        // Melodic arp scale pools per chord (octave-shifted soft bell/kalimba notes)
        private val ARP_POOLS = arrayOf(
            doubleArrayOf(293.66, 349.23, 440.00, 523.25, 659.25, 523.25, 440.00, 349.23),
            doubleArrayOf(293.66, 392.00, 493.88, 587.33, 659.25, 587.33, 493.88, 392.00),
            doubleArrayOf(261.63, 329.63, 392.00, 493.88, 587.33, 493.88, 392.00, 329.63),
            doubleArrayOf(220.00, 329.63, 392.00, 523.25, 659.25, 523.25, 392.00, 329.63)
        )

        private val ARP_PATTERN = intArrayOf(0, 2, 4, 3, 5, 4, 2, 6)
    }

    private data class SfxVoice(
        var freq: Double,
        var phase: Double = 0.0,
        var ageSamples: Int = 0,
        val durationSamples: Int,
        val delaySamples: Int = 0,
        val gain: Double = 0.16,
        val decayRate: Double = 14.0
    )

    @Volatile
    private var isRunning = false

    @Volatile
    private var isMuted = false

    private var audioThread: Thread? = null
    private val pendingSfx = ConcurrentLinkedQueue<SfxVoice>()

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun isMuted(): Boolean = isMuted

    fun playPlaceSound() {
        if (!isRunning || isMuted) return
        pendingSfx.add(
            SfxVoice(
                freq = 587.33, // D5 crisp soft click-chime
                durationSamples = (SAMPLE_RATE * 0.09).toInt(),
                gain = 0.12,
                decayRate = 38.0
            )
        )
    }

    fun playVictoryChime() {
        if (!isRunning || isMuted) return
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        notes.forEachIndexed { idx, f ->
            pendingSfx.add(
                SfxVoice(
                    freq = f,
                    durationSamples = (SAMPLE_RATE * 0.65).toInt(),
                    delaySamples = (SAMPLE_RATE * 0.09 * idx).toInt(),
                    gain = 0.16,
                    decayRate = 5.5
                )
            )
        }
    }

    @Synchronized
    fun start() {
        if (isRunning) return
        isRunning = true
        val thread = Thread(::runAudioLoop, "FlowMusicAudioThread").apply {
            isDaemon = true
        }
        audioThread = thread
        thread.start()
    }

    @Synchronized
    fun stop() {
        isRunning = false
        audioThread?.join(300)
        audioThread = null
        pendingSfx.clear()
    }

    private fun runAudioLoop() {
        runCatching {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
        }

        val minBuf = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(CHUNK_SAMPLES * 2)

        val track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBuf * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        }.getOrNull() ?: return

        try {
            track.play()
        } catch (_: Throwable) {
            runCatching { track.release() }
            return
        }

        val buffer = ShortArray(CHUNK_SAMPLES)
        val activeSfx = mutableListOf<SfxVoice>()

        val padPhases = DoubleArray(5)
        val padCurrentFreqs = CHORDS[0].copyOf()
        var lfoPhase = 0.0

        var globalStep = 0
        var stepSample = 0
        var arpFreq = ARP_POOLS[0][0]
        var arpPhase = 0.0
        var arpAge = 0

        var pulsePhase = 0.0
        var pulseAge = STEP_SAMPLES // silent until first trigger

        var masterGain = if (isMuted) 0.0 else 1.0

        try {
            while (isRunning) {
                while (true) {
                    val v = pendingSfx.poll() ?: break
                    if (activeSfx.size < 12) activeSfx.add(v)
                }

                val targetMaster = if (isMuted) 0.0 else 1.0

                for (i in 0 until CHUNK_SAMPLES) {
                    // Smooth mute/unmute ramp to avoid clicks
                    masterGain += (targetMaster - masterGain) * 0.002

                    if (stepSample >= STEP_SAMPLES) {
                        stepSample = 0
                        globalStep++
                        val bar = (globalStep / STEPS_PER_BAR) % NUM_BARS
                        val stepInBar = globalStep % STEPS_PER_BAR
                        val phraseVariation = ((globalStep / (STEPS_PER_BAR * NUM_BARS)) % 2)
                        val noteIdx = (ARP_PATTERN[stepInBar] + phraseVariation) % ARP_POOLS[bar].size
                        arpFreq = ARP_POOLS[bar][noteIdx]
                        arpAge = 0

                        // Subtle soft conveyor pulse on beats 0 and 4
                        if (stepInBar == 0 || stepInBar == 4) {
                            pulseAge = 0
                            pulsePhase = 0.0
                        }
                    }

                    val bar = (globalStep / STEPS_PER_BAR) % NUM_BARS
                    val targetChord = CHORDS[bar]

                    // 1. Warm breathing pad
                    lfoPhase += TWO_PI * 0.14 / SAMPLE_RATE
                    if (lfoPhase >= TWO_PI) lfoPhase -= TWO_PI
                    val padLfo = 0.80 + 0.20 * sin(lfoPhase)

                    var padSample = 0.0
                    for (v in 0 until 5) {
                        padCurrentFreqs[v] += (targetChord[v] - padCurrentFreqs[v]) * 0.00045
                        padPhases[v] += TWO_PI * padCurrentFreqs[v] / SAMPLE_RATE
                        if (padPhases[v] >= TWO_PI) padPhases[v] -= TWO_PI
                        val weight = if (v == 0) 0.28 else 0.13
                        padSample += sin(padPhases[v]) * weight
                        if (v > 0) {
                            padSample += sin(padPhases[v] * 2.0) * 0.02
                        }
                    }
                    padSample *= padLfo * 0.65

                    // 2. Gentle flow arpeggio (kalimba/rhodes envelope)
                    val arpTime = arpAge.toDouble() / SAMPLE_RATE
                    val arpAttack = (arpAge / (SAMPLE_RATE * 0.006)).coerceAtMost(1.0)
                    val arpEnv = arpAttack * exp(-arpTime * 5.2)
                    arpPhase += TWO_PI * arpFreq / SAMPLE_RATE
                    if (arpPhase >= TWO_PI) arpPhase -= TWO_PI
                    val arpSample = (sin(arpPhase) * 0.82 + sin(arpPhase * 2.0) * 0.18) * arpEnv * 0.26

                    // 3. Soft sub-pulse
                    var pulseSample = 0.0
                    if (pulseAge < SAMPLE_RATE * 0.16) {
                        val pt = pulseAge.toDouble() / SAMPLE_RATE
                        val pFreq = 42.0 + 46.0 * exp(-pt * 35.0)
                        pulsePhase += TWO_PI * pFreq / SAMPLE_RATE
                        if (pulsePhase >= TWO_PI) pulsePhase -= TWO_PI
                        val pAttack = (pulseAge / (SAMPLE_RATE * 0.004)).coerceAtMost(1.0)
                        pulseSample = sin(pulsePhase) * pAttack * exp(-pt * 18.0) * 0.22
                    }

                    // 4. Micro-SFX mix
                    var sfxSample = 0.0
                    val sfxIt = activeSfx.iterator()
                    while (sfxIt.hasNext()) {
                        val sfx = sfxIt.next()
                        if (sfx.ageSamples < sfx.delaySamples) {
                            sfx.ageSamples++
                            continue
                        }
                        val activeAge = sfx.ageSamples - sfx.delaySamples
                        if (activeAge >= sfx.durationSamples) {
                            sfxIt.remove()
                            continue
                        }
                        val st = activeAge.toDouble() / SAMPLE_RATE
                        val env = (activeAge / (SAMPLE_RATE * 0.003)).coerceAtMost(1.0) * exp(-st * sfx.decayRate)
                        sfx.phase += TWO_PI * sfx.freq / SAMPLE_RATE
                        if (sfx.phase >= TWO_PI) sfx.phase -= TWO_PI
                        sfxSample += (sin(sfx.phase) * 0.85 + sin(sfx.phase * 2.0) * 0.15) * env * sfx.gain
                        sfx.ageSamples++
                    }

                    val rawMix = (padSample + arpSample + pulseSample + sfxSample) * masterGain
                    val limited = tanh(rawMix * 1.15) * 0.42
                    buffer[i] = (limited * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

                    stepSample++
                    arpAge++
                    pulseAge++
                }

                val written = track.write(buffer, 0, CHUNK_SAMPLES)
                if (written < 0) break
            }
        } finally {
            runCatching { track.stop() }
            runCatching { track.release() }
        }
    }
}
