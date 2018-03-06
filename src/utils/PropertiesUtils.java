package utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {

    private static final Log LOGGER = LogFactory.getLog(PropertiesUtils.class);

	private static String mySqlIp_port;
	private static String mySqlDatabase;
	private static String mySqlUsr;
	private static String mySqlPwd;
	private static String mySqlDriverName;

	private static String timeHour;
	private static String timeMinute;
	private static String timeSecond;
	private static String period;

	private static String osName;
	private static String osArch;
	static {
        Properties properties = new Properties();
        InputStream inputStream = PropertiesUtils.class.getResourceAsStream("/resource/jdbc.properties");
        try {
            properties.load(inputStream);

	        mySqlIp_port = properties.getProperty("MySQL_ip_port");
	        mySqlDatabase = properties.getProperty("MySQL_database");
	        mySqlUsr = properties.getProperty("MySQL_usr");
	        mySqlPwd = properties.getProperty("MySQL_pwd");
	        mySqlDriverName = properties.getProperty("MySQL_driverClassName");

	        timeHour = properties.getProperty("Time_hour");
	        timeMinute = properties.getProperty("Time_minute");
	        timeSecond = properties.getProperty("Time_second");
	        period = properties.getProperty("period");

	        osName = properties.getProperty("OS_name");
	        osArch = properties.getProperty("OS_arch");
        } catch (Exception e) {
	        LOGGER.info("错误信息:" + e.toString());
        }
    }

	public static String getMySqlIp_port(){
		return mySqlIp_port;
	}
	public static String getMySqlDatabase(){
		return mySqlDatabase;
	}
	public static String getMySqlUsr(){
		return mySqlUsr;
	}
	public static String getMySqlPwd(){
		return mySqlPwd;
	}
	public static String getMySqlDriverName(){
		return mySqlDriverName;
	}

	public static int getTimeHour(){
		return timeFormat(timeHour,23);
	}
	public static int getTimeMinute(){
		return timeFormat(timeMinute,59);
	}
	public static int getTimeSecond() {
		return timeFormat(timeSecond,59);
	}
	public static int getPeriod() {
		return timeFormat(period,Integer.MAX_VALUE);
	}

	private static int timeFormat(String field, int max){
		int t = 0;
		try {
			t = Integer.valueOf(field);
			if (t < 0 || t > max){
				throw new Exception("格式不正确");
			}
		}catch (Exception e){
			LOGGER.info("定时器参数不正确,请检查格式:0<=Time_hour<=23,0<=Time_minute<=59,0<=Time_second<=59");
		}
		return t;
	}

	public static String getOsName() {
		return osName;
	}
	public static String getOsArch() {
		return osArch;
	}
}
