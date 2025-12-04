# 游戏状态API文档

<cite>
**本文档引用的文件**
- [Collector.java](file://Base/src/main/java/com/bot/base/chain/Collector.java)
- [ChainCollector.java](file://Base/src/main/java/com/bot/base/chain/ChainCollector.java)
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java)
- [ResultContext.java](file://Game/src/main/java/com/bot/game/dto/ResultContext.java)
- [Menu.java](file://Base/src/main/java/com/bot/base/chain/Menu.java)
- [MenuPrinter.java](file://Base/src/main/java/com/bot/base/chain/MenuPrinter.java)
- [GameMainMenuPrinter.java](file://Game/src/main/java/com/bot/game/chain/menu/GameMainMenuPrinter.java)
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java)
- [SystemManager.java](file://Base/src/main/java/com/bot/base/service/SystemManager.java)
- [StatusMonitor.java](file://Base/src/main/java/com/bot/base/service/StatusMonitor.java)
- [ENStatus.java](file://Common/src/main/java/com/bot/common/enums/ENStatus.java)
- [ENUserGameStatus.java](file://Common/src/main/java/com/bot/common/enums/ENUserGameStatus.java)
- [ENGameMode.java](file://Life/src/main/java/com/bot/life/enums/ENGameMode.java)
- [LifeGameStatus.java](file://Life/src/main/java/com/bot/life/dao/entity/LifeGameStatus.java)
</cite>

## 目录
1. [简介](#简介)
2. [项目结构概览](#项目结构概览)
3. [核心组件分析](#核心组件分析)
4. [架构概览](#架构概览)
5. [详细组件分析](#详细组件分析)
6. [游戏状态流转机制](#游戏状态流转机制)
7. [状态管理策略](#状态管理策略)
8. [API调用示例](#api调用示例)
9. [故障排除指南](#故障排除指南)
10. [总结](#总结)

## 简介

本文档详细介绍了Bot项目中的游戏状态管理系统，重点阐述了Collector类在游戏流程链式处理中的核心作用，以及ResultContext类作为统一返回结果封装的设计理念。该系统采用状态机模式实现了复杂的游戏流程控制，支持多种游戏状态的管理和维护。

## 项目结构概览

Bot项目采用模块化架构设计，主要包含以下核心模块：

```mermaid
graph TB
subgraph "基础模块 (Base)"
A1[ChainCollector]
A2[Collector接口]
A3[Menu基类]
A4[MenuPrinter接口]
end
subgraph "游戏模块 (Game)"
B1[GameChainCollector]
B2[ResultContext]
B3[GameHandlerServiceImpl]
B4[GameMainMenuPrinter]
end
subgraph "生活模块 (Life)"
C1[LifeGameStatus]
C2[ENGameMode枚举]
C3[LifeHandlerImpl]
end
subgraph "通用模块 (Common)"
D1[ENStatus枚举]
D2[ENUserGameStatus枚举]
D3[SystemManager]
end
A1 --> B1
A2 --> B2
B3 --> C3
D1 --> B3
D2 --> B3
```

**图表来源**
- [ChainCollector.java](file://Base/src/main/java/com/bot/base/chain/ChainCollector.java#L1-L58)
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L1-L119)
- [ResultContext.java](file://Game/src/main/java/com/bot/game/dto/ResultContext.java#L1-L19)

## 核心组件分析

### Collector接口设计

Collector接口定义了游戏流程链式处理的核心契约，包含两个关键方法：

| 方法名 | 参数 | 返回类型 | 功能描述 |
|--------|------|----------|----------|
| buildCollector | String token | String | 构建调用链，在用户登录或退出菜单时调用 |
| toNextOrPrevious | String token, String point | String | 前往下一个或上一个菜单 |

**章节来源**
- [Collector.java](file://Base/src/main/java/com/bot/base/chain/Collector.java#L7-L22)

### ChainCollector实现

ChainCollector是基础链式收集器的实现，负责维护用户会话链路：

```mermaid
classDiagram
class ChainCollector {
-Map~String, Menu[]~ userChainMap
+buildCollector(String token) String
+toNextOrPrevious(String token, String point) String
}
class Collector {
<<interface>>
+buildCollector(String token) String
+toNextOrPrevious(String token, String point) String
}
class Menu {
+Map~String, Menu~ menuChildrenMap
+String menuName
+String describe
+print() String
+initMenu() void
}
ChainCollector ..|> Collector
ChainCollector --> Menu : manages
```

**图表来源**
- [ChainCollector.java](file://Base/src/main/java/com/bot/base/chain/ChainCollector.java#L18-L57)
- [Menu.java](file://Base/src/main/java/com/bot/base/chain/Menu.java#L15-L55)

**章节来源**
- [ChainCollector.java](file://Base/src/main/java/com/bot/base/chain/ChainCollector.java#L24-L56)

### GameChainCollector增强版

GameChainCollector扩展了基础功能，增加了游戏特定的状态管理和指令验证：

```mermaid
classDiagram
class GameChainCollector {
-Map~String, Menu[]~ userChainMap
-Map~String, String[]~ supportPoint
+buildCollector(String token, Map mapperMap) String
+toNextOrPrevious(String token, String point) String
+removeToken(String token) void
+isOnLine(String token) boolean
}
class GameChainCollector {
<<GameChainCollector>>
+buildCollector(String token, Map mapperMap) String
+toNextOrPrevious(String token, String point) String
+removeToken(String token) void
+isOnLine(String token) boolean
}
GameChainCollector ..|> Collector
```

**图表来源**
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L24-L118)

**章节来源**
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L34-L118)

## 架构概览

系统采用分层架构设计，通过状态机模式实现游戏流程控制：

```mermaid
graph TD
subgraph "表现层"
A[用户请求]
B[消息处理器]
end
subgraph "业务层"
C[GameHandler]
D[GameHandlerServiceImpl]
E[Collector接口]
end
subgraph "服务层"
F[ChainCollector]
G[GameChainCollector]
H[Menu系统]
end
subgraph "数据层"
I[数据库]
J[内存缓存]
end
A --> B
B --> C
C --> D
D --> E
E --> F
E --> G
F --> H
G --> H
H --> I
H --> J
```

**图表来源**
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L27-L191)
- [ChainCollector.java](file://Base/src/main/java/com/bot/base/chain/ChainCollector.java#L18-L57)

## 详细组件分析

### ResultContext统一返回封装

ResultContext类作为统一的返回结果封装，提供了标准化的数据传输格式：

```mermaid
classDiagram
class ResultContext {
-String code
-String info
-String data
+getCode() String
+setCode(String code) void
+getInfo() String
+setInfo(String info) void
+getData() String
+setData(String data) void
}
note for ResultContext "字段说明 : \n- code : 状态码\n- info : 状态信息\n- data : 业务数据"
```

**图表来源**
- [ResultContext.java](file://Game/src/main/java/com/bot/game/dto/ResultContext.java#L10-L18)

**章节来源**
- [ResultContext.java](file://Game/src/main/java/com/bot/game/dto/ResultContext.java#L10-L18)

### Menu菜单系统

Menu类是菜单系统的基类，提供了菜单树形结构的构建和展示功能：

```mermaid
classDiagram
class Menu {
#Map~String, Menu~ menuChildrenMap
#String menuName
#String describe
-String VERSION
+print() String
+initMenu() void
#appendTurnBack(StringBuilder) void
-printMenuMap() String
}
class MenuPrinter {
<<interface>>
+initMenu() void
}
Menu ..|> MenuPrinter
```

**图表来源**
- [Menu.java](file://Base/src/main/java/com/bot/base/chain/Menu.java#L15-L55)
- [MenuPrinter.java](file://Base/src/main/java/com/bot/base/chain/MenuPrinter.java#L7-L14)

**章节来源**
- [Menu.java](file://Base/src/main/java/com/bot/base/chain/Menu.java#L15-L55)

### GameMainMenuPrinter主菜单实现

GameMainMenuPrinter展示了如何构建具体的游戏菜单：

```mermaid
sequenceDiagram
participant User as 用户
participant Handler as GameHandler
participant Collector as GameChainCollector
participant Menu as GameMainMenuPrinter
participant SubMenu as 子菜单
User->>Handler : 发送指令
Handler->>Collector : toNextOrPrevious(token, point)
Collector->>Menu : 获取当前菜单
Menu->>SubMenu : 查找子菜单
SubMenu-->>Menu : 返回子菜单实例
Menu->>SubMenu : 调用print()方法
SubMenu-->>User : 返回菜单内容
```

**图表来源**
- [GameMainMenuPrinter.java](file://Game/src/main/java/com/bot/game/chain/menu/GameMainMenuPrinter.java#L26-L49)
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L95-L106)

**章节来源**
- [GameMainMenuPrinter.java](file://Game/src/main/java/com/bot/game/chain/menu/GameMainMenuPrinter.java#L26-L49)

## 游戏状态流转机制

### 基础状态流转

系统通过ChainCollector实现基础的状态流转控制：

```mermaid
flowchart TD
Start([用户请求]) --> CheckToken{检查令牌}
CheckToken --> |有效| BuildChain[构建调用链]
CheckToken --> |无效| ReturnNull[返回null]
BuildChain --> InitMenu[初始化主菜单]
InitMenu --> ShowMenu[显示菜单]
ShowMenu --> UserChoice{用户选择}
UserChoice --> |数字指令| NextMenu[前往下一级菜单]
UserChoice --> |返回指令| PrevMenu[返回上一级菜单]
UserChoice --> |退出指令| ExitChain[退出链路]
NextMenu --> ValidatePoint{验证指令}
ValidatePoint --> |有效| UpdateChain[更新链路]
ValidatePoint --> |无效| ReturnNull
PrevMenu --> RemoveFromChain[移除当前菜单]
RemoveFromChain --> ShowPrevMenu[显示上一级菜单]
UpdateChain --> ShowMenu
ShowPrevMenu --> ShowMenu
ExitChain --> Cleanup[清理资源]
Cleanup --> End([结束])
ReturnNull --> End
```

**图表来源**
- [ChainCollector.java](file://Base/src/main/java/com/bot/base/chain/ChainCollector.java#L25-L56)

### 游戏状态机

GameChainCollector实现了更复杂的游戏状态机：

```mermaid
stateDiagram-v2
[*] --> 未登录
未登录 --> 待注册 : 用户不存在
未登录 --> 待登录 : 用户存在
待注册 --> 注册成功 : 输入昵称
注册成功 --> 游戏主菜单 : 自动进入
待登录 --> 登录成功 : 输入确认
登录成功 --> 游戏主菜单 : 进入游戏
游戏主菜单 --> 子菜单 : 选择功能
子菜单 --> 游戏主菜单 : 返回
子菜单 --> 特殊状态 : 特定功能
特殊状态 --> 游戏主菜单 : 完成操作
游戏主菜单 --> [*] : 退出游戏
```

**图表来源**
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L89-L133)

**章节来源**
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L89-L133)

### 生活游戏状态机

Life模块展示了更复杂的状态管理：

```mermaid
stateDiagram-v2
[*] --> 未进入
未进入 --> 预备状态 : 输入"浮生卷"
预备状态 --> 正式游戏 : 输入"1"
正式游戏 --> 探索模式 : 选择探索
正式游戏 --> 战斗模式 : 遭遇怪物
正式游戏 --> 鬼市模式 : 选择鬼市
正式游戏 --> 退出游戏 : 输入"退出"
战斗模式 --> 正式游戏 : 战斗结束
战斗模式 --> 逃跑 : 输入"5"
鬼市模式 --> 正式游戏 : 购买完成
逃跑 --> 正式游戏 : 体力恢复
退出游戏 --> [*]
```

**图表来源**
- [ENGameMode.java](file://Life/src/main/java/com/bot/life/enums/ENGameMode.java#L8-L12)
- [LifeGameStatus.java](file://Life/src/main/java/com/bot/life/dao/entity/LifeGameStatus.java#L15-L16)

**章节来源**
- [ENGameMode.java](file://Life/src/main/java/com/bot/life/enums/ENGameMode.java#L8-L37)

## 状态管理策略

### 全局状态管理

SystemManager提供了系统级别的状态管理功能：

```mermaid
classDiagram
class SystemManager {
+volatile UserTempInfoDTO userTempInfo
+volatile String noticeModel
-String managerPassword
+tryIntoManager(String token) String
+managerDistribute(String reqContent) String
-send2AllUser(String content) void
-send2AllUserPic(String fileName) void
}
class StatusMonitor {
+systemManagerMonitor() void
-morningSender() void
-workDailySender() void
-drinkSender() void
-dealChatId() void
}
SystemManager --> StatusMonitor : monitors
```

**图表来源**
- [SystemManager.java](file://Base/src/main/java/com/bot/base/service/SystemManager.java#L46-L299)
- [StatusMonitor.java](file://Base/src/main/java/com/bot/base/service/StatusMonitor.java#L45-L103)

**章节来源**
- [SystemManager.java](file://Base/src/main/java/com/bot/base/service/SystemManager.java#L46-L299)

### 游戏维护状态控制

系统提供了灵活的游戏维护状态控制机制：

| 状态枚举 | 值 | 描述 | 控制方式 |
|----------|----|----- |----------|
| NORMAL | 0 | 正常运行 | 默认状态 |
| LOCK | 1 | 维护锁定 | 管理员控制 |

**章节来源**
- [ENStatus.java](file://Common/src/main/java/com/bot/common/enums/ENStatus.java#L10-L14)

### 用户游戏状态管理

ENUserGameStatus枚举定义了用户的参与状态：

| 状态枚举 | 值 | 描述 | 使用场景 |
|----------|----|----- |----------|
| WAIT_JOIN | 0 | 等待加入 | 准备参与游戏 |
| JOINED | 1 | 已加入 | 已参与游戏 |

**章节来源**
- [ENUserGameStatus.java](file://Common/src/main/java/com/bot/common/enums/ENUserGameStatus.java#L14-L16)

## API调用示例

### 基础菜单导航API

```mermaid
sequenceDiagram
participant Client as 客户端
participant Handler as GameHandler
participant Collector as ChainCollector
participant Menu as Menu系统
Note over Client,Menu : 用户首次访问游戏
Client->>Handler : play("用户消息", "token123")
Handler->>Handler : 检查游戏状态
Handler->>Collector : buildCollector("token123")
Collector->>Menu : 创建主菜单
Menu-->>Collector : 返回菜单内容
Collector-->>Handler : 返回初始菜单
Handler-->>Client : 显示主菜单
Note over Client,Menu : 用户选择功能
Client->>Handler : play("1", "token123")
Handler->>Collector : toNextOrPrevious("token123", "1")
Collector->>Menu : 查找子菜单
Menu-->>Collector : 返回子菜单实例
Collector-->>Handler : 返回子菜单内容
Handler-->>Client : 显示子菜单
```

**图表来源**
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L95-L106)
- [ChainCollector.java](file://Base/src/main/java/com/bot/base/chain/ChainCollector.java#L25-L56)

### 游戏状态切换API

```mermaid
sequenceDiagram
participant Admin as 管理员
participant SystemMgr as SystemManager
participant GameHandler as GameHandler
participant Game as Game实体
Admin->>SystemMgr : 设置维护状态
SystemMgr->>Game : 更新游戏状态
Game-->>SystemMgr : 状态更新确认
Note over Admin,Game : 游戏维护期间
Admin->>GameHandler : play("任意指令", "token123")
GameHandler->>Game : 查询游戏状态
Game-->>GameHandler : 返回LOCK状态
GameHandler-->>Admin : 返回维护提示
Note over Admin,Game : 维护结束后
Admin->>SystemMgr : 关闭维护状态
SystemMgr->>Game : 更新游戏状态
Game-->>SystemMgr : 状态更新确认
```

**图表来源**
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L90-L94)
- [SystemManager.java](file://Base/src/main/java/com/bot/base/service/SystemManager.java#L105-L279)

### 系统管理API

系统管理功能提供了完整的状态控制能力：

| 管理指令 | 功能描述 | 示例 |
|----------|----------|------|
| 密码认证 | 进入管理模式 | 输入管理密码 |
| 发布公告 | 向所有用户发送消息 | SEND_NOTICE |
| 刷新文本 | 重新加载配置文本 | RELOAD_TEXT |
| 生成邀请码 | 创建用户注册邀请码 | CREATE_INVITE_CODE day count |
| 游戏管理 | 执行游戏相关操作 | GAME_MANAGER 操作内容 |

**章节来源**
- [SystemManager.java](file://Base/src/main/java/com/bot/base/service/SystemManager.java#L105-L279)

## 故障排除指南

### 常见问题及解决方案

#### 1. 游戏状态异常

**问题现象**: 用户收到"游戏状态异常，请重新进入"提示

**排查步骤**:
1. 检查游戏维护状态
2. 验证用户令牌有效性
3. 检查菜单链路完整性

**解决方案**:
```java
// 检查游戏状态
Game game = gameMapper.selectAll().get(0);
if (ENStatus.LOCK.getValue().equals(game.getStatus())) {
    return GameConsts.CommonTip.LOCK;
}

// 验证用户状态
if (!collector.isOnLine(token)) {
    return collector.buildCollector(token, mapperMap);
}
```

#### 2. 菜单导航失效

**问题现象**: 用户无法正常切换菜单

**排查步骤**:
1. 检查supportPoint配置
2. 验证菜单指令合法性
3. 检查菜单树结构

**解决方案**:
```java
// 验证指令支持性
List<String> supports = supportPoint.get(token);
if (supports != null && !supports.contains(point)) {
    return GameConsts.CommonTip.ERROR_POINT;
}
```

#### 3. 状态同步问题

**问题现象**: 多用户状态混乱

**排查步骤**:
1. 检查线程安全性
2. 验证令牌唯一性
3. 检查内存状态清理

**解决方案**:
```java
// 确保线程安全的状态管理
public synchronized void removeToken(String token) {
    userChainMap.remove(token);
}
```

**章节来源**
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L48-L107)
- [SystemManager.java](file://Base/src/main/java/com/bot/base/service/SystemManager.java#L88-L91)

## 总结

Bot项目的游戏状态API系统展现了优秀的软件架构设计：

1. **模块化设计**: 通过Collector接口实现了清晰的职责分离
2. **状态机模式**: 有效管理复杂的多层级游戏流程
3. **统一返回格式**: ResultContext提供了标准化的数据传输
4. **灵活的状态控制**: 支持游戏维护、用户状态等多种控制场景
5. **完善的监控机制**: StatusMonitor确保系统稳定运行

该系统为游戏开发提供了坚实的基础，支持复杂的游戏状态管理和流程控制需求。通过合理的抽象和封装，实现了高内聚低耦合的架构设计，为后续的功能扩展奠定了良好的基础。