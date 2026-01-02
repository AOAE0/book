#!/bin/bash

# 校园二手书交易平台华为云快速部署脚本
# 此脚本将在CentOS系统上自动安装和配置所需环境

set -e  # 遇到错误时退出

echo "============================================"
echo "校园二手书交易平台华为云部署脚本"
echo "============================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否以root身份运行
check_root() {
    if [[ $EUID -ne 0 ]]; then
        log_error "此脚本需要以root身份运行"
        exit 1
    fi
}

# 检查系统版本
check_system() {
    if ! command -v yum &> /dev/null; then
        log_error "此脚本仅支持CentOS/RHEL系统"
        exit 1
    fi
    
    log_info "系统检查通过"
}

# 安装Java 21
install_java() {
    log_info "开始安装Java 21..."
    
    # 更新系统
    yum update -y
    
    # 安装Java 21
    yum install -y java-21-openjdk java-21-openjdk-devel
    
    # 设置JAVA_HOME
    export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
    echo "export JAVA_HOME=$JAVA_HOME" >> /etc/profile
    echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> /etc/profile
    
    # 验证安装
    if java -version && javac -version; then
        log_info "Java 21安装成功"
    else
        log_error "Java 21安装失败"
        exit 1
    fi
}

# 安装Maven
install_maven() {
    log_info "开始安装Maven..."
    
    # 检查Maven是否已安装
    if command -v mvn &> /dev/null; then
        log_info "Maven已安装，跳过安装步骤"
        return
    fi
    
    # 创建安装目录
    mkdir -p /usr/local/maven
    
    # 检查本地Maven包
    if [ -f "/root/apache-maven-3.6.3-bin.tar.gz" ]; then
        cd /usr/local/maven
        tar -xzf /root/apache-maven-3.6.3-bin.tar.gz
        log_info "使用本地Maven包安装完成"
    else
        log_warn "未找到本地Maven包，请手动下载并放置到/root/目录"
        log_info "下载链接: https://mirrors.huaweicloud.com/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz"
        return 1
    fi
    
    # 配置环境变量
    echo 'export MAVEN_HOME=/usr/local/maven/apache-maven-3.6.3' >> /etc/profile
    echo 'export PATH=$MAVEN_HOME/bin:$PATH' >> /etc/profile
    
    # 验证安装
    source /etc/profile
    if mvn -version; then
        log_info "Maven安装成功"
    else
        log_error "Maven安装失败"
        return 1
    fi
}

# 创建项目目录
create_project_structure() {
    log_info "创建项目目录结构..."
    
    PROJECT_DIR="/home/book-platform"
    
    # 创建目录
    mkdir -p $PROJECT_DIR
    mkdir -p $PROJECT_DIR/logs
    mkdir -p $PROJECT_DIR/uploads
    mkdir -p $PROJECT_DIR/backup
    
    # 设置权限
    chown -R root:root $PROJECT_DIR
    chmod 755 $PROJECT_DIR
    
    log_info "项目目录创建完成: $PROJECT_DIR"
}

# 配置防火墙
configure_firewall() {
    log_info "配置防火墙规则..."
    
    # 开启防火墙
    systemctl start firewalld
    systemctl enable firewalld
    
    # 开放应用端口
    firewall-cmd --permanent --add-port=8080/tcp
    firewall-cmd --reload
    
    log_info "防火墙配置完成"
}

# 配置systemd服务
setup_systemd_service() {
    log_info "配置systemd服务..."
    
    # 复制服务文件
    if [ -f "book-platform.service" ]; then
        cp book-platform.service /etc/systemd/system/
        systemctl daemon-reload
        systemctl enable book-platform.service
        log_info "systemd服务配置完成"
    else
        log_warn "未找到book-platform.service文件"
    fi
}

# 部署完成后的说明
show_deployment_info() {
    echo ""
    echo "============================================"
    echo "部署完成！请执行以下步骤："
    echo "============================================"
    echo "1. 上传应用文件到 /home/book-platform/ 目录："
    echo "   - book-api-0.0.1-SNAPSHOT.jar"
    echo "   - application-cloud.yml"
    echo "   - mysql_init.sql"
    echo ""
    echo "2. 配置数据库连接："
    echo "   编辑 application-cloud.yml 文件"
    echo "   修改spring.datasource相关配置"
    echo ""
    echo "3. 初始化数据库："
    echo "   mysql -h RDS内网地址 -u root -p wisebookpal < mysql_init.sql"
    echo ""
    echo "4. 启动应用："
    echo "   systemctl start book-platform.service"
    echo ""
    echo "5. 查看应用状态："
    echo "   systemctl status book-platform.service"
    echo ""
    echo "6. 查看应用日志："
    echo "   journalctl -u book-platform.service -f"
    echo ""
    echo "应用访问地址：http://您的ECS公网IP:8080/book-api"
    echo "============================================"
}

# 主函数
main() {
    log_info "开始执行华为云部署脚本..."
    
    check_root
    check_system
    
    # 安装Java
    install_java
    
    # 安装Maven（可选）
    install_maven
    
    # 创建项目结构
    create_project_structure
    
    # 配置防火墙
    configure_firewall
    
    # 配置systemd服务
    setup_systemd_service
    
    # 显示部署信息
    show_deployment_info
    
    log_info "基础环境部署完成！"
}

# 脚本参数处理
case "${1:-}" in
    --help|-h)
        echo "使用方法: $0 [选项]"
        echo "选项:"
        echo "  --help, -h     显示帮助信息"
        echo "  --java-only    仅安装Java"
        echo "  --maven-only   仅安装Maven"
        echo "  --all          安装所有组件（默认）"
        exit 0
        ;;
    --java-only)
        check_root
        check_system
        install_java
        ;;
    --maven-only)
        install_maven
        ;;
    --all|"")
        main
        ;;
    *)
        log_error "未知选项: $1"
        echo "使用 --help 查看帮助信息"
        exit 1
        ;;
esac