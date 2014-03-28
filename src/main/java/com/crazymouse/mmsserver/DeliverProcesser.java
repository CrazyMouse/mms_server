package com.crazymouse.mmsserver;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.List;

/**
 * Title ：
 * Description :
 * Create Time: 14-3-28 下午2:12
 */
public class DeliverProcesser {
    private ConfigUtil configUtil;

    public DeliverProcesser() {
        configUtil= new ConfigUtil();
        configUtil.setConfig("url","http://localhost:8099");//todo
    }

    public void sendDeliver(Mm7Deliver deliver) {
        HttpPost httpPost = new HttpPost(configUtil.getConfig("url"));
        httpPost.addHeader("Content-Type", "text/xml;charset=\"UTF-8\"");
        httpPost.addHeader("Connection", "keep-alive");
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(256);
        cm.setDefaultMaxPerRoute(32);
        HttpClient httpClient = HttpClients.createMinimal(cm);
        HttpContext context = new BasicHttpContext();
        List<String> reportXmls = DomBuilderUtil.buildReport(deliver);
        for (String reportXml : reportXmls) {

            StringEntity entity = new StringEntity(reportXml, "UTF-8");
            httpPost.setEntity(entity);
            try {
                httpClient.execute(httpPost,context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
