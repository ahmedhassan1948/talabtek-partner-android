# بناء APK موقّع بدون Gradle — يتطلب JDK 17 + Android SDK (build-tools 34 + platform 34)
param([switch]$Staged)
$ErrorActionPreference = 'Stop'

# أدوات Java لا تتعامل مع المسارات العربية على ويندوز، لذا نبني من مسار ASCII مؤقت.
$origin = $PSScriptRoot
$isAscii = $origin -notmatch '[^\x00-\x7F]'
if (-not $isAscii -and -not $Staged) {
  $stage = Join-Path $env:TEMP 'talabtek-app-build'
  if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
  New-Item -ItemType Directory -Force -Path $stage | Out-Null
  Get-ChildItem $origin -Force |
    Where-Object { $_.Name -notin @('build', 'releases', '.git') } |
    ForEach-Object { Copy-Item $_.FullName (Join-Path $stage $_.Name) -Recurse -Force }
  & powershell -ExecutionPolicy Bypass -File (Join-Path $stage 'build.ps1') -Staged
  if ($LASTEXITCODE -ne 0) { throw 'فشل البناء' }
  $outDir = Join-Path $origin 'releases'
  New-Item -ItemType Directory -Force -Path $outDir | Out-Null
  Copy-Item (Join-Path $stage 'releases\*.apk') $outDir -Force
  Get-ChildItem $outDir -Filter '*.apk' | Select-Object Name, @{n = 'MB'; e = { [math]::Round($_.Length / 1MB, 2) } } | Format-Table -AutoSize
  exit 0
}

$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } elseif ($env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT } else { "$env:LOCALAPPDATA\Android\Sdk" }
$bt = Join-Path $sdk 'build-tools\34.0.0'
$androidJar = Join-Path $sdk 'platforms\android-34\android.jar'
$javaHome = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { Split-Path (Split-Path (Get-Command java).Source) }
$javac = Join-Path $javaHome 'bin\javac.exe'
$jar = Join-Path $javaHome 'bin\jar.exe'
$keytool = Join-Path $javaHome 'bin\keytool.exe'

foreach ($tool in @($javac, $jar, $keytool, (Join-Path $bt 'aapt2.exe'), (Join-Path $bt 'd8.bat'), (Join-Path $bt 'zipalign.exe'), (Join-Path $bt 'apksigner.bat'), $androidJar)) {
  if (-not (Test-Path $tool)) { throw "أداة غير موجودة: $tool" }
}

$keystore = Join-Path $PSScriptRoot 'keystore\talabtek-release.jks'
$storePass = 'talabtek123'
$keyAlias = 'talabtek'
if (-not (Test-Path $keystore)) {
  New-Item -ItemType Directory -Force -Path (Split-Path $keystore) | Out-Null
  & $keytool -genkeypair -v -keystore $keystore -alias $keyAlias -keyalg RSA -keysize 2048 -validity 10000 `
    -storepass $storePass -keypass $storePass `
    -dname 'CN=Talabtek, OU=Mobile, O=Talabtek, L=Muscat, ST=Muscat, C=OM'
  if ($LASTEXITCODE -ne 0) { throw 'فشل إنشاء مفتاح التوقيع' }
}

$work = Join-Path $PSScriptRoot 'build'
if (Test-Path $work) { Remove-Item $work -Recurse -Force }
New-Item -ItemType Directory -Force -Path $work | Out-Null

& (Join-Path $bt 'aapt2.exe') compile --dir (Join-Path $PSScriptRoot 'res') -o (Join-Path $work 'res.zip')
if ($LASTEXITCODE -ne 0) { throw 'aapt2 compile failed' }

& (Join-Path $bt 'aapt2.exe') link -o (Join-Path $work 'base.apk') -I $androidJar `
  --manifest (Join-Path $PSScriptRoot 'AndroidManifest.xml') `
  -R (Join-Path $work 'res.zip') --java (Join-Path $work 'gen') --auto-add-overlay
if ($LASTEXITCODE -ne 0) { throw 'aapt2 link failed' }

$sources = @(Get-ChildItem -Recurse -File (Join-Path $PSScriptRoot 'java') -Filter '*.java' | ForEach-Object { $_.FullName })
$sources += @(Get-ChildItem -Recurse -File (Join-Path $work 'gen') -Filter '*.java' | ForEach-Object { $_.FullName })
& $javac -encoding UTF-8 -source 17 -target 17 -classpath $androidJar -d (Join-Path $work 'classes') @sources
if ($LASTEXITCODE -ne 0) { throw 'javac failed' }

$classes = @(Get-ChildItem -Recurse -File (Join-Path $work 'classes') -Filter '*.class' | ForEach-Object { $_.FullName })
New-Item -ItemType Directory -Force -Path (Join-Path $work 'dex') | Out-Null
& (Join-Path $bt 'd8.bat') --lib $androidJar --min-api 23 --output (Join-Path $work 'dex') @classes
if ($LASTEXITCODE -ne 0) { throw 'd8 failed' }

Copy-Item (Join-Path $work 'base.apk') (Join-Path $work 'unsigned.apk') -Force
& $jar uf (Join-Path $work 'unsigned.apk') -C (Join-Path $work 'dex') classes.dex
if ($LASTEXITCODE -ne 0) { throw 'jar failed' }

& (Join-Path $bt 'zipalign.exe') -f -p 4 (Join-Path $work 'unsigned.apk') (Join-Path $work 'aligned.apk')
if ($LASTEXITCODE -ne 0) { throw 'zipalign failed' }

$releases = Join-Path $PSScriptRoot 'releases'
New-Item -ItemType Directory -Force -Path $releases | Out-Null
$outApk = Join-Path $releases 'talabtek-partner-1.0.0.apk'
& (Join-Path $bt 'apksigner.bat') sign --ks $keystore --ks-key-alias $keyAlias `
  --ks-pass "pass:$storePass" --key-pass "pass:$storePass" `
  --out $outApk (Join-Path $work 'aligned.apk')
if ($LASTEXITCODE -ne 0) { throw 'apksigner failed' }

$certs = & (Join-Path $bt 'apksigner.bat') verify --print-certs $outApk
$certs | Select-Object -First 4
Write-Host "✅ $outApk" -ForegroundColor Green
exit 0
