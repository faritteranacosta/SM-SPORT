@echo off
REM Script para reconstruir el contenedor Docker del backend (Windows)
REM Uso: rebuild-docker.bat

echo 🛑 Deteniendo contenedores...
docker-compose down

echo 🗑️  Eliminando imagen antigua...
docker rmi sm-sport-sm-sport 2>nul

echo 🔨 Reconstruyendo imagen sin caché...
docker-compose build --no-cache sm-sport

echo 🚀 Iniciando contenedores...
docker-compose up -d

echo ✅ Reconstrucción completada!
echo 📋 Ver logs con: docker-compose logs -f sm-sport

pause

