package com.crazymouse.mmsserver.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Title ：
 * Description :
 * Create Time: 14-3-27 下午4:09
 */
public class Mm7Submit {
    private String transactionID;
    private boolean deliveryReport;
    private String subject;
    private String serviceCode;
    private  String vaspId;
    private  String vasId;
    private  String sendAddr;

    private List<String> phones = new ArrayList<String>();

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public boolean isDeliveryReport() {
        return deliveryReport;
    }

    public void setDeliveryReport(boolean deliveryReport) {
        this.deliveryReport = deliveryReport;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getVaspId() {
        return vaspId;
    }

    public void setVaspId(String vaspId) {
        this.vaspId = vaspId;
    }

    public String getVasId() {
        return vasId;
    }

    public void setVasId(String vasId) {
        this.vasId = vasId;
    }

    public String getSendAddr() {
        return sendAddr;
    }

    public void setSendAddr(String sendAddr) {
        this.sendAddr = sendAddr;
    }

    public List<String> getPhones() {
        return phones;
    }
}
