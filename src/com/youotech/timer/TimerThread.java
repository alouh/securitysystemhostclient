package com.youotech.timer;

import com.youotech.Main;
import com.youotech.database.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import utils.Tools;

import java.sql.Connection;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: HanJiafeng
 * @Date: 10:14 2018/1/17
 * @Desc:
 */
public class TimerThread{

    private boolean isRunning = true;
    private static final Log LOGGER = LogFactory.getLog(TimerThread.class);
    public static byte flag = Timer.ALL_IP_TIMER;
    public final static Boolean SYNCHRONIZED_FLAG = false;

    public void run() {

        Data data = new Data();
        Connection connection = data.getConnection();//获取数据库连接
        Main main = new Main();
        Time oldTime = new Time(-25200000);//旧定时器,默认凌晨一点

        while (isRunning){

            try {
                //如果连接不可用,试图关闭连接然后重新获取一个连接
                if (!connection.isValid(1000)){
                    data.closeConnection(connection);
                    connection = data.getConnection();
                }

                List<String> ipList = Tools.getAllIp();//获取本机所有IP地址

                if (flag == Timer.ALL_IP_TIMER || flag == Timer.SINGER_IP_TIMER_USED){
                    //当定时器是所有IP和已经用过的指定IP定时器,获取新的定时器
                    Timer timer = data.getTimer(connection,ipList);
                    Time newTime = timer.getTime();
                    if (!oldTime.equals(newTime)) {
                        synchronized (SYNCHRONIZED_FLAG) {//获取同步锁时才能修改值
                            flag = timer.getFlag();
                        }
                        main.closeTimer();
                        main.executeTimer(timer);
                        oldTime = newTime;
                    }
                }
                Thread.sleep(1000 * 3);
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        data.closeConnection(connection);//关闭连接

        /*Data data = new Data();
        Connection connection = data.getConnection();//获取数据库连接
        Main main = new Main();
        List<String> ipList = new ArrayList<>();
        ipList.add("*");
        String defaultTimerStr = data.getTimer(connection,ipList);
        int[] currentTimerInts = stringToArrays(defaultTimerStr);
        defaultTimerInts = currentTimerInts;//如果有通用定时器时间,则覆盖默认的
        main.executeTimer(currentTimerInts[0], currentTimerInts[1], currentTimerInts[2]);//先使用默认时间执行定时器

        while (isRunning){
            try {
                //如果连接不可用,试图关闭连接然后重新获取一个连接
                if (!connection.isValid(1000)){
                    data.closeConnection(connection);
                    connection = data.getConnection();
                }
                ipList = Tools.getAllIp();//获取本机所有IP地址
                String timerStr = data.getTimer(connection, ipList);//查询IP对应的定时器时间

                int[] dbTimerInts = stringToArrays(timerStr);//取出时分秒
                boolean startIsRunning = main.getStatus();//获取是否有正在运行的任务

                if (!Arrays.equals(dbTimerInts, currentTimerInts) && !startIsRunning) {
                    main.closeTimer();//关闭原先定时器
                    main.executeTimer(dbTimerInts[0], dbTimerInts[1], dbTimerInts[2]);//执行新的定时器
                    currentTimerInts = dbTimerInts;
                }
                Thread.sleep(1000 * 3);
            }catch (Exception ignore){}
        }*/

    }

    /**
     * 转换定时器时间格式为整形数组
     * @param str 字符串时间
     * @return 整型数组,int[0]:时,int[1]:分,int[2]:秒
     */
    /*private int[] stringToArrays(String str){

        int[] timers = new int[3];

        try {
            //str = str.substring(11,19);
            String[] strings = str.split(":");
            for (int i = 0;i < 3;i ++){
                timers[i] = Integer.valueOf(strings[i]);
            }
        }catch (Exception e){
            timers[0] = defaultTimerInts[0];
            timers[1] = defaultTimerInts[1];
            timers[2] = defaultTimerInts[2];
        }

        return timers;
    }*/
    /**
     * 关闭获取定时器时间
     */
    public void close(){
        isRunning = false;
    }
}
