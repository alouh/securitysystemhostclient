package com.youotech.scan;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import utils.InSensitiveSet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hanjiafeng
 * @date 2017/12/1下午 2:40
 * @desc 扫描端口
 */
public class Scanner {

	private static final Log LOGGER = LogFactory.getLog(Scanner.class);

	/**
	 * 检测udp端口是否开放
	 * @param udpSet 应开放udp端口list
	 * @return 未开放udp端口
	 */
	public Set<String> udpPortCheck(Set<String> udpSet) {
		DatagramSocket socket = null;
        Set<String> resultList = new HashSet<>();

		for (String port : udpSet) {
			try {
				int portInt = Integer.valueOf(port);
				socket = new DatagramSocket(portInt);
			} catch (NumberFormatException ignored){
			} catch (BindException e){
				resultList.add(port);//如果端口开放,存入list
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					Objects.requireNonNull(socket).close();
				} catch (Exception ignored) {
				}
			}
		}
		return resultList;
	}

	/**
	 * 检测tcp端口是否开放
	 * @param tcpSet 应开放tcp端口list
	 * @return 未开放tcp端口
	 */
	public Set<String> tcpPortCheck(Set<String> tcpSet) {
		ServerSocket serverSocket = null;
		Set<String> resultList = new HashSet<>();

		for (String port : tcpSet) {
			try {
				int portInt = Integer.valueOf(port);
				serverSocket = new ServerSocket(portInt);
			} catch (BindException e){
				resultList.add(port);//如果端口开放,存入list
			} catch (Exception e){
				e.printStackTrace();
			}finally {
				try {
					Objects.requireNonNull(serverSocket).close();
				}catch (Exception ignored){
				}
			}
		}
		return resultList;
	}

	/**
	 * 检测软件是否安装
	 * @param softPatchRuleSet 应安装软件列表
	 * @return 未安装软件列表
	 */
	public Set<String> softPatchCheck(Set<String> softPatchRuleSet){

        Set<String> resultSet = new HashSet<>();

        InSensitiveSet softPatchSet = getSoftPatchSet();//获取软件和补丁列表

        LOGGER.info("注册表软件和补丁信息:" + softPatchSet);

		for (String ruleStr : softPatchRuleSet){
			if (softPatchSet.contains(ruleStr)){
				resultSet.add(ruleStr);
			}
		}

		softPatchRuleSet.removeAll(resultSet);

		return softPatchRuleSet;
	}

    /**
     * 获取当前系统中已安装并且在注册表存在的软件和补丁
     * @return 软件和补丁名称列表
     */
	private InSensitiveSet getSoftPatchSet(){

        InSensitiveSet softPatchSet = new InSensitiveSet();

        Runtime runtime = Runtime.getRuntime();
        BufferedReader reader = null;
        Pattern pattern = Pattern.compile("kb[0-9]{0,20}", Pattern.CASE_INSENSITIVE);

        try{
            //获取软件列表
            Set<String> tempSoftSet = new HashSet<>();//临时软件set
            Process process = runtime.exec("reg query HKEY_LOCAL_MACHINE\\SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null){//获取注册表中所有记录
                tempSoftSet.add(line);
            }
            for (String regeditStr : tempSoftSet) {//检测每一条记录
                process = runtime.exec("reg query " + regeditStr);
                reader = new BufferedReader(new InputStreamReader(process.getInputStream(),"gbk"));
                while ((line = reader.readLine()) != null) {
                    if (line.contains("DisplayName")) {//获取有名称的记录,并存储名称
                        line = line.replace("    DisplayName    REG_SZ    ","");
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()){
                            line = matcher.group();
                        }
                        softPatchSet.add(line);
                    }
                }
            }
            //获取补丁列表
            process = runtime.exec("wmic qfe list full");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null){

                int fixIndex = line.indexOf("HotFixID=");
                if (fixIndex != -1){
                    line = line.substring(9);
                    softPatchSet.add(line);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                Objects.requireNonNull(reader).close();
            }catch (Exception ignore){}
        }

        return softPatchSet;
    }
}
