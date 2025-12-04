# 状态查询API文档

<cite>
**本文档引用的文件**
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java)
- [Collector.java](file://Game/src/main/java/com/bot/game/chain/Collector.java)
- [GameHandler.java](file://Game/src/main/java/com/bot/game/service/GameHandler.java)
- [GameCommonHolder.java](file://Game/src/main/java/com/bot/game/service/GameCommonHolder.java)
- [StatusMonitor.java](file://Base/src/main/java/com/bot/base/service/StatusMonitor.java)
- [SystemConfigCache.java](file://Common/src/main/java/com/bot/common/config/SystemConfigCache.java)
- [DefaultChatServiceImpl.java](file://Base/src/main/java/com/bot/base/service/impl/DefaultChatServiceImpl.java)
- [GamePlayer.java](file://Game/src/main/java/com/bot/game/dao/entity/GamePlayer.java)
- [ENStatus.java](file://Common/src/main/java/com/bot/common/enums/ENStatus.java)
- [ENRegStatus.java](file://Common/src/main/java/com/bot/common/enums/ENRegStatus.java)
</cite>

## 目录
1. [概述](#概述)
2. [系统架构](#系统架构)
3. [核心组件分析](#核心组件分析)
4. [状态查询实现](#状态查询实现)
5. [在线状态管理](#在线状态管理)
6. [性能优化策略](#性能优化策略)
7. [故障排除指南](#故障排除指南)
8. [总结](#总结)

## 概述

本文档详细介绍了Bot项目中的状态查询API系统，重点解析玩家在线状态查询接口的实现机制。该系统通过Collector接口的isOnLine方法实现状态判断，结合token有效性验证、会话超时机制和智能缓存策略，为高并发场景提供稳定的状态查询服务。

## 系统架构

```mermaid
graph TB
subgraph "客户端层"
Client[客户端应用]
API[状态查询API]
end
subgraph "服务层"
GameHandler[GameHandler接口]
GameHandlerImpl[GameHandlerServiceImpl]
Collector[Collector接口]
end
subgraph "状态管理层"
GameCommonHolder[GameCommonHolder]
StatusMonitor[StatusMonitor]
SystemConfigCache[SystemConfigCache]
end
subgraph "数据存储层"
GamePlayer[(GamePlayer表)]
JXCache[(JXCache表)]
BotUser[(BotUser表)]
end
Client --> API
API --> GameHandler
GameHandler --> GameHandlerImpl
GameHandlerImpl --> Collector
Collector --> GameCommonHolder
GameCommonHolder --> StatusMonitor
StatusMonitor --> SystemConfigCache
SystemConfigCache --> BotUser
GameCommonHolder --> GamePlayer
GameCommonHolder --> JXCache
```

**架构图来源**
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L26-L189)
- [Collector.java](file://Game/src/main/java/com/bot/game/chain/Collector.java#L8-L38)
- [GameCommonHolder.java](file://Game/src/main/java/com/bot/game/service/GameCommonHolder.java#L28-L109)

## 核心组件分析

### GameHandler接口

GameHandler是游戏处理器的核心接口，定义了状态查询和交互的基本方法。

```mermaid
classDiagram
class GameHandler {
<<interface>>
+String exit(String token)
+String play(String reqContent, String token)
+String manage(String reqContent)
}
class GameHandlerServiceImpl {
-Collector collector
-GamePlayerMapper gamePlayerMapper
-String[] WAIT_REG
-String[] WAIT_LOGIN
+String exit(String token)
+String play(String reqContent, String token)
+String manage(String reqContent)
-GamePlayer getGamePlayer(String token, String nickName)
-boolean isExsitName(String nickName)
-Map~String,Object~ getMapperMap()
}
GameHandler <|-- GameHandlerServiceImpl
```

**类图来源**
- [GameHandler.java](file://Game/src/main/java/com/bot/game/service/GameHandler.java#L7-L29)
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L26-L189)

### Collector接口与状态判断

Collector接口定义了状态查询的核心方法，其中isOnLine方法负责判断玩家是否在线。

```mermaid
classDiagram
class Collector {
<<interface>>
+String buildCollector(String token, Map~String,Object~ mapperMap)
+String toNextOrPrevious(String token, String point)
+void removeToken(String token)
+boolean isOnLine(String token)
}
note for Collector "isOnLine方法实现：<br/>1. Token有效性验证<br/>2. 会话超时检查<br/>3. 在线状态缓存"
```

**类图来源**
- [Collector.java](file://Game/src/main/java/com/bot/game/chain/Collector.java#L8-L38)

### 状态监控器

StatusMonitor负责系统的整体状态监控和会话管理。

```mermaid
sequenceDiagram
participant SM as StatusMonitor
participant SC as SystemConfigCache
participant DT as DefaultChatServiceImpl
participant PS as PictureService
SM->>SM : 定时任务启动(每5分钟)
SM->>SC : 检查用户到期时间
SC-->>SM : 返回过期用户列表
SM->>DT : 清理过期会话
DT->>DT : 移除TOKEN_2_BASE_CHAT_ID_MAP中的过期token
SM->>PS : 清理等待处理图片
PS->>PS : 移除WAIT_DEAL_PICTURE_MAP中的过期token
SM->>SM : 继续下一轮监控
```

**序列图来源**
- [StatusMonitor.java](file://Base/src/main/java/com/bot/base/service/StatusMonitor.java#L67-L91)
- [DefaultChatServiceImpl.java](file://Base/src/main/java/com/bot/base/service/impl/DefaultChatServiceImpl.java#L116-L129)

**节来源**
- [StatusMonitor.java](file://Base/src/main/java/com/bot/base/service/StatusMonitor.java#L67-L91)
- [DefaultChatServiceImpl.java](file://Base/src/main/java/com/bot/base/service/impl/DefaultChatServiceImpl.java#L116-L129)

## 状态查询实现

### isOnLine方法判断逻辑

状态查询的核心在于isOnLine方法的实现，该方法通过多层验证确保状态准确性。

```mermaid
flowchart TD
Start([开始状态查询]) --> CheckToken{Token是否存在?}
CheckToken --> |否| Offline[返回离线状态]
CheckToken --> |是| CheckTimeout{会话是否超时?}
CheckTimeout --> |是| CleanupSession[清理过期会话]
CleanupSession --> Offline
CheckTimeout --> |否| CheckOnline{是否在线状态?}
CheckOnline --> |是| Online[返回在线状态]
CheckOnline --> |否| Offline
Online --> End([结束])
Offline --> End
```

**流程图来源**
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L95-L97)

### 玩家状态管理

GamePlayer实体记录玩家的基本状态信息，包括在线状态、灵魂力量等关键属性。

| 字段名 | 类型 | 描述 | 默认值 |
|--------|------|------|--------|
| id | String | 玩家唯一标识(通常为token) | - |
| status | String | 玩家状态(NORMAL/LOCK) | NORMAL |
| soulPower | Integer | 灵魂力量 | 1 |
| actionPoint | Integer | 行动点数 | 100 |
| money | Integer | 金钱数量 | 0 |

**表格来源**
- [GamePlayer.java](file://Game/src/main/java/com/bot/game/dao/entity/GamePlayer.java#L12-L33)

### 系统配置缓存

SystemConfigCache提供了全局的配置缓存机制，支持高效的在线状态查询。

```mermaid
classDiagram
class SystemConfigCache {
<<static>>
+Map~String,Date~ userDateMap
+Map~String,ENChatEngine~ userChatEngine
+String[] userWorkDaily
+String[] openServer
+String[] emojiUser
+String[] bottleUser
+Map~String,String~ welcomeMap
+CopyOnWriteArrayList~ActivityAwardDTO~ activityAwardList
+loadUsers() void
+loadUserConfig() void
}
note for SystemConfigCache "用户到期时间缓存 : <br/>userDateMap : token -> 到期时间<br/>支持快速过期检查"
```

**类图来源**
- [SystemConfigCache.java](file://Common/src/main/java/com/bot/common/config/SystemConfigCache.java#L64-L115)

**节来源**
- [SystemConfigCache.java](file://Common/src/main/java/com/bot/common/config/SystemConfigCache.java#L64-L115)
- [GamePlayer.java](file://Game/src/main/java/com/bot/game/dao/entity/GamePlayer.java#L12-L33)

## 在线状态管理

### 会话超时机制

系统实现了多层次的会话超时控制机制：

1. **聊天会话超时**：3小时无活动自动清理
2. **图片处理超时**：10分钟无响应自动清理  
3. **用户到期检查**：实时验证用户权限状态

```mermaid
sequenceDiagram
participant User as 用户
participant Monitor as StatusMonitor
participant Cache as SystemConfigCache
participant Session as 会话管理器
User->>Session : 发送消息
Session->>Cache : 检查用户状态
Cache-->>Session : 返回用户信息
Session->>Session : 验证token有效性
Session->>Session : 检查会话超时
alt 会话超时
Session->>Session : 清理过期会话
Session-->>User : 提示超时退出
else 会话有效
Session-->>User : 处理请求
end
```

**序列图来源**
- [StatusMonitor.java](file://Base/src/main/java/com/bot/base/service/StatusMonitor.java#L116-L129)

### 在线状态缓存策略

系统采用分层缓存策略优化状态查询性能：

| 缓存层级 | 缓存类型 | 过期策略 | 查询优先级 |
|----------|----------|----------|------------|
| L1 | 内存缓存 | 实时更新 | 最高 |
| L2 | 数据库缓存 | 定时刷新 | 中等 |
| L3 | 文件缓存 | 手动触发 | 最低 |

**表格来源**
- [StatusMonitor.java](file://Base/src/main/java/com/bot/base/service/StatusMonitor.java#L48-L66)

### 状态同步机制

```mermaid
graph LR
subgraph "状态变更源"
UserAction[用户操作]
TimerTask[定时任务]
ManualOp[手动操作]
end
subgraph "状态同步"
SyncManager[状态同步器]
CacheUpdate[缓存更新]
DBSync[数据库同步]
end
subgraph "状态消费者"
API[API接口]
Monitor[监控系统]
Analytics[统计分析]
end
UserAction --> SyncManager
TimerTask --> SyncManager
ManualOp --> SyncManager
SyncManager --> CacheUpdate
SyncManager --> DBSync
CacheUpdate --> API
CacheUpdate --> Monitor
CacheUpdate --> Analytics
DBSync --> API
DBSync --> Monitor
DBSync --> Analytics
```

**图表来源**
- [GameCommonHolder.java](file://Game/src/main/java/com/bot/game/service/GameCommonHolder.java#L43-L83)

**节来源**
- [StatusMonitor.java](file://Base/src/main/java/com/bot/base/service/StatusMonitor.java#L116-L129)
- [GameCommonHolder.java](file://Game/src/main/java/com/bot/game/service/GameCommonHolder.java#L43-L83)

## 性能优化策略

### 响应时间优化

为了提升状态查询的响应速度，系统采用了以下优化策略：

1. **内存优先查询**：优先从内存缓存获取状态信息
2. **批量状态检查**：支持批量查询多个用户的在线状态
3. **异步状态更新**：状态变更采用异步更新机制

```mermaid
flowchart TD
Query[状态查询请求] --> MemCache{内存缓存命中?}
MemCache --> |是| FastReturn[快速返回结果]
MemCache --> |否| BatchCheck[批量数据库查询]
BatchCheck --> UpdateCache[更新缓存]
UpdateCache --> Return[返回结果]
FastReturn --> Return
Return --> Monitor[性能监控]
```

### 高并发处理

针对高并发场景，系统实现了以下优化措施：

| 优化技术 | 应用场景 | 性能提升 |
|----------|----------|----------|
| 连接池管理 | 数据库连接 | 减少连接开销 |
| 异步处理 | 状态更新 | 提升吞吐量 |
| 分布式锁 | 并发控制 | 保证数据一致性 |
| 限流机制 | 请求控制 | 防止系统过载 |

### 缓存命中率监控

系统内置了缓存命中率监控机制：

```mermaid
graph TB
subgraph "监控指标"
HitRate[缓存命中率]
ResponseTime[响应时间]
QPS[每秒查询数]
ErrorRate[错误率]
end
subgraph "监控工具"
Metrics[指标收集]
Dashboard[监控面板]
Alert[告警系统]
end
subgraph "优化策略"
CacheTuning[缓存调优]
LoadBalance[负载均衡]
CapacityPlanning[容量规划]
end
HitRate --> Metrics
ResponseTime --> Metrics
QPS --> Metrics
ErrorRate --> Metrics
Metrics --> Dashboard
Dashboard --> Alert
Dashboard --> CacheTuning
Dashboard --> LoadBalance
Dashboard --> CapacityPlanning
```

### 会话清理策略

系统实现了智能的会话清理策略：

1. **主动清理**：定期扫描并清理过期会话
2. **被动清理**：用户操作触发的会话重置
3. **紧急清理**：系统资源不足时的强制清理

**节来源**
- [StatusMonitor.java](file://Base/src/main/java/com/bot/base/service/StatusMonitor.java#L67-L91)

## 故障排除指南

### 常见问题诊断

| 问题类型 | 症状描述 | 可能原因 | 解决方案 |
|----------|----------|----------|----------|
| 状态查询超时 | 接口响应时间过长 | 缓存失效、数据库压力大 | 检查缓存配置、优化数据库查询 |
| 在线状态异常 | 用户实际在线但显示离线 | 会话超时、状态不同步 | 重启状态同步服务 |
| Token验证失败 | 合法用户无法登录 | Token过期、配置错误 | 更新Token配置 |

### 性能调优建议

1. **缓存优化**
   - 增加缓存容量
   - 优化缓存淘汰策略
   - 实现预热机制

2. **数据库优化**
   - 添加必要的索引
   - 优化查询语句
   - 实现读写分离

3. **系统配置**
   - 调整线程池大小
   - 优化JVM参数
   - 配置合适的超时时间

### 监控告警配置

建议配置以下监控指标：

- **缓存命中率**：目标≥95%
- **平均响应时间**：目标≤100ms
- **错误率**：目标≤0.1%
- **并发连接数**：动态调整

**节来源**
- [StatusMonitor.java](file://Base/src/main/java/com/bot/base/service/StatusMonitor.java#L67-L91)

## 总结

Bot项目的状态查询API系统通过精心设计的架构和优化策略，实现了高效、稳定的玩家在线状态查询服务。系统的核心优势包括：

1. **多层次状态验证**：从Token验证到会话超时检查的完整验证链
2. **智能缓存策略**：内存缓存与数据库缓存的有机结合
3. **高并发处理能力**：异步处理和连接池管理确保系统稳定性
4. **完善的监控体系**：实时监控和自动告警机制

该系统为Bot项目的持续发展提供了坚实的技术基础，能够满足大规模用户群体的状态查询需求。通过持续的优化和监控，系统能够保持高性能和高可用性，为用户提供优质的体验。