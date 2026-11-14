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
 * Multi-track procedural ambient/flow synthesizer soundtrack using Android AudioTrack.
 * Plays an endless cycle of 5 cohesive industrial-zen tracks with detuned warm pads,
 * polyphonic kalimba/bell plucks, soft sub-bass, micro-percussion, and damped echo delay,
 * using zero external audio assets.
 */
class FlowMusicEngine {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHUNK_SAMPLES = 2048
        private const val TWO_PI = 2.0 * PI
        private const val STEPS_PER_BAR = 16

        private data class FlowTrack(
            val title: String,
            val bpm: Int,
            val barsPerCycle: Int,
            val sectionAChords: Array<DoubleArray>,
            val sectionBChords: Array<DoubleArray>,
            val sectionAPools: Array<DoubleArray>,
            val sectionBPools: Array<DoubleArray>,
            val arpPatternA: IntArray,
            val arpPatternB: IntArray,
            val bassPattern: IntArray,
            val shimmerSteps: IntArray,
            val padBright: Double,
            val arpBright: Double,
            val delayFeedback: Double,
            val hatMask: Int
        )

        private val TRACKS = listOf(
            // 1. Conveyor Drift — classic D Dorian warm Rhodes/kalimba flow
            FlowTrack(
                title = "Conveyor Drift",
                bpm = 94,
                barsPerCycle = 24,
                sectionAChords = arrayOf(
                    doubleArrayOf(73.42, 174.61, 220.00, 261.63, 329.63), // Dm9
                    doubleArrayOf(98.00, 174.61, 246.94, 293.66, 329.63), // G13
                    doubleArrayOf(65.41, 164.81, 196.00, 246.94, 293.66), // Cmaj9
                    doubleArrayOf(110.00, 164.81, 196.00, 261.63, 329.63) // Am7
                ),
                sectionBChords = arrayOf(
                    doubleArrayOf(116.54, 174.61, 220.00, 293.66, 349.23), // Bbmaj7
                    doubleArrayOf(65.41, 164.81, 220.00, 261.63, 392.00),  // C6
                    doubleArrayOf(73.42, 174.61, 220.00, 261.63, 329.63),  // Dm9
                    doubleArrayOf(110.00, 164.81, 246.94, 261.63, 329.63)  // Am9
                ),
                sectionAPools = arrayOf(
                    doubleArrayOf(293.66, 349.23, 440.00, 523.25, 659.25, 587.33, 440.00, 349.23),
                    doubleArrayOf(293.66, 392.00, 493.88, 587.33, 659.25, 587.33, 493.88, 392.00),
                    doubleArrayOf(261.63, 329.63, 392.00, 493.88, 587.33, 493.88, 392.00, 329.63),
                    doubleArrayOf(220.00, 329.63, 392.00, 523.25, 659.25, 523.25, 392.00, 329.63)
                ),
                sectionBPools = arrayOf(
                    doubleArrayOf(349.23, 440.00, 523.25, 587.33, 698.46, 587.33, 523.25, 440.00),
                    doubleArrayOf(329.63, 392.00, 440.00, 523.25, 659.25, 523.25, 440.00, 392.00),
                    doubleArrayOf(293.66, 349.23, 440.00, 523.25, 659.25, 587.33, 523.25, 440.00),
                    doubleArrayOf(329.63, 392.00, 493.88, 523.25, 659.25, 523.25, 493.88, 392.00)
                ),
                arpPatternA = intArrayOf(0, -1, 2, -1, 4, -1, 3, 2, 5, -1, 4, -1, 2, -1, 6, -1),
                arpPatternB = intArrayOf(0, -1, 2, 3, 4, -1, 5, -1, 6, -1, 4, 3, 2, -1, 5, 4),
                bassPattern = intArrayOf(1, 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 0, 0, 0, 2, 0),
                shimmerSteps = intArrayOf(0, 6, 12),
                padBright = 0.024,
                arpBright = 0.18,
                delayFeedback = 0.36,
                hatMask = 0b0100_0100_0100_0100
            ),
            // 2. Copper Rain — luminous F Lydian glass bells & gentle droplets
            FlowTrack(
                title = "Copper Rain",
                bpm = 84,
                barsPerCycle = 24,
                sectionAChords = arrayOf(
                    doubleArrayOf(87.31, 164.81, 220.00, 261.63, 392.00),  // Fmaj9
                    doubleArrayOf(98.00, 164.81, 246.94, 293.66, 392.00),  // G6
                    doubleArrayOf(82.41, 196.00, 246.94, 293.66, 329.63),  // Em7
                    doubleArrayOf(110.00, 196.00, 246.94, 261.63, 329.63)  // Am9
                ),
                sectionBChords = arrayOf(
                    doubleArrayOf(73.42, 174.61, 220.00, 261.63, 329.63),  // Dm9
                    doubleArrayOf(87.31, 164.81, 220.00, 261.63, 349.23),  // Fmaj7
                    doubleArrayOf(65.41, 164.81, 196.00, 246.94, 293.66),  // Cmaj9
                    doubleArrayOf(98.00, 196.00, 220.00, 246.94, 293.66)   // Gadd9
                ),
                sectionAPools = arrayOf(
                    doubleArrayOf(349.23, 440.00, 493.88, 523.25, 659.25, 783.99, 659.25, 523.25),
                    doubleArrayOf(392.00, 440.00, 493.88, 587.33, 659.25, 783.99, 659.25, 493.88),
                    doubleArrayOf(329.63, 392.00, 493.88, 587.33, 659.25, 783.99, 587.33, 493.88),
                    doubleArrayOf(329.63, 392.00, 440.00, 523.25, 659.25, 783.99, 523.25, 440.00)
                ),
                sectionBPools = arrayOf(
                    doubleArrayOf(293.66, 349.23, 440.00, 523.25, 659.25, 587.33, 440.00, 349.23),
                    doubleArrayOf(349.23, 440.00, 523.25, 659.25, 698.46, 659.25, 523.25, 440.00),
                    doubleArrayOf(261.63, 329.63, 392.00, 493.88, 587.33, 659.25, 587.33, 493.88),
                    doubleArrayOf(293.66, 392.00, 440.00, 493.88, 587.33, 783.99, 587.33, 440.00)
                ),
                arpPatternA = intArrayOf(0, -1, -1, 2, -1, 4, -1, 3, 5, -1, 4, -1, 2, -1, -1, 1),
                arpPatternB = intArrayOf(0, -1, 3, 4, -1, 5, -1, 4, 6, -1, 5, -1, 3, 2, -1, 4),
                bassPattern = intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0),
                shimmerSteps = intArrayOf(2, 10, 14),
                padBright = 0.018,
                arpBright = 0.25,
                delayFeedback = 0.42,
                hatMask = 0b0010_0000_0100_0010
            ),
            // 3. Midnight Smelter — focused late-night Aeolian pulse
            FlowTrack(
                title = "Midnight Smelter",
                bpm = 96,
                barsPerCycle = 24,
                sectionAChords = arrayOf(
                    doubleArrayOf(110.00, 164.81, 196.00, 246.94, 261.63), // Am9
                    doubleArrayOf(87.31, 164.81, 196.00, 220.00, 261.63),  // Fmaj9
                    doubleArrayOf(73.42, 174.61, 220.00, 261.63, 329.63),  // Dm9
                    doubleArrayOf(82.41, 196.00, 246.94, 293.66, 329.63)   // Em7
                ),
                sectionBChords = arrayOf(
                    doubleArrayOf(98.00, 164.81, 246.94, 293.66, 392.00),  // G6
                    doubleArrayOf(87.31, 174.61, 220.00, 261.63, 329.63),  // Fmaj7
                    doubleArrayOf(73.42, 174.61, 220.00, 261.63, 293.66),  // Dm7
                    doubleArrayOf(82.41, 164.81, 220.00, 246.94, 329.63)   // Esus4
                ),
                sectionAPools = arrayOf(
                    doubleArrayOf(220.00, 261.63, 329.63, 392.00, 493.88, 523.25, 493.88, 392.00),
                    doubleArrayOf(220.00, 261.63, 349.23, 392.00, 440.00, 523.25, 440.00, 349.23),
                    doubleArrayOf(293.66, 349.23, 440.00, 523.25, 587.33, 659.25, 523.25, 440.00),
                    doubleArrayOf(246.94, 329.63, 392.00, 493.88, 587.33, 659.25, 587.33, 493.88)
                ),
                sectionBPools = arrayOf(
                    doubleArrayOf(293.66, 392.00, 493.88, 587.33, 659.25, 587.33, 493.88, 392.00),
                    doubleArrayOf(261.63, 349.23, 440.00, 523.25, 659.25, 523.25, 440.00, 349.23),
                    doubleArrayOf(293.66, 349.23, 440.00, 523.25, 587.33, 523.25, 440.00, 349.23),
                    doubleArrayOf(246.94, 329.63, 440.00, 493.88, 659.25, 587.33, 493.88, 329.63)
                ),
                arpPatternA = intArrayOf(0, -1, 2, -1, 3, 4, -1, 2, 5, -1, 4, -1, 3, -1, 1, -1),
                arpPatternB = intArrayOf(0, 2, -1, 4, 3, -1, 5, 4, 6, -1, 5, 3, 4, -1, 2, -1),
                bassPattern = intArrayOf(1, 0, 0, 2, 0, 0, 1, 0, 1, 0, 0, 0, 2, 0, 1, 0),
                shimmerSteps = intArrayOf(0, 8),
                padBright = 0.030,
                arpBright = 0.14,
                delayFeedback = 0.34,
                hatMask = 0b0101_0010_0101_0010
            ),
            // 4. Clockwork Tide — rhythmic G Mixolydian polyrhythmic groove
            FlowTrack(
                title = "Clockwork Tide",
                bpm = 100,
                barsPerCycle = 24,
                sectionAChords = arrayOf(
                    doubleArrayOf(98.00, 185.00, 220.00, 246.94, 293.66),  // Gmaj9
                    doubleArrayOf(73.42, 174.61, 220.00, 261.63, 293.66),  // Dm7
                    doubleArrayOf(65.41, 164.81, 196.00, 246.94, 293.66),  // Cmaj9
                    doubleArrayOf(73.42, 196.00, 220.00, 293.66, 392.00)   // Dsus4
                ),
                sectionBChords = arrayOf(
                    doubleArrayOf(82.41, 185.00, 196.00, 246.94, 293.66),  // Em9
                    doubleArrayOf(65.41, 164.81, 196.00, 246.94, 293.66),  // Cmaj9
                    doubleArrayOf(110.00, 164.81, 196.00, 261.63, 329.63), // Am7
                    doubleArrayOf(73.42, 185.00, 220.00, 246.94, 293.66)   // D6
                ),
                sectionAPools = arrayOf(
                    doubleArrayOf(293.66, 369.99, 392.00, 440.00, 493.88, 587.33, 493.88, 392.00),
                    doubleArrayOf(293.66, 349.23, 440.00, 523.25, 587.33, 659.25, 523.25, 440.00),
                    doubleArrayOf(261.63, 329.63, 392.00, 493.88, 587.33, 659.25, 587.33, 493.88),
                    doubleArrayOf(293.66, 392.00, 440.00, 587.33, 659.25, 587.33, 440.00, 392.00)
                ),
                sectionBPools = arrayOf(
                    doubleArrayOf(329.63, 369.99, 392.00, 493.88, 587.33, 739.99, 587.33, 493.88),
                    doubleArrayOf(329.63, 392.00, 493.88, 523.25, 587.33, 659.25, 587.33, 493.88),
                    doubleArrayOf(261.63, 329.63, 392.00, 440.00, 523.25, 659.25, 523.25, 440.00),
                    doubleArrayOf(293.66, 369.99, 440.00, 493.88, 587.33, 739.99, 587.33, 440.00)
                ),
                arpPatternA = intArrayOf(0, -1, 1, 3, -1, 4, 2, -1, 5, -1, 3, 4, -1, 2, 6, -1),
                arpPatternB = intArrayOf(0, 2, -1, 3, 5, -1, 4, 2, 6, -1, 5, 3, 4, -1, 2, 4),
                bassPattern = intArrayOf(1, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 2, 0, 0, 2, 0),
                shimmerSteps = intArrayOf(4, 12),
                padBright = 0.022,
                arpBright = 0.21,
                delayFeedback = 0.35,
                hatMask = 0b0100_0101_0100_0101
            ),
            // 5. Solar Circuit — uplifting E Dorian atmospheric horizon
            FlowTrack(
                title = "Solar Circuit",
                bpm = 88,
                barsPerCycle = 24,
                sectionAChords = arrayOf(
                    doubleArrayOf(82.41, 185.00, 196.00, 246.94, 293.66),  // Em9
                    doubleArrayOf(110.00, 185.00, 196.00, 277.18, 329.63), // A13
                    doubleArrayOf(73.42, 185.00, 220.00, 277.18, 329.63),  // Dmaj9
                    doubleArrayOf(123.47, 185.00, 220.00, 293.66, 329.63)  // Bm7
                ),
                sectionBChords = arrayOf(
                    doubleArrayOf(65.41, 164.81, 196.00, 246.94, 293.66),  // Cmaj9
                    doubleArrayOf(73.42, 185.00, 220.00, 246.94, 293.66),  // D6
                    doubleArrayOf(82.41, 185.00, 196.00, 246.94, 293.66),  // Em9
                    doubleArrayOf(123.47, 164.81, 185.00, 220.00, 329.63)  // B7sus4
                ),
                sectionAPools = arrayOf(
                    doubleArrayOf(329.63, 369.99, 493.88, 587.33, 659.25, 739.99, 659.25, 493.88),
                    doubleArrayOf(329.63, 440.00, 554.37, 659.25, 739.99, 659.25, 554.37, 440.00),
                    doubleArrayOf(293.66, 369.99, 440.00, 554.37, 659.25, 587.33, 440.00, 369.99),
                    doubleArrayOf(293.66, 369.99, 493.88, 587.33, 659.25, 587.33, 493.88, 369.99)
                ),
                sectionBPools = arrayOf(
                    doubleArrayOf(261.63, 329.63, 392.00, 493.88, 587.33, 659.25, 587.33, 493.88),
                    doubleArrayOf(293.66, 369.99, 440.00, 493.88, 587.33, 739.99, 587.33, 440.00),
                    doubleArrayOf(329.63, 369.99, 493.88, 587.33, 739.99, 659.25, 587.33, 493.88),
                    doubleArrayOf(329.63, 369.99, 440.00, 493.88, 659.25, 587.33, 493.88, 440.00)
                ),
                arpPatternA = intArrayOf(0, -1, 2, -1, 4, -1, 5, -1, 3, -1, 4, 2, 1, -1, 6, -1),
                arpPatternB = intArrayOf(0, -1, 2, 4, -1, 5, 3, -1, 6, -1, 4, 3, 2, -1, 5, -1),
                bassPattern = intArrayOf(1, 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 0, 2, 0, 0, 0),
                shimmerSteps = intArrayOf(0, 6, 10, 14),
                padBright = 0.020,
                arpBright = 0.22,
                delayFeedback = 0.40,
                hatMask = 0b0010_0100_0010_0100
            )
        )
    }

    private data class PluckVoice(
        val freq: Double,
        var phase: Double = 0.0,
        var ageSamples: Int = 0,
        val durationSamples: Int,
        val gain: Double,
        val harmonicMix: Double,
        val decayRate: Double
    )

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

    @Volatile
    private var currentTrackIndex = 0

    @Volatile
    private var skipRequested = false

    @Volatile
    var onTrackChanged: ((String) -> Unit)? = null

    private var audioThread: Thread? = null
    private val pendingSfx = ConcurrentLinkedQueue<SfxVoice>()

    fun getCurrentTrackTitle(): String = TRACKS[currentTrackIndex].title

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun isMuted(): Boolean = isMuted

    fun nextTrack() {
        skipRequested = true
    }

    fun playPlaceSound() {
        if (!isRunning || isMuted) return
        pendingSfx.add(
            SfxVoice(
                freq = 587.33,
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
                    durationSamples = (SAMPLE_RATE * 0.70).toInt(),
                    delaySamples = (SAMPLE_RATE * 0.09 * idx).toInt(),
                    gain = 0.16,
                    decayRate = 5.2
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
        val activePlucks = mutableListOf<PluckVoice>()

        // Warm ambient echo ring buffer (1 second)
        val delayBuf = DoubleArray(SAMPLE_RATE)
        var delayWriteIdx = 0
        var delayLp = 0.0

        // Detuned twin pad oscillators
        val padPhasesA = DoubleArray(5)
        val padPhasesB = DoubleArray(5)
        var activeTrack = TRACKS[currentTrackIndex]
        val padCurrentFreqs = activeTrack.sectionAChords[0].copyOf()
        var lfoPhase = 0.0

        // Soft plucked sub-bass
        var bassPhase = 0.0
        var bassFreq = activeTrack.sectionAChords[0][0]
        var bassAge = SAMPLE_RATE

        // Soft conveyor pulse & micro-hat noise
        var pulsePhase = 0.0
        var pulseAge = SAMPLE_RATE
        var hatAge = SAMPLE_RATE
        var lfsr = 0x5EED1234
        var prevNoise = 0.0

        var stepSamples = ((SAMPLE_RATE * 60.0 / activeTrack.bpm) / 4.0).toInt()
        var globalStep = 0
        var stepSample = 0

        var masterGain = if (isMuted) 0.0 else 1.0
        var songGain = 0.0
        var fadingOutForSkip = false
        onTrackChanged?.invoke(activeTrack.title)

        try {
            while (isRunning) {
                while (true) {
                    val v = pendingSfx.poll() ?: break
                    if (activeSfx.size < 12) activeSfx.add(v)
                }

                if (skipRequested && !fadingOutForSkip) {
                    skipRequested = false
                    fadingOutForSkip = true
                }

                val targetMaster = if (isMuted) 0.0 else 1.0

                for (i in 0 until CHUNK_SAMPLES) {
                    masterGain += (targetMaster - masterGain) * 0.002

                    val totalStepsInSong = activeTrack.barsPerCycle * STEPS_PER_BAR
                    val barTotal = (globalStep / STEPS_PER_BAR) % activeTrack.barsPerCycle
                    val phraseIndex = barTotal / 4 // 0..5 across 24 bars: A1, A2, B1, B2, A3, Outro
                    val isOutroBar = phraseIndex == 5 && (barTotal % 4 >= 2)
                    val isFadingOut = fadingOutForSkip || isOutroBar

                    if (isFadingOut) {
                        songGain = (songGain - 0.000045).coerceAtLeast(0.0)
                        if (songGain <= 0.0005 && (fadingOutForSkip || globalStep >= totalStepsInSong - 1)) {
                            // Cross into next song smoothly
                            currentTrackIndex = (currentTrackIndex + 1) % TRACKS.size
                            activeTrack = TRACKS[currentTrackIndex]
                            stepSamples = ((SAMPLE_RATE * 60.0 / activeTrack.bpm) / 4.0).toInt()
                            globalStep = 0
                            stepSample = 0
                            fadingOutForSkip = false
                            activePlucks.clear()
                            onTrackChanged?.invoke(activeTrack.title)
                        }
                    } else {
                        songGain = (songGain + 0.000035).coerceAtMost(1.0)
                    }

                    // Advance 16th-note step sequencer
                    if (stepSample >= stepSamples) {
                        stepSample = 0
                        globalStep++
                        if (globalStep >= totalStepsInSong) {
                            currentTrackIndex = (currentTrackIndex + 1) % TRACKS.size
                            activeTrack = TRACKS[currentTrackIndex]
                            stepSamples = ((SAMPLE_RATE * 60.0 / activeTrack.bpm) / 4.0).toInt()
                            globalStep = 0
                            activePlucks.clear()
                            onTrackChanged?.invoke(activeTrack.title)
                        }

                        val curBar = (globalStep / STEPS_PER_BAR) % activeTrack.barsPerCycle
                        val curPhrase = curBar / 4
                        val inSectionB = curPhrase == 2 || curPhrase == 3
                        val barInPhrase = curBar % 4
                        val stepInBar = globalStep % STEPS_PER_BAR

                        val chord = if (inSectionB) activeTrack.sectionBChords[barInPhrase] else activeTrack.sectionAChords[barInPhrase]
                        val pool = if (inSectionB) activeTrack.sectionBPools[barInPhrase] else activeTrack.sectionAPools[barInPhrase]
                        val pattern = if (inSectionB) activeTrack.arpPatternB else activeTrack.arpPatternA

                        // Main polyphonic kalimba/bell voice
                        val rawIdx = pattern[stepInBar]
                        if (rawIdx >= 0 && activePlucks.size < 8) {
                            val variation = (curPhrase % 2)
                            val noteFreq = pool[(rawIdx + variation) % pool.size]
                            val accent = if (stepInBar == 0 || stepInBar == 8) 1.0 else 0.82
                            activePlucks.add(
                                PluckVoice(
                                    freq = noteFreq,
                                    durationSamples = (SAMPLE_RATE * 0.95).toInt(),
                                    gain = 0.21 * accent,
                                    harmonicMix = activeTrack.arpBright,
                                    decayRate = 4.6
                                )
                            )
                        }

                        // Delicate octave/fifth shimmer during Section B & A3
                        if ((curPhrase in 2..4) && stepInBar in activeTrack.shimmerSteps && activePlucks.size < 8) {
                            val shimmerFreq = pool[(stepInBar + 3) % pool.size] * 2.0
                            activePlucks.add(
                                PluckVoice(
                                    freq = shimmerFreq,
                                    durationSamples = (SAMPLE_RATE * 1.15).toInt(),
                                    gain = 0.085,
                                    harmonicMix = 0.28,
                                    decayRate = 3.4
                                )
                            )
                        }

                        // Plucked sub-bass
                        val bassTrig = activeTrack.bassPattern[stepInBar]
                        if (bassTrig > 0) {
                            bassFreq = if (bassTrig == 2) chord[0] * 1.5 else chord[0]
                            bassAge = 0
                        }

                        // Soft pulse kick on beats 0 & 8 (subtle flow momentum)
                        if (stepInBar == 0 || (curPhrase in 1..4 && stepInBar == 8)) {
                            pulseAge = 0
                            pulsePhase = 0.0
                        }

                        // Micro-brush shaker on hatMask bits (after intro bar)
                        if (curPhrase >= 1 && ((activeTrack.hatMask ushr (15 - stepInBar)) and 1) == 1) {
                            hatAge = 0
                        }
                    }

                    val curBar = (globalStep / STEPS_PER_BAR) % activeTrack.barsPerCycle
                    val curPhrase = curBar / 4
                    val inSectionB = curPhrase == 2 || curPhrase == 3
                    val barInPhrase = curBar % 4
                    val targetChord = if (inSectionB) activeTrack.sectionBChords[barInPhrase] else activeTrack.sectionAChords[barInPhrase]

                    // 1. Warm detuned breathing pad
                    lfoPhase += TWO_PI * 0.12 / SAMPLE_RATE
                    if (lfoPhase >= TWO_PI) lfoPhase -= TWO_PI
                    val padLfo = 0.78 + 0.22 * sin(lfoPhase)

                    var padSample = 0.0
                    for (v in 0 until 5) {
                        padCurrentFreqs[v] += (targetChord[v] - padCurrentFreqs[v]) * 0.00042
                        val incA = TWO_PI * padCurrentFreqs[v] / SAMPLE_RATE
                        val incB = TWO_PI * (padCurrentFreqs[v] * 1.0022) / SAMPLE_RATE
                        padPhasesA[v] += incA
                        padPhasesB[v] += incB
                        if (padPhasesA[v] >= TWO_PI) padPhasesA[v] -= TWO_PI
                        if (padPhasesB[v] >= TWO_PI) padPhasesB[v] -= TWO_PI

                        val weight = if (v == 0) 0.22 else 0.115
                        val osc = (sin(padPhasesA[v]) + sin(padPhasesB[v])) * 0.5
                        padSample += osc * weight
                        if (v > 0) {
                            padSample += sin(padPhasesA[v] * 2.0) * activeTrack.padBright
                        }
                    }
                    padSample *= padLfo * 0.58

                    // 2. Soft plucked sub-bass
                    var bassSample = 0.0
                    if (bassAge < SAMPLE_RATE) {
                        val bt = bassAge.toDouble() / SAMPLE_RATE
                        val bAttack = (bassAge / (SAMPLE_RATE * 0.012)).coerceAtMost(1.0)
                        val bEnv = bAttack * exp(-bt * 3.8)
                        bassPhase += TWO_PI * bassFreq / SAMPLE_RATE
                        if (bassPhase >= TWO_PI) bassPhase -= TWO_PI
                        bassSample = (sin(bassPhase) * 0.88 + sin(bassPhase * 2.0) * 0.12) * bEnv * 0.20
                    }

                    // 3. Polyphonic kalimba / bell plucks
                    var melodicDry = 0.0
                    val pluckIt = activePlucks.iterator()
                    while (pluckIt.hasNext()) {
                        val p = pluckIt.next()
                        if (p.ageSamples >= p.durationSamples) {
                            pluckIt.remove()
                            continue
                        }
                        val pt = p.ageSamples.toDouble() / SAMPLE_RATE
                        val attack = (p.ageSamples / (SAMPLE_RATE * 0.005)).coerceAtMost(1.0)
                        val env = attack * exp(-pt * p.decayRate)
                        p.phase += TWO_PI * p.freq / SAMPLE_RATE
                        if (p.phase >= TWO_PI) p.phase -= TWO_PI
                        val tone = sin(p.phase) * (1.0 - p.harmonicMix) + sin(p.phase * 2.0) * p.harmonicMix
                        melodicDry += tone * env * p.gain
                        p.ageSamples++
                    }

                    // 4. Damped dual-tap ambient delay on melodic plucks
                    val tap1Idx = (delayWriteIdx - (SAMPLE_RATE * 0.34).toInt() + SAMPLE_RATE) % SAMPLE_RATE
                    val tap2Idx = (delayWriteIdx - (SAMPLE_RATE * 0.52).toInt() + SAMPLE_RATE) % SAMPLE_RATE
                    val rawTap = delayBuf[tap1Idx] * 0.62 + delayBuf[tap2Idx] * 0.38
                    delayLp += 0.24 * (rawTap - delayLp)
                    delayBuf[delayWriteIdx] = melodicDry + delayLp * activeTrack.delayFeedback
                    delayWriteIdx = (delayWriteIdx + 1) % SAMPLE_RATE
                    val melodicSample = melodicDry + delayLp * 0.38

                    // 5. Soft conveyor kick pulse & brushed micro-hat
                    var percSample = 0.0
                    if (pulseAge < SAMPLE_RATE * 0.16) {
                        val pt = pulseAge.toDouble() / SAMPLE_RATE
                        val pFreq = 42.0 + 44.0 * exp(-pt * 36.0)
                        pulsePhase += TWO_PI * pFreq / SAMPLE_RATE
                        if (pulsePhase >= TWO_PI) pulsePhase -= TWO_PI
                        val pAttack = (pulseAge / (SAMPLE_RATE * 0.004)).coerceAtMost(1.0)
                        percSample += sin(pulsePhase) * pAttack * exp(-pt * 19.0) * 0.17
                    }
                    if (hatAge < SAMPLE_RATE * 0.045) {
                        val ht = hatAge.toDouble() / SAMPLE_RATE
                        // Deterministic fast LFSR noise + simple 1-pole high-pass
                        val bit = ((lfsr ushr 0) xor (lfsr ushr 2) xor (lfsr ushr 3) xor (lfsr ushr 5)) and 1
                        lfsr = (lfsr ushr 1) or (bit shl 30)
                        val white = ((lfsr and 0xFFFF) / 32768.0) - 1.0
                        val hp = white - prevNoise
                        prevNoise = white
                        percSample += hp * exp(-ht * 95.0) * 0.022
                    }

                    // 6. Micro-SFX mix (not attenuated by song transition gain)
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

                    val musicMix = (padSample + bassSample + melodicSample + percSample) * songGain
                    val rawMix = (musicMix + sfxSample) * masterGain
                    val limited = tanh(rawMix * 1.12) * 0.45
                    buffer[i] = (limited * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

                    stepSample++
                    bassAge++
                    pulseAge++
                    hatAge++
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
