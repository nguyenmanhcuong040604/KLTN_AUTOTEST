@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "WRAPPER_DIR=%SCRIPT_DIR%.mvn\wrapper"
set "DIST_DIR=%WRAPPER_DIR%\dists"
set "MAVEN_VERSION=3.9.11"
set "MAVEN_HOME=%DIST_DIR%\apache-maven-%MAVEN_VERSION%"
set "MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd"
set "MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip"

if not exist "%MAVEN_CMD%" (
  echo Apache Maven %MAVEN_VERSION% was not found locally.
  echo Downloading Maven distribution...
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference='Stop';" ^
    "$dist='%DIST_DIR%';" ^
    "$mavenHome='%MAVEN_HOME%';" ^
    "$url='%MAVEN_URL%';" ^
    "$zip=Join-Path $dist 'apache-maven-%MAVEN_VERSION%-bin.zip';" ^
    "$tmp=Join-Path $dist 'tmp';" ^
    "New-Item -ItemType Directory -Force -Path $dist | Out-Null;" ^
    "if (Test-Path $tmp) { Remove-Item -Recurse -Force $tmp };" ^
    "New-Item -ItemType Directory -Force -Path $tmp | Out-Null;" ^
    "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12;" ^
    "Invoke-WebRequest -Uri $url -OutFile $zip;" ^
    "Expand-Archive -Path $zip -DestinationPath $tmp -Force;" ^
    "if (Test-Path $mavenHome) { Remove-Item -Recurse -Force $mavenHome };" ^
    "Move-Item -Path (Join-Path $tmp 'apache-maven-%MAVEN_VERSION%') -Destination $mavenHome;" ^
    "Remove-Item -Recurse -Force $tmp;" ^
    "Remove-Item -Force $zip;"

  if errorlevel 1 (
    echo Failed to download Maven %MAVEN_VERSION%.
    exit /b 1
  )
)

call "%MAVEN_CMD%" %*
exit /b %ERRORLEVEL%
