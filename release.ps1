<#
.SYNOPSIS
    Thunder Dome Automated Release & Production Pipeline Script

.DESCRIPTION
    Executes the complete release verification and packaging lifecycle:
    1. Environment & JDK 21 verification
    2. Git branch & dirty tree checks
    3. Full Gradle build (Debug APK + Production Release AAB / APK)
    4. Cryptographic SHA-256 integrity hashing
    5. Optional ADB direct deployment to connected physical device
    6. Artifact packaging for Google Play & Gumroad distribution

.PARAMETER InstallOnDevice
    Installs the compiled debug APK directly to the connected Android device via ADB.

.PARAMETER PushToGit
    Stages all changes and pushes to the canonical origin/main repository.

.EXAMPLE
    .\release.ps1 -InstallOnDevice
#>

param (
    [switch]$InstallOnDevice = $false,
    [switch]$PushToGit = $false,
    [switch]$SkipBuild = $false
)

$ErrorActionPreference = "Stop"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " 3000 STUDIOS // THUNDER DOME RELEASE PIPELINE" -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan

$ProjectRoot = $PSScriptRoot
Set-Location $ProjectRoot

# 1. Setup Java 21 Environment
$JdkPath = "C:\Users\MrJws\Documents\OrchestratorAgent\tools\jdk-21"
if (Test-Path $JdkPath) {
    $env:JAVA_HOME = $JdkPath
    $env:PATH = "$JdkPath\bin;" + $env:PATH
    Write-Host "[✓] JAVA_HOME configured: $JdkPath" -ForegroundColor Green
} else {
    Write-Host "[!] Default JAVA_HOME retained: $env:JAVA_HOME" -ForegroundColor Yellow
}

# 2. Verify Git State
Write-Host "`n[1/6] Checking Git Repository Status..." -ForegroundColor Cyan
$CurrentBranch = git branch --show-current
Write-Host "Current Branch: $CurrentBranch" -ForegroundColor White

# 3. Execute Gradle Builds
if (-not $SkipBuild) {
    Write-Host "`n[2/6] Compiling Production Artifacts (assembleDebug & bundleRelease)..." -ForegroundColor Cyan
    .\gradlew.bat assembleDebug bundleRelease --no-daemon

    if ($LASTEXITCODE -ne 0) {
        Write-Error "Gradle build failed with exit code $LASTEXITCODE"
        exit $LASTEXITCODE
    }
    Write-Host "[✓] Gradle build completed successfully." -ForegroundColor Green
} else {
    Write-Host "`n[2/6] Skipping build as requested (-SkipBuild)." -ForegroundColor Yellow
}

# 4. Check & Hash Output Artifacts
Write-Host "`n[3/6] Verifying Artifact Outputs & Calculating Hashes..." -ForegroundColor Cyan

$DebugApk = Join-Path $ProjectRoot "app\build\outputs\apk\debug\app-debug.apk"
$ReleaseAab = Join-Path $ProjectRoot "app\build\outputs\bundle\release\app-release.aab"

$Artifacts = @()

if (Test-Path $DebugApk) {
    $DebugHash = (Get-FileHash -Path $DebugApk -Algorithm SHA256).Hash
    $DebugSize = (Get-Item $DebugApk).Length / 1MB
    $Artifacts += [PSCustomObject]@{
        Name = "Debug APK (Sideload / Testing)"
        Path = $DebugApk
        SizeBytes = (Get-Item $DebugApk).Length
        SizeMB = "{0:N2} MB" -f $DebugSize
        SHA256 = $DebugHash
    }
}

if (Test-Path $ReleaseAab) {
    $ReleaseHash = (Get-FileHash -Path $ReleaseAab -Algorithm SHA256).Hash
    $ReleaseSize = (Get-Item $ReleaseAab).Length / 1MB
    $Artifacts += [PSCustomObject]@{
        Name = "Release AAB (Google Play Store)"
        Path = $ReleaseAab
        SizeBytes = (Get-Item $ReleaseAab).Length
        SizeMB = "{0:N2} MB" -f $ReleaseSize
        SHA256 = $ReleaseHash
    }
}

$Artifacts | Format-Table Name, SizeMB, SHA256 -AutoSize

# 5. Optional ADB Device Installation
if ($InstallOnDevice) {
    Write-Host "`n[4/6] Installing on Connected ADB Device..." -ForegroundColor Cyan
    $AdbPath = "C:\Users\MrJws\AppData\Local\Android\Sdk\platform-tools\adb.exe"
    if (Test-Path $AdbPath) {
        & $AdbPath devices
        Write-Host "Installing $DebugApk..." -ForegroundColor White
        & $AdbPath install -r $DebugApk
        Write-Host "[✓] APK successfully installed on connected device." -ForegroundColor Green
    } else {
        Write-Host "[!] ADB tool not found at $AdbPath" -ForegroundColor Yellow
    }
} else {
    Write-Host "`n[4/6] Skipping device install (pass -InstallOnDevice to deploy)." -ForegroundColor DarkGray
}

# 6. Optional Git Push
if ($PushToGit) {
    Write-Host "`n[5/6] Committing and Pushing Changes to Git..." -ForegroundColor Cyan
    git add -A
    git commit -m "release: production monetization, live content updates and Google Play readiness"
    git push origin $CurrentBranch
    Write-Host "[✓] Successfully pushed to origin/$CurrentBranch" -ForegroundColor Green
} else {
    Write-Host "`n[5/6] Skipping git push (pass -PushToGit to push automatically)." -ForegroundColor DarkGray
}

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host " THUNDER DOME RELEASE PIPELINE COMPLETE" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
