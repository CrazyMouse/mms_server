package com.crazymouse.mmsserver;

import com.crazymouse.mmsserver.Entity.Mm7Submit;
import com.crazymouse.mmsserver.util.Statistic;
import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class HttpMMsHandler implements HttpRequestHandler {
    private final static Logger logger = LoggerFactory.getLogger(HttpMMsHandler.class);
    private static final AtomicLong count = new AtomicLong(1000000000000L);
    private final String prefix = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
    private DeliverProcesser deliverProcesser ;


    public void setDeliverProcesser(DeliverProcesser deliverProcesser) {
        this.deliverProcesser = deliverProcesser;
    }

    @Override
    public void handle(org.apache.http.HttpRequest request, org.apache.http.HttpResponse response, org.apache.http.protocol.HttpContext context) throws HttpException, IOException {
        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (method.equals("GET")) {
            response.setStatusCode(HttpStatus.SC_OK);
            StringEntity entity = new StringEntity("<html><body><h1>服务正常</h1></body></html>", ContentType.create("text/html", "UTF-8"));
            response.setEntity(entity);
        }else if (method.equals("POST")) {
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                byte[] entityContent = EntityUtils.toByteArray(entity);
                String content = new String(entityContent, 0, entityContent.length, "UTF-8");
                String mm7Head = content.substring(content.indexOf("<env:Envelope"),
                        content.indexOf("</env:Envelope>") + 15);
                SAXReader saxreader = new SAXReader();
                try {
                    StringReader mm7HeadR = new StringReader(mm7Head);
                    org.dom4j.Document doc = saxreader.read(mm7HeadR);
                    Mm7Submit submit = processSubmitDoc(doc);
                    Statistic.addSubmit();
                    String messageID = processResp(response, submit);
                    if (submit.isDeliveryReport()) {
                        deliverProcesser.processRpt(submit, messageID);
                    }
                } catch (DocumentException e) {
                    logger.error("数据解析错误:{}", e);
                }
            }
        }else {
            throw new MethodNotSupportedException(method + " method not supported");
        }
    }

    private String processResp(HttpResponse response, Mm7Submit submit) {
        response.addHeader(HTTP.CONN_KEEP_ALIVE, "timeout=3");
        Namespace envSpace = new Namespace("env", "http://schemas.xmlsoap.org/soap/envelope/");
        Namespace mm7Space = new Namespace("mm7", "http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-6-MM7-1-0");
        Document resp = DocumentFactory.getInstance().createDocument();
        Element root = new DefaultElement(new QName("Envelope", envSpace));
        Element header = new DefaultElement(new QName("Header", envSpace));
        Element body = new DefaultElement(new QName("Body", envSpace));
        Element transactionID = new DefaultElement(new QName("TransactionID", mm7Space));
        Element submitRsp = new DefaultElement(new QName("SubmitRsp", mm7Space));
        Element mm7Version = new DefaultElement("MM7Version");
        Element status = new DefaultElement("Status");
        Element statusCode = new DefaultElement("StatusCode");
        Element statusText = new DefaultElement("StatusText");
        Element messageID = new DefaultElement("MessageID");
        transactionID.setText(submit.getTransactionID());
        mm7Version.setText("6.3.0");
        statusCode.setText("1000");
        statusText.setText("发送成功");
        messageID.setText(prefix + count.incrementAndGet());
        resp.setRootElement(root);
        root.add(header);
        root.add(body);
        header.add(transactionID);
        body.add(submitRsp);
        submitRsp.add(mm7Version);
        submitRsp.add(status);
        submitRsp.add(messageID);
        status.add(statusCode);
        status.add(statusText);
        StringEntity respEntity = new StringEntity(resp.asXML(), "UTF-8");
        response.setEntity(respEntity);
        return messageID.getText();
    }

    private Mm7Submit processSubmitDoc(Document doc) {
        Mm7Submit submit = new Mm7Submit();
        submit.setTransactionID(doc.getRootElement().element("Header").element("TransactionID").getTextTrim());
        Element submitReq = doc.getRootElement().element("Body").element("SubmitReq");
        submit.setDeliveryReport("true".equalsIgnoreCase(submitReq.elementTextTrim("DeliveryReport")));
        submit.setSubject(submitReq.elementTextTrim("Subject"));
        submit.setServiceCode(submitReq.elementTextTrim("ServiceCode"));
        Element senderIdentification = submitReq.element("SenderIdentification");
        submit.setVaspId(senderIdentification.elementTextTrim("VASPID"));
        submit.setVasId(senderIdentification.elementTextTrim("VASID"));
        submit.setSendAddr(senderIdentification.elementTextTrim("SenderAddress"));
        Element recipients = submitReq.element("Recipients");
        List<Element> phones = recipients.element("To").elements();
        for (Element phone : phones) {
            submit.getPhones().add(phone.getTextTrim());
        }
        return submit;
    }
}