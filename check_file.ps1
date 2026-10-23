$filePath = "C:\Users\Krishna\Nexus\core\database\src\main\kotlin\com\nexus\database\NexusDatabase.kt"
$bytes = [System.IO.File]::ReadAllBytes($filePath)
Write-Host ("Size: " + $bytes.Length + " bytes")
Write-Host ("BOM: " + ($bytes[0].ToString("X2")) + " " + ($bytes[1].ToString("X2")) + " " + ($bytes[2].ToString("X2")))
Write-Host ("Line endings (first CRLF?): " + ($bytes[0x0D] -eq 13 -and $bytes[0x0E] -eq 10))
Write-Host ("Last byte: " + $bytes[$bytes.Length-1].ToString("X2"))
Write-Host ("Second to last: " + $bytes[$bytes.Length-2].ToString("X2"))
