$maxRetries = 60
$retryCount = 0
$started = $false
Write-Output "Waiting for application to start..."
while ($retryCount -lt $maxRetries) {
    try {
        $response = Invoke-WebRequest -Uri http://localhost:8080 -Method Get -ErrorAction Stop
        $started = $true
        break
    } catch {
        if ($_.Exception.Response.StatusCode -eq 404 -or $_.Exception.Response.StatusCode -eq 401 -or $_.Exception.Response.StatusCode -eq 403) {
            $started = $true
            break
        }
        Start-Sleep -Seconds 2
        $retryCount++
    }
}

if (-not $started) {
    Write-Output "Failed to start or connect to localhost:8080"
    exit 1
}

Write-Output "App is running. Testing login..."
$body = '{"email":"demo@ironhack.com","password":"password"}'
$loginResp = Invoke-WebRequest -Uri http://localhost:8080/api/login -Method Post -Body $body -ContentType "application/json" -SessionVariable session

Write-Output "Login Response Status: $($loginResp.StatusCode)"
Write-Output "Set-Cookie Header: $($loginResp.Headers['Set-Cookie'])"

Write-Output "`nTesting /api/me with cookie..."
$meResp = Invoke-WebRequest -Uri http://localhost:8080/api/me -Method Get -WebSession $session
Write-Output "Me Response Status: $($meResp.StatusCode)"
Write-Output "Me Response Body: $($meResp.Content)"

Write-Output "`nTesting logout..."
$logoutResp = Invoke-WebRequest -Uri http://localhost:8080/api/logout -Method Post -WebSession $session
Write-Output "Logout Response Status: $($logoutResp.StatusCode)"
Write-Output "Set-Cookie Header: $($logoutResp.Headers['Set-Cookie'])"

Write-Output "`nTesting /api/me after logout..."
try {
    $meResp2 = Invoke-WebRequest -Uri http://localhost:8080/api/me -Method Get -WebSession $session
    Write-Output "FAIL: SHOULD HAVE FAILED, BUT GOT: $($meResp2.StatusCode)"
} catch {
    Write-Output "SUCCESS: Expected failure for /api/me after logout."
    Write-Output "Status: $($_.Exception.Response.StatusCode)"
}
