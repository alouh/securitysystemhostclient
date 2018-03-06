package com.youotech.timer;

import java.sql.Time;

/**
 * @Author: HanJiafeng
 * @Date: 11:16 2018/3/6
 * @Desc:
 */
public class Timer {

    public static final byte SINGER_IP_TIMER_UNUSED = 0;//未使用的单个IP定时器
    public static final byte SINGER_IP_TIMER_USED = 1;//已使用的单个IP定时器
    public static final byte ALL_IP_TIMER = 2;//所有IP定时器

    private String ip;//ip
    private Time time;//定时器数组
    private byte flag;//标志位

    /**
     * 默认定时器
     */
    public Timer(){
        this.ip = "*";
        this.time = new Time(-25200000);//默认凌晨一点
        this.flag = ALL_IP_TIMER;
    }

    public Timer(String ip,Time timer){
        this.ip = ip;
        this.time = timer;
    }

    public byte getFlag() {
        return flag;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time timer) {
        this.time = timer;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
