package com.bot.base.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.bot.base.dto.jx.teamcd.TeamCdBaseInfo;
import com.bot.base.dto.jx.teamcd.TeamCdBossInfo;
import com.bot.base.dto.jx.teamcd.TeamCdDetailData;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@Slf4j
public class JXTeamCdInitUtil {

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
            TeamCdBaseInfo teamCdBaseInfo = JSONUtil.toBean(jsonData, TeamCdBaseInfo.class);
            String imageUrl = teamCdBaseInfo.getPersonAvatar();
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
                g2d.drawImage(subImage, 800, 520, 300, 300, null);
            }

            // 5. 绘制文字
            drawText(g2d, teamCdBaseInfo.getServerName(), 260, 305, Color.BLACK);
            drawText(g2d, teamCdBaseInfo.getForceName(), 660, 305, Color.BLACK);
            drawText(g2d, teamCdBaseInfo.getTongName(), 995, 305, Color.BLACK);
            drawText(g2d, teamCdBaseInfo.getRoleName(), 260, 430, Color.BLACK);
            drawText(g2d, teamCdBaseInfo.getBodyName(), 660, 430, Color.BLACK);
            drawText(g2d, teamCdBaseInfo.getCampName(), 995, 430, Color.BLACK);
            if (CollectionUtil.isNotEmpty(teamCdBaseInfo.getData())) {
                int baseX = 20;
                int baseY = 1120;
                for (TeamCdDetailData detailData : teamCdBaseInfo.getData()) {
                    int x = baseX;
                    int y = baseY;
                    drawTextMini(g2d, String.format("%s·%s", detailData.getMapType(), detailData.getMapName()), x, y, Color.BLACK);
                    x += 450;

                    for (TeamCdBossInfo bossInfo : detailData.getBossProgress()) {
                        drawTextMini(g2d, bossInfo.getName(), x, y, bossInfo.getFinished() ? Color.GREEN : Color.RED);
                        x += 125;
                    }
                    baseY += 80;

                }
            }
            // 6. 保存结果
            g2d.dispose();
            String fileName = teamCdBaseInfo.getRoleName() + "_" + System.currentTimeMillis() + ".jpg";
            ImageIO.write(template, "jpg", new File("/data/files/picCache/" + fileName));
            return fileName;
//            ImageIO.write(template, "jpg", new File("output.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            log.error("生成图片失败", e);
        }
        return null;
    }

    private static void drawText(Graphics2D g2d, String text, int x, int y, Color color) {
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 40));
        g2d.setColor(color);
        g2d.drawString(text, x, y);
    }

    private static void drawTextMini(Graphics2D g2d, String text, int x, int y, Color color) {
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 30));
        g2d.setColor(color);
        g2d.drawString(text, x, y);
    }

}
