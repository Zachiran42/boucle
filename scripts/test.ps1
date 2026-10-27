param([string]$ToolchainFile = '', [string]$BuildDir = '')
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$projectRoot = Split-Path -Parent $PSScriptRoot
if (-not $ToolchainFile) { $ToolchainFile = Join-Path $projectRoot '.toolchain.json' }
if (-not [System.IO.Path]::IsPathRooted($ToolchainFile)) { $ToolchainFile = Join-Path $projectRoot $ToolchainFile }
if (-not (Test-Path -LiteralPath $ToolchainFile -PathType Leaf)) { throw 'Configurez .toolchain.json ou fournissez -ToolchainFile.' }
$tools = Get-Content -Raw -LiteralPath $ToolchainFile | ConvertFrom-Json
$java = Join-Path $tools.javaHome 'bin\java.exe'
$javac = Join-Path $tools.javaHome 'bin\javac.exe'
if (-not $BuildDir) { $BuildDir = Join-Path $projectRoot 'build' }
if (-not [System.IO.Path]::IsPathRooted($BuildDir)) { $BuildDir = Join-Path $projectRoot $BuildDir }
$classes = Join-Path $BuildDir ('tests-' + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$engine = Join-Path $projectRoot 'android\src\fr\cercle\boucle\GameEngine.java'
$test = Join-Path $projectRoot 'tests\GameEngineTest.java'
& $javac -encoding UTF-8 -source 8 -target 8 -d $classes $engine $test
if ($LASTEXITCODE -ne 0) { throw 'Compilation des tests impossible.' }
& $java -ea -cp $classes fr.cercle.boucle.GameEngineTest
if ($LASTEXITCODE -ne 0) { throw 'Les tests du moteur ont echoue.' }
Write-Output 'Tests du moteur termines.'