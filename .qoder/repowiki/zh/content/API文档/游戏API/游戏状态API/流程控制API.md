# 流程控制API

<cite>
**本文档引用的文件**
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java)
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java)
- [Collector.java](file://Game/src/main/java/com/bot/game/chain/Collector.java)
- [GameMainMenuPrinter.java](file://Game/src/main/java/com/bot/game/chain/menu/GameMainMenuPrinter.java)
- [Menu.java](file://Game/src/main/java/com/bot/game/chain/Menu.java)
- [GameHandler.java](file://Game/src/main/java/com/bot/game/service/GameHandler.java)
- [BattleServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/BattleServiceImpl.java)
- [GameConsts.java](file://Common/src/main/java/com/bot/common/constant/GameConsts.java)
- [BaseConsts.java](file://Common/src/main/java/com/bot/common/constant/BaseConsts.java)
</cite>

## 目录
1. [概述](#概述)
2. [架构设计](#架构设计)
3. [核心接口分析](#核心接口分析)
4. [Collector接口实现机制](#collector接口实现机制)
5. [状态流转控制逻辑](#状态流转控制逻辑)
6. [完整交互序列](#完整交互序列)
7. [异常处理与恢复机制](#异常处理与恢复机制)
8. [性能优化考虑](#性能优化考虑)
9. [总结](#总结)

## 概述

Bot项目的游戏流程控制API是一个基于责任链模式的状态管理系统，通过Collector接口及其具体实现类GameChainCollector，实现了玩家游戏状态的完整生命周期管理。该系统支持玩家从注册登录到游戏退出的全流程控制，包括菜单导航、状态验证、会话管理和异常恢复等功能。

## 架构设计

### 系统架构图

```mermaid
graph TB
subgraph "客户端层"
Client[客户端请求]
end
subgraph "服务层"
GameHandler[GameHandler接口]
GameHandlerImpl[GameHandlerServiceImpl]
end
subgraph "流程控制层"
Collector[Collector接口]
GameChainCollector[GameChainCollector实现]
end
subgraph "菜单管理层"
Menu[Menu抽象类]
MainMenu[GameMainMenuPrinter]
SubMenu[子菜单实现]
end
subgraph "业务服务层"
Player[Player接口]
BattleService[BattleServiceImpl]
OtherServices[其他业务服务]
end
Client --> GameHandler
GameHandler --> GameHandlerImpl
GameHandlerImpl --> Collector
Collector --> GameChainCollector
GameChainCollector --> Menu
Menu --> MainMenu
Menu --> SubMenu
SubMenu --> Player
Player --> BattleService
Player --> OtherServices
```

**图表来源**
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L27-L189)
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L24-L119)
- [Collector.java](file://Game/src/main/java/com/bot/game/chain/Collector.java#L8-L38)

### 核心组件关系图

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
+String exit(String token)
+String play(String reqContent, String token)
+String manage(String reqContent)
-GamePlayer getGamePlayer(String token, String nickName)
-boolean isExsitName(String nickName)
-Map getMapperMap()
}
class Collector {
<<interface>>
+String buildCollector(String token, Map mapperMap)
+String toNextOrPrevious(String token, String point)
+void removeToken(String token)
+boolean isOnLine(String token)
}
class GameChainCollector {
-Map~String,Menu[]~ userChainMap
+Map~String,String[]~ supportPoint
+String buildCollector(String token, Map mapperMap)
+String toNextOrPrevious(String token, String point)
+void removeToken(String token)
+boolean isOnLine(String token)
}
class Menu {
<<abstract>>
#Map~String,Menu~ menuChildrenMap
#Map~String,CommonPlayer~ playServiceMap
#String menuName
#String describe
+String print(String token)
+void initMenu()
+void getDescribe(String token)
+void reInitMenu(String token)
}
class GameMainMenuPrinter {
-String token
+GameMainMenuPrinter(Map mapperMap, String token)
+void initMenu()
+void appendTurnBack(StringBuilder stringBuilder)
+void getDescribe(String token)
}
GameHandler <|-- GameHandlerServiceImpl
GameHandlerServiceImpl --> Collector
Collector <|-- GameChainCollector
Menu <|-- GameMainMenuPrinter
GameChainCollector --> Menu
```

**图表来源**
- [GameHandler.java](file://Game/src/main/java/com/bot/game/service/GameHandler.java#L7-L28)
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L27-L189)
- [Collector.java](file://Game/src/main/java/com/bot/game/chain/Collector.java#L8-L38)
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L24-L119)
- [Menu.java](file://Game/src/main/java/com/bot/game/chain/Menu.java#L19-L76)

## 核心接口分析

### Collector接口设计

Collector接口定义了游戏流程控制的核心方法，包含了四个关键操作：

#### buildCollector方法
负责在玩家首次进入游戏时构建调用链，初始化菜单结构。

#### toNextOrPrevious方法  
实现在游戏过程中的菜单跳转功能，支持前进和后退操作。

#### removeToken方法  
在玩家退出游戏时清理会话信息，释放资源。

#### isOnLine方法  
用于状态校验，判断玩家是否处于在线状态。

**章节来源**
- [Collector.java](file://Game/src/main/java/com/bot/game/chain/Collector.java#L8-L38)

### GameHandler接口职责

GameHandler接口作为服务层入口，定义了游戏的主要业务方法：

- **exit方法**：处理玩家退出请求
- **play方法**：处理游戏主流程逻辑
- **manage方法**：处理管理员操作

**章节来源**
- [GameHandler.java](file://Game/src/main/java/com/bot/game/service/GameHandler.java#L7-L28)

## Collector接口实现机制

### buildCollector方法实现机制

buildCollector方法是流程控制的核心入口，其实现机制如下：

```mermaid
flowchart TD
Start([开始构建调用链]) --> ValidateToken{验证Token}
ValidateToken --> |为空| ReturnNull[返回null]
ValidateToken --> |有效| InitChain[初始化菜单链表]
InitChain --> CreateMenu[创建主菜单实例]
CreateMenu --> SetMapper[设置Mapper映射]
SetMapper --> AddToChain[添加到用户链表]
AddToChain --> PrintMenu[打印菜单]
PrintMenu --> End([返回菜单内容])
ReturnNull --> End
```

**图表来源**
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L34-L44)

#### 实现特点

1. **Token验证**：确保传入的用户标识有效
2. **链表初始化**：创建新的菜单链表结构
3. **主菜单创建**：基于GameMainMenuPrinter构建初始菜单
4. **Mapper注入**：将数据库访问层注入到菜单中
5. **状态存储**：将构建的链表存储到全局映射中

**章节来源**
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L34-L44)

### toNextOrPrevious方法实现机制

toNextOrPrevious方法实现了复杂的菜单导航逻辑：

```mermaid
flowchart TD
Start([开始菜单导航]) --> GetChain[获取用户菜单链表]
GetChain --> ChainExists{链表是否存在}
ChainExists --> |否| ReturnNull[返回null]
ChainExists --> |是| CheckSupport[检查支持的指令]
CheckSupport --> HasSupport{是否有支持指令}
HasSupport --> |是| ValidatePoint[验证指令有效性]
HasSupport --> |否| CheckZero{是否为返回指令}
ValidatePoint --> PointValid{指令是否有效}
PointValid --> |否| ReturnError[返回错误信息]
PointValid --> |是| NavigateForward[向前导航]
CheckZero --> |是| NavigateBackward[向后导航]
CheckZero --> |否| CheckDoubleZero{是否为主菜单返回}
CheckDoubleZero --> |是| ResetChain[重置为主菜单]
CheckDoubleZero --> |否| TryService[尝试调用服务]
TryService --> ServiceFound{找到对应服务}
ServiceFound --> |是| ExecuteService[执行服务逻辑]
ServiceFound --> |否| TryFriend[尝试添加好友]
TryFriend --> IsWaitingFriend{正在等待添加好友}
IsWaitingFriend --> |是| AddFriend[执行添加好友]
IsWaitingFriend --> |否| TryMessage[尝试发送消息]
TryMessage --> IsWritingMessage{正在写消息}
IsWritingMessage --> |是| SendMessage[执行发送消息]
IsWritingMessage --> |否| CheckBattleDetail[检查战斗详情]
CheckBattleDetail --> HasBattleDetail{有战斗详情}
HasBattleDetail --> |是| ReturnBattleDetail[返回战斗详情]
HasBattleDetail --> |否| CheckVersionHistory[检查版本历史]
CheckVersionHistory --> HasHistory{有历史记录}
HasHistory --> |是| ReturnHistory[返回历史记录]
HasHistory --> |否| ReturnUnknown[返回未知指令]
NavigateForward --> AddToChain[添加到链表]
AddToChain --> PrintMenu[打印目标菜单]
NavigateBackward --> RemoveLast[移除最后一个菜单]
RemoveLast --> PrintPrevious[打印前一个菜单]
ResetChain --> ClearChain[清空链表]
ClearChain --> AddMain[添加主菜单]
AddMain --> PrintMain[打印主菜单]
ExecuteService --> PrintResult[打印执行结果]
AddFriend --> PrintResult
SendMessage --> PrintResult
ReturnBattleDetail --> PrintResult
ReturnHistory --> PrintResult
ReturnUnknown --> PrintResult
ReturnError --> PrintResult
ReturnNull --> End([结束])
PrintMenu --> End
PrintPrevious --> End
PrintMain --> End
PrintResult --> End
```

**图表来源**
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L47-L107)

#### 导航策略

1. **支持指令验证**：检查当前输入是否在允许的指令范围内
2. **菜单层级导航**：支持多层级菜单的前进和后退
3. **服务集成**：支持直接调用业务服务
4. **特殊指令处理**：处理返回、主菜单返回等特殊指令
5. **动态服务调用**：支持运行时动态绑定业务服务

**章节来源**
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L47-L107)

### removeToken方法实现机制

removeToken方法负责清理玩家的会话信息：

```mermaid
sequenceDiagram
participant Client as 客户端
participant Handler as GameHandler
participant Collector as GameChainCollector
participant Storage as 用户链表存储
Client->>Handler : exit(token)
Handler->>Collector : removeToken(token)
Collector->>Storage : userChainMap.remove(token)
Storage-->>Collector : 移除成功
Collector-->>Handler : void返回
Handler-->>Client : "退出成功"
```

**图表来源**
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L109-L112)
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L82-L84)

**章节来源**
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L109-L112)

### isOnLine方法实现机制

isOnLine方法用于状态校验：

```mermaid
flowchart TD
Start([检查玩家在线状态]) --> CheckToken{Token是否存在}
CheckToken --> |存在| ReturnTrue[返回true]
CheckToken --> |不存在| ReturnFalse[返回false]
ReturnTrue --> End([结束])
ReturnFalse --> End
```

**图表来源**
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L114-L117)

**章节来源**
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L114-L117)

## 状态流转控制逻辑

### 游戏状态机转换图

```mermaid
stateDiagram-v2
[*] --> 待注册
待注册 --> 注册中 : 输入昵称
注册中 --> 已注册 : 注册成功
已注册 --> 待登录 : 发送登录请求
待登录 --> 登录中 : 验证凭据
登录中 --> 已登录 : 登录成功
已登录 --> 游戏主界面 : 开始游戏
游戏主界面 --> 子菜单 : 选择功能
子菜单 --> 游戏主界面 : 返回
子菜单 --> 游戏主界面 : 主菜单返回
游戏主界面 --> 战斗场景 : 进入战斗
战斗场景 --> 游戏主界面 : 战斗结束
游戏主界面 --> [*] : 退出游戏
已登录 --> [*] : 异常断开
已注册 --> [*] : 异常断开
待登录 --> [*] : 异常断开
待注册 --> [*] : 异常断开
```

### GameHandlerServiceImpl中的状态控制

GameHandlerServiceImpl通过play方法实现了复杂的状态控制逻辑：

```mermaid
sequenceDiagram
participant Client as 客户端
participant Handler as GameHandlerServiceImpl
participant Collector as GameChainCollector
participant DB as 数据库
Client->>Handler : play(reqContent, token)
Handler->>DB : 查询游戏状态
DB-->>Handler : 游戏状态信息
Handler->>Handler : 检查游戏是否维护
alt 游戏维护中
Handler-->>Client : "游戏维护中"
else 游戏正常
Handler->>Collector : isOnLine(token)
Collector-->>Handler : 在线状态
alt 玩家已在线
Handler->>Collector : toNextOrPrevious(token, reqContent)
Collector-->>Handler : 菜单响应
Handler-->>Client : 菜单内容
else 玩家待登录
Handler->>Handler : 处理登录逻辑
Handler-->>Client : 登录提示
else 玩家待注册
Handler->>Handler : 处理注册逻辑
Handler-->>Client : 注册提示
else 新用户
Handler->>Handler : 添加到待注册列表
Handler-->>Client : 注册提示
end
end
```

**图表来源**
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L88-L131)

**章节来源**
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L88-L131)

## 完整交互序列

### 玩家首次进入游戏流程

```mermaid
sequenceDiagram
participant User as 玩家
participant Handler as GameHandler
participant Collector as GameChainCollector
participant Menu as GameMainMenuPrinter
participant DB as 数据库
User->>Handler : 发送消息
Handler->>DB : 查询玩家信息
DB-->>Handler : 玩家不存在
Handler->>Handler : 添加到WAIT_REG
Handler-->>User : "请输入昵称完成注册"
User->>Handler : 输入昵称
Handler->>DB : 检查昵称是否存在
DB-->>Handler : 昵称可用
Handler->>DB : 创建玩家记录
DB-->>Handler : 创建成功
Handler->>DB : 插入初始道具
DB-->>Handler : 插入成功
Handler->>Collector : buildCollector(token, mapperMap)
Collector->>Menu : 创建主菜单
Menu-->>Collector : 主菜单实例
Collector-->>Handler : 菜单内容
Handler-->>User : "欢迎回来,[昵称]"
```

**图表来源**
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L124-L121)
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L34-L44)

### 游戏过程中的菜单导航

```mermaid
sequenceDiagram
participant User as 玩家
participant Handler as GameHandler
participant Collector as GameChainCollector
participant Menu as 当前菜单
participant Service as 业务服务
User->>Handler : 发送指令(如"1")
Handler->>Collector : toNextOrPrevious(token, "1")
Collector->>Collector : 获取当前菜单链表
Collector->>Menu : 查找子菜单
Menu-->>Collector : 找到子菜单
Collector->>Collector : 添加子菜单到链表
Collector->>Menu : 打印子菜单
Menu-->>Collector : 菜单内容
Collector-->>Handler : 菜单响应
Handler-->>User : 子菜单内容
User->>Handler : 发送指令("0")
Handler->>Collector : toNextOrPrevious(token, "0")
Collector->>Collector : 移除最后一个菜单
Collector->>Menu : 打印前一个菜单
Menu-->>Collector : 上级菜单内容
Collector-->>Handler : 菜单响应
Handler-->>User : 上级菜单内容
```

**图表来源**
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L47-L107)

### 玩家退出游戏流程

```mermaid
sequenceDiagram
participant User as 玩家
participant Handler as GameHandler
participant Collector as GameChainCollector
participant Storage as 会话存储
User->>Handler : 发送"退出"
Handler->>Collector : removeToken(token)
Collector->>Storage : 移除用户会话
Storage-->>Collector : 移除成功
Collector-->>Handler : void
Handler-->>User : "退出成功"
```

**图表来源**
- [GameHandlerServiceImpl.java](file://Game/src/main/java/com/bot/game/service/impl/GameHandlerServiceImpl.java#L82-L84)
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L109-L112)

## 异常处理与恢复机制

### 异常中断处理

系统提供了多层次的异常处理机制：

#### Token验证异常
```mermaid
flowchart TD
Start([接收请求]) --> ValidateToken{Token验证}
ValidateToken --> |无效| LogError[记录错误日志]
ValidateToken --> |有效| ProcessRequest[处理请求]
LogError --> ReturnError[返回错误信息]
ProcessRequest --> Success[处理成功]
Success --> End([结束])
ReturnError --> End
```

#### 状态校验异常
```mermaid
flowchart TD
Start([状态检查]) --> CheckOnline{玩家在线}
CheckOnline --> |离线| ReturnNull[返回null]
CheckOnline --> |在线| CheckChain{链表存在}
CheckChain --> |不存在| ReturnNull
CheckChain --> |存在| ProcessRequest[处理请求]
ReturnNull --> End([结束])
ProcessRequest --> End
```

**章节来源**
- [GameChainCollector.java](file://Game/src/main/java/com/bot/game/chain/GameChainCollector.java#L47-L107)

### 会话恢复机制

#### 自动恢复策略
1. **链表重建**：当链表丢失时，尝试重建菜单结构
2. **状态同步**：通过数据库同步玩家状态
3. **缓存清理**：定期清理无效的会话缓存

#### 异常恢复流程
```mermaid
flowchart TD
DetectError[检测到异常] --> CheckType{异常类型}
CheckType --> |Token失效| RebuildChain[重建调用链]
CheckType --> |状态不一致| SyncState[同步状态]
CheckType --> |网络中断| RetryConnection[重试连接]
RebuildChain --> NotifyUser[通知用户重新开始]
SyncState --> RestoreSession[恢复会话]
RetryConnection --> ResumeOperation[恢复操作]
NotifyUser --> End([结束])
RestoreSession --> End
ResumeOperation --> End
```

### 错误响应机制

系统提供了统一的错误响应格式：

| 错误类型 | 响应内容 | 处理方式 |
|---------|---------|---------|
| 未知指令 | "未知指令，回复【0】返回上级菜单，【00】返回主菜单" | 提供导航选项 |
| 指令错误 | "指令错误，请输入正确的指令" | 保持当前状态 |
| 游戏维护 | "当前游戏维护中，请稍后再来" | 提示用户等待 |
| 网络异常 | "网络连接异常，请重试" | 建议重新连接 |

**章节来源**
- [GameConsts.java](file://Common/src/main/java/com/bot/common/constant/GameConsts.java#L12-L48)

## 性能优化考虑

### 内存管理优化

1. **链表复用**：通过静态Map复用菜单链表对象
2. **及时清理**：removeToken方法确保会话结束后立即清理内存
3. **弱引用**：对于长时间不活跃的会话使用弱引用

### 并发控制优化

```mermaid
graph TD
Request[并发请求] --> Lock[互斥锁保护]
Lock --> ThreadSafe[线程安全操作]
ThreadSafe --> Release[释放锁]
Release --> Response[返回响应]
subgraph "优化策略"
AtomicOps[原子操作]
CopyOnWrite[写时复制]
ReadWriteLock[读写锁]
end
```

### 缓存策略

1. **菜单缓存**：缓存常用的菜单结构
2. **状态缓存**：缓存玩家状态信息
3. **Mapper缓存**：缓存数据库访问层对象

## 总结

Bot项目的流程控制API通过Collector接口和GameChainCollector实现类，构建了一个完整的游戏状态管理系统。该系统具有以下特点：

### 核心优势

1. **清晰的职责分离**：GameHandler负责业务逻辑，Collector负责流程控制
2. **灵活的状态管理**：支持多种游戏状态和状态间的平滑转换
3. **完善的异常处理**：多层次的异常捕获和恢复机制
4. **良好的扩展性**：基于接口的设计便于功能扩展

### 技术亮点

1. **责任链模式**：实现菜单导航的灵活控制
2. **状态机设计**：清晰表达游戏状态流转
3. **服务集成**：无缝集成业务服务
4. **会话管理**：完善的会话生命周期管理

### 应用价值

该流程控制API不仅适用于当前的游戏场景，其设计理念和实现方式可以推广到其他需要复杂状态管理的应用系统中，为开发者提供了一个优秀的参考实现。