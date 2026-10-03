param(
    [string]$BaseUrl = 'http://localhost:8080',
    [Parameter(Mandatory = $true)][string]$Email,
    [Parameter(Mandatory = $true)][string]$Senha,
    [Parameter(Mandatory = $true)][long]$EmpresaId,
    [string]$Inicio = '2026-09-01T00:00:00',
    [string]$Fim = '2026-09-30T23:59:59'
)

$ErrorActionPreference = 'Stop'

function Get-Json([string]$Method, [string]$Uri, [hashtable]$Headers = @{}, [object]$Body = $null) {
    $params = @{ Method = $Method; Uri = $Uri; Headers = $Headers; UseBasicParsing = $true }
    if ($null -ne $Body) {
        $params.ContentType = 'application/json'
        $params.Body = ($Body | ConvertTo-Json -Compress)
    }
    return Invoke-RestMethod @params
}

$health = Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/actuator/health/readiness"
if ($health.StatusCode -ne 200) { throw "Readiness retornou $($health.StatusCode)." }

$login = Get-Json 'POST' "$BaseUrl/api/v1/auth/login" @{} @{ email = $Email; senha = $Senha }
if ([string]::IsNullOrWhiteSpace($login.accessToken)) { throw 'Login não retornou accessToken.' }
$headers = @{ Authorization = "Bearer $($login.accessToken)" }

$me = Get-Json 'GET' "$BaseUrl/api/v1/auth/me" $headers
if ([long]$me.empresaId -ne $EmpresaId) { throw 'O usuário autenticado não pertence à empresa informada.' }

$summary = Get-Json 'GET' "$BaseUrl/api/v1/empresas/$EmpresaId/analytics/resumo?inicio=$Inicio&fim=$Fim" $headers
if ($null -eq $summary) { throw 'Analytics summary não retornou payload.' }

Write-Output 'SMOKE PASS: readiness, login, /auth/me e analytics/resumo.'
Write-Output 'Dashboard dedicado não foi encontrado no inventário atual; analytics/resumo é o endpoint executivo disponível.'
