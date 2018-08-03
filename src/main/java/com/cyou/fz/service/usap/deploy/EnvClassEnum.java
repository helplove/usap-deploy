package com.cyou.fz.service.usap.deploy;

/**
 * create by Li Bang Zhu on 2018/8/3
 */

public enum EnvClassEnum {

    DEV("开发环境"),
    TEST("测试环境"),
    PRO("线上环境")
    ;


    public String getClassName() {
        return ClassName;
    }

    private String ClassName;

    EnvClassEnum(String className) {
        ClassName = className;
    }
}
