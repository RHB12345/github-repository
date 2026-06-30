param(
    [string]$Url = "http://localhost:8080",
    [int]$TimeoutSeconds = 90
)

for ($i = 0; $i -lt $TimeoutSeconds; $i++) {
    try {
        Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 2 | Out-Null
        Start-Process $Url
        exit 0
    } catch {
        Start-Sleep -Seconds 1
    }
}

Start-Process $Url
