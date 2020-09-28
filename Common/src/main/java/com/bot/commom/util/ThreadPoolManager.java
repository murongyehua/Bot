package com.bot.commom.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.concurrent.*;

/**
 * @author murongyehua
 * @version 1.0 2019/10/18
 */
public class ThreadPoolManager extends ThreadPoolExecutor {

    /**
     * 基础线程核心线程数
     */
    private final static int BASE_THREAD_POOL_CORE_SIZE = 10;
    /**
     * 基础线程最大线程数
     */
    private final static int BASE_THREAD_POOL_MAX_SIZE = 50;
    /**
     * 紧急线程核心线程数
     */
    private final static int EMERGENCY_THREAD_POOL_CORE_SIZE = 3;
    /**
     * 紧急线程最大线程数
     */
    private final static int EMERGENCY_THREAD_POOL_MAX_SIZE = 10;
    /**
     * 线程存活时间 单位：秒
     */
    private final static int THREAD_ALIVE_TIME_SECONDS = 60;
    /**
     * 定时任务线程数(推荐1)
     */
    private final static int IN_TIME_THREAD_POOL_SIZE = 1;
    /**
     * 定时任务触发间隔，单位：毫秒 如 1000 * 60 * 60 * 24 为一天
     */
    private final static int DELAY_TIME = 3000;
    /**
     * 定时任务首次执行的时间(可以使用配置的方式设置，这里手动写死，格式HH:mm:ss)
     */
    private final static String START_TIME = "10:24:00";
    /**
     * 定时任务是否正在运行的标记
     */
    private static volatile boolean inTimeStartFlag = false;

    private final static LinkedBlockingQueue<Runnable> BASE_TASK = new LinkedBlockingQueue<>();

    private final static LinkedBlockingQueue<Runnable> EMERGENCY_TASK = new LinkedBlockingQueue<>();

    private final static LinkedBlockingQueue<Runnable> IN_TIME_TASK = new LinkedBlockingQueue<>();

    private static ExecutorService baseExecutor = new ThreadPoolManager(BASE_THREAD_POOL_CORE_SIZE,BASE_THREAD_POOL_MAX_SIZE,THREAD_ALIVE_TIME_SECONDS,TimeUnit.SECONDS,BASE_TASK);

    private static ExecutorService emergencyExecutor = new ThreadPoolManager(EMERGENCY_THREAD_POOL_CORE_SIZE,EMERGENCY_THREAD_POOL_MAX_SIZE,THREAD_ALIVE_TIME_SECONDS,TimeUnit.SECONDS,EMERGENCY_TASK);

    private static ExecutorService inTimeExecutor = new ThreadPoolManager(IN_TIME_THREAD_POOL_SIZE,IN_TIME_THREAD_POOL_SIZE,THREAD_ALIVE_TIME_SECONDS,TimeUnit.SECONDS,BASE_TASK);

    private ThreadPoolManager(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit timeUnit, BlockingQueue<Runnable> queue){
        super(corePoolSize, maxPoolSize, keepAliveTime, timeUnit, queue);
    }

    /**
     * 添加基础任务
     * @param task
     * @return
     */
    public static Future addBaseTask(Runnable task){
        return baseExecutor.submit(task);
    }

    /**
     * 添加紧急任务
     * @param task
     * @return
     */
    public static Future addEmergencyTask(Runnable task){
        return emergencyExecutor.submit(task);
    }

    /**
     *  添加定时任务
     * @param task
     */
    public static void addInTimeTask(Runnable task){
        if (IN_TIME_TASK.contains(task)) {
            throw new RuntimeException("任务重复添加");
        }
        if (IN_TIME_TASK.size() == 0) {
            IN_TIME_TASK.offer(task);
            inTimeExecutor.execute(() -> runInTimeTask());
        }else {
            IN_TIME_TASK.offer(task);
        }
    }

    private static void runInTimeTask(){
        while (true) {
            if (isInTime() || inTimeStartFlag) {
                inTimeStartFlag = true;
                for (Runnable runnable : IN_TIME_TASK) {
                    runnable.run();
                }
                try{
                    Thread.sleep(DELAY_TIME);
                }catch (Exception e){
                    System.out.println("定时任务执行异常");
                }
            }
        }
    }

    private static boolean isInTime(){
       return DateUtil.parse(DateUtil.format(new Date(),DatePattern.NORM_DATE_PATTERN) + " " + START_TIME, DatePattern.NORM_DATETIME_PATTERN).getTime()
               == System.currentTimeMillis();
    }

}
