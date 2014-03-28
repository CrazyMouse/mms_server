package com.crazymouse.mmsserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Title ：
 * Description :
 * Create Time: 14-3-28 下午1:48
 */
public class Mm7Deliver {
    private String transactionID;
    private String mm7Version;
    private String sender;
    private List<String> phones = new ArrayList<String>();
    private String timStamp;
    private String relayServerID;
    private String messageID;
    private String mmstatus;
    private String statusText;

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public String getMm7Version() {
        return mm7Version;
    }

    public void setMm7Version(String mm7Version) {
        this.mm7Version = mm7Version;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public List<String> getPhones() {
        return phones;
    }

    public String getTimStamp() {
        return timStamp;
    }

    public void setTimStamp(String timStamp) {
        this.timStamp = timStamp;
    }

    public String getRelayServerID() {
        return relayServerID;
    }

    public void setRelayServerID(String relayServerID) {
        this.relayServerID = relayServerID;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getMmstatus() {
        return mmstatus;
    }

    public void setMmstatus(String mmstatus) {
        this.mmstatus = mmstatus;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
}
