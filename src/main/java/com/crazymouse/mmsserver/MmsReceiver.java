package com.crazymouse.mmsserver;

import com.crazymouse.mmsserver.util.ConfigUtil;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;


public class MmsReceiver {
    private final static Logger logger = LoggerFactory.getLogger(MmsReceiver.class);
    private ConfigUtil configUtil;
    private HttpMMsHandler mMsHandler;

    public void setmMsHandler(HttpMMsHandler mMsHandler) {
        this.mMsHandler = mMsHandler;
    }

    public void setConfigUtil(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    public void doServer() {
        HttpProcessor httpproc = HttpProcessorBuilder.create().add(new ResponseDate()).add(new ResponseServer("MmsServer/1.1")).add(new ResponseContent()).add(new ResponseConnControl()).build();

        UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
        reqistry.register("*", mMsHandler);

        HttpService httpService = new HttpService(httpproc, reqistry);

        try {
            Thread t = new RequestListenerThread(Integer.valueOf(configUtil.getConfig("ListenPort")), httpService);
            t.setDaemon(false);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class RequestListenerThread extends Thread {

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
            logger.info("Listening on port：【{}】", this.serversocket.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                    Socket socket = this.serversocket.accept();
                    logger.info("Received Connection From:{}", socket.getInetAddress());
                    HttpServerConnection conn = this.connFactory.createConnection(socket);
                    Thread t = new WorkerThread(this.httpService, conn, false);
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

        public WorkerThread(final HttpService httpservice, final HttpServerConnection conn, boolean closeConn) {
            super();
            this.httpservice = httpservice;
            this.conn = conn;
            this.closeConn = closeConn;
        }

        @Override
        public void run() {
            BasicHttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && this.conn.isOpen()) {
                    this.httpservice.handleRequest(this.conn, context);
                    if (closeConn) {
                        conn.close();
                    }
                }
            } catch (ConnectionClosedException ex) {
                logger.info("【Client closed connection】");
            } catch (IOException ex) {
                logger.error("I/O error: " + ex.getMessage());
            } catch (HttpException ex) {
                logger.error("Unrecoverable HTTP protocol violation: " + ex.getMessage());
            } finally {
                try {
                    this.conn.shutdown();
                } catch (IOException ignore) {}
            }
        }
    }
}