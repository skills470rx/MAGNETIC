#!/bin/sh
set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=8.7
DIST_DIR="$APP_HOME/.gradle-bootstrap/gradle-$GRADLE_VERSION"
GRADLE_BIN="$DIST_DIR/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$APP_HOME/.gradle-bootstrap"
  ZIP="$APP_HOME/.gradle-bootstrap/gradle-$GRADLE_VERSION-bin.zip"
  URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

  if [ ! -f "$ZIP" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl -fL "$URL" -o "$ZIP"
    elif command -v wget >/dev/null 2>&1; then
      wget -O "$ZIP" "$URL"
    else
      echo "ERROR: curl or wget is required to bootstrap Gradle $GRADLE_VERSION" >&2
      exit 1
    fi
  fi

  if command -v unzip >/dev/null 2>&1; then
    unzip -q -o "$ZIP" -d "$APP_HOME/.gradle-bootstrap"
  else
    echo "ERROR: unzip is required to bootstrap Gradle $GRADLE_VERSION" >&2
    exit 1
  fi
fi

exec "$GRADLE_BIN" "$@"
