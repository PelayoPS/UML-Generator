$buildDoneFile = $args[0]
$cancelFile = "$env:TEMP\build.cancel"
$parentPID = $args[1]

# Esperar un momento para asegurar que los archivos están liberados
Start-Sleep -Milliseconds 500

$chars = @('|', '/', '-', '\')
try {
    while (-not (Test-Path $buildDoneFile)) {
        if ((Test-Path $cancelFile) -or -not (Get-Process -Id $parentPID -ErrorAction SilentlyContinue)) {
            exit
        }
        foreach ($char in $chars) {
            Write-Host "`r    Construyendo... [$char]" -NoNewline
            Start-Sleep -Milliseconds 200
            if (Test-Path $buildDoneFile) {
                break
            }
        }
    }
    Write-Host "`r    Construyendo... [OK]"
}
catch {
    exit
}