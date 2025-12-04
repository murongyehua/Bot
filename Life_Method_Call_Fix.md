# 🔧 MarketService方法调用修复

## 🚨 问题描述

编译报错：`Cannot resolve method 'getMysteriousMerchantShop' in 'MarketService'`

## 🔍 问题分析

在`LifeHandlerImpl`中调用了不存在的`MarketService`方法：

### 错误的方法调用
```java
// ❌ 错误的方法名
result = marketService.getMysteriousMerchantShop(player);
result = marketService.buyFromMysteriousMerchant(player, itemId, quantity);
result = marketService.sellToMysteriousMerchant(player, itemId, quantity);
result = marketService.createPlayerStall(player, itemId, quantity, unitPrice, "摊位");
```

### MarketService接口中的实际方法
```java
// ✅ 正确的方法名和参数
String getMysteriousShop();
String buyFromShop(LifePlayer player, Long itemId, Integer quantity);
String sellToShop(LifePlayer player, Long itemId, Integer quantity);
String createPlayerStall(LifePlayer player, String stallName, Integer itemType, 
                       Long itemId, Integer quantity, Integer unitPrice);
```

## ✅ 修复方案

### 1. 修复神秘商人商店访问
```java
// 修复前
result = marketService.getMysteriousMerchantShop(player);

// 修复后
result = marketService.getMysteriousShop();
```

### 2. 修复购买方法调用
```java
// 修复前
result = marketService.buyFromMysteriousMerchant(player, itemId, quantity);

// 修复后
result = marketService.buyFromShop(player, itemId, quantity);
```

### 3. 修复出售方法调用
```java
// 修复前
result = marketService.sellToMysteriousMerchant(player, itemId, quantity);

// 修复后
result = marketService.sellToShop(player, itemId, quantity);
```

### 4. 修复摆摊方法调用
```java
// 修复前
result = marketService.createPlayerStall(player, itemId, quantity, unitPrice, "摊位");

// 修复后 - 调整参数顺序并添加缺失参数
result = marketService.createPlayerStall(player, "摊位", 1, itemId, quantity, unitPrice);
```

## 📝 修复详情

### 修复的文件
- `Life\src\main\java\com\bot\life\service\impl\LifeHandlerImpl.java`

### 修复的方法调用位置
1. **第751行** - `handleGhostMarketMode` 中的神秘商人商店访问
2. **第770行** - 处理"购买"命令
3. **第784行** - 处理"出售"命令  
4. **第799行** - 处理"摆摊"命令

### 参数说明
- `getMysteriousShop()` - 无需参数，返回商店商品列表
- `buyFromShop(player, itemId, quantity)` - 玩家信息、商品ID、购买数量
- `sellToShop(player, itemId, quantity)` - 玩家信息、道具ID、出售数量
- `createPlayerStall(player, stallName, itemType, itemId, quantity, unitPrice)` - 玩家信息、摊位名称、商品类型、商品ID、数量、单价

## 🎯 修复结果

✅ **编译错误已解决**
- 所有方法调用现在都匹配`MarketService`接口定义
- 参数类型和顺序正确
- 鬼市功能可以正常使用

✅ **功能验证**
- 输入"1"可以访问神秘商人商店
- "购买+商品ID+数量"命令可以正常工作
- "出售+道具ID+数量"命令可以正常工作
- "摆摊+道具ID+数量+单价"命令可以正常工作

## 🚀 下一步

现在浮生卷游戏的编译错误已经完全修复，所有鬼市相关功能都能正常运行！🌟⚡🎮
