#!/bin/bash
set -e

APK_DIR=~/android-build-workspace/APKs
KEYSTORE=~/test.keystore
PASSWORD="password"

# 1. 检查 keystore 是否已存在，不存在则生成
if [ ! -f "$KEYSTORE" ]; then
    echo "正在生成测试签名密钥 test.keystore..."
    keytool -genkey -v -keystore "$KEYSTORE" -alias testkey -keyalg RSA -keysize 2048 -validity 10000 -storepass "$PASSWORD" -keypass "$PASSWORD" -dname "CN=test, O=test, C=US"
fi

# 2. 找到所有的 unsigned.apk 并签名
echo "正在为 APK 签名..."
for apk in $APK_DIR/*-unsigned.apk; do
    if [ -f "$apk" ]; then
        signed_apk="${apk/-unsigned.apk/-signed.apk}"
        echo "正在签名: $(basename "$apk") -> $(basename "$signed_apk")"
        
        # 3. 使用 apksigner 签名
        /home/codespace/android-sdk/build-tools/34.0.0/apksigner sign \
            --ks "$KEYSTORE" \
            --ks-key-alias testkey \
            --ks-pass pass:"$PASSWORD" \
            --key-pass pass:"$PASSWORD" \
            --out "$signed_apk" \
            "$apk"
    fi
done

echo "=========================================="
echo "🎉 所有 APK 签名完成！"
echo "已在 ~/android-build-workspace/APKs/ 下生成对应的 -signed.apk 文件。"
echo "=========================================="
