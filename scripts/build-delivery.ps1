param(
    [string]$GroupNo = "组号"
)

$ErrorActionPreference = "Stop"
$projectRoot = [System.IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$releaseRoot = [System.IO.Path]::GetFullPath((Join-Path $projectRoot "release"))
$stagingRoot = Join-Path $releaseRoot "staging"
$sourceZipPath = Join-Path $releaseRoot ("{0}+台球厅会员管理系统-源码.zip" -f $GroupNo)
$fullZipPath = Join-Path $releaseRoot ("{0}+台球厅会员管理系统-完整包.zip" -f $GroupNo)

if (-not $releaseRoot.StartsWith($projectRoot + [System.IO.Path]::DirectorySeparatorChar)) {
    throw "发布目录不在项目内，已停止。"
}

Push-Location (Join-Path $projectRoot "backend")
try {
    & mvn.cmd clean package
    if ($LASTEXITCODE -ne 0) { throw "后端构建失败" }
} finally {
    Pop-Location
}

Push-Location (Join-Path $projectRoot "frontend")
try {
    & npm.cmd ci
    if ($LASTEXITCODE -ne 0) { throw "前端依赖安装失败" }
    & npm.cmd run build
    if ($LASTEXITCODE -ne 0) { throw "前端构建失败" }
} finally {
    Pop-Location
}

if (Test-Path -LiteralPath $releaseRoot) {
    Remove-Item -LiteralPath $releaseRoot -Recurse -Force
}
New-Item -ItemType Directory -Path $stagingRoot -Force | Out-Null

$sourceRoot = Join-Path $stagingRoot "source"
New-Item -ItemType Directory -Path $sourceRoot -Force | Out-Null
Copy-Item -LiteralPath (Join-Path $projectRoot "README.md") -Destination $sourceRoot
Copy-Item -LiteralPath (Join-Path $projectRoot "database") -Destination $sourceRoot -Recurse
Copy-Item -LiteralPath (Join-Path $projectRoot "docs") -Destination $sourceRoot -Recurse

$backendSource = Join-Path $sourceRoot "backend"
New-Item -ItemType Directory -Path $backendSource -Force | Out-Null
Copy-Item -LiteralPath (Join-Path $projectRoot "backend\pom.xml") -Destination $backendSource
Copy-Item -LiteralPath (Join-Path $projectRoot "backend\.mvn") -Destination $backendSource -Recurse
Copy-Item -LiteralPath (Join-Path $projectRoot "backend\src") -Destination $backendSource -Recurse

$frontendSource = Join-Path $sourceRoot "frontend"
New-Item -ItemType Directory -Path $frontendSource -Force | Out-Null
foreach ($name in @("package.json", "package-lock.json", "vite.config.js", "index.html")) {
    Copy-Item -LiteralPath (Join-Path $projectRoot "frontend\$name") -Destination $frontendSource
}
Copy-Item -LiteralPath (Join-Path $projectRoot "frontend\src") -Destination $frontendSource -Recurse

Compress-Archive -Path (Join-Path $sourceRoot "*") -DestinationPath $sourceZipPath -CompressionLevel Optimal
$sourceZip = Get-Item -LiteralPath $sourceZipPath
$sourceSizeMb = [math]::Round($sourceZip.Length / 1MB, 2)
if ($sourceZip.Length -gt 20MB) {
    throw "源码包大小为 $sourceSizeMb MB，超过课程要求的 20MB：$sourceZipPath"
}

$executableRoot = Join-Path $stagingRoot "executable"
New-Item -ItemType Directory -Path $executableRoot -Force | Out-Null
$jar = Get-ChildItem -LiteralPath (Join-Path $projectRoot "backend\target") -Filter "*.jar" -File |
    Where-Object { $_.Name -notlike "*.original" } | Select-Object -First 1
if (-not $jar) { throw "未找到后端 jar" }
Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $executableRoot "billiard-club-backend.jar")
Copy-Item -LiteralPath (Join-Path $projectRoot "frontend\dist") -Destination (Join-Path $executableRoot "frontend-dist") -Recurse

Compress-Archive -Path (Join-Path $stagingRoot "*") -DestinationPath $fullZipPath -CompressionLevel Optimal
$fullZip = Get-Item -LiteralPath $fullZipPath
$fullSizeMb = [math]::Round($fullZip.Length / 1MB, 2)
if ($fullZip.Length -gt 20MB) {
    Write-Warning "完整包大小为 $fullSizeMb MB，超过课程要求的 20MB；请向老师确认是否可分开提交源码包和可执行包。"
}

Write-Host "源码包生成成功：$sourceZipPath ($sourceSizeMb MB)"
Write-Host "完整包生成成功：$fullZipPath ($fullSizeMb MB)"
