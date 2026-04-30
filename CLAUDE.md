# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Kotlin Multiplatform / Compose Multiplatform mythology-themed app (`rootProject.name = "OlympX"`) targeting **Android** and **iOS** (`iosArm64`, `iosSimulatorArm64`). Only one Gradle module: `:composeApp`. Kotlin package root is `org.example.project`; Android applicationId is `org.example.project`.

Versions are pinned in `gradle/libs.versions.toml` (Kotlin 2.3.20, Compose Multiplatform 1.10.3, AGP 8.11.2, CameraX 1.4.1, JVM target 11, min/target/compile SDK 24/36/36, iOS deployment target 18.2).

## Commands

```bash
# Android — debug build
./gradlew :composeApp:assembleDebug

# Run all common tests
./gradlew :composeApp:allTests

# Target-specific tests
./gradlew :composeApp:testDebugUnitTest        # Android unit tests
./gradlew :composeApp:iosSimulatorArm64Test    # iOS simulator tests

# Single test filter
./gradlew :composeApp:allTests --tests "org.example.project.ComposeAppCommonTest.example"

# Clean
./gradlew clean
```

iOS: open `iosApp/iosApp.xcodeproj` in Xcode, or use the KMP run configuration. The Compile Kotlin Framework build phase runs `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode` automatically.

Unsigned IPA: `./build_unsigned_ipa.command` (archives `iosApp/` via `xcodebuild` with `CODE_SIGNING_ALLOWED=NO`, packages a Payload zip into `OlympX.ipa` at the repo root).

## Architecture

### Top-level shell

`App()` invokes `Gray(loading, noInternet, white)` with three slots. `Gray` is `expect/actual`; both actuals wrap content in `MythTheme { AppGate(...) }`. **`AppGate`** (in `commonMain`) owns the state machine — it renders the `loading` slot, waits 2 seconds, then animates to `white`. The `noInternet` slot is fed straight through and must not be modified.

### Navigation

`AppNavGraph` wraps everything in `MediaPickerHost { ... }` (expect/actual — installs the `LocalMediaPicker` CompositionLocal and, on Android, overlays the CameraX capture UI). It renders a `MythicBottomBar` with four tabs (`NavTab.World/Journey/Play/Profile`) and uses `AnimatedContent` for tab transitions. A separate state for `webViewUrl` overlays `WebViewScreen` on top of the nav graph when a legal URL is tapped.

### Theme

`theme/MythColors.kt` defines the blue/cyan/sky palette plus `AccentGradient`, `CardGradient`, `GoldGradient` brushes. `MythTheme` sets a `darkColorScheme` + typography; used inside each `Gray` actual so theme applies across loading/nav/etc.

### Reusable UI

- `ui/effects/AuroraBackground` — animated multi-layer radial gradient + vertical tint.
- `ui/effects/ParticleField` — Canvas-drawn floating particle system seeded per call site.
- `ui/effects/Shimmer` — `Modifier.mythShimmer()` diagonal gradient sweep.
- `ui/components/MythicCard`, `GlassRow`, `MythicChip`, `GlowingButton`, `MythicDialog`, `ChoiceSheet`, `MythicProgressBar` — the design primitives screens compose from. Prefer these before introducing one-off containers.

### Game logic (Play screen)

`screens/play/GameState.kt` is the single source of truth for the card-battle game. `PlayScreen` is UI only — mutations go through `game.fight() / nextCard() / resetBuild() / shuffleDeck() / forgeReplacement(type)`. Equipment pool (10 per type) and the 50-card enemy deck are built via `CardCatalog` in `GameModels.kt`. Keep this separation: no game rules inside composables.

### Media capture / gallery

`media/MediaPicker.kt` declares `MediaPickerHandle` (common) and `MediaPickerHost` (expect). A handle is read via `LocalMediaPicker.current`. Outcomes are a sealed `PickerOutcome` (`Success(bytes)`, `Canceled`, `PermissionDenied(permanent)`, `Error`).

- **Android (`media/MediaPicker.android.kt`)** — permission + launcher logic lives inside the host composable, using `rememberLauncherForActivityResult`. `ActivityResultContracts.PickVisualMedia` handles gallery (Photo Picker preferred, falls back to READ_MEDIA_IMAGES / READ_EXTERNAL_STORAGE). Camera permission gates rendering of `CameraCaptureScreen` (CameraX `Preview` + `ImageCapture` bound to `LocalLifecycleOwner`). Permanent denials open the in-app `MythicDialog` with a Settings deep-link.
- **iOS (`media/MediaPicker.ios.kt`)** — presents `UIImagePickerController(sourceType = .camera)` for photos and `PHPickerViewController` for the gallery. Delegates are stored in top-level `currentCameraDelegate` / `currentPickerDelegate` vars (strong refs — UIKit's `.delegate` is weak). All permission checks (`AVCaptureDevice.authorizationStatusForMediaType` / `PHPhotoLibrary.authorizationStatusForAccessLevel`) run on the main queue; all callbacks dispatch via `NSOperationQueue.mainQueue.addOperationWithBlock`. Presentation uses `ViewControllerHolder.topViewController()` (stored from `MainViewController`) — **do not** rely on `UIApplication.keyWindow` or connected-scene lookups.

### Image decode

`platform/PlatformImage.kt` → `decodeImageBitmap(bytes: ByteArray): ImageBitmap?`. Android uses `BitmapFactory.decodeByteArray`; iOS uses `org.jetbrains.skia.Image.makeFromEncoded(...).asComposeImageBitmap()` and the `NSData.toByteArray()` helper (memcpy via `usePinned`).

### WebView

`PlatformWebView` is expect/actual (WebView on Android, WKWebView on iOS). `WebViewScreen` wraps it in a full-screen overlay with a close button. Profile → Legal Section opens Android privacy via this screen; iOS additionally shows Terms of Use.

### Platform detection

`platform/PlatformInfo.kt` exposes `currentPlatform: TargetPlatform`. Profile uses this to show the Android vs iOS legal link layout (per product spec the URLs differ).

## Conventions

- Typesafe project accessors enabled in `settings.gradle.kts`.
- Compose resources live under `olympx.composeapp.generated.resources.Res`.
- Android `MainActivity` installs `AppContextHolder.application` in `onCreate`; iOS `MainViewController()` installs `ViewControllerHolder.rootController`.
- Info.plist carries `NSCameraUsageDescription`, `NSPhotoLibraryUsageDescription`, `NSPhotoLibraryAddUsageDescription` — required for any camera/gallery flow.
- `AndroidManifest.xml` declares `CAMERA`, `READ_MEDIA_IMAGES`, `READ_EXTERNAL_STORAGE` (maxSdk=32), plus optional camera features. Do not remove these when touching the manifest.
- `Gray` actuals and `AppGate` contain the loading→white gate; implement new boot-time logic there, not inside `LoadingScreen`.
- Do **not** modify `screens/NoInternetScreen.kt` — it's a fixed slot consumed by `App.kt`.
- Release build uses R8 (`isMinifyEnabled = true`); no custom proguard rules.
