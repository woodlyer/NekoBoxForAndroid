#!/bin/bash
set -e

# ==========================================
# 1. 初始化工作区和下载依赖工具
# ==========================================
mkdir -p ~/android-build-workspace && cd ~/android-build-workspace
echo "[1/6] 正在安装编译依赖 (JDK 17, Unzip 等)..."
sudo apt-get update && sudo apt-get install -y openjdk-17-jdk unzip wget curl jq

# ==========================================
# 2. 自动配置 Android SDK 和 NDK
# ==========================================
export ANDROID_HOME=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

if [ ! -d "$ANDROID_HOME" ]; then
    echo "[2/6] 正在下载并配置 Android SDK & NDK..."
    mkdir -p $ANDROID_HOME/cmdline-tools
    cd $ANDROID_HOME/cmdline-tools
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -O cmd.zip
    unzip -q cmd.zip && mv cmdline-tools latest && rm cmd.zip
    
    yes | sdkmanager --licenses > /dev/null
    sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0" "ndk;25.2.9519653" > /dev/null
fi
export ANDROID_NDK_HOME=$ANDROID_HOME/ndk/25.2.9519653

# ==========================================
# 3. 克隆仓库代码
# ==========================================
cd ~/android-build-workspace
echo "[3/6] 正在克隆您的 NekoBox 和 Gost 插件代码..."
if [ ! -d "NekoBoxForAndroid" ]; then
    git clone -b add-gost-support https://github.com/woodlyer/NekoBoxForAndroid.git
fi
if [ ! -d "plugins" ]; then
    git clone -b add-gost-support https://github.com/woodlyer/plugins.git
fi

# ==========================================
# 4. 下载 Gost 核心二进制程序
# ==========================================
echo "[4/6] 正在下载 Gost 核心二进制文件..."
GOST_DIR=~/android-build-workspace/plugins/app_gost/src/main/jniLibs
mkdir -p $GOST_DIR/arm64-v8a $GOST_DIR/armeabi-v7a $GOST_DIR/x86 $GOST_DIR/x86_64

GOST_VER="3.0.0-rc10"
cd /tmp
wget -q "https://github.com/go-gost/gost/releases/download/v${GOST_VER}/gost_${GOST_VER}_linux_arm64.tar.gz" -O arm64.tar.gz
wget -q "https://github.com/go-gost/gost/releases/download/v${GOST_VER}/gost_${GOST_VER}_linux_armv7.tar.gz" -O arm32.tar.gz
wget -q "https://github.com/go-gost/gost/releases/download/v${GOST_VER}/gost_${GOST_VER}_linux_386.tar.gz" -O x86.tar.gz
wget -q "https://github.com/go-gost/gost/releases/download/v${GOST_VER}/gost_${GOST_VER}_linux_amd64.tar.gz" -O x86_64.tar.gz

tar -xzf arm64.tar.gz gost && mv gost $GOST_DIR/arm64-v8a/libgost.so
tar -xzf arm32.tar.gz gost && mv gost $GOST_DIR/armeabi-v7a/libgost.so
tar -xzf x86.tar.gz gost && mv gost $GOST_DIR/x86/libgost.so
tar -xzf x86_64.tar.gz gost && mv gost $GOST_DIR/x86_64/libgost.so
chmod +x $GOST_DIR/*/libgost.so

# ==========================================
# 5. 开始编译 NekoBoxForAndroid 主程序
# ==========================================
echo "[5/6] 正在编译 NekoBox 主程序 (这可能需要一些时间，请耐心等待)..."
cd ~/android-build-workspace/NekoBoxForAndroid/libcore
./init.sh
./build.sh
cd ~/android-build-workspace/NekoBoxForAndroid
./gradlew assembleRelease

# ==========================================
# 6. 开始编译 Gost 插件
# ==========================================
echo "[6/6] 正在编译 Gost 插件..."
cd ~/android-build-workspace/plugins
./gradlew :app_gost:assembleRelease

# ==========================================
# 7. 提取产物
# ==========================================
mkdir -p ~/android-build-workspace/APKs
cp ~/android-build-workspace/NekoBoxForAndroid/app/build/outputs/apk/release/*.apk ~/android-build-workspace/APKs/
cp ~/android-build-workspace/plugins/app_gost/build/outputs/apk/release/*.apk ~/android-build-workspace/APKs/

echo "=========================================="
echo "🎉 编译大功告成！"
echo "您的 APK 安装包已经存放在: ~/android-build-workspace/APKs/ 目录下。"
echo "=========================================="
