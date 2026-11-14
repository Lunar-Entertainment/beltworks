# Privacy Policy for Beltworks

**Effective Date:** November 14, 2026  
**Application:** Beltworks (`com.lunarentertainment.beltworks`)  
**Developer:** Lunar Entertainment  

Lunar Entertainment respects your privacy. This Privacy Policy explains how **Beltworks** handles information when you use the Android mobile application.

---

## 1. Summary

- **No Developer Account or Cloud Tracking:** Lunar Entertainment does not operate user accounts, analytics servers, or custom tracking in Beltworks.
- **Optional Help Ads (Google AdMob):** Beltworks includes **one optional skippable ad integration** powered by **Google AdMob** used exclusively when you tap **? Help** inside a puzzle level (and only when **Help Ad Before Hint** is turned ON in Settings).
- **In-Game Ad Toggle:** Help Ads are enabled by default to support development, and can be toggled **OFF** at any time in the in-game **Settings** menu.

---

## 2. Information Handled by Third-Party Advertising (Google AdMob)

When **Help Ad Before Hint** is enabled in Settings, Beltworks uses the **Google Mobile Ads SDK (AdMob)** to load and display an optional skippable Help ad (`INTERNET` and `ACCESS_NETWORK_STATE` permissions).

When AdMob loads an ad, Google may automatically process technical information such as:
- Device Advertising ID (`com.google.android.gms.permission.AD_ID`), subject to your Android OS ad privacy settings
- IP address (used for approximate region / fraud prevention)
- Basic device and app interaction diagnostics needed to serve and measure the ad

You can review Google's Privacy Policy here: https://policies.google.com/privacy  
If you toggle **Help Ad Before Hint** OFF in Settings, Beltworks does not trigger Help ad requests when opening Help.

---

## 3. Information Beltworks Does Not Collect

Lunar Entertainment does **not** directly collect, store on external servers, or sell:
- Names, email addresses, passwords, or account profiles
- Precise GPS location
- Contacts, photos, media, microphone, or camera data
- Payment or financial information

---

## 4. Local On-Device Storage

To preserve your gameplay progress and preferences, Beltworks stores minimal settings locally on your device using Android's private `SharedPreferences` (`MODE_PRIVATE`):
- Best star ratings, lowest belt counts, and unlocked level progression
- Last played level ID
- Audio preference (`music_enabled`)
- Help Ad toggle state (`help_ads_enabled`) and update notice acknowledgment (`last_seen_notice_version`)

This data stays on your device unless you have Android system backup enabled via your device settings. You can delete all locally stored data at any time by clearing the app's storage in Android Settings or uninstalling the app.

---

## 5. Audio

All music and sound effects in Beltworks are synthesized locally on your device via Android's `AudioTrack` API without downloading external audio streams.

---

## 6. Children's Privacy

Beltworks does not knowingly collect personal information from children under 13 (or the applicable age in your jurisdiction). Parents or guardians can disable optional Help Ads at any time in the app's **Settings** menu or restrict Advertising ID access in Android system settings.

---

## 7. Changes to This Privacy Policy

If future updates change how data or ads are handled, this Privacy Policy will be updated along with a new effective date, and significant updates are highlighted in the app's update notice.

---

## 8. Contact

If you have questions about this Privacy Policy or Beltworks, please contact **Lunar Entertainment**:

- GitHub Repository: https://github.com/Lunar-Entertainment/micro-factory-belt
- Support / Issues: https://github.com/Lunar-Entertainment/micro-factory-belt/issues
