param(
    [string]$OutputDirectory = "performance-data",
    [int]$Companies = 1,
    [int]$SectorsPerCompany = 10,
    [int]$EmployeesPerCompany = 500,
    [int]$PartsPerCompany = 1000,
    [int]$MachinesPerCompany = 100,
    [int]$ProductionRowsPerCompany = 100000,
    [int]$AuditRowsPerCompany = 1000,
    [int]$Seed = 1701
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
if ($Companies -lt 1 -or $SectorsPerCompany -lt 1 -or $EmployeesPerCompany -lt 1 -or $PartsPerCompany -lt 1 -or $MachinesPerCompany -lt 1 -or $ProductionRowsPerCompany -lt 1 -or $AuditRowsPerCompany -lt 1) {
    throw "Todos os volumes devem ser maiores que zero."
}

New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
$manifest = [ordered]@{
    generator = "TASK17-v1"
    seed = $Seed
    generatedAt = [DateTime]::UtcNow.ToString("o")
    syntheticOnly = $true
    volumes = [ordered]@{
        companies = $Companies
        sectors = $Companies * $SectorsPerCompany
        employees = $Companies * $EmployeesPerCompany
        parts = $Companies * $PartsPerCompany
        machines = $Companies * $MachinesPerCompany
        production = $Companies * $ProductionRowsPerCompany
        audit = $Companies * $AuditRowsPerCompany
    }
    cleanup = "Remover o diretório gerado e limpar a base de benchmark; nunca apontar para dados de produção."
}
$manifest | ConvertTo-Json -Depth 6 | Set-Content -Encoding UTF8 (Join-Path $OutputDirectory "manifest.json")

$random = [Random]::new($Seed)
$productionPath = Join-Path $OutputDirectory "production.jsonl"
try {
    $writer = [IO.StreamWriter]::new($productionPath, $false, [Text.UTF8Encoding]::new($false))
    for ($company = 1; $company -le $Companies; $company++) {
        for ($row = 1; $row -le $ProductionRowsPerCompany; $row++) {
            $sector = 1 + (($row - 1) % $SectorsPerCompany)
            $employee = 1 + (($row - 1) % $EmployeesPerCompany)
            $part = 1 + (($row - 1) % $PartsPerCompany)
            $start = [DateTime]::new(2026, 1, 1).AddMinutes($row % 525600)
            $record = [ordered]@{ company = $company; sector = $sector; employee = $employee; part = $part; quantity = 1 + $random.Next(1, 20); start = $start.ToString("o"); end = $start.AddMinutes(30 + $random.Next(1, 90)).ToString("o") }
            $writer.WriteLine(($record | ConvertTo-Json -Compress))
        }
    }
}
finally {
    if ($null -ne $writer) { $writer.Dispose() }
}

Write-Output "Dataset sintético gerado em $OutputDirectory. Não contém dados reais."
