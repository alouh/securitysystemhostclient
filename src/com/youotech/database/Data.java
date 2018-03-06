package com.youotech.database;

import com.youotech.timer.Timer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import utils.PropertiesUtils;

import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * @author hanjiafeng
 * @date 2017/12/1下午 3:43
 * @desc 数据库操作类
 */
public class Data {


    private final Log LOGGER = LogFactory.getLog(Data.class);

	public Connection getConnection() {

	    Connection connection = null;

		LOGGER.info("建立数据库连接");
		String usr = PropertiesUtils.getMySqlUsr();
		String pwd = PropertiesUtils.getMySqlPwd();
		String ip_port = PropertiesUtils.getMySqlIp_port();
		String database = PropertiesUtils.getMySqlDatabase();
		String driverName = PropertiesUtils.getMySqlDriverName();

		String url = String.format("jdbc:mysql://%s/%s?useUnicode=true&characterEncoding=utf-8&useSSL=false",ip_port,database);
		try {
			Class.forName(driverName);
			connection = DriverManager.getConnection(url,usr,pwd);
			LOGGER.info("数据库连接已建立:" + connection);
		}catch (Exception e){
			LOGGER.info(String.format("获取数据库连接异常:url(%s),user(%s),password(%s)",url,usr,pwd));
			LOGGER.error("错误信息:" + e.toString());
		}

		return connection;
	}

	/**
	 * 根据操作系统类型获取规则
	 * @param os 操作系统名称
	 * @param bit 操作系统架构
	 */
	public List<Set<String>> selectRules(Connection connection, String os, String bit){
		Statement statement = null;
		ResultSet resultSet = null;
		String sqlStr = String.format("SELECT SR_TYPE,SR_RNAME FROM SE_RULES WHERE SD_OS='%s' AND SD_OSTYPE='%s'", os, bit);
		Set<String> tcpList = new HashSet<>();
		Set<String> udpList = new HashSet<>();
		Set<String> softList = new HashSet<>();

		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlStr);
			while (resultSet.next()){
				String srType = resultSet.getString(1);
				String srName = resultSet.getString(2);
				switch (srType) {
					case "0":
						tcpList.add(srName);
						break;
					case "1":
						udpList.add(srName);
						break;
					default:
						softList.add(srName);
						break;
				}
			}
		}catch (Exception e){
			LOGGER.info("错误信息:" + e.toString());
		}finally {
            try {
                Objects.requireNonNull(resultSet).close();
            }catch (Exception e){
                e.printStackTrace();
            }
			try {
				Objects.requireNonNull(statement).close();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return Arrays.asList(tcpList,udpList,softList);
	}

    /**
     * 删除当天的扫描结果,插入新的扫描结果
     * @param connection
     * @param list
     * @param sdId
     * @param flag
     */
	public void insertResult(Connection connection, List<String> list,int sdId,int flag){

		Statement deleteStatement = null;
		int flag1 = flag;
		if (flag != 2){
		    flag1 = 1;
        }
		String sqlStr = "DELETE FROM SE_RESULT WHERE SD_ID = " + sdId + " AND ST_DATE='" + new Date(System.currentTimeMillis()) + "' AND SR_RULES=" + flag1 ;
		try {
            deleteStatement = connection.createStatement();
            if (flag == 0 || flag == 2) {
                deleteStatement.executeUpdate(sqlStr);
            }
        }catch (Exception e){
            LOGGER.info("错误信息:" + e.toString());
        }finally {
            try {
                Objects.requireNonNull(deleteStatement).close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (list.size() == 0){
            return;
        }
        PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("INSERT INTO SE_RESULT(SD_ID,SR_TYPE,ST_REASON,ST_DATE,SD_TYPE,SR_RNAME,SR_RULES)VALUES (?,?,?,?,?,?,?)");
			for (String s : list){
				statement.setInt(1,sdId);
				if (flag == 2) {
					if (s.contains("KB") || s.contains("kb")) {
						statement.setInt(2, 3);
						statement.setString(3, "补丁未安装");
					} else {
						statement.setInt(2, 2);
						statement.setString(3, "软件未安装");
					}
					statement.setInt(7,2);
				}else{
					statement.setInt(2, flag);
					statement.setString(3, "端口已经开放");
					statement.setInt(7,1);
				}
				statement.setDate(4,new Date(System.currentTimeMillis()));
				statement.setString(5,"主机设备");
				statement.setString(6,s);
				statement.addBatch();
			}
			statement.executeBatch();
			LOGGER.info("成功上传设备异常信息,标志位:" + flag + ",异常内容:" + list);
		}catch (Exception e){
			LOGGER.info("错误信息:" + e.toString());
		}finally {
			try {
				Objects.requireNonNull(statement).close();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	/**
	 * 根据IP地址查询本机在设备表中的ID
	 * @return id
	 */
	public int selectSdId(Connection connection, String ip){

		Statement statement = null;
		ResultSet resultSet = null;
		int sdId = -1;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT SD_ID FROM SE_DEVICE WHERE SD_TYPE = '主机设备' AND SD_IP='" + ip + "'");
			if (resultSet.next()){
				sdId = resultSet.getInt(1);
			}
		}catch (Exception e){
			LOGGER.info("错误信息:" + e.toString());
		}finally {
            try {
                Objects.requireNonNull(resultSet).close();
            }catch (Exception e){
                e.printStackTrace();
            }
			try {
				Objects.requireNonNull(statement).close();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return sdId;
	}

    /**
     * 获取IP对应的定时器时间
     * @param connection 数据库连接
     * @param ips 本地所有IP地址
     * @return 定时器时间
     */
	public Timer getTimer(Connection connection,List<String> ips) {

        Statement statement = null;
        ResultSet resultSet = null;
        List<Timer> timerList = new ArrayList<>();

        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM SE_SCANCONFIGURATION WHERE SC_FLAG = 0");
            while (resultSet.next()) {
                String ip = resultSet.getString("SD_IP");
                Time time = resultSet.getTime("SC_DATE");

                Timer timer = new Timer(ip, time);
                timerList.add(timer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Objects.requireNonNull(resultSet).close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Objects.requireNonNull(statement).close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Timer allIpTimer = new Timer();//如果定时器表中没有所有IP的定时器,使用默认定时器
        for (Timer timer : timerList) {
            String ip = timer.getIp();
            if (ips.contains(ip)) {
                timer.setFlag(Timer.SINGER_IP_TIMER_UNUSED);
                return timer;
            }
            if (ip.equals("*")) {
                timer.setFlag(Timer.ALL_IP_TIMER);
                allIpTimer = timer;
            }
        }

        return allIpTimer;
    }

    /**
     * 更新用过的IP定时器
     * @param connection 数据库连接
     * @param ip 目的IP地址
     */
    public void updateSingleIpTimer(Connection connection,String ip) {

        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate("UPDATE SE_SCANCONFIGURATION SET SC_FLAG = 1 WHERE SD_IP = '" + ip + "'");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                Objects.requireNonNull(statement).close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 插入日志
     * @param connection 数据库连接
     * @param type 日志类型
     * @param logStr 日志内容
     */
    public void insertLog(Connection connection,String type,String logStr){

	    Statement statement = null;
	    String sqlStr = "insert into SE_LOG (SL_TYPE,SL_COUNT,SL_DATE)value('" + type + "','" + logStr + "' ,'" + new Timestamp(System.currentTimeMillis()) + "')";
	    try {
	        statement = connection.createStatement();
	        statement.executeUpdate(sqlStr);
        }catch (Exception e){
	        e.printStackTrace();
        }finally {
	        try {
	            Objects.requireNonNull(statement).close();
            }catch (Exception e){
	            e.printStackTrace();
            }
        }
    }

	/**
	 * 关闭数据库连接
	 */
	public void closeConnection(Connection connection){
		try {
			connection.close();
			LOGGER.info(connection + "已关闭");
		}catch (Exception e){
			LOGGER.info(e.toString());
		}
	}
}
