# Magnetic Lab V2

Magnetic Lab is an **offline Android experiment application** for exploring touch behavior, motion, display response, and publicly exposed device capabilities. It is an observation and visualization tool, not a RAM booster, CPU booster, FPS unlocker, root utility, or system bypass tool.

## Included experiments

The home lab provides navigation to five focused modules:

- **Magnetic Field:** the original V1 touch-path experiment, expanded with attraction, repulsion, neutral mode, field rings, raw-versus-processed paths, draggable target, and reset controls.
- **Touch Response:** raw and smoothed touch trails with an explicitly labeled experimental prediction visualization.
- **Display Motion:** moving dot, grid, motion trail, and pattern tests with the accessible display refresh rate shown when available.
- **Device Explorer:** display, device, and capability information exposed through public Android APIs, with honest statuses such as Supported, Not Detected, and Unavailable.
- **System State:** local memory, battery, storage, and display telemetry without fabricated CPU or thermal values.

## Privacy and permissions

The application requests no startup permissions, account, network access, analytics, ads, or cloud service. Touch paths and device information remain local to the device. Values that are not available through public Android APIs are labeled as unavailable rather than guessed.

## Build

The project keeps the existing application package `com.magneticlab.v1` for compatibility. With an Android SDK configured, build a debug APK with:

```bash
./gradlew assembleDebug
```

The launcher icon is a text-free magnetic-field vector icon, and the application label is **Magnetic Lab V2**.
