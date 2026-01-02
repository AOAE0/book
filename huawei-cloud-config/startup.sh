#!/bin/bash

# 校园二手书交易平台启动脚本
# 适用于华为云ECS环境

# 设置项目路径
PROJECT_HOME="/home/book-platform"
JAR_NAME="book-api-0.0.1-SNAPSHOT.jar"
LOG_FILE="/var/log/book-platform.log"

# 创建必要的目录
mkdir -p $PROJECT_HOME/logs
mkdir -p $PROJECT_HOME/uploads

# 设置环境变量
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH=$JAVA_HOME/bin:$PATH

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "Java未安装，请先安装Java 21"
    exit 1
fi

echo "启动校园二手书交易平台..."
echo "项目路径: $PROJECT_HOME"
echo "日志文件: $LOG_FILE"

# 进入项目目录
cd $PROJECT_HOME

# 启动应用
nohup java -jar $JAR_NAME \
    --spring.profiles.active=cloud \
    --server.port=8080 \
    >> $LOG_FILE 2>&1 &

# 获取进程ID
PID=$!
echo "应用已启动，进程ID: $PID"

# 保存PID到文件
echo $PID > /tmp/book-platform.pid

# 等待几秒检查应用是否正常启动
sleep 5

# 检查进程是否仍在运行
if ps -p $PID > /dev/null; then
    echo "应用启动成功！"
    echo "访问地址: http://your-ecs-public-ip:8080/book-api"
else
    echo "应用启动失败，请检查日志: $LOG_FILE"
    exit 1
fi