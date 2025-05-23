#!/bin/bash

# Colores para mensajes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Iniciando construcción del APK...${NC}"

# Verificar si Docker está instalado
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker no está instalado. Por favor, instala Docker primero.${NC}"
    exit 1
fi

# Verificar si Gradle está instalado
if ! command -v gradle &> /dev/null; then
    echo -e "${YELLOW}Gradle no está instalado. Instalando...${NC}"
    sudo apt-get update && sudo apt-get install -y gradle || {
        echo -e "${RED}Error al instalar Gradle${NC}"
        exit 1
    }
fi

# Crear directorio gradle/wrapper si no existe
mkdir -p gradle/wrapper

# Construir la imagen Docker
echo -e "${YELLOW}Construyendo imagen Docker...${NC}"
docker build -t multiplication-game-builder . || {
    echo -e "${RED}Error al construir la imagen Docker${NC}"
    exit 1
}

# Crear un contenedor temporal
echo -e "${YELLOW}Creando contenedor temporal...${NC}"
docker create --name temp-container multiplication-game-builder || {
    echo -e "${RED}Error al crear el contenedor temporal${NC}"
    exit 1
}

# Extraer el APK
echo -e "${YELLOW}Extrayendo el APK...${NC}"
docker cp temp-container:/app/app/build/outputs/apk/debug/app-debug.apk ./app-debug.apk || {
    echo -e "${RED}Error al extraer el APK${NC}"
    exit 1
}

# Limpiar el contenedor temporal
echo -e "${YELLOW}Limpiando contenedor temporal...${NC}"
docker rm temp-container || {
    echo -e "${RED}Error al limpiar el contenedor temporal${NC}"
    exit 1
}

# Verificar si el APK se generó correctamente
if [ -f "./app-debug.apk" ]; then
    echo -e "${GREEN}¡APK generado exitosamente!${NC}"
    echo -e "${GREEN}El APK se encuentra en: $(pwd)/app-debug.apk${NC}"
else
    echo -e "${RED}Error: No se pudo generar el APK${NC}"
    exit 1
fi 