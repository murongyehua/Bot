package com.bot.base.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.jx.attribute.*;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class JXAttributeInitUtil {

    public static List<String> base_attributes = new ArrayList<String>(){{add("根骨");add("身法");add("力道");add("体质");add("元气");add("御劲");add("化劲");add("无双");}};

    public static String init(String templatePath, String jsonData, String showUrl) {
        try {
            // 1. 加载模板并强制转为RGB（修复颜色空间问题）
            BufferedImage template = ImageIO.read(new File(templatePath));
            BufferedImage rgbTemplate = new BufferedImage(
                    template.getWidth(),
                    template.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );
            Graphics2D gTemplate = rgbTemplate.createGraphics();
            gTemplate.drawImage(template, 0, 0, null);
            gTemplate.dispose();
            template = rgbTemplate;

            // 2. 创建Graphics2D对象
            Graphics2D g2d = template.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 3. 加载在线子图片并移除透明通道
            AttributeBaseInfo attributeBaseInfo = JSONUtil.toBean(jsonData, AttributeBaseInfo.class);
            String imageUrl = attributeBaseInfo.getPersonAvatar();

            // 绘制文字
            // 角色信息
            drawTextBig(g2d, attributeBaseInfo.getRoleName(), 150, 520, Color.BLACK);
            drawTextNormal(g2d, attributeBaseInfo.getServerName(), 160, 700, Color.BLACK);
            drawTextSmall(g2d, "门派", 130, 1100, Color.BLACK);
            drawTextSmall(g2d, attributeBaseInfo.getForceName(), 700, 1100, Color.BLACK);
            drawTextSmall(g2d, "心法", 130, 1250, Color.BLACK);
            drawTextSmall(g2d, attributeBaseInfo.getKungfuName(), 700, 1250, Color.BLACK);
            drawTextSmall(g2d, "体型", 130, 1400, Color.BLACK);
            drawTextSmall(g2d, attributeBaseInfo.getBodyName(), 700, 1400, Color.BLACK);
            drawTextSmall(g2d, "阵营", 130, 1550, Color.BLACK);
            drawTextSmall(g2d, attributeBaseInfo.getCampName(), 700, 1550, Color.BLACK);
            drawTextSmall(g2d, "帮会", 130, 1700, Color.BLACK);
            drawTextSmall(g2d, attributeBaseInfo.getTongName(), 700, 1700, Color.BLACK);
            drawTextScore(g2d, String.valueOf(attributeBaseInfo.getPanelList().getScore()),1125, 666, Color.red);

            // 详细属性
            if (CollectionUtil.isNotEmpty(attributeBaseInfo.getPanelList().getPanel())) {
                int baseY = 2000;
                for (AttributeDetailInfo attributeDetailInfo : attributeBaseInfo.getPanelList().getPanel()) {
                    drawTextSmall(g2d, attributeDetailInfo.getName(), 130, baseY, Color.BLACK);
                    drawTextSmall(g2d, BigDecimal.valueOf(attributeDetailInfo.getValue()).stripTrailingZeros().toPlainString() + (attributeDetailInfo.getPercent() ? "%" : ""), 700, baseY, Color.BLACK);
                    baseY += 80;
                }
            }

            // 绘制装备
            if (CollectionUtil.isNotEmpty(attributeBaseInfo.getEquipList())) {
                int baseY = 950;
                for (EquipInfo equipInfo : attributeBaseInfo.getEquipList()) {
                    // 图标
                    drawPic(g2d, equipInfo.getIcon(), 1150, baseY, 200, 200);
                    // 类型
                    drawPic(g2d, equipInfo.getEquipType().getIcon(), 1150, baseY, 50, 50);
                    // 名称
                    drawTextSmall(g2d, equipInfo.getName(),1400, baseY + 50, Color.MAGENTA);
                    // 品质等级
                    drawTextSmall(g2d, "（" + equipInfo.getQuality() + "）",1400 + equipInfo.getName().length() * 50, baseY + 50, Color.BLACK);
                    // 主属性 要排除基础属性
                    List<ModifyType> modifyTypeList = equipInfo.getModifyType();
                    List<String> attributes = new ArrayList<>();
                    for (ModifyType modifyType : modifyTypeList) {
                        if (base_attributes.contains(modifyType.getName())) {
                            continue;
                        }
                        attributes.add(modifyType.getName());
                    }
                    String mainAttribute = CollectionUtil.join(attributes, StrUtil.SPACE);
                    drawTextSmall(g2d, mainAttribute,1400, baseY + 110, Color.BLACK);
                    if (equipInfo.getSource() != null && equipInfo.getSource().startsWith("套装兑换")) {
                        drawTextSmall(g2d, "套装兑换",1400, baseY + 170, Color.BLACK);
                    }else {
                        drawTextSmall(g2d, equipInfo.getSource() == null ? "" : trimSemicolonParts(equipInfo.getSource()),1400, baseY + 170, Color.BLACK);
                    }
                    // 精炼等级 先画未精炼的
                    int maxStrength = Integer.parseInt(equipInfo.getMaxStrengthLevel());
                    int strength = Integer.parseInt(equipInfo.getStrengthLevel());
                    int not = maxStrength - strength;
                    int basex = 3660;
                    if (not > 0) {
                        for (int index=0;index<not;index++) {
                            drawPicLocal(g2d, "/data/files/template/star.jpg", basex, baseY, 50, 50);
                            basex -= 60;
                        }
                    }
                    if (strength > 0) {
                        for (int index=0;index<strength;index++) {
                            drawPicLocal(g2d, "/data/files/template/full_star.jpg", basex, baseY, 50, 50);
                            basex -= 60;
                        }
                    }
                    // 如果是武器就是五彩石 如果是其他部位就是附魔
                    basex = 3660;
                    if ("武器".equals(equipInfo.getKind())) {
                        if (equipInfo.getColorStone() != null && StrUtil.isNotEmpty(equipInfo.getColorStone().getName())) {
                            basex -= equipInfo.getColorStone().getName().length() * 50;
                            drawPic(g2d, equipInfo.getColorStone().getIcon(), basex, baseY + 60, 60,60);
                            basex += 70;
                            drawTextSmall(g2d, equipInfo.getColorStone().getName(), basex, baseY + 110, Color.MAGENTA);
                        }
                    }else {
                        if (CollectionUtil.isNotEmpty(equipInfo.getPermanentEnchant())) {
                            basex -= equipInfo.getPermanentEnchant().get(0).getName().length() * 50;
                            drawPic(g2d, equipInfo.getPermanentEnchant().get(0).getIcon(), basex, baseY + 60, 60,60);
                            basex += 70;
                            drawTextSmall(g2d, equipInfo.getPermanentEnchant().get(0).getName(), basex, baseY + 110, Color.CYAN);
                        }
                    }
                    // 五行石
                    basex = 3650;
                    if (CollectionUtil.isNotEmpty(equipInfo.getFiveStone())) {
                        for (FiveStoneInfo fiveStoneInfo : equipInfo.getFiveStone()){
                            drawPic(g2d, fiveStoneInfo.getIcon(), basex, baseY + 150, 60,60);
                            basex -= 70;
                        }
                    }
                    baseY += 323;
                }
            }
            // 奇穴
            if (CollectionUtil.isNotEmpty(attributeBaseInfo.getQixueList())) {
                int qixueBaseX = 250;
                for (QIXUEInfo qixueInfo : attributeBaseInfo.getQixueList()) {
                    drawPic(g2d, qixueInfo.getIcon(), qixueBaseX, 4900, 200,200);
                    drawTextSmall(g2d, qixueInfo.getName(), qixueBaseX + 20, 5200, Color.BLACK);
                    qixueBaseX += 300;
                }
            }

            // 名片
            if (showUrl != null) {
                drawPic(g2d, showUrl, 130, 3220, 925,1350);
            }
            // 6. 保存结果
            g2d.dispose();
            String fileName = attributeBaseInfo.getRoleName() + "_" + System.currentTimeMillis() + ".jpg";
            ImageIO.write(template, "jpg", new File("/data/files/picCache/" + fileName));
            return fileName;
//            ImageIO.write(template, "jpg", new File("output.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            log.error("生成图片失败", e);
        }
        return null;
    }

    private static String trimSemicolonParts(String input) {
        String[] parts = input.split(";", -1);  // 使用-1保留结尾空字符串
        return String.join(";", parts.length > 3
                ? Arrays.copyOfRange(parts, 0, 3)  // 取前3段
                : parts);                           // 保持原样
    }

    private static void drawPic(Graphics2D g2d, String imageUrl, int x, int y, int width, int height) throws IOException {
        BufferedImage subImage = ImageIO.read(new URL(imageUrl));
        BufferedImage opaqueSubImage = new BufferedImage(
                subImage.getWidth(),
                subImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        Graphics2D gSub = opaqueSubImage.createGraphics();
        gSub.drawImage(subImage, 0, 0, null);
        gSub.dispose();
        subImage = opaqueSubImage;

        // 4. 绘制子图片（设置高质量缩放）
        g2d.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );
        g2d.drawImage(subImage, x, y, width, height, null);
    }

    private static void drawPicLocal(Graphics2D g2d, String imagePath, int x, int y, int width, int height) throws IOException {
        BufferedImage subImage = ImageIO.read(new File(imagePath));
        BufferedImage opaqueSubImage = new BufferedImage(
                subImage.getWidth(),
                subImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        Graphics2D gSub = opaqueSubImage.createGraphics();
        gSub.drawImage(subImage, 0, 0, null);
        gSub.dispose();
        subImage = opaqueSubImage;

        // 4. 绘制子图片（设置高质量缩放）
        g2d.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );
        g2d.drawImage(subImage, x, y, width, height, null);
    }

    private static void drawTextBig(Graphics2D g2d, String text, int x, int y, Color color) {
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 155));
        g2d.setColor(color);
        g2d.drawString(text, x, y);
    }

    private static void drawTextScore(Graphics2D g2d, String text, int x, int y, Color color) {
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 150));
        g2d.setColor(color);
        g2d.drawString(text, x, y);
    }

    private static void drawTextNormal(Graphics2D g2d, String text, int x, int y, Color color) {
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 100));
        g2d.setColor(color);
        g2d.drawString(text, x, y);
    }


    private static void drawTextSmall(Graphics2D g2d, String text, int x, int y, Color color) {
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 58));
        g2d.setColor(color);
        g2d.drawString(text, x, y);
    }

    private static void drawTextMini(Graphics2D g2d, String text, int x, int y, Color color) {
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 30));
        g2d.setColor(color);
        g2d.drawString(text, x, y);
    }

}
