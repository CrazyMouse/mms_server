package com.crazymouse.mmsserver;

import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.*;
import org.apache.http.util.EntityUtils;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;


public class MmsServerMain {
    public static AtomicLong submitCount = new AtomicLong(0);
    public static void main(String[] args) throws Exception {

        // Set up the HTTP protocol processor
        HttpProcessor httpproc = HttpProcessorBuilder.create().add(new ResponseDate()).add(new ResponseServer("Test/1.1")).add(new ResponseContent()).add(new ResponseConnControl()).build();

        // Set up request handlers
        UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
        reqistry.register("*", new HttpMMsHandler());

        // Set up the HTTP service
        HttpService httpService = new HttpService(httpproc, reqistry);

        Thread t = new RequestListenerThread(8080, httpService);
        t.setDaemon(false);
        t.start();
    }

    static class HttpMMsHandler implements HttpRequestHandler {
        private static AtomicLong count = new AtomicLong(1000000000000L);
        private String prefix = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        private DeliverProcesser deliverProcesser = new DeliverProcesser();//todo

        public void setDeliverProcesser(DeliverProcesser deliverProcesser) {
            this.deliverProcesser = deliverProcesser;
        }

        @Override
        public void handle(org.apache.http.HttpRequest request, org.apache.http.HttpResponse response, org.apache.http.protocol.HttpContext context) throws HttpException, IOException {
            System.out.println("Do Handle:"+MmsServerMain.submitCount.incrementAndGet());
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (method.equals("GET")) {
                response.setStatusCode(HttpStatus.SC_OK);
                StringEntity entity = new StringEntity("<html><body><h1>服务正常</h1></body></html>", ContentType.create("text/html", "UTF-8"));
                response.setEntity(entity);
            }else if (method.equals("POST")) {
                if (request instanceof HttpEntityEnclosingRequest) {
                    HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                    ContentType contentType = ContentType.get(entity);
                    byte[] entityContent = EntityUtils.toByteArray(entity);
                    String content = new String(entityContent, 0, entityContent.length, "UTF-8");
                    String mm7Head = content.substring(content.indexOf("<env:Envelope"),
                            content.indexOf("</env:Envelope>") + 15);
                    SAXReader saxreader = new SAXReader();
                    try {
                        StringReader mm7HeadR = new StringReader(mm7Head);
                        org.dom4j.Document doc = saxreader.read(mm7HeadR);
                        Mm7Submit submit = processSubmitDoc(doc);

                        String messageID = processResp(response, submit);
                        //状态报告处理
//                        if(submit.isDeliveryReport()){
//                            processRpt(submit,messageID);
//                        }
                    } catch (DocumentException e) {
                        System.out.println("数据解析错误");
                    }
                    System.out.println("Incoming entity content (bytes): " + entityContent.length);
                }
            }else {
                throw new MethodNotSupportedException(method + " method not supported");
            }

        }

        private void processRpt(Mm7Submit submit, String messageID) {
            Mm7Deliver deliver = new Mm7Deliver();
            deliver.setTransactionID("0311000113163140328131646302");
            deliver.setMessageID(messageID);
            deliver.setSender(submit.getSendAddr()+submit.getServiceCode());
            deliver.getPhones().addAll(submit.getPhones());
            deliver.setTimStamp(Calendar.getInstance().getTime().toString());
            deliver.setRelayServerID("960001");
            deliver.setMmstatus("Retrieved");
            deliver.setStatusText("1000");
            deliver.setMm7Version("6.3.0");
            deliverProcesser.sendDeliver(deliver);

        }

        private String processResp(HttpResponse response, Mm7Submit submit) {
            response.addHeader(HTTP.CONN_KEEP_ALIVE,"timeout=3");
            Namespace envSpace = new Namespace("env","http://schemas.xmlsoap.org/soap/envelope/");
            Namespace mm7Space = new Namespace("mm7","http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-6-MM7-1-0");
            Document resp = DocumentFactory.getInstance().createDocument();
            Element root = new DefaultElement(new QName("Envelope",envSpace));
//            root.addNamespace("env","http://schemas.xmlsoap.org/soap/envelope/");
//            root.setQName(new QName());
            Element header = new DefaultElement(new QName("Header",envSpace));
            Element body = new DefaultElement(new QName("Body",envSpace));
            Element transactionID = new DefaultElement(new QName("TransactionID",mm7Space));
            Element submitRsp = new DefaultElement(new QName("SubmitRsp",mm7Space));
            Element mm7Version = new DefaultElement("MM7Version");
            Element status = new DefaultElement("Status");
            Element statusCode = new DefaultElement("StatusCode");
            Element statusText = new DefaultElement("StatusText");
            Element messageID = new DefaultElement("MessageID");
            transactionID.setText(submit.getTransactionID());
            mm7Version.setText("6.3.0");
            statusCode.setText("1000");
            statusText.setText("发送成功");
            messageID.setText(prefix+count.incrementAndGet());
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
            StringEntity respEntity = new StringEntity(resp.asXML(),"UTF-8");
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

    static class RequestListenerThread extends Thread {

        private final HttpConnectionFactory<DefaultBHttpServerConnection> connFactory;
        private final ServerSocket serversocket;
        private final HttpService httpService;

        public RequestListenerThread(final int port, final HttpService httpService) throws IOException {
            this.connFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
            this.serversocket = new ServerSocket(port);
            this.httpService = httpService;
        }

        @Override
        public void run() {
            System.out.println("Listening on port " + this.serversocket.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                    // Set up HTTP connection
                    Socket socket = this.serversocket.accept();
                    System.out.println("Incoming connection from " + socket.getInetAddress());
                    HttpServerConnection conn = this.connFactory.createConnection(socket);

                    // Start worker thread
                    Thread t = new WorkerThread(this.httpService, conn,false);
                    t.setDaemon(true);
                    t.start();
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    System.err.println("I/O error initialising connection thread: " + e.getMessage());
                    break;
                }
            }
        }
    }

    static class WorkerThread extends Thread {

        private final HttpService httpservice;
        private final HttpServerConnection conn;
        private final boolean closeConn;

        public WorkerThread(final HttpService httpservice, final HttpServerConnection conn,boolean closeConn) {
            super();
            this.httpservice = httpservice;
            this.conn = conn;
            this.closeConn = closeConn;
        }

        @Override
        public void run() {
            System.out.println("New connection thread");
            BasicHttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && this.conn.isOpen()) {
                    this.httpservice.handleRequest(this.conn, context);
                    if(closeConn){
                        conn.close();
                    }
                }
            } catch (ConnectionClosedException ex) {
                System.err.println("Client closed connection");
            } catch (IOException ex) {
                System.err.println("I/O error: " + ex.getMessage());
            } catch (HttpException ex) {
                System.err.println("Unrecoverable HTTP protocol violation: " + ex.getMessage());
            } finally {
                try {
                    this.conn.shutdown();
                } catch (IOException ignore) {}
            }
        }
    }
}