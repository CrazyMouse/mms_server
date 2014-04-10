package com.crazymouse.mmsserver;

import com.crazymouse.mmsserver.Entity.Mm7Deliver;
import com.crazymouse.mmsserver.Entity.Mm7Submit;
import com.crazymouse.mmsserver.util.ConfigUtil;
import com.crazymouse.mmsserver.util.DomBuilderUtil;
import com.crazymouse.mmsserver.util.Statistic;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * Title ：状态报告处理
 * Description :用httpclient发送送达报告
 * Create Time: 14-3-28 下午2:12
 */
public class DeliverProcesser {
    private final static Logger logger = LoggerFactory.getLogger(DeliverProcesser.class);
    private ConfigUtil configUtil;
    private PoolingHttpClientConnectionManager connectionManager;
    private HttpClient httpClient;

    public void init() {
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(256);
        connectionManager.setDefaultMaxPerRoute(256);
        httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
    }

    public void sendDeliver(Mm7Deliver deliver) {
        HttpPost httpPost = new HttpPost(configUtil.getConfig("url"));
        httpPost.addHeader("Content-Type", "text/xml;charset=\"UTF-8\"");
        httpPost.addHeader("Connection", "keep-alive");

        HttpContext context = new BasicHttpContext();
        List<String> reportXmls = DomBuilderUtil.buildReport(deliver);
        for (String reportXml : reportXmls) {

            StringEntity entity = new StringEntity(reportXml, "UTF-8");
            httpPost.setEntity(entity);
            try {
                HttpResponse response = httpClient.execute(httpPost, context);
                EntityUtils.toByteArray(response.getEntity());
                Statistic.addDeliver();
            } catch (IOException e) {
                logger.error("状态报告发送异常:{}", e);
            }
        }
    }

    public void setConfigUtil(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    public void processRpt(Mm7Submit submit, String messageID) {
        boolean focusCloseDeliver = Boolean.parseBoolean(configUtil.getConfig("FocusCloseDeliver"));
        if (focusCloseDeliver) {
            return;
        }
        Mm7Deliver deliver = new Mm7Deliver();
        deliver.setTransactionID("0311000113163140328131646302");
        deliver.setMessageID(messageID);
        deliver.setSender(submit.getSendAddr() + submit.getServiceCode());
        deliver.getPhones().addAll(submit.getPhones());
        deliver.setTimStamp(Calendar.getInstance().getTime().toString());
        deliver.setRelayServerID("960001");
        deliver.setMmstatus("Retrieved");
        deliver.setStatusText("1000");
        deliver.setMm7Version("6.3.0");
        sendDeliver(deliver);
    }
}
