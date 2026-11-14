# Privacy Policy for Beltworks

**Effective Date:** November 14, 2026  
**Application:** Beltworks (`com.lunarentertainment.beltworks`)  
**Developer:** Lunar Entertainment  

Lunar Entertainment respects your privacy. This Privacy Policy explains how **Beltworks** handles information when you use the Android mobile application.

---

## 1. Summary

**Beltworks does not collect, store on external servers, share, or sell any personal data.**

The app runs 100% offline on your device.

---

## 2. Information We Do Not Collect

Beltworks does **not** collect or process:
- Personal identifiers (name, email address, phone number, account IDs, or advertising IDs)
- Device location data
- Usage analytics, telemetry, or crash reports sent to external servers
- Contacts, photos, media, microphone, camera, or sensor data
- Financial or payment information

The application does **not** include advertisements, third-party analytics SDKs, social media SDKs, or tracking libraries.

---

## 3. Permissions & Network Access

Beltworks requests **no sensitive Android permissions** and does **not** request the `INTERNET` permission in its `AndroidManifest.xml`. The game cannot transmit data over the network.

All audio in the game is synthesized locally on your device using Android's `AudioTrack` API without downloading or streaming external media.

---

## 4. Local On-Device Storage

To preserve your gameplay progress, Beltworks stores minimal non-personal game state locally on your device using Android's private `SharedPreferences` (`MODE_PRIVATE`):
- Best star ratings and lowest belt counts per level
- Last played level ID
- Soundtrack mute/unmute preference (`music_enabled`)

This data stays strictly on your device unless you have Android system device backup enabled on your phone (via Android's standard `allowBackup` setting, managed through your Google/Android device settings). You can delete all locally stored game data at any time by clearing the app's storage in Android Settings or uninstalling the app.

---

## 5. Children's Privacy

Because Beltworks collects no personal information from anyone and operates entirely offline without accounts or ads, no personal data from children under 13 (or the applicable age in your jurisdiction) is collected or transmitted.

---

## 6. Changes to This Privacy Policy

If a future version of Beltworks introduces features that affect data handling (such as cloud saves, leaderboards, or optional online services), this Privacy Policy will be updated before those changes take effect, along with a revised effective date.

---

## 7. Contact

If you have questions about this Privacy Policy or Beltworks, please contact **Lunar Entertainment** via the project repository:

- GitHub Repository: https://github.com/Lunar-Entertainment/micro-factory-belt
- Support / Issues: https://github.com/Lunar-Entertainment/micro-factory-belt/issues
