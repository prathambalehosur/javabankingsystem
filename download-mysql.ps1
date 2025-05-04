$url = "https://dev.mysql.com/get/Downloads/MySQLInstaller/mysql-installer-community-8.0.36.0.msi"
$output = "$PSScriptRoot\mysql-installer-community.msi"

Write-Host "Downloading MySQL Installer..."
Invoke-WebRequest -Uri $url -OutFile $output
Write-Host "Download complete. MySQL Installer saved to: $output"
Write-Host "Please run the installer manually to install MySQL."
