$ErrorActionPreference = "Stop"

$projectDir = $PSScriptRoot
$envFile = Join-Path (Split-Path $projectDir -Parent) ".env"
$composeFile = Join-Path (Split-Path $projectDir -Parent) "compose.yaml"

if (-not (Test-Path $envFile)) {
    throw "Project .env file not found: $envFile"
}

# Read only the test database password; never print its value.
$passwordLine = Get-Content $envFile |
    Where-Object { $_ -match '^\s*PG_TEST_DB_PASSWORD=' } |
    Select-Object -First 1

if (-not $passwordLine -or
    $passwordLine -notmatch '^\s*PG_TEST_DB_PASSWORD=(.*)$') {
    throw "PG_TEST_DB_PASSWORD is missing from .env"
}

$password = $Matches[1].Trim()

# Support simple quoted values in .env.
if ($password.Length -ge 2) {
    if (($password.StartsWith('"') -and $password.EndsWith('"')) -or
        ($password.StartsWith("'") -and $password.EndsWith("'"))) {
        $password = $password.Substring(1, $password.Length - 2)
    }
}

if ([string]::IsNullOrWhiteSpace($password)) {
    throw "PG_TEST_DB_PASSWORD is empty in .env"
}

$env:PG_TEST_DB_URL = "jdbc:postgresql://localhost:15432/expense_tracker_concurrency_test"
$env:PG_TEST_DB_USERNAME = "expense_test_user"
$env:PG_TEST_DB_PASSWORD = $password

Write-Host "Checking dedicated PostgreSQL test container..."

$containerStatus = docker compose -f $composeFile --profile testing ps --format json postgres-test
if ($LASTEXITCODE -ne 0) {
    throw "Could not inspect the PostgreSQL test container."
}

if ($containerStatus -notmatch '"Health"\s*:\s*"healthy"') {
    throw "PostgreSQL test container is not healthy. Start/check it with: docker compose -f `"$composeFile`" --profile testing up -d postgres-test"
}

Write-Host "Running Maven tests..."
Push-Location $projectDir
try {
    & .\mvnw.cmd test
    $testExitCode = $LASTEXITCODE
}
finally {
    Pop-Location
    Remove-Item Env:PG_TEST_DB_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:PG_TEST_DB_URL -ErrorAction SilentlyContinue
    Remove-Item Env:PG_TEST_DB_USERNAME -ErrorAction SilentlyContinue
}

if ($testExitCode -ne 0) {
    throw "Maven tests failed with exit code $testExitCode"
}

Write-Host "All Maven tests passed." -ForegroundColor Green
