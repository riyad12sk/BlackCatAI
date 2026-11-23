# BLACK CAT AI — Development Plan v1.0

## Product direction
Build a privacy-first Android personal assistant in Kotlin and Jetpack Compose. Bengali and English are first-class languages. Android permissions and platform restrictions are respected; no hidden recording, lock-screen bypass, or silent sensitive actions.

## Repository baseline
- Existing Android application module: `app`
- Existing package/application ID: `com.blackcat.ai`
- Existing Android Gradle Plugin: 8.2.0
- Existing Kotlin plugin: 1.9.22
- Existing compile/target SDK: 34
- Existing CI workflow: `.github/workflows/main.yml`

## Delivery phases
1. **Foundation and build** — preserve the existing project, establish reproducible CI, build a debug APK, and package source + APK.
2. **Assistant dashboard** — Compose dark/neon UI, status, language selection, editable preferred form of address, settings navigation.
3. **Voice I/O** — Android speech recognition and Text-to-Speech, with explicit microphone activation and visible listening state.
4. **Safe phone actions** — open supported apps/settings, open dialer, and handle user-confirmed actions. Do not send SMS or place calls silently.
5. **Reminders** — local reminder storage and notification scheduling, with notification permission handling.
6. **AI backend** — add a secure HTTPS backend adapter. Provider credentials must remain server-side; no API key is embedded in the APK.
7. **Owner security and memory** — local preferences, PIN/biometric confirmation for sensitive actions, and user-controlled memory management.
8. **Background activation research** — evaluate an Android-compliant foreground service and wake-word engine, including battery and microphone restrictions. Do not claim always-on wake word until tested on physical devices.
9. **QA and release** — unit/build checks, permission review, debug APK, signed release APK (requires a private signing key supplied securely by the owner), and device testing report.

## First build scope
The first CI build should deliver a working installable foundation: dashboard, language/address preferences, explicit speech-to-text, spoken responses, and safe basic intents. Features that need a backend, signing credentials, device-specific validation, or restricted Android privileges are marked as pending rather than simulated.

## Acceptance and truthfulness
- A successful Gradle build proves compilation, not real-device behavior.
- A debug APK is not a signed production release.
- A source ZIP and an APK bundle are separate deliverables.
- Always-on wake word, speaker verification, lock-screen behavior, AI provider connectivity, alarms, SMS, and call integration must be individually implemented and tested before being marked complete.
