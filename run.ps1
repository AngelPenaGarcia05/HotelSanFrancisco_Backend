# run.ps1 — Arranca el backend cargando las variables del archivo .env local.
#
# Motivo: con `.\mvnw spring-boot:run`, Spring Boot NO lee el archivo .env por sí
# solo (no hay librería dotenv ni spring.config.import configurado). Este script
# inyecta cada variable del .env en el entorno del proceso y luego arranca el
# Maven Wrapper. No requiere tener Maven instalado (usa mvnw.cmd).
#
# Uso (desde la raíz del proyecto, en PowerShell):
#   .\run.ps1
#
# Nota: el .env está en .gitignore (contiene secretos) y NO se versiona. Este
# script sí se versiona; no contiene credenciales, solo la lógica de arranque.

Push-Location $PSScriptRoot
try {
    $envFile = Join-Path $PSScriptRoot '.env'

    if (Test-Path $envFile) {
        Get-Content $envFile | ForEach-Object {
            $line = $_.Trim()

            # Ignora líneas vacías y comentarios.
            if ($line -eq '' -or $line.StartsWith('#')) { return }

            # Separa por el PRIMER '=' (los valores pueden contener '=').
            $idx = $line.IndexOf('=')
            if ($idx -lt 1) { return }

            $name  = $line.Substring(0, $idx).Trim()
            $value = $line.Substring($idx + 1).Trim()

            # Quita comillas envolventes si las hubiera.
            if ($value.Length -ge 2 -and
                (($value.StartsWith('"') -and $value.EndsWith('"')) -or
                 ($value.StartsWith("'") -and $value.EndsWith("'")))) {
                $value = $value.Substring(1, $value.Length - 2)
            }

            [Environment]::SetEnvironmentVariable($name, $value, 'Process')
        }
        Write-Host "Variables del .env cargadas en el entorno del proceso." -ForegroundColor Green
    }
    else {
        Write-Warning ".env no encontrado en $PSScriptRoot; se arranca con los defaults de application.yaml."
    }

    & (Join-Path $PSScriptRoot 'mvnw.cmd') spring-boot:run
}
finally {
    Pop-Location
}
