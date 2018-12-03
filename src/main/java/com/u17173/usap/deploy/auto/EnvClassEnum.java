package com.u17173.usap.deploy.auto;

/**
 * @author Li Bang Zhu
 * @date 2018/12/3
 */
public enum EnvClassEnum {

    /**
     *开发环境
     */
    DEV("开发环境", "http://10.5.121.174/", "admin", "111111"),
    /**
     *测试环境
     */
    TEST("测试环境", "http://10.59.120.16:8080/soamanage/", "admin", "111111"),
    /**
     *线上环境
     */
    PRO("线上环境", "http://soa.internal.17173.com/", "admin", "soa@173")
    ;




    private String className;

    private String urlPre;

    private String userName;

    private String password;

    EnvClassEnum(String className, String urlPre, String userName, String password) {
        this.className = className;
        this.urlPre = urlPre;
        this.userName = userName;
        this.password = password;
    }

    public String getUrlPre() {
        return urlPre;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
