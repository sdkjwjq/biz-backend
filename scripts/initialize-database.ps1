# Requires Windows PowerShell 5.1+ or PowerShell 7+ and the MySQL 8 client.
[CmdletBinding()]
param(
    [ValidatePattern('^[A-Za-z][A-Za-z0-9_]{0,63}$')]
    [string]$Database = 'biz',
    [string]$Server = '127.0.0.1',
    [ValidateRange(1, 65535)]
    [int]$Port = 3306,
    [string]$User = 'root',
    [string]$MysqlPath = 'mysql'
)

$ErrorActionPreference = 'Stop'
$backendRoot = [IO.Path]::GetFullPath((Split-Path $PSScriptRoot -Parent))
$manifest = Get-Content -Raw -Encoding UTF8 -LiteralPath (Join-Path $backendRoot 'data/first-run/manifest.json') | ConvertFrom-Json
$client = (Get-Command $MysqlPath -CommandType Application -ErrorAction Stop).Source
$connection = @('--no-defaults', '--protocol=TCP', "--host=$Server", "--port=$Port", "--user=$User", '--default-character-set=utf8mb4', '--batch', '--skip-column-names')
$previousPassword = [Environment]::GetEnvironmentVariable('MYSQL_PWD', 'Process')
$OutputEncoding = New-Object System.Text.UTF8Encoding($false)

if ($Database.ToLowerInvariant() -in @('mysql', 'sys', 'information_schema', 'performance_schema')) {
    throw 'A MySQL system database cannot be used for first-run initialization.'
}

function Get-SqlPath([string]$RelativePath) {
    $path = [IO.Path]::GetFullPath((Join-Path $backendRoot $RelativePath))
    if (-not $path.StartsWith($backendRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
        throw "SQL path is outside the backend directory: $RelativePath"
    }
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "SQL file is missing: $RelativePath"
    }
    return $path
}

function Invoke-MySqlQuery([string]$Sql, [string]$Schema = '') {
    $arguments = $connection
    if ($Schema) { $arguments += "--database=$Schema" }
    $result = & $client @arguments "--execute=$Sql"
    if ($LASTEXITCODE -ne 0) { throw "MySQL query failed (exit $LASTEXITCODE)." }
    return $result
}

function Import-SqlFile([string]$RelativePath) {
    $path = Get-SqlPath $RelativePath
    Write-Host "Applying $RelativePath"
    # Pipe UTF-8 SQL directly, so MySQL batch mode stops on the first SQL error.
    # Do not use SOURCE, --force, or PowerShell's unsupported input redirection.
    Get-Content -Raw -Encoding UTF8 -LiteralPath $path | & $client @connection "--database=$Database"
    if ($LASTEXITCODE -ne 0) { throw "Import failed: $RelativePath (exit $LASTEXITCODE)." }
}

# Validate every input before making any database changes.
$files = @($manifest.snapshot.path) + @($manifest.migrations) + @($manifest.verification)
foreach ($file in $files) {
    $path = Get-SqlPath $file
    $text = Get-Content -Raw -Encoding UTF8 -LiteralPath $path
    if ($text -match '(?im)^\s*(?:USE\s|(?:CREATE|DROP|ALTER)\s+DATABASE\b|SOURCE\s|\\\.)') {
        throw "SQL must stay in the selected database and cannot include other files: $file"
    }
}
$hasher = [Security.Cryptography.SHA256]::Create()
$snapshotStream = [IO.File]::OpenRead((Get-SqlPath $manifest.snapshot.path))
try { $actualHash = [BitConverter]::ToString($hasher.ComputeHash($snapshotStream)).Replace('-', '') }
finally { $snapshotStream.Dispose(); $hasher.Dispose() }
if ($actualHash -ne $manifest.snapshot.sha256) {
    throw 'The original database snapshot does not match its SHA-256 checksum.'
}

$created = $false
try {
    if ($null -eq $previousPassword) {
        $securePassword = Read-Host 'MySQL password (empty if none)' -AsSecureString
        $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        try {
            $env:MYSQL_PWD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
        }
        finally {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
        }
    }

    $version = Invoke-MySqlQuery 'SELECT VERSION();'
    if ($version -notmatch '^(8\.|9\.)' -or $version -match 'MariaDB') {
        throw "MySQL 8.0+ is required; connected server reports $version."
    }
    $exists = @(Invoke-MySqlQuery "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='$Database';")
    if ($exists.Count -ne 0) {
        throw "Database '$Database' already exists. No changes were made. Use a new -Database name."
    }
    # CREATE without IF NOT EXISTS also refuses a concurrently created database.
    Invoke-MySqlQuery ('CREATE DATABASE `' + $Database + '` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;')
    $created = $true
    Import-SqlFile $manifest.snapshot.path
    foreach ($migration in $manifest.migrations) { Import-SqlFile $migration }
    Import-SqlFile $manifest.verification
    Write-Host "Initialized '$Database' from the $($manifest.snapshot.date) snapshot and current schema migrations."
}
catch {
    if ($created) {
        Write-Warning "Initialization stopped. The newly created database '$Database' was kept for inspection; it was not dropped or retried."
    }
    throw
}
finally {
    if ($null -eq $previousPassword) { Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue }
    else { $env:MYSQL_PWD = $previousPassword }
}
