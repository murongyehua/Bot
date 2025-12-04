package com.bot.life.service.impl;

import com.bot.life.dao.entity.LifePlayer;
import com.bot.life.dao.entity.LifeRealmConfig;
import com.bot.life.dao.mapper.LifeRealmConfigMapper;
import com.bot.life.service.ImageGenerationService;
import com.bot.life.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 图片生成服务实现
 * @author Assistant
 */
@Service
public class ImageGenerationServiceImpl implements ImageGenerationService {
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private LifeRealmConfigMapper realmConfigMapper;
    
    private static final String BACKGROUND_IMAGE_PATH = "life_back.png";
    private static final String OUTPUT_DIR = "/data/files/life_pic/";
    private static final String BASE_URL = "http://113.45.63.97/file/life_pic/";
    
    /**
     * 生成带角色状态的游戏图像
     */
    public String generateGameImageWithStatus(String content, LifePlayer player) {
        String basicStatus = getPlayerBasicStatus(player);
        return generateGameImage(basicStatus + content);
    }
    
    @Override
    public String generateGameImage(String content) {
        try {
            // 确保输出目录存在
            File outputDir = new File(OUTPUT_DIR);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 加载背景图片
            BufferedImage backgroundImage = loadBackgroundImage();
            
            // 创建图形上下文
            Graphics2D g2d = backgroundImage.createGraphics();
            setupGraphics(g2d);
            
            // 绘制文本
            drawText(g2d, content, backgroundImage.getWidth(), backgroundImage.getHeight());
            
            g2d.dispose();
            
            // 保存图片
            String fileName = UUID.randomUUID().toString() + ".png";
            String outputPath = OUTPUT_DIR + fileName;
            ImageIO.write(backgroundImage, "PNG", new File(outputPath));
            
            // 返回公网URL
            return BASE_URL + fileName;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 生成角色基本状态显示
     */
    private String getPlayerBasicStatus(LifePlayer player) {
        if (player == null) {
            return "";
        }

        StringBuilder status = new StringBuilder();
        status.append("════════════════════════════════════════════════════\n");
        status.append(String.format("%s | 境界：%s | ", player.getNickname(), getRealmName(player.getLevel())));
        // 获取当前境界的修为上限
        LifeRealmConfig currentRealm = realmConfigMapper.selectByLevel(player.getLevel());
        long maxCultivation = currentRealm != null ? currentRealm.getMaxCultivation() : 0;
        status.append(String.format("修为：%s/%s | ", formatNumber(player.getCultivation()), formatNumber(maxCultivation)));
        status.append(String.format("地图：%s | ", getMapName(player.getCurrentMapId())));
        status.append(String.format("血量：%d/%d", player.getHealth(), player.getMaxHealth()));
        status.append("\n════════════════════════════════════════════════════\n");

        return status.toString();
    }
    
    @Override
    public String generatePlayerStatusImage(String userId) {
        LifePlayer player = playerService.getPlayerByUserId(userId);
        if (player == null) {
            return generateGameImage("角色不存在，请先创建角色！");
        }
        
        StringBuilder status = new StringBuilder();
        status.append("『").append(player.getNickname()).append("』的状态\n\n");
        status.append("等级：").append(player.getLevel()).append("\n");
        long nextLevelExp = getNextLevelExperience(player.getLevel());
        status.append("经验：").append(formatNumber(player.getExperience() != null ? player.getExperience() : 0)).append("/").append(formatNumber(nextLevelExp)).append("\n");
        status.append("境界：").append(getRealmName(player.getLevel())).append("\n");
        // 获取当前境界的修为上限
        LifeRealmConfig currentRealm = realmConfigMapper.selectByLevel(player.getLevel());
        long maxCultivation = currentRealm != null ? currentRealm.getMaxCultivation() : 0;
        status.append("修为：").append(formatNumber(player.getCultivation())).append("/").append(formatNumber(maxCultivation)).append("\n");
        status.append("属性：").append(getAttributeName(player.getAttribute())).append("\n\n");
        
        // 紧凑型属性展示
        status.append("『基础属性』\n");
        status.append(String.format("速度：%d | 体质：%d | 灵力：%d | 力量：%d\n\n", 
            player.getSpeed(), player.getConstitution(), player.getSpiritPower(), player.getStrength()));
        
        status.append("『战斗属性』\n");
        status.append(String.format("血量：%d/%d | 攻击：%d | 防御：%d\n", 
            player.getHealth(), player.getMaxHealth(), player.getAttackPower(), player.getDefense()));
        status.append(String.format("会心：%.2f%% | 会心效果：%.2f%% | 破防：%.2f%%\n\n", 
            player.getCriticalRate(), player.getCriticalDamage(), player.getArmorBreak()));

        status.append(String.format("体力：%d/%d | 灵粹：%d\n", 
            player.getStamina(), player.getMaxStamina(), 
            player.getSpirit() != null ? player.getSpirit() : 0));
        
        return generateGameImage(status.toString());
    }
    
    @Override
    public String generateBattleResultImage(String battleLog) {
        return generateGameImage(battleLog);
    }
    
    private BufferedImage loadBackgroundImage() throws IOException {
        // 尝试从项目根目录加载背景图片
        File backgroundFile = new File(BACKGROUND_IMAGE_PATH);
        if (backgroundFile.exists()) {
            return ImageIO.read(backgroundFile);
        }
        
        // 如果根目录没有，尝试从类路径加载
        try {
            java.io.InputStream resource = getClass().getClassLoader().getResourceAsStream(BACKGROUND_IMAGE_PATH);
            if (resource != null) {
                return ImageIO.read(resource);
            }
        } catch (Exception e) {
            // 忽略异常，使用默认背景
        }
        
        // 如果都没有找到，创建默认背景
        {
            // 如果背景图不存在，创建一个默认的背景
            BufferedImage defaultBg = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = defaultBg.createGraphics();
            
            // 创建渐变背景
            GradientPaint gradient = new GradientPaint(0, 0, new Color(240, 248, 255), 
                                                     0, 600, new Color(176, 196, 222));
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, 800, 600);
            
            // 添加边框
            g2d.setColor(new Color(70, 130, 180));
            g2d.setStroke(new BasicStroke(5));
            g2d.drawRect(10, 10, 780, 580);
            
            g2d.dispose();
            return defaultBg;
        }
    }
    
    private void setupGraphics(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
    
    private void drawText(Graphics2D g2d, String content, int imageWidth, int imageHeight) {
        // 设置字体
        Font titleFont = new Font("Microsoft YaHei", Font.BOLD, 24);
        Font contentFont = new Font("Microsoft YaHei", Font.PLAIN, 18);
        
        g2d.setColor(new Color(25, 25, 112)); // 深蓝色
        
        String[] lines = content.split("\n");
        int y = 50; // 起始Y位置 - 调整为50像素
        int lineHeight = 30;
        int margin = 50;
        
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                y += lineHeight / 2; // 空行间距减半
                continue;
            }
            
            // 检查是否是标题（包含『』的行）
            if (line.contains("『") && line.contains("』")) {
                g2d.setFont(titleFont);
                g2d.setColor(new Color(220, 20, 60)); // 红色标题
            } else {
                g2d.setFont(contentFont);
                g2d.setColor(new Color(25, 25, 112)); // 深蓝色内容
            }
            
            // 处理长文本换行
            String[] wrappedLines = wrapText(line, g2d.getFontMetrics(), imageWidth - 2 * margin);
            for (String wrappedLine : wrappedLines) {
                g2d.drawString(wrappedLine, margin, y);
                y += lineHeight;
                
                // 防止文本超出图片边界
                if (y > imageHeight - 50) {
                    break;
                }
            }
        }
    }
    
    private String[] wrapText(String text, FontMetrics metrics, int maxWidth) {
        if (metrics.stringWidth(text) <= maxWidth) {
            return new String[]{text};
        }
        
        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split("");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine + word;
            if (metrics.stringWidth(testLine) <= maxWidth) {
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines.toArray(new String[0]);
    }
    
    private String getRealmName(Integer level) {
        LifeRealmConfig realm = realmConfigMapper.selectByLevel(level);
        return realm != null ? realm.getRealmName() : "未知境界";
    }
    
    private String getMapName(Long mapId) {
        // 简化的地图名称映射
        if (mapId == null) return "未知地点";
        switch (mapId.intValue()) {
            case 1: return "新手村";
            case 2: return "青云山";
            case 3: return "幽谷";
            case 4: return "烈火峡谷";
            case 5: return "鬼市";
            default: return "神秘之地";
        }
    }
    
    /**
     * 计算下一等级所需经验值
     * 采用公式：exp = level * 1000 + level^2 * 500
     */
    private long getNextLevelExperience(Integer currentLevel) {
        int nextLevel = currentLevel + 1;
        return nextLevel * 1000L + nextLevel * nextLevel * 500L;
    }
    
    private String getAttributeName(Integer attribute) {
        switch (attribute) {
            case 1: return "金";
            case 2: return "木";
            case 3: return "水";
            case 4: return "火";
            case 5: return "土";
            default: return "无";
        }
    }
    
    private String formatNumber(Long number) {
        if (number < 10000) {
            return number.toString();
        } else if (number < 100000000) {
            return String.format("%.1f万", number / 10000.0);
        } else {
            return String.format("%.1f亿", number / 100000000.0);
        }
    }
}
