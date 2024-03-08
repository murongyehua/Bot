package com.bot.base.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.bot.common.constant.BaseConsts;
import com.bot.common.enums.ENWorkExcelLocation;
import com.bot.common.util.SendMsgUtil;
import com.bot.common.util.ThreadPoolManager;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class WorkManager {

    public final static List<String> WORK_TOKENS = new LinkedList<>();

    public final static CopyOnWriteArrayList<String> WAIT_DEAL_DATA_LIST = new CopyOnWriteArrayList<>();

    @Value("${base.file.path}")
    private String baseFilePath;

    private final static Map<String, Integer> TARGET_MAP = new HashMap<String, Integer>(){{
        put("现金扫对公：", 1);
        put("支付宝扫对公户：", 2);
        put("微信扫对公户：", 3);
        put("单日耗卡：", 4);
    }};


    /**
     * 进入工作模式
     * @param token
     * @return
     */
    public String entryWork(String token) {
        // 检查文件
        String monthDate = DateUtil.format(new Date(), DatePattern.SIMPLE_MONTH_PATTERN);
        String filePath = baseFilePath + File.separator + monthDate + ".xlsx";
        boolean isExist = FileUtil.exist(filePath);
        if (!isExist) {
            // 不存在就创建
            FileUtil.copy(baseFilePath + File.separator + "template.xlsx", filePath, false);
            // 给两个表填入日期
            this.fillDate2Excel(filePath, "核对表");
            this.fillDate2Excel(filePath, "统计表");
        }
        WORK_TOKENS.add(token);
        return BaseConsts.Work.ENTRY_TIPS;
    }

    /**
     * 解析工作指令
     * @param content
     * @return
     */
    public String doWork(String content, String token) {
        // 退出
        if (BaseConsts.Work.EXIT.equals(content)) {
            WorkManager.WORK_TOKENS.remove(token);
            return BaseConsts.Work.EXIT_SUCCESS;
        }
        // 结束
        if (BaseConsts.Work.FINISH.equals(content)) {
            return getExcelFile();
        }
        // 提取
        if (BaseConsts.Work.FETCH.equals(content)) {
            return fetchData();
        }
        // 处理
        if (BaseConsts.Work.START_DEAL.equals(content)) {
            ThreadPoolManager.addBaseTask(this::autoAnalysis);
            return BaseConsts.Work.START_DEAL_TIP;
        }
        // 默认 收集数据
        WAIT_DEAL_DATA_LIST.add(content);
        return String.format(BaseConsts.Work.SAVE_DATA_TIP, WAIT_DEAL_DATA_LIST.size());
    }

    /**
     * 提取
     * @return
     */
    public String fetchData() {
        String monthDate = DateUtil.format(new Date(), DatePattern.SIMPLE_MONTH_PATTERN);
        String filePath = baseFilePath + File.separator + monthDate + ".xlsx";
        if (!FileUtil.exist(filePath)) {
            return BaseConsts.Work.ERROR;
        }
        String reloadResult = this.reloadExcel(filePath);
        if (reloadResult != null) {
            return reloadResult;
        }
        StringBuilder result = new StringBuilder(String.format("%s月%s日生活馆各店业绩汇总：", DateUtil.thisMonth() + 1, DateUtil.thisDayOfMonth()));
        ExcelReader reader = ExcelUtil.getReader(filePath, 1);
        for (ENWorkExcelLocation workExcelLocation: ENWorkExcelLocation.values()) {
            result.append(StrUtil.CRLF);
            // 店名
            result.append(reader.readCellValue(workExcelLocation.getBaseIndexX(), workExcelLocation.getTotalY()));
            result.append(StrUtil.CRLF);
            // 往下取十五个项，如果遇到连续两个空的就结束
            int blankCount = 0;
            for (int number = 1; number < 16; number++) {
                String option = (String) reader.readCellValue(workExcelLocation.getBaseIndexX(), workExcelLocation.getTotalY() + number);
                if (StrUtil.isEmpty(option)) {
                    blankCount++;
                    // 每次遇到空白 增加一个空行
                    result.append(StrUtil.CRLF);
                    if (blankCount == 2) {
                        break;
                    }
                    continue;
                }
                // 不为空的时候要重置计数
                blankCount = 0;
                // 数据项
                result.append(option);
                // 数据
                result.append(reader.readCellValue(workExcelLocation.getBaseIndexX() + DateUtil.thisDayOfMonth(), workExcelLocation.getTotalY() + number)).append("元");
            }
        }

        return result.toString();
    }

    private void autoAnalysis() {
        // 逐条处理数据
        StringBuilder failInfo = new StringBuilder();
        int total = WAIT_DEAL_DATA_LIST.size();
        int success = WAIT_DEAL_DATA_LIST.size();
        int fail = 0;
        for (String data : WAIT_DEAL_DATA_LIST) {
            String dealResult = this.analysis2Excel(data);
            if (dealResult != null) {
                success--;
                fail++;
                failInfo.append("失败记录").append(fail).append("：").append(StrUtil.CRLF).append(data).append(StrUtil.CRLF);
            }
        }
        WAIT_DEAL_DATA_LIST.clear();
        for (String token : WORK_TOKENS) {
            if (fail == 0) {
                SendMsgUtil.sendMsg(token, String.format(BaseConsts.Work.DEAL_SUCCESS_RESULT, total));
            }else {
                SendMsgUtil.sendMsg(token, String.format(BaseConsts.Work.DEAL_FAIL_RESULT, total, success, fail));
                SendMsgUtil.sendMsg(token, failInfo.toString());
            }
        }

    }

    /**
     * 解析文本到excel表中
     * @param content
     * @return 只有处理成功的时候返回null
     */
    public String analysis2Excel(String content) {
        String monthDate = DateUtil.format(new Date(), DatePattern.SIMPLE_MONTH_PATTERN);
        String filePath = baseFilePath + File.separator + monthDate + ".xlsx";
        // 还是判断一下存不存在，不存在返回异常
        if (!FileUtil.exist(filePath)) {
            return BaseConsts.Work.ERROR;
        }
        try {
            // 处理空白行
            content = this.cleanBlankLine(content);
            // 解析文本
            ENWorkExcelLocation targetLocation = null;
            for (ENWorkExcelLocation excelLocation : ENWorkExcelLocation.values()) {
                if (content.startsWith(excelLocation.getKeyWords().get(0))) {
                    targetLocation = excelLocation;
                    break;
                }
            }
            if (targetLocation == null) {
                return BaseConsts.Work.ILL_INFO;
            }
            // 获取当天的坐标
            int x = targetLocation.getBaseIndexX() + DateUtil.thisDayOfMonth();
            int y = targetLocation.getBaseIndexY();
            ExcelWriter writer = ExcelUtil.getWriter(filePath, "核对表");
            for (String lineContent : content.split(StrUtil.LF)) {
                for (String key : TARGET_MAP.keySet()) {
                    if (lineContent.startsWith(key)) {
                        // 需要填到对应表里的值 兼容中英文冒号
                        String[] contentArr = lineContent.trim().split("：").length > 1 ? lineContent.trim().split("：") : lineContent.trim().split(":");
                        String finalNumber = contentArr[1].trim().replace("元", "");
                        writer.writeCellValue(x, y + TARGET_MAP.get(key), finalNumber);
                    }
                }
            }
            writer.flush();
            writer.close();
            return null;
        }catch (Exception e) {
            e.printStackTrace();
            return BaseConsts.Work.ILL_INFO;
        }
    }

    public String getExcelFile() {
        String monthDate = DateUtil.format(new Date(), DatePattern.SIMPLE_MONTH_PATTERN);
        String filePath = baseFilePath + File.separator + monthDate + ".xlsx";
        if (!FileUtil.exist(filePath)) {
            return BaseConsts.Work.FILE_NOT_EXIST;
        }
        String reloadResult = this.reloadExcel(filePath);
        if (reloadResult != null) {
            return reloadResult;
        }
        return String.format("http://47.92.127.30/file/%s.xlsx", monthDate);
    }

    private void fillDate2Excel(String filePath, String sheetName) {
        // 填入当月时间
        ExcelWriter writer = ExcelUtil.getWriter(filePath, sheetName);
        // 初始坐标
        int x = 2;
        int y = 1;
        for (int day = 1; day < 32; day++) {
            writer.writeCellValue(x++, y, String.format("%s月%s日", DateUtil.thisMonth() + 1, day));
        }
        writer.flush();
        writer.close();
    }

    private String reloadExcel(String filePath) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            // 刷新公式
            workbook.setForceFormulaRecalculation(true);
            //使用evaluateFormulaCell对函数单元格进行强行更新计算
            workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
            FileOutputStream fos = new FileOutputStream(filePath);
            workbook.write(fos);
            fis.close();
            fos.close();
        }catch (Exception e) {
            e.printStackTrace();
            return BaseConsts.Work.EXCEPTION;
        }

        return null;

    }


    private String cleanBlankLine(String content) {
        List<String> contentArr = Arrays.asList(content.split(StrUtil.LF));
        List<String> contentList = new ArrayList<>(contentArr);
        contentList.removeIf(StrUtil::isBlank);
        return String.join(StrUtil.LF, contentList);
    }

}
