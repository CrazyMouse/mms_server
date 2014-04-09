package com.crazymouse.mmsserver;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Title ：
 * Description :
 * Create Time: 14-4-4 下午2:25
 */
public class MmsMain {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:/conf/applicationContext.xml");
    }
}
