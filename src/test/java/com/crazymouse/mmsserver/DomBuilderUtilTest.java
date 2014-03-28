package com.crazymouse.mmsserver;

import junit.framework.Assert;

import java.util.List;

/**
 * Title ：
 * Description :
 * Create Time: 14-3-28 下午2:43
 */
public class DomBuilderUtilTest {

    @org.junit.Test
    public void testBuildReport() throws Exception {
        Mm7Deliver deliver = new Mm7Deliver();
        deliver.getPhones().add("13711112222");
        deliver.getPhones().add("13898999999");
        deliver.setTransactionID("1111");
        deliver.setMm7Version("000000");
        deliver.setSender("343434");
        deliver.setTimStamp("dfasfs");
        deliver.setRelayServerID("sfsafsa");
        deliver.setMessageID("dfasfd");
        deliver.setMmstatus("dsfsa");
        deliver.setStatusText("1000");
        List<String> list = DomBuilderUtil.buildReport(deliver);
        for (String str : list) {
            System.out.println(str);
        }
        Assert.assertTrue(list.size()==2);
    }
}
