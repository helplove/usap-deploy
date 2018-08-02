package com.cyou.fz.service.usap.deploy;


public class Constants {
    //开发环境SOA平台
    //http://10.5.121.174/
    //http://localhost:8080/
    private static final String DEV_URL_PRE = "http://10.5.121.174/";

    //测试环境SOA平台
    private static final String TEST_URL_PRE = "http://10.59.120.16:8080/soamanage/";


    //线上环境SOA平台
    private static String PRO_URL_PRE = "http://soa.internal.17173.com/";


    public static String getDevUrlPre() {
        return DEV_URL_PRE;
    }

    public static String getTestUrlPre() {
        return TEST_URL_PRE;
    }

    public static String getProUrlPre() {
        return PRO_URL_PRE;
    }

    public static void setProUrlPre(String proUrlPre) {
        PRO_URL_PRE = proUrlPre;
    }
}
