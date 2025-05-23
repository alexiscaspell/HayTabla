FROM ubuntu:22.04

# Evitar interacciones durante la instalación
ENV DEBIAN_FRONTEND=noninteractive

# Instalar dependencias necesarias
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    wget \
    unzip \
    git \
    && rm -rf /var/lib/apt/lists/*

# Configurar variables de entorno
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$JAVA_HOME/bin:$PATH:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools

# Descargar e instalar Android SDK Command Line Tools
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip \
    && unzip commandlinetools-linux-9477386_latest.zip -d ${ANDROID_HOME}/cmdline-tools \
    && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && rm commandlinetools-linux-9477386_latest.zip

# Aceptar licencias y instalar componentes necesarios
RUN yes | sdkmanager --licenses \
    && sdkmanager "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0"

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos del proyecto
COPY . .

# Descargar e instalar Gradle
RUN wget https://services.gradle.org/distributions/gradle-8.2-bin.zip \
    && unzip gradle-8.2-bin.zip \
    && mv gradle-8.2 /opt/gradle \
    && rm gradle-8.2-bin.zip

# Configurar Gradle en el PATH
ENV PATH=/opt/gradle/bin:$PATH

# Inicializar Gradle Wrapper y construir el APK
RUN gradle wrapper && \
    chmod +x ./gradlew && \
    ./gradlew assembleDebug

# El APK se generará en app/build/outputs/apk/debug/app-debug.apk 