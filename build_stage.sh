#!/bin/bash
set -e

# 设置环境变量
export ANDROID_HOME=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_NDK_HOME=$ANDROID_HOME/ndk/25.2.9519653

# 创建工作区
mkdir -p ~/android-build-workspace

stage_env() {
    echo "=========================================="
    echo "[阶段 1/6] 正在安装编译依赖 (JDK 17, Unzip 等)..."
    echo "=========================================="
    sudo apt-get update && sudo apt-get install -y openjdk-17-jdk unzip wget curl jq

    if [ ! -d "$ANDROID_HOME" ]; then
        echo "[阶段 2/6] 正在下载并配置 Android SDK & NDK..."
        mkdir -p $ANDROID_HOME/cmdline-tools
        cd $ANDROID_HOME/cmdline-tools
        wget -q https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -O cmd.zip
        unzip -q cmd.zip && mv cmdline-tools latest && rm cmd.zip

        yes | sdkmanager --licenses > /dev/null
        sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0" "ndk;25.2.9519653" > /dev/null
    else
        echo "Android SDK 已存在，跳过下载。"
    fi
}

stage_clone() {
    echo "=========================================="
    echo "[阶段 3/6] 正在克隆 NekoBox 和 Gost 插件代码..."
    echo "=========================================="
    cd ~/android-build-workspace
    if [ ! -d "NekoBoxForAndroid" ]; then
        git clone -b add-gost-support https://github.com/woodlyer/NekoBoxForAndroid.git
    else
        echo "NekoBoxForAndroid 已存在，跳过克隆。"
    fi
    if [ ! -d "plugins" ]; then
        git clone -b add-gost-support https://github.com/woodlyer/plugins.git
    else
        echo "plugins 已存在，跳过克隆。"
    fi
}

stage_gost() {
    echo "=========================================="
    echo "[阶段 4/6] 正在下载 Gost 核心二进制文件 (v2)..."
    echo "=========================================="
    GOST_DIR=~/android-build-workspace/plugins/app_gost/src/main/jniLibs
    mkdir -p $GOST_DIR/arm64-v8a $GOST_DIR/armeabi-v7a $GOST_DIR/x86 $GOST_DIR/x86_64

    # 检查是否已下载
    if [ -f "$GOST_DIR/arm64-v8a/libgost.so" ] && [ -f "$GOST_DIR/armeabi-v7a/libgost.so" ] && grep -q "gost-linux-armv8" "$GOST_DIR/arm64-v8a/libgost.so" 2>/dev/null; then
        echo "Gost 二进制文件已存在，跳过下载。"
        return
    fi

    # 清除旧版本以防混淆
    rm -f $GOST_DIR/*/libgost.so

    GOST_VER="2.11.5"
    cd /tmp
    wget -q "https://github.com/ginuerzh/gost/releases/download/v${GOST_VER}/gost-linux-armv8-${GOST_VER}.gz" -O arm64.gz
    wget -q "https://github.com/ginuerzh/gost/releases/download/v${GOST_VER}/gost-linux-armv7-${GOST_VER}.gz" -O arm32.gz
    wget -q "https://github.com/ginuerzh/gost/releases/download/v${GOST_VER}/gost-linux-386-${GOST_VER}.gz" -O x86.gz
    wget -q "https://github.com/ginuerzh/gost/releases/download/v${GOST_VER}/gost-linux-amd64-${GOST_VER}.gz" -O x86_64.gz

    gzip -df arm64.gz && mv arm64 $GOST_DIR/arm64-v8a/libgost.so
    gzip -df arm32.gz && mv arm32 $GOST_DIR/armeabi-v7a/libgost.so
    gzip -df x86.gz && mv x86 $GOST_DIR/x86/libgost.so
    gzip -df x86_64.gz && mv x86_64 $GOST_DIR/x86_64/libgost.so
    chmod +x $GOST_DIR/*/libgost.so
    echo "Gost (v2) 核心二进制文件下载完成。"
}

stage_core() {
    echo "=========================================="
    echo "[阶段 5/6] 正在初始化并编译 NekoBox libcore..."
    echo "=========================================="
    cd ~/android-build-workspace/NekoBoxForAndroid
    chmod +x run buildScript/lib/core/*.sh buildScript/init/*.sh
    ./run lib core init
    ./run lib core build
}

stage_app() {
    echo "=========================================="
    echo "[阶段 6/6 - 部分A] 正在编译 NekoBox 主程序 APK..."
    echo "=========================================="
    cd ~/android-build-workspace/NekoBoxForAndroid
    ./gradlew assembleRelease
    
    mkdir -p ~/android-build-workspace/APKs
    find ~/android-build-workspace/NekoBoxForAndroid/app/build/outputs/apk/ -name "*.apk" -exec cp {} ~/android-build-workspace/APKs/ \;
    echo "NekoBox 主程序编译完成，APK 已保存至 ~/android-build-workspace/APKs/"
}

stage_plugin() {
    echo "=========================================="
    echo "[阶段 6/6 - 部分B] 正在编译 Gost 插件 APK..."
    echo "=========================================="
    cd ~/android-build-workspace/plugins
    ./gradlew :app_gost:assembleRelease

    mkdir -p ~/android-build-workspace/APKs
    find ~/android-build-workspace/plugins/app_gost/build/outputs/apk/ -name "*.apk" -exec cp {} ~/android-build-workspace/APKs/ \;
    echo "Gost 插件编译完成，APK 已保存至 ~/android-build-workspace/APKs/"
}

show_help() {
    echo "用法: $0 [阶段名]"
    echo "可选阶段列表:"
    echo "  env     - 安装依赖环境 (apt 依赖, Android SDK & NDK)"
    echo "  clone   - 克隆源代码仓库"
    echo "  gost    - 下载 Gost 核心二进制文件"
    echo "  core    - 初始化并编译 Go libcore 核心"
    echo "  app     - 编译 NekoBox 客户端主程序 APK"
    echo "  plugin  - 编译 Gost 插件 APK"
    echo "  all     - 顺序跑完所有阶段 (默认)"
    echo "例如: $0 core  (单独编译 Go libcore)"
}

# 解析输入参数
STAGE=${1:-all}

case "$STAGE" in
    env)
        stage_env
        ;;
    clone)
        stage_clone
        ;;
    gost)
        stage_gost
        ;;
    core)
        stage_core
        ;;
    app)
        stage_app
        ;;
    plugin)
        stage_plugin
        ;;
    all)
        stage_env
        stage_clone
        stage_gost
        stage_core
        stage_app
        stage_plugin
        echo "=========================================="
        echo "🎉 所有阶段编译大功告成！"
        echo "你的 APK 安装包已经存放在: ~/android-build-workspace/APKs/ 目录下。"
        echo "=========================================="
        ;;
    -h|--help|help)
        show_help
        ;;
    *)
        echo "错误: 未知的阶段名称 '$STAGE'"
        show_help
        exit 1
        ;;
esac
