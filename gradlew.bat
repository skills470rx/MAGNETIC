@echo off
setlocal
set GRADLE_VERSION=8.7
set APP_HOME=%~dp0
set DIST_DIR=%APP_HOME%\.gradle-bootstrap\gradle-%GRADLE_VERSION%
if exist "%DIST_DIR%\bin\gradle.bat" (
  call "%DIST_DIR%\bin\gradle.bat" %*
  exit /b %ERRORLEVEL%
)
echo This bootstrap launcher is intended for Unix cloud builders.
echo Use Android Studio or install Gradle 8.7 on Windows.
exit /b 1
