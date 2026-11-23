# BLACK CAT AI

A Kotlin + Jetpack Compose Android personal assistant foundation.

## Current first-build features
- Bengali and English dashboard labels.
- Editable preferred form of address and language preference (stored locally).
- Explicit microphone activation using Android SpeechRecognizer.
- Spoken responses using the Android TextToSpeech engine.
- Safe quick actions: open YouTube/Facebook/WhatsApp when installed, open the phone dialer, and open Wi-Fi/Bluetooth/display settings.
- Dark black/purple/cyan interface.
- No hidden audio capture and no silent calls/SMS.

## Build
The GitHub Actions workflow builds a debug APK and packages the APK together with a source ZIP as blackcat-ai-complete.zip.

Open Actions → Build Android APK → latest successful run → Artifacts.

## Important limitations in this build
- This is a functional foundation, not the full feature list in the requirements.
- Always-on wake word/background listening is not included; Android background microphone rules and battery restrictions require a separately tested foreground-service/wake-word implementation.
- Online AI is not active until a secure backend endpoint and provider credentials are configured. Never put provider API keys in the APK.
- Voice gender/quality depends on the TTS voices installed on the device.
- Voice biometrics, PIN/biometric gating, reminders, long-term memory manager, SMS/call integrations, and lock-screen features remain later phases.
- Debug APK is not a signed production release. A release signing key must be created and handled privately by the owner.
- GitHub CI build success confirms compilation only; install and test on a physical Android device before release.
