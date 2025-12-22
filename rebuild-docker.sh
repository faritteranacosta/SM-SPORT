#!/bin/bash

# Script para reconstruir el contenedor Docker del backend
# Uso: ./rebuild-docker.sh

echo "🛑 Deteniendo contenedores..."
docker-compose down

echo "🗑️  Eliminando imagen antigua..."
docker rmi sm-sport-sm-sport 2>/dev/null || true

echo "🔨 Reconstruyendo imagen sin caché..."
docker-compose build --no-cache sm-sport

echo "🚀 Iniciando contenedores..."
docker-compose up -d

echo "✅ Reconstrucción completada!"
echo "📋 Ver logs con: docker-compose logs -f sm-sport"

