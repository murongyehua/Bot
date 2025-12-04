package com.bot.life.service.impl;

import com.bot.life.dao.entity.*;
import com.bot.life.dao.mapper.*;
import com.bot.life.service.InventoryService;
import com.bot.life.service.MarketService;
import com.bot.life.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 鬼市服务实现
 * @author Assistant
 */
@Service
public class MarketServiceImpl implements MarketService {
    
    @Autowired
    private LifeShopMapper shopMapper;
    
    @Autowired
    private LifePlayerStallMapper playerStallMapper;
    
    @Autowired
    private LifeItemMapper itemMapper;
    
    @Autowired
    private LifePlayerItemMapper playerItemMapper;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private InventoryService inventoryService;
    
    private final Random random = new Random();
    
    @Override
    public String getMarketMainMenu(LifePlayer player) {
        StringBuilder menu = new StringBuilder();
        menu.append("『鬼市』\n\n");
        menu.append("欢迎来到神秘的鬼市！\n");
        menu.append(String.format("当前灵粹：%d\n\n", player.getSpirit() != null ? player.getSpirit() : 0));
        
        menu.append("『可用操作』\n");
        menu.append("1. 神秘商人商店\n");
        menu.append("2. 玩家摊位\n");
        menu.append("3. 创建摊位\n");
        menu.append("4. 出售道具\n\n");
        
        menu.append("发送对应数字选择操作\n");
        menu.append("可发送『返回』回到上一菜单");
        
        return menu.toString();
    }
    
    @Override
    public String getMysteriousShop() {
        // 检查是否需要刷新商店
        refreshMysteriousShop();
        
        List<LifeShop> shopItems = shopMapper.selectAll();
        
        if (shopItems.isEmpty()) {
            return "『神秘商人商店』\n\n商人暂时没有商品出售...";
        }
        
        StringBuilder shop = new StringBuilder();
        shop.append("『神秘商人商店』\n\n");
        shop.append("今日折扣已刷新！\n\n");
        
        int index = 1;
        for (LifeShop shopItem : shopItems) {
            LifeItem item = shopItem.getItem();
            if (item != null) {
                shop.append(String.format("%d. %s - %d灵粹", index, item.getName(), shopItem.getCurrentPrice()));
                if (shopItem.getDiscount().compareTo(BigDecimal.ONE) < 0) {
                    shop.append(String.format("(%.1f折)", shopItem.getDiscount().doubleValue() * 10));
                }
                shop.append("\n");
                index++;
            }
        }
        
        shop.append("\n发送数字查看详情\n");
        shop.append("发送『返回』回到鬼市主菜单");
        
        return shop.toString();
    }
    
    @Override
    public String getShopItemDetail(int index, LifePlayer player) {
        List<LifeShop> shopItems = shopMapper.selectAll();
        
        if (index < 1 || index > shopItems.size()) {
            return "商品不存在！\n\n发送『返回』回到商店列表";
        }
        
        LifeShop shopItem = shopItems.get(index - 1);
        LifeItem item = shopItem.getItem();
        
        if (item == null) {
            return "商品信息异常！\n\n发送『返回』回到商店列表";
        }
        
        StringBuilder detail = new StringBuilder();
        detail.append("『商品详情』\n\n");
        detail.append("名称：").append(item.getName()).append("\n");
        detail.append("类型：").append(getItemTypeName(item.getType())).append("\n");
        detail.append("描述：").append(item.getDescription()).append("\n\n");
        detail.append("价格：").append(shopItem.getCurrentPrice()).append("灵粹");
        if (shopItem.getDiscount().compareTo(BigDecimal.ONE) < 0) {
            detail.append(String.format("（原价%d，%.1f折）", 
                shopItem.getBasePrice(), shopItem.getDiscount().doubleValue() * 10));
        }
        detail.append("\n");
        detail.append(String.format("你的灵粹：%d\n\n", player.getSpirit() != null ? player.getSpirit() : 0));
        
        detail.append("发送『购买+数量』购买此商品\n");
        detail.append("例如：购买 1（购买1个）\n");
        detail.append("发送『返回』回到商店列表");
        
        return detail.toString();
    }
    
    private String getItemTypeName(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "修为类";
            case 2: return "属性类";
            case 3: return "体力类";
            case 4: return "升级法宝类";
            case 5: return "恢复类";
            case 6: return "技能书";
            default: return "其他";
        }
    }
    
    @Override
    public String buyFromShop(LifePlayer player, Long itemId, Integer quantity) {
        LifeShop shopItem = shopMapper.selectByPrimaryKey(itemId);
        if (shopItem == null) {
            return "商品不存在！";
        }
        
        LifeItem item = itemMapper.selectByPrimaryKey(shopItem.getItemId());
        if (item == null) {
            return "商品数据异常！";
        }
        
        long totalCost = (long) shopItem.getCurrentPrice() * quantity;
        long currentSpirit = player.getSpirit() != null ? player.getSpirit() : 0;
        if (currentSpirit < totalCost) {
            return String.format("灵粹不足！需要%d灵粹，当前只有%d灵粹。", totalCost, currentSpirit);
        }
        
        // 扣除灵粹
        player.setSpirit(currentSpirit - totalCost);
        playerService.updatePlayer(player);
        
        // 添加道具到背包
        boolean success = inventoryService.addItem(player.getId(), shopItem.getItemId(), quantity);
        
        if (success) {
            return String.format("『购买成功！』\n\n购买了%d个『%s』\n消耗灵粹：%d\n剩余灵粹：%d", 
                               quantity, item.getName(), totalCost, player.getSpirit() != null ? player.getSpirit() : 0);
        } else {
            // 购买失败，退还灵粹
            player.setSpirit((player.getSpirit() != null ? player.getSpirit() : 0) + totalCost);
            playerService.updatePlayer(player);
            return "购买失败，请重试！";
        }
    }
    
    @Override
    public String sellToShop(LifePlayer player, Long itemId, Integer quantity) {
        LifePlayerItem playerItem = playerItemMapper.selectByPlayerIdAndItemId(player.getId(), itemId);
        if (playerItem == null || playerItem.getQuantity() < quantity) {
            return "你没有足够的该道具！";
        }
        
        LifeItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            return "道具不存在！";
        }
        
        // 查找商店中的基础价格
        LifeShop shopItem = shopMapper.selectByItemId(itemId);
        int sellPrice;
        if (shopItem != null) {
            // 按基础价格的2折出售
            sellPrice = (int) (shopItem.getBasePrice() * 0.2);
        } else {
            // 默认售价
            sellPrice = 10;
        }
        
        long totalEarning = (long) sellPrice * quantity;
        
        // 减少道具数量
        playerItem.setQuantity(playerItem.getQuantity() - quantity);
        playerItem.setUpdateTime(new Date());
        playerItemMapper.updateByPrimaryKey(playerItem);
        
        // 增加灵粹
        player.setSpirit((player.getSpirit() != null ? player.getSpirit() : 0) + totalEarning);
        playerService.updatePlayer(player);
        
        return String.format("『出售成功！』\n\n出售了%d个『%s』\n获得灵粹：%d\n当前灵粹：%d", 
                           quantity, item.getName(), totalEarning, player.getSpirit() != null ? player.getSpirit() : 0);
    }
    
    @Override
    public String getAllPlayerStalls() {
        List<LifePlayerStall> stalls = playerStallMapper.selectAll();
        
        if (stalls.isEmpty()) {
            return "『玩家摊位』\n\n当前没有玩家摆摊...";
        }
        
        StringBuilder stallList = new StringBuilder();
        stallList.append("『玩家摊位』\n\n");
        
        for (LifePlayerStall stall : stalls) {
            LifePlayer seller = playerService.getPlayerById(stall.getPlayerId());
            String itemName = "";
            
            if (stall.getItemType() == 1) {
                LifeItem item = itemMapper.selectByPrimaryKey(stall.getItemId());
                itemName = item != null ? item.getName() : "未知道具";
            } else {
                // TODO: 装备名称获取
                itemName = "装备";
            }
            
            stallList.append(String.format("『%s』的摊位\n", seller != null ? seller.getNickname() : "未知"));
            stallList.append(String.format("商品：%s x%d\n", itemName, stall.getQuantity()));
            stallList.append(String.format("单价：%d灵粹\n", stall.getUnitPrice()));
            stallList.append(String.format("摊位ID：%d\n\n", stall.getId()));
        }
        
        stallList.append("发送『购买摊位+摊位ID+数量』购买商品\n");
        stallList.append("例如：购买摊位1 2");
        
        return stallList.toString();
    }
    
    @Override
    public String createPlayerStall(LifePlayer player, String stallName, Integer itemType, 
                                  Long itemId, Integer quantity, Integer unitPrice) {
        // 检查玩家是否有该道具
        LifePlayerItem playerItem = playerItemMapper.selectByPlayerIdAndItemId(player.getId(), itemId);
        if (playerItem == null || playerItem.getQuantity() < quantity) {
            return "你没有足够的该道具！";
        }
        
        // 检查是否已有相同商品的摊位
        LifePlayerStall existingStall = playerStallMapper.selectByPlayerIdAndItemId(player.getId(), itemId);
        if (existingStall != null) {
            return "你已经有该商品的摊位了！";
        }
        
        try {
            // 创建摊位
            LifePlayerStall stall = new LifePlayerStall();
            stall.setPlayerId(player.getId());
            stall.setStallName(stallName);
            stall.setItemType(itemType);
            stall.setItemId(itemId);
            stall.setQuantity(quantity);
            stall.setUnitPrice(unitPrice);
            stall.setCreateTime(new Date());
            
            playerStallMapper.insert(stall);
            
            // 减少玩家道具数量
            playerItem.setQuantity(playerItem.getQuantity() - quantity);
            playerItem.setUpdateTime(new Date());
            playerItemMapper.updateByPrimaryKey(playerItem);
            
            LifeItem item = itemMapper.selectByPrimaryKey(itemId);
            return String.format("『摊位创建成功！』\n\n摊位名：%s\n商品：%s x%d\n单价：%d灵粹", 
                               stallName, item != null ? item.getName() : "未知", quantity, unitPrice);
            
        } catch (Exception e) {
            e.printStackTrace();
            return "创建摊位失败！";
        }
    }
    
    @Override
    public String buyFromPlayerStall(LifePlayer buyer, Long stallId, Integer quantity) {
        LifePlayerStall stall = playerStallMapper.selectByPrimaryKey(stallId);
        if (stall == null) {
            return "摊位不存在！";
        }
        
        if (stall.getQuantity() < quantity) {
            return String.format("商品数量不足！摊位只有%d个商品。", stall.getQuantity());
        }
        
        long totalCost = (long) stall.getUnitPrice() * quantity;
        long buyerSpirit = buyer.getSpirit() != null ? buyer.getSpirit() : 0;
        if (buyerSpirit < totalCost) {
            return String.format("灵粹不足！需要%d灵粹。", totalCost);
        }
        
        // 不能从自己的摊位购买
        if (stall.getPlayerId().equals(buyer.getId())) {
            return "不能从自己的摊位购买！";
        }
        
        try {
            // 扣除买家灵粹
            buyer.setSpirit(buyerSpirit - totalCost);
            playerService.updatePlayer(buyer);
            
            // 增加卖家灵粹
            LifePlayer seller = playerService.getPlayerById(stall.getPlayerId());
            if (seller != null) {
                seller.setSpirit((seller.getSpirit() != null ? seller.getSpirit() : 0) + totalCost);
                playerService.updatePlayer(seller);
            }
            
            // 给买家添加道具
            inventoryService.addItem(buyer.getId(), stall.getItemId(), quantity);
            
            // 减少摊位商品数量
            stall.setQuantity(stall.getQuantity() - quantity);
            if (stall.getQuantity() <= 0) {
                // 商品售完，删除摊位
                playerStallMapper.deleteByPrimaryKey(stallId);
            } else {
                playerStallMapper.updateByPrimaryKey(stall);
            }
            
            LifeItem item = itemMapper.selectByPrimaryKey(stall.getItemId());
            return String.format("『购买成功！』\n\n从『%s』的摊位购买了%d个『%s』\n花费灵粹：%d\n剩余灵粹：%d", 
                               seller != null ? seller.getNickname() : "未知", 
                               quantity, item != null ? item.getName() : "未知", 
                               totalCost, buyer.getSpirit() != null ? buyer.getSpirit() : 0);
            
        } catch (Exception e) {
            e.printStackTrace();
            return "购买失败！";
        }
    }
    
    @Override
    public void refreshMysteriousShop() {
        // 检查是否需要刷新（每日刷新）
        LocalDate today = LocalDate.now();
        List<LifeShop> shopItems = shopMapper.selectAll();
        
        boolean needRefresh = shopItems.isEmpty() || 
                             shopItems.stream().anyMatch(item -> 
                                 item.getLastRefreshDate() == null || 
                                 !isSameDay(item.getLastRefreshDate(), new Date()));
        
        if (needRefresh) {
            // 刷新所有商品的折扣
            for (LifeShop shopItem : shopItems) {
                // 6-9折随机折扣
                double discount = 0.6 + random.nextDouble() * 0.3;
                shopItem.setDiscount(BigDecimal.valueOf(discount));
                shopItem.setCurrentPrice((int) (shopItem.getBasePrice() * discount));
                shopItem.setLastRefreshDate(new Date());
                shopMapper.updateByPrimaryKey(shopItem);
            }
        }
    }
    
    private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
