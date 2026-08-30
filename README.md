# Magnetic Lab V1

Offline Android touch experiment.

## Build structure
This archive is intentionally packaged with Gradle build files at the ZIP root:

- gradlew
- settings.gradle
- build.gradle
- app/build.gradle

The `gradlew` launcher bootstraps Gradle 8.7 directly, so the cloud builder does not
fall back to an unrelated system Gradle installation.

## What it does
- Tracks raw touch coordinates inside its own canvas
- Applies a mathematical magnetic attraction model only to the app's own visual test data
- Shows raw and processed paths
- Adjustable strength, radius and smoothing
- Movable target
- Reset and enable toggle

No server, login, Internet permission, accessibility service, root, overlay, or input injection.
