package com.youotech.scan;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
	public List<String> udpPortCheck(Set<String> udpSet) {
		DatagramSocket socket = null;
		List<String> resultList = new ArrayList<>();

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
	public List<String> tcpPortCheck(Set<String> tcpSet) {
		ServerSocket serverSocket = null;
		List<String> resultList = new ArrayList<>();

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
	 * @param softSet 应安装软件列表
	 * @return 未安装软件列表
	 */
	public List<String> softCheck(Set<String> softSet){
		List<String> resultList = new ArrayList<>();
		StringBuilder softStr = new StringBuilder();
		Runtime runtime = Runtime.getRuntime();
		BufferedReader reader = null;
		try {
			Process process = runtime.exec("reg query HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\Installer\\Products");
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null){
				BufferedReader reader1 = null;
				try {
					String queryStr = "reg query " + line + " /v ProductName";
					Process process1 = runtime.exec(queryStr);
					reader1 = new BufferedReader(new InputStreamReader(process1.getInputStream()));
					reader1.readLine();
					reader1.readLine();
					String line1;
					while ((line1 = reader1.readLine()) != null){
						String softName = line1.replace("    ProductName    REG_SZ    ","").toUpperCase();
						softStr.append(softName).append(",,");
					}
				}catch (Exception e){
					e.printStackTrace();
				}finally {
					try {
						Objects.requireNonNull(reader1).close();
					}catch (Exception ignored){
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			try {
				Objects.requireNonNull(reader).close();
			}catch (Exception ignored){
			}
		}
        LOGGER.info("注册表软件信息:" + softStr);
		for (String s : softSet){
			if (!softStr.toString().contains(s)){
				resultList.add(s);
			}
		}
		return resultList;
	}
}
