# run.ps1 — Arranca el backend cargando las variables del archivo .env local.
#
# Motivo: con `.\mvnw spring-boot:run`, Spring Boot NO lee el archivo .env por sí
# solo (no hay librería dotenv ni spring.config.import configurado). Este script
# inyecta cada variable del .env en el entorno del proceso y luego arranca el
# Maven Wrapper. No requiere tener Maven instalado (usa mvnw.cmd).
#
# Validación estricta: si una línea del .env está mal formada (no es un comentario,
# no está vacía y no cumple el formato CLAVE=valor con un nombre de variable
# válido), el script muestra un error con el número de línea y el contenido, y
# ABORTA con código de salida 1 sin arrancar la app. Así los errores de
# configuración se detectan al instante (y las herramientas que revisan el exit
# code detectan el fallo) en lugar de quedar ocultos.
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
        $lineNo = 0
        foreach ($raw in Get-Content $envFile) {
            $lineNo++
            $line = $raw.Trim()

            # Líneas vacías y comentarios: válidas, se omiten.
            if ($line -eq '' -or $line.StartsWith('#')) { continue }

            # Separa por el PRIMER '=' (los valores pueden contener '=').
            $idx = $line.IndexOf('=')
            if ($idx -lt 1) {
                Write-Error "run.ps1: .env línea $lineNo mal formada (se esperaba CLAVE=valor): '$raw'"
                exit 1
            }

            $name  = $line.Substring(0, $idx).Trim()
            $value = $line.Substring($idx + 1).Trim()

            # El nombre debe ser un identificador de variable de entorno válido.
            if ($name -notmatch '^[A-Za-z_][A-Za-z0-9_]*$') {
                Write-Error "run.ps1: .env línea $lineNo con nombre de variable inválido '$name': '$raw'"
                exit 1
            }

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
