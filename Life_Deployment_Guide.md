# 浮生卷模块部署指南

## 环境要求

- Java 8+
- MySQL 5.7+
- Maven 3.6+
- Spring Boot 2.3.4

## 部署步骤

### 1. 数据库初始化

执行 `Life_Database_Init.sql` 脚本初始化数据库：

```sql
-- 连接到MySQL数据库
mysql -u root -p

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS bot DEFAULT CHARSET utf8mb4;
USE bot;

-- 执行初始化脚本
source Life_Database_Init.sql;
```

### 2. 配置文件修改

在 `Boot/src/main/resources/application.properties` 中确认数据库连接配置：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/bot?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# MyBatis配置
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.type-aliases-package=com.bot.*.dao.entity
```

### 3. 背景图片配置

将 `life_back.png` 背景图片放在项目根目录下，或修改 `ImageGenerationServiceImpl` 中的路径配置。

### 4. Maven编译

```bash
# 在项目根目录执行
mvn clean compile

# 或者完整构建
mvn clean package -DskipTests
```

### 5. 启动应用

```bash
# 启动Spring Boot应用
java -jar Boot/target/Boot-1.5.0.0.jar

# 或使用Maven启动
mvn spring-boot:run -pl Boot
```

## 验证部署

### 1. 检查日志

启动后检查控制台日志，确认：
- Spring容器正常启动
- 数据库连接成功
- Life模块的Bean正常加载

### 2. 测试游戏功能

通过聊天机器人发送 `浮生卷` 测试游戏入口是否正常工作。

### 3. 数据库验证

检查数据库表是否正确创建：

```sql
-- 查看Life模块相关表
SHOW TABLES LIKE 'life_%';

-- 检查初始数据
SELECT * FROM life_map;
SELECT * FROM life_monster;
SELECT * FROM life_item;
```

## 配置说明

### 系统配置表

Life模块的配置存储在 `life_system_config` 表中，可以动态修改：

| 配置键 | 默认值 | 说明 |
|--------|--------|------|
| speed_armor_break_rate | 0.005 | 每点速度增加的破防率 |
| constitution_health_rate | 10 | 每点体质增加的血量 |
| constitution_defense_rate | 1 | 每点体质增加的防御 |
| spirit_critical_rate | 0.01 | 每点灵力增加的会心率 |
| spirit_critical_damage_rate | 0.005 | 每点灵力增加的会心效果 |
| strength_attack_rate | 6 | 每点力量增加的攻击力 |
| strength_armor_break_rate | 0.01 | 每点力量增加的破防率 |
| attribute_restraint_damage_bonus | 20 | 属性克制伤害加成百分比 |
| attribute_restrained_defense_penalty | 10 | 被属性克制防御减少百分比 |
| max_armor_break_rate | 30 | 最大破防率 |

### 图片生成配置

图片生成服务的配置在 `ImageGenerationServiceImpl` 中：

```java
private static final String BACKGROUND_IMAGE_PATH = "life_back.png";
private static final String OUTPUT_DIR = "temp/life_images/";
```

可根据实际部署环境调整路径。

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查数据库服务是否启动
   - 验证连接字符串、用户名、密码
   - 确认数据库权限设置

2. **图片生成失败**
   - 检查背景图片文件是否存在
   - 确认输出目录权限
   - 验证字体文件是否可用

3. **模块加载失败**
   - 检查Maven依赖是否正确
   - 确认包扫描路径配置
   - 验证Spring配置注解

### 日志配置

在 `logback-spring.xml` 中添加Life模块的日志配置：

```xml
<logger name="com.bot.life" level="DEBUG"/>
```

### 性能优化

1. **数据库连接池**
   ```properties
   spring.datasource.hikari.maximum-pool-size=20
   spring.datasource.hikari.minimum-idle=5
   ```

2. **图片缓存**
   - 考虑实现图片缓存机制
   - 定期清理临时文件

3. **内存优化**
   - 监控游戏状态缓存大小
   - 实现过期清理机制

## 监控和维护

### 数据库维护

定期执行以下维护任务：

```sql
-- 清理过期临时文件记录
DELETE FROM life_temp_files WHERE expire_time < NOW();

-- 检查玩家数据完整性
SELECT COUNT(*) FROM life_player;
SELECT COUNT(*) FROM life_game_status;

-- 分析热门功能
SELECT config_key, config_value FROM life_system_config;
```

### 应用监控

监控关键指标：
- 在线游戏玩家数量
- 图片生成响应时间
- 数据库连接池状态
- 内存使用情况

### 备份策略

建议定期备份：
- 玩家数据（life_player表）
- 游戏状态（life_game_status表）
- 系统配置（life_system_config表）

## 扩展开发

### 添加新功能

1. 在对应的包中创建新的实体类
2. 创建对应的Mapper接口和XML
3. 实现业务逻辑服务
4. 在LifeHandler中添加新的指令处理
5. 更新数据库脚本和文档

### 自定义配置

通过修改 `life_system_config` 表中的配置来调整游戏平衡性：

```sql
-- 修改属性影响系数
UPDATE life_system_config SET config_value = '15' WHERE config_key = 'constitution_health_rate';

-- 修改属性克制效果
UPDATE life_system_config SET config_value = '25' WHERE config_key = 'attribute_restraint_damage_bonus';
```

## 技术支持

如遇到部署问题，请检查：
1. 项目结构是否完整
2. 依赖版本是否兼容
3. 数据库版本是否支持
4. 系统环境是否满足要求

---

*部署完成后，请参考 `Life_User_Manual.md` 了解游戏操作方法。*
