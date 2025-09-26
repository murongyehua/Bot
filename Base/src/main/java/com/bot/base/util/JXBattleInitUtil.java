package com.bot.base.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.jx.battle.BattleBaseInfo;
import com.bot.base.dto.jx.battle.BattleDetailData;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@Slf4j
public class JXBattleInitUtil {

    public static String init(String templatePath, String jsonData) {
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
            BattleBaseInfo battleBaseInfo = JSONUtil.toBean(jsonData, BattleBaseInfo.class);
            String imageUrl = battleBaseInfo.getPersonAvatar();
            if (imageUrl != null) {
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
                g2d.drawImage(subImage, 1400, 100, 500, 500, null);
            }

            // 5. 绘制文字
            drawText(g2d, battleBaseInfo.getServerName(), 290, 130);
            drawText(g2d, battleBaseInfo.getTongName(), 960, 130);
            drawText(g2d, battleBaseInfo.getRoleName(), 290, 260);
            drawText(g2d, battleBaseInfo.getCampName(), 960, 260);
            drawText(g2d, battleBaseInfo.getForceName(), 290, 390);
            drawText(g2d, battleBaseInfo.getBodyName(), 290, 530);
            JSONObject jsonObject = JSONUtil.parseObj(battleBaseInfo.getPerformance());
            BattleDetailData tow = null;
            BattleDetailData three = null;
            BattleDetailData five = null;
            if (jsonObject.get("2v2") instanceof JSONObject) {
                tow = JSONUtil.toBean(JSONUtil.parseObj(jsonObject.get("2v2")), BattleDetailData.class);
            }
            if (jsonObject.get("3v3") instanceof JSONObject) {
                three = JSONUtil.toBean(JSONUtil.parseObj(jsonObject.get("3v3")), BattleDetailData.class);
            }
            if (jsonObject.get("5v5") instanceof JSONObject) {
                five = JSONUtil.toBean(JSONUtil.parseObj(jsonObject.get("5v5")), BattleDetailData.class);
            }
            // 2v2 第一排
            drawText(g2d, tow == null ? "/" : String.valueOf(tow.getMmr()), 650, 980);
            drawText(g2d, tow == null ? "/" : String.valueOf(tow.getGrade()), 1050, 980);
            drawText(g2d, tow == null ? "/" : tow.getRanking(), 1440, 980);
            // 2v2 第二排
            drawText(g2d, tow == null ? "/" : String.valueOf(tow.getTotalCount()), 650, 1155);
            drawText(g2d, tow == null ? "/" : String.valueOf(tow.getWinCount()), 1050, 1155);
            drawText(g2d, tow == null ? "/" : tow.getWinRate() + "%", 1440, 1155);

            // 3v3 第一排
            drawText(g2d, three == null ? "/" : String.valueOf(three.getMmr()), 650, 1560);
            drawText(g2d, three == null ? "/" : String.valueOf(three.getGrade()), 1050, 1560);
            drawText(g2d, three == null ? "/" : three.getRanking(), 1440, 1560);
            // 3v3第二排
            drawText(g2d, three == null ? "/" : String.valueOf(three.getTotalCount()), 650, 1760);
            drawText(g2d, three == null ? "/" : String.valueOf(three.getWinCount()), 1050, 1760);
            drawText(g2d, three == null ? "/" : three.getWinRate() + "%", 1440, 1760);

            // 5v5 第一排
            drawText(g2d, five == null ? "/" : String.valueOf(five.getMmr()), 650, 2160);
            drawText(g2d, five == null ? "/" : String.valueOf(five.getGrade()), 1050, 2160);
            drawText(g2d, five == null ? "/" : five.getRanking(), 1440, 2160);
            // 5v5第二排
            drawText(g2d, five == null ? "/" : String.valueOf(five.getTotalCount()), 650, 2360);
            drawText(g2d, five == null ? "/" : String.valueOf(five.getWinCount()), 1050, 2360);
            drawText(g2d, five == null ? "/" : five.getWinRate() + "%", 1440, 2360);
            // 6. 保存结果
            g2d.dispose();
            String fileName = battleBaseInfo.getRoleName() + "_" + System.currentTimeMillis() + ".jpg";
            ImageIO.write(template, "jpg", new File("/data/files/picCache/" + fileName));
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("生成图片失败", e);
        }
        return null;
    }

    private static void drawText(Graphics2D g2d, String text, int x, int y) {
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 66));
        g2d.setColor(Color.BLACK);
//        FontMetrics metrics = g2d.getFontMetrics();
//        int textX = (template.getWidth() - metrics.stringWidth(text)) / 2;
//        int textY = 50 + metrics.getAscent();
        g2d.drawString(text, x, y);
    }

}
