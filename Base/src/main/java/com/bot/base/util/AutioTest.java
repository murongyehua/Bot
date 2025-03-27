package com.bot.base.util;

import com.bot.common.util.AudioTransUtil;

import java.io.File;

public class AutioTest {

    public static void main(String[] args) {
        File sourceFile = new File("C:\\Users\\11371\\Documents\\工作临时文件\\audio_1742653259612.pcm");
        File targetFile = new File("C:\\Users\\11371\\Documents\\工作临时文件\\audio_1742634727045.silk");
        AudioTransUtil.mp3ToAmrUtil(sourceFile, targetFile);
    }

}
