package com.crazymouse.mmsserver;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Title ：
 * Description :
 * Create Time: 14-3-28 下午2:18
 */
public class DomBuilderUtil {

    public static List<String> buildReport(Mm7Deliver deliver) {
        List<String> result = new ArrayList<String>();
        Document document = new DefaultDocument();
        Namespace envSpace = new Namespace("env", "http://schemas.xmlsoap.org/soap/envelope/");
        Namespace mm7Space = new Namespace("mm7", "http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-6-MM7-1-0");
        Element envelope = new DefaultElement(new QName("Envelope", envSpace));
        Element header = new DefaultElement(new QName("Header", envSpace));
        Element body = new DefaultElement(new QName("Body", envSpace));
        Element transactionID = new DefaultElement(new QName("TransactionID", mm7Space));
        Element DeliveryReportReq = new DefaultElement(new QName("DeliveryReportReq", mm7Space));
        Element mm7Version = new DefaultElement("MM7Version");
        Element sender = new DefaultElement("Sender");
        Element recipient = new DefaultElement("Recipient");
        Element number = new DefaultElement("Number");
        Element timeStamp = new DefaultElement("TimeStamp");
        Element mMSRelayServerID = new DefaultElement("MMSRelayServerID");
        Element messageID = new DefaultElement("MessageID");
        Element mMStatus = new DefaultElement("MMStatus");
        Element statusText = new DefaultElement("StatusText");
        transactionID.setText(deliver.getTransactionID());
        mm7Version.setText(deliver.getMm7Version());
        sender.setText(deliver.getSender());
        timeStamp.setText(deliver.getTimStamp());
        mMSRelayServerID.setText(deliver.getRelayServerID());
        messageID.setText(deliver.getMessageID());
        mMStatus.setText(deliver.getMmstatus());
        statusText.setText(deliver.getStatusText());
        document.setRootElement(envelope);
        envelope.add(header);
        envelope.add(body);
        header.add(transactionID);
        body.add(DeliveryReportReq);
        DeliveryReportReq.add(mm7Version);
        DeliveryReportReq.add(sender);
        DeliveryReportReq.add(recipient);
        DeliveryReportReq.add(timeStamp);
        DeliveryReportReq.add(mMSRelayServerID);
        DeliveryReportReq.add(messageID);
        DeliveryReportReq.add(mMStatus);
        DeliveryReportReq.add(statusText);
        recipient.add(number);
        for (String s : deliver.getPhones()) {
            number.setText(s);
            result.add(document.asXML());
        }
        return result;
    }
}
