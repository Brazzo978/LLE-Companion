[CmdletBinding()]
param(
    [string] $KeystorePath = (Join-Path `
        ([Environment]::GetFolderPath('MyDocuments')) `
        'LLE-signing-private\lle-release.p12'),
    [string] $KeyAlias = 'lle-release'
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)

if ([string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
    throw 'Set JAVA_HOME to a JDK 17 installation before building the Play bundle.'
}
if (-not (Test-Path -LiteralPath $KeystorePath)) {
    throw "Upload keystore not found: $KeystorePath"
}

$gradle = Join-Path $root 'gradlew.bat'
$jarsigner = Join-Path $env:JAVA_HOME 'bin\jarsigner.exe'
$apksigner = Join-Path $env:LOCALAPPDATA `
    'Android\Sdk\build-tools\36.0.0\apksigner.bat'
$apk = Join-Path $root 'app\build\outputs\apk\release\app-release.apk'
$bundle = Join-Path $root 'app\build\outputs\bundle\release\app-release.aab'
$expectedCertificateSha256 = `
    '5397d6ace3e9d2f14d8ffd2285e26e9f1b26635589cac3a3dc95c0deff76b8ee'
$password = Read-Host 'Companion upload-key password' -AsSecureString
$passwordPointer = [IntPtr]::Zero

try {
    $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
    $plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    $env:LLE_COMPANION_UPLOAD_KEYSTORE = `
        (Resolve-Path -LiteralPath $KeystorePath).Path
    $env:LLE_COMPANION_UPLOAD_STORE_PASSWORD = $plainPassword
    $env:LLE_COMPANION_UPLOAD_KEY_ALIAS = $KeyAlias
    $env:LLE_COMPANION_UPLOAD_KEY_PASSWORD = $plainPassword

    & $gradle --no-daemon clean test lint assembleRelease bundleRelease `
            --console=plain
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle failed with exit code $LASTEXITCODE"
    }
    if (-not (Test-Path -LiteralPath $apk)) {
        throw "Expected signed GitHub APK is missing: $apk"
    }
    if (-not (Test-Path -LiteralPath $bundle)) {
        throw "Expected Play bundle is missing: $bundle"
    }

    if (-not (Test-Path -LiteralPath $apksigner)) {
        throw "apksigner 36.0.0 not found: $apksigner"
    }
    $certificateOutput = & $apksigner verify --verbose --print-certs $apk
    if ($LASTEXITCODE -ne 0) {
        throw 'The generated APK signature is invalid.'
    }
    $certificateLine = $certificateOutput |
            Select-String 'certificate SHA-256 digest:' |
            Select-Object -First 1
    if ($null -eq $certificateLine) {
        throw 'Could not read the generated APK certificate fingerprint.'
    }
    $certificateSha256 = (($certificateLine.Line -split ': ', 2)[1] `
            -replace ':', '').Trim().ToLowerInvariant()
    if ($certificateSha256 -ne $expectedCertificateSha256) {
        throw "Unexpected APK signing certificate: $certificateSha256"
    }

    & $jarsigner -verify -verbose -certs $bundle
    if ($LASTEXITCODE -ne 0) {
        throw 'The generated AAB signature is invalid.'
    }

    $apkHash = (Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant()
    $bundleHash = (Get-FileHash -LiteralPath $bundle -Algorithm SHA256).Hash.ToLowerInvariant()
    Write-Host "Signed GitHub APK: $apk"
    Write-Host "SHA-256: $apkHash"
    Write-Host "Signed Play bundle: $bundle"
    Write-Host "SHA-256: $bundleHash"
} finally {
    Remove-Item Env:LLE_COMPANION_UPLOAD_KEYSTORE -ErrorAction SilentlyContinue
    Remove-Item Env:LLE_COMPANION_UPLOAD_STORE_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:LLE_COMPANION_UPLOAD_KEY_ALIAS -ErrorAction SilentlyContinue
    Remove-Item Env:LLE_COMPANION_UPLOAD_KEY_PASSWORD -ErrorAction SilentlyContinue
    if ($passwordPointer -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
}
