param(
    [string]$OutputDirectory = (Join-Path (Get-Location) 'tmp-homologacao-fixtures'),
    [string]$ValidDate = '2026-10-01',
    [string]$ValidFileName = 'valid-production.xlsx',
    [switch]$IncludeOverLimit
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.IO.Compression
New-Item -ItemType Directory -Force $OutputDirectory | Out-Null

function Escape-Xml([string]$Value) {
    return [System.Security.SecurityElement]::Escape($Value)
}

function Write-ZipEntry($Archive, [string]$Path, [string]$Content) {
    $Entry = $Archive.CreateEntry($Path)
    $Stream = $Entry.Open()
    $Writer = [System.IO.StreamWriter]::new($Stream, [System.Text.UTF8Encoding]::new($false))
    try { $Writer.Write($Content) } finally { $Writer.Dispose() }
}

function New-Xlsx([string]$Path, [object[]]$Rows) {
    $Headers = @('funcionario', 'peca', 'quantidade', 'data', 'inicio', 'fim', 'setor', 'maquina', 'turno')
    $AllRows = @($Headers)
    $AllRows = ,$Headers
    foreach ($DataRow in $Rows) { $AllRows += ,$DataRow }
    $CellXml = New-Object System.Collections.Generic.List[string]
    for ($rowIndex = 0; $rowIndex -lt $AllRows.Count; $rowIndex++) {
        $Cells = New-Object System.Collections.Generic.List[string]
        for ($columnIndex = 0; $columnIndex -lt $AllRows[$rowIndex].Count; $columnIndex++) {
            $column = [char]([int][char]'A' + $columnIndex)
            $value = Escape-Xml ([string]$AllRows[$rowIndex][$columnIndex])
            $Cells.Add("<c r=""$column$($rowIndex + 1)"" t=""inlineStr""><is><t>$value</t></is></c>")
        }
        $CellXml.Add("<row r=""$($rowIndex + 1)"">$($Cells -join '')</row>")
    }
    $Sheet = "<?xml version=""1.0"" encoding=""UTF-8"" standalone=""yes""?><worksheet xmlns=""http://schemas.openxmlformats.org/spreadsheetml/2006/main""><sheetData>$($CellXml -join '')</sheetData></worksheet>"
    $Workbook = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Produção" sheetId="1" r:id="rId1"/></sheets></workbook>'
    $ContentTypes = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>'
    $RootRels = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>'
    $WorkbookRels = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>'
    $FileStream = [System.IO.File]::Open($Path, [System.IO.FileMode]::Create)
    $Archive = [System.IO.Compression.ZipArchive]::new($FileStream, [System.IO.Compression.ZipArchiveMode]::Create)
    try {
        Write-ZipEntry $Archive '[Content_Types].xml' $ContentTypes
        Write-ZipEntry $Archive '_rels/.rels' $RootRels
        Write-ZipEntry $Archive 'xl/workbook.xml' $Workbook
        Write-ZipEntry $Archive 'xl/_rels/workbook.xml.rels' $WorkbookRels
        Write-ZipEntry $Archive 'xl/worksheets/sheet1.xml' $Sheet
    } finally { $Archive.Dispose(); $FileStream.Dispose() }
}

$CompanyA = [ordered]@{ nome = 'Metalúrgica Homologação'; codigo = 'HML-A' }
$CompanyB = [ordered]@{ nome = 'Metalúrgica Isolamento'; codigo = 'HML-B' }
$Sectors = @('Pintura', 'Solda', 'KTL', 'Laser')
$Employees = @('00147', '00148', '00149')
$Parts = @('PEC-A', 'PEC-B', 'PEC-C')
$Machines = @('PINT-01', 'SOLDA-01', 'LASER-01')
$Official = [ordered]@{
    current = [ordered]@{ production = 10000; grossMinutes = 600; stoppageMinutes = 60; productiveMinutes = 540; goal = 11000; industrialValue = 8500 }
    previous = [ordered]@{ production = 8000; stoppageMinutes = 80; industrialValue = 7000 }
    expected = [ordered]@{ productionGrowthPercent = 25; stoppageReductionPercent = 25; goalAchievementPercent = 90.91; stoppagePercent = 10; industrialValueGrowthPercent = 21.43 }
}
$Dataset = [ordered]@{
    seed = 'homologacao-backend-v1-2026-09-29'
    companies = @($CompanyA, $CompanyB)
    sectors = $Sectors
    employees = $Employees
    parts = $Parts
    machines = $Machines
    official = $Official
}
$Dataset | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath (Join-Path $OutputDirectory 'dataset.json') -Encoding utf8

$ValidRows = @(
    @('HML-001', 'HML-PEC-A', '100', $ValidDate, '08:00', '10:00', 'HML-KTL', 'HML-KTL-01', 'TURNO-A'),
    @('HML-002', 'HML-PEC-B', '200', $ValidDate, '10:00', '12:00', 'HML-KTL', 'HML-KTL-01', 'TURNO-A'),
    @('HML-003', 'HML-PEC-C', '300', $ValidDate, '13:00', '15:00', 'HML-KTL', 'HML-KTL-01', 'TURNO-A')
)
New-Xlsx (Join-Path $OutputDirectory $ValidFileName) $ValidRows

[System.IO.File]::WriteAllBytes((Join-Path $OutputDirectory 'empty.xlsx'), [byte[]]::new(0))
Set-Content -LiteralPath (Join-Path $OutputDirectory 'invalid-format.txt') -Value 'not an xlsx file' -Encoding utf8
New-Xlsx (Join-Path $OutputDirectory 'missing-column.xlsx') @(@('00147', 'PEC-A', '100', '2026-09-01', '08:00'))
New-Xlsx (Join-Path $OutputDirectory 'invalid-employee.xlsx') @(@('99999', 'PEC-A', '100', '2026-09-01', '08:00', '09:00', 'Pintura', 'PINT-01', 'TURNO-A'))
New-Xlsx (Join-Path $OutputDirectory 'invalid-part.xlsx') @(@('00147', 'PEC-X', '100', '2026-09-01', '08:00', '09:00', 'Pintura', 'PINT-01', 'TURNO-A'))
New-Xlsx (Join-Path $OutputDirectory 'invalid-quantity.xlsx') @(@('00147', 'PEC-A', '-1', '2026-09-01', '08:00', '09:00', 'Pintura', 'PINT-01', 'TURNO-A'))
New-Xlsx (Join-Path $OutputDirectory 'invalid-time.xlsx') @(@('00147', 'PEC-A', '100', '2026-09-01', '25:99', '26:00', 'Pintura', 'PINT-01', 'TURNO-A'))

if ($IncludeOverLimit) {
    $Rows = for ($i = 1; $i -le 10001; $i++) { @('00147', 'PEC-A', '1', '2026-09-01', '08:00', '08:01', 'Pintura', 'PINT-01', 'TURNO-A') }
    New-Xlsx (Join-Path $OutputDirectory 'over-limit.xlsx') $Rows
}

@{
    outputDirectory = (Resolve-Path $OutputDirectory).Path
    files = (Get-ChildItem -LiteralPath $OutputDirectory -File | Select-Object -ExpandProperty Name)
    generatedAt = (Get-Date).ToUniversalTime().ToString('o')
} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $OutputDirectory 'manifest.json') -Encoding utf8

Write-Output (Resolve-Path $OutputDirectory).Path
