param(
    [string]$Version = "26.2-1.0.0-port.7",
    [string]$MinecraftDir = "D:\minecraft\.minecraft",
    [string]$NeoForgeVersion = "26.2-NeoForge_26.2.0.93",
    [string]$LibXJar = "",
    [string]$Jdk = ""
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

if (-not $Jdk) {
    $bundled = Get-ChildItem "$root\tools\jdk" -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($bundled) { $Jdk = $bundled.FullName } elseif ($env:JAVA_HOME) { $Jdk = $env:JAVA_HOME }
    else { throw "JDK 25 not found. Pass -Jdk <path>." }
}
$javac = Join-Path $Jdk "bin\javac.exe"
$jarTool = Join-Path $Jdk "bin\jar.exe"

if (-not $LibXJar) { throw "Pass -LibXJar <path to LibX-26.2-8.0.0.jar>." }
if (-not (Test-Path $LibXJar)) { throw "LibX jar not found: $LibXJar" }

$gameJar = Join-Path $MinecraftDir "versions\$NeoForgeVersion\$NeoForgeVersion.jar"
if (-not (Test-Path $gameJar)) { throw "Minecraft/NeoForge version jar not found: $gameJar" }

# Classpath: game jar + NeoForge universal + all Maven libraries + LibX
$cp = New-Object System.Collections.Generic.List[string]
# Prefer the NeoForge-patched client jar so patched methods such as
# ArgumentTypeInfos.registerByClass are visible at compile time.
$patched = Join-Path $MinecraftDir "libraries\net\neoforged\minecraft-client-patched\26.2.0.93\minecraft-client-patched-26.2.0.93.jar"
if (Test-Path $patched) { $cp.Add($patched) }
$cp.Add($gameJar)
$universal = Join-Path $MinecraftDir "libraries\net\neoforged\neoforge\26.2.0.93\neoforge-26.2.0.93-universal.jar"
if (Test-Path $universal) { $cp.Add($universal) }
Get-ChildItem (Join-Path $MinecraftDir "libraries") -Recurse -Filter *.jar -ErrorAction SilentlyContinue |
    ForEach-Object { if ($_.FullName -ne $gameJar) { $cp.Add($_.FullName) } }
$cp.Add((Resolve-Path $LibXJar).Path)

$sources = Get-ChildItem "$root\src" -Recurse -Filter *.java | ForEach-Object {
    $_.FullName.Replace($root + "\", "") -replace "\\", "/"
}

$build = "$root\build"
Remove-Item $build -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $build | Out-Null

$enc = New-Object System.Text.UTF8Encoding($false)
$argsFile = "$root\build.args"
$srcFile = "$root\build.sources"
$cpArgs = ($cp | ForEach-Object { $_ -replace '\\', '/' }) -join ";"
$outDir = $build -replace '\\', '/'
[IO.File]::WriteAllText($argsFile, ('-cp "' + $cpArgs + '"' + "`r`n-encoding UTF-8`r`n-d " + $outDir + "`r`n"), $enc)
[IO.File]::WriteAllText($srcFile, (($sources -join "`r`n") + "`r`n"), $enc)

Write-Host "Compiling ($Version) ..."
Push-Location $root
& $javac '-J-Duser.language=en' '-J-Duser.country=US' "@$argsFile" "-Xmaxerrs" "500" "@$srcFile"
$code = $LASTEXITCODE
Pop-Location
if ($code -ne 0) { throw "compile failed ($code)" }

# sync version into the shipped toml
$toml = "$root\res\META-INF\neoforge.mods.toml"
$tomlText = [IO.File]::ReadAllText($toml)
$tomlText = $tomlText -replace '(?m)^version = "[^"]*"', "version = `"$Version`""
[IO.File]::WriteAllText($toml, $tomlText, $enc)

Write-Host "Packaging ..."
$stage = "$root\stage"
Remove-Item $stage -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $stage | Out-Null
Copy-Item "$build\*" $stage -Recurse -Force
Copy-Item "$root\res\*" $stage -Recurse -Force
New-Item -ItemType Directory -Force -Path "$stage\META-INF" | Out-Null
Copy-Item "$root\LICENSE" "$stage\META-INF\LICENSE" -Force
Copy-Item "$root\NOTICE" "$stage\META-INF\NOTICE" -Force
Copy-Item "$root\THIRD-PARTY-NOTICES.md" "$stage\META-INF\THIRD-PARTY-NOTICES.md" -Force

New-Item -ItemType Directory -Force -Path "$root\dist" | Out-Null
$out = "$root\dist\ReBongo-$Version.jar"
Remove-Item $out -Force -ErrorAction SilentlyContinue
& $jarTool --create --file $out -C $stage .
if ($LASTEXITCODE -ne 0) { throw "jar failed" }

Remove-Item $argsFile, $srcFile -Force -ErrorAction SilentlyContinue
Write-Host "Built: $out"
