$ErrorActionPreference = "Stop"

# ------------------------------------------------------------
# Paths
# ------------------------------------------------------------

$apiDir = $PSScriptRoot
$projectRoot = Split-Path -Parent $apiDir
$envFile = Join-Path $projectRoot ".env"

# Support common Docker Compose file names.
$composeCandidates = @(
    "compose.yaml",
    "compose.yml",
    "docker-compose.yaml",
    "docker-compose.yml"
)

$composeFile = $null

foreach ($candidate in $composeCandidates) {
    $candidatePath = Join-Path $projectRoot $candidate

    if (Test-Path $candidatePath) {
        $composeFile = $candidatePath
        break
    }
}

if (-not $composeFile) {
    throw "Docker Compose file not found in: $projectRoot"
}

if (-not (Test-Path $envFile)) {
    throw "Root .env file not found: $envFile"
}

# ------------------------------------------------------------
# Test database configuration
# ------------------------------------------------------------

# Keep the JDBC hostname expected by PostgresTestDatabaseGuard.
$testJdbcHost = "localhost"

# Use the loopback IP for the Windows port check.
$testPortHost = "127.0.0.1"

$testPort = 15432
$testDatabase = "expense_tracker_concurrency_test"
$testUsername = "expense_test_user"

# Save the original process environment variables.
$originalEnv = @{
    PG_TEST_DB_URL      = [Environment]::GetEnvironmentVariable(
        "PG_TEST_DB_URL", "Process"
    )
    PG_TEST_DB_USERNAME = [Environment]::GetEnvironmentVariable(
        "PG_TEST_DB_USERNAME", "Process"
    )
    PG_TEST_DB_PASSWORD = [Environment]::GetEnvironmentVariable(
        "PG_TEST_DB_PASSWORD", "Process"
    )
}

try {
    # --------------------------------------------------------
    # Read the test database password without printing it.
    # An existing process environment variable takes priority.
    # --------------------------------------------------------

    $password = [Environment]::GetEnvironmentVariable(
        "PG_TEST_DB_PASSWORD", "Process"
    )

    if ([string]::IsNullOrWhiteSpace($password)) {
        $passwordLine = Get-Content $envFile |
            Where-Object {
                $_ -match '^\s*PG_TEST_DB_PASSWORD\s*='
            } |
            Select-Object -First 1

        if (-not $passwordLine) {
            throw "PG_TEST_DB_PASSWORD was not found in the root .env file."
        }

        $password = $passwordLine -replace `
            '^\s*PG_TEST_DB_PASSWORD\s*=\s*', ''

        $password = $password.Trim()

        # Remove matching surrounding quotes, if present.
        if ($password.Length -ge 2) {
            $firstChar = $password[0]
            $lastChar = $password[$password.Length - 1]

            if (
                ($firstChar -eq '"' -and $lastChar -eq '"') -or
                ($firstChar -eq "'" -and $lastChar -eq "'")
            ) {
                $password = $password.Substring(
                    1,
                    $password.Length - 2
                )
            }
        }
    }

    if ([string]::IsNullOrWhiteSpace($password)) {
        throw "PG_TEST_DB_PASSWORD is empty."
    }

    # Set environment variables for the Maven test process.
    $env:PG_TEST_DB_URL = (
        "jdbc:postgresql://${testJdbcHost}:${testPort}/${testDatabase}"
    )
    $env:PG_TEST_DB_USERNAME = $testUsername
    $env:PG_TEST_DB_PASSWORD = $password

    Write-Host ""
    Write-Host "=============================================="
    Write-Host " Expense Tracker - Backend Test Runner"
    Write-Host "=============================================="
    Write-Host ""

    Write-Host "JDBC URL: $env:PG_TEST_DB_URL"
    Write-Host "Test database: $testDatabase"
    Write-Host "Test username: $testUsername"
    Write-Host "Password: [hidden]"
    Write-Host ""

    # --------------------------------------------------------
    # Start and wait for the PostgreSQL test database.
    # --------------------------------------------------------

    Write-Host "=== Starting PostgreSQL test database ==="

    Push-Location $projectRoot

    try {
        & docker compose `
            --env-file $envFile `
            -f $composeFile `
            --profile testing `
            up -d --wait --wait-timeout 90 postgres-test

        if ($LASTEXITCODE -ne 0) {
            Write-Host ""
            Write-Host "PostgreSQL test database startup failed."
            Write-Host "Recent container logs:"
            & docker compose `
                --env-file $envFile `
                -f $composeFile `
                --profile testing `
                logs --tail 80 postgres-test

            throw "Could not start the PostgreSQL test database."
        }

        Write-Host ""
        Write-Host "=== Checking PostgreSQL readiness ==="

        & docker compose `
            --env-file $envFile `
            -f $composeFile `
            --profile testing `
            exec -T postgres-test `
            sh -lc 'pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"'

        if ($LASTEXITCODE -ne 0) {
            throw "PostgreSQL did not pass the readiness check."
        }
    }
    finally {
        Pop-Location
    }

    # --------------------------------------------------------
    # Check that Windows can reach the published test port.
    # --------------------------------------------------------

    Write-Host ""
    Write-Host "=== Checking the Windows database port ==="

    $portCheck = Test-NetConnection `
        -ComputerName $testPortHost `
        -Port $testPort `
        -WarningAction SilentlyContinue

    if (-not $portCheck.TcpTestSucceeded) {
        throw "Cannot connect to PostgreSQL on ${testPortHost}:${testPort}."
    }

    Write-Host "Test database is healthy and its port is reachable."

    # --------------------------------------------------------
    # Run the complete Maven test suite.
    # --------------------------------------------------------

    Write-Host ""
    Write-Host "=== Running complete Maven test suite ==="
    Write-Host ""

    Push-Location $apiDir

    try {
        & .\mvnw.cmd test
        $mavenExitCode = $LASTEXITCODE
    }
    finally {
        Pop-Location
    }

    if ($mavenExitCode -ne 0) {
        Write-Host ""
        Write-Host "Maven tests failed with exit code $mavenExitCode."
        Write-Host ""

        # Show useful diagnostic lines from the test reports.
        $reportDirectory = Join-Path $apiDir "target\surefire-reports"

        if (Test-Path $reportDirectory) {
            $reportFiles = Get-ChildItem `
                -Path $reportDirectory `
                -Filter "*.txt" `
                -File

            foreach ($report in $reportFiles) {
                $diagnosticLines = Select-String `
                    -Path $report.FullName `
                    -Pattern "Caused by:|PSQLException|Connection refused|authentication failed|Unable to determine Dialect|does not exist|JDBC URL" `
                    -Context 1, 3

                if ($diagnosticLines) {
                    Write-Host "---- $($report.Name) ----"

                    $diagnosticLines |
                        Select-Object -First 20 |
                        ForEach-Object {
                            Write-Host $_
                        }
                }
            }
        }

        throw "Backend test suite failed. Review the Maven output and Surefire reports."
    }

    Write-Host ""
    Write-Host "=============================================="
    Write-Host " All backend tests passed successfully."
    Write-Host "=============================================="
}
finally {
    # --------------------------------------------------------
    # Restore the caller's original environment.
    # Do not stop the test database automatically.
    # --------------------------------------------------------

    foreach ($name in $originalEnv.Keys) {
        $originalValue = $originalEnv[$name]

        if ($null -eq $originalValue) {
            Remove-Item "Env:$name" -ErrorAction SilentlyContinue
        }
        else {
            Set-Item "Env:$name" $originalValue
        }
    }
}