package com.zhouqianbin.demo.engine;

/**
 * @Copyright (C), 2018
 * @FileName: WakeResult
 * @Author: 周千滨
 * @Date: 2018/12/27 14:25
 * @Description:
 * @Version: 1.0.0
 * @UpdateHistory: 修改历史
 * @修改人: 周千滨
 * @修改描述: 创建文件
 */

public class WakeResult {

    //操作类型
    private String sst;
    //唤醒词id
    private String id;
    //得分
    private String score;
    //前端点
    private String bos;
    //尾端点
    private String eos;

    public String getSst() {
        return sst;
    }

    public void setSst(String sst) {
        this.sst = sst;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getBos() {
        return bos;
    }

    public void setBos(String bos) {
        this.bos = bos;
    }

    public String getEos() {
        return eos;
    }

    public void setEos(String eos) {
        this.eos = eos;
    }

    @Override
    public String toString() {
        return "WakeResult{" +
                "sst='" + sst + '\'' +
                ", id='" + id + '\'' +
                ", score='" + score + '\'' +
                ", bos='" + bos + '\'' +
                ", eos='" + eos + '\'' +
                '}';
    }
}
