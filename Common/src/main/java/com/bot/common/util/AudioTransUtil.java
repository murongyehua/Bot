package com.bot.common.util;

import ws.schild.jave.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class AudioTransUtil {

    public static void mp3ToSilkUtil(File source, File target){
        try {
            ProcessBuilder builder = new ProcessBuilder("/data/project/silk-v3-decoder/silk/encoder", source.getAbsolutePath(), target.getAbsolutePath(), "-tencent");
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void mp3ToAmrUtil(File source, File target){
        AudioAttributes audio = new AudioAttributes();
        //audio.setCodec("libmp3lame");//mp3
        //audio.setCodec("libopencore_amrnb");//amr-nb
        audio.setCodec("libvo_amrwbenc");//amr-wb
        //audio.setCodec("pcm_s16le");//wav
        audio.setChannels(1);
        audio.setSamplingRate(16000);
        EncodingAttributes attrs = new EncodingAttributes();
        //attrs.setFormat("mp3");
        attrs.setFormat("amr");  //转换格式
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        try {
            MultimediaObject multimediaObject  = new MultimediaObject(source);
            encoder.encode(multimediaObject,target, attrs);
        } catch (IllegalArgumentException | EncoderException e) {
            e.printStackTrace();
        }
    }

    public static long getPCMDurationMilliSecond(File file) {
        // 参数需根据实际音频调整（如16位、单声道、16000Hz）
        int sampleRate = 24000;
        int bitsPerSample = 16;
        int channels = 1;
        long bytesPerSecond = (sampleRate * (bitsPerSample / 8) * channels);
        long seconds = file.length() / bytesPerSecond;
        long milliseconds = (file.length() % bytesPerSecond) * 1000 / bytesPerSecond;
        return seconds * 1000 + milliseconds;
    }

}
