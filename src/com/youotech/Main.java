package com.youotech;

import com.youotech.database.Data;
import com.youotech.scan.Scanner;
import com.youotech.timer.TimerThread;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import utils.PropertiesUtils;
import utils.Tools;

import java.sql.Connection;
import java.sql.Time;
import java.util.*;

import static com.youotech.timer.Timer.SINGER_IP_TIMER_UNUSED;
import static com.youotech.timer.Timer.SINGER_IP_TIMER_USED;
import static com.youotech.timer.TimerThread.SYNCHRONIZED_FLAG;

public class Main{

    private static final Log LOGGER = LogFactory.getLog(Main.class);

    private TimerTask timerTask;
    

    public static void main(String[] args) {

        TimerThread timerThread = new TimerThread();
        timerThread.run();
    }

    /**
     * 执行定时器任务
     */
    public void executeTimer(com.youotech.timer.Timer ipTimer){

        Time time = ipTimer.getTime();
        LOGGER.info("定时器时间:" + time.toString());

        //执行任务时间
	    Calendar calendar = Calendar.getInstance();
	    calendar.set(Calendar.HOUR_OF_DAY, time.getHours());// 控制时
	    calendar.set(Calendar.MINUTE, time.getMinutes());// 控制分
	    calendar.set(Calendar.SECOND, time.getSeconds());// 控制秒
	    Date date = calendar.getTime();
	    Timer timer = new Timer();
	    timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    new Main().start(ipTimer);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
	    timer.scheduleAtFixedRate(timerTask, date,1000 * PropertiesUtils.getPeriod());
    }

    /**
     * 取消定时器任务
     */
    public void closeTimer(){
        try {
            if (timerTask != null) {
                LOGGER.info("定时器任务已取消");
                timerTask.cancel();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void start(com.youotech.timer.Timer timer){

        Data data = new Data();
	    Connection connection = data.getConnection();//获取数据库连接

	    /*String osName = PropertiesUtils.getOsName();//操作系统名称
	    String osArch = PropertiesUtils.getOsArch();//操作系统构架
	    LOGGER.info(String.format("本机操作系统名称:%s,架构:%s",osName,osArch));

	    //获取本机设备表中的ID
	    int sdId = -1;
	    List<String> ipList = Tools.getAllIp();
	    for (String ip : ipList){
	        sdId = data.selectSdId(connection,ip);
	        if (sdId != -1){
	            break;
            }
        }

	    LOGGER.info("本机在设备表中的ID:" + sdId);
	    if (sdId == -1){
	    	LOGGER.error("设备表中无此设备信息");
	    	data.closeConnection(connection);
	    	return;
	    }

	    //规则list,arg0:tcpList,arg1:udpList,arg2:softList
	    List<Set<String>> ruleLists = data.selectRules(connection,osName,osArch);
	    LOGGER.info(String.format("TCP端口规则:%s\r\nUDP端口规则:%s\r\n软件规则:%s",ruleLists.get(0),ruleLists.get(1),ruleLists.get(2)));

	    Scanner scanner = new Scanner();
	    //检测TCP端口
	    List<String> errTcpList = scanner.tcpPortCheck(ruleLists.get(0));
	    LOGGER.info("异常TCP端口:" + errTcpList);
	    data.insertResult(connection,errTcpList,sdId,0);
	    //检测UDP端口
	    List<String> errUdpList = scanner.udpPortCheck(ruleLists.get(1));
	    LOGGER.info("异常UDP端口:" + errUdpList);
	    data.insertResult(connection,errUdpList,sdId,1);
	    //检测软件
	    Set<String> tempSoftRuleSet = ruleLists.get(2);
	    Set<String> softRuleSet = new HashSet<>();
	    for (String str : tempSoftRuleSet){
		    softRuleSet.add(str.toUpperCase());
	    }
	    List<String> errSoftList = scanner.softCheck(softRuleSet);

	    LOGGER.info("异常软件列表:" + errSoftList);
	    data.insertResult(connection,errSoftList,sdId,2);

	    StringBuilder logStr = new StringBuilder(Tools.getHostName());
	    logStr.append(":违规TCP端口:");
        for (String t:errTcpList){
            logStr.append(t).append(",");
        }
        logStr.append("违规UDP端口:");
        for (String u:errUdpList){
            logStr.append(u).append(",");
        }
        logStr.append("违规软件和补丁:");
        for (String s:errSoftList){
            logStr.append(s).append(",");
        }
	    data.insertLog(connection,"1",logStr.toString());*/

	    System.out.println("-----------运行-----------");

        if (timer.getFlag() == SINGER_IP_TIMER_UNUSED) {
            synchronized (SYNCHRONIZED_FLAG) {
                TimerThread.flag = SINGER_IP_TIMER_USED;
            }
            //更新指定ip标志
            data.updateSingleIpTimer(connection,timer.getIp());
        }

        data.closeConnection(connection);//关闭数据库连接
    }
}
