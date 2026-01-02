# 华为云RDS数据库配置说明

## RDS实例创建后需要进行的配置

### 1. 获取数据库连接信息

在华为云控制台中，进入RDS实例详情页面：

1. 进入"连接信息"标签页
2. 记录以下信息：
   - **内网地址**：如 `192.168.1.100`
   - **端口**：3306（默认）
   - **数据库用户名**：root
   - **数据库密码**：创建实例时设置的密码

### 2. 配置安全组

确保RDS实例的安全组允许ECS实例访问：

1. 在RDS实例页面，点击安全组名称
2. 添加入方向规则：
   - 协议：TCP
   - 端口：3306
   - 源地址：ECS实例所在的安全组或子网
   - 描述：允许ECS访问数据库

### 3. 创建数据库

连接到RDS实例后，执行以下SQL创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS wisebookpal DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. 创建专用数据库用户（推荐）

为了安全起见，建议创建专用用户：

```sql
-- 创建专用用户
CREATE USER 'book_user'@'%' IDENTIFIED BY 'your_secure_password';

-- 授予权限
GRANT ALL PRIVILEGES ON wisebookpal.* TO 'book_user'@'%';

-- 刷新权限
FLUSH PRIVILEGES;
```

### 5. 更新应用配置

使用专用用户更新 `application-cloud.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://rds内网地址:3306/wisebookpal?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
    username: book_user  # 使用专用用户
    password: your_secure_password  # 使用安全密码
```

### 6. 导入数据

使用MySQL客户端导入数据库结构：

```bash
# 使用root用户导入
mysql -h rds内网地址 -u root -p wisebookpal < mysql_init.sql

# 或者使用专用用户
mysql -h rds内网地址 -u book_user -p wisebookpal < mysql_init.sql
```

### 7. 验证连接

测试数据库连接是否正常：

```bash
# 连接到数据库
mysql -h rds内网地址 -u book_user -p

# 选择数据库
USE wisebookpal;

# 查看表
SHOW TABLES;

# 查看用户数据
SELECT COUNT(*) FROM users;
```

### 8. 配置备份策略

华为云RDS提供自动备份功能：

1. 进入RDS实例的"备份恢复"页面
2. 开启自动备份
3. 设置备份保留期（如7天）
4. 配置备份时间段（建议在业务低峰期）

### 9. 监控和告警

配置数据库监控：

1. 进入RDS实例的"监控告警"页面
2. 开启监控指标：
   - CPU使用率
   - 内存使用率
   - 连接数
   - 磁盘使用率
3. 设置告警规则和通知方式

### 10. 常见问题

#### 连接失败
- 检查内网地址是否正确
- 确认ECS和RDS在同一VPC
- 验证安全组配置
- 检查用户名密码

#### 性能问题
- 检查数据库规格是否满足需求
- 优化SQL查询
- 考虑升级RDS规格
- 添加合适的索引

#### 存储空间不足
- 清理不必要的日志
- 删除过期数据
- 升级RDS存储空间

## 注意事项

1. **安全**：定期更新数据库密码，不要使用简单密码
2. **备份**：设置自动备份，定期手动备份重要数据
3. **监控**：开启监控告警，及时发现问题
4. **维护**：定期检查数据库性能和安全状态
5. **成本**：根据实际需求选择合适的RDS规格，避免资源浪费