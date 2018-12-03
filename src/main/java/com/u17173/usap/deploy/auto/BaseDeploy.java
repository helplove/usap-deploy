package com.u17173.usap.deploy.auto;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Li Bang Zhu
 * @date 2018/12/3
 */
public class BaseDeploy {
    private static final Log log = LogFactory.get(BaseDeploy.class);

    private static final String DEV = "dev";
    private static final String TEST = "test";
    private static final String PRODUCT = "product";

    protected static void deploy(String arg, EnvClassEnum envClassEnum) throws Exception {
        log.info("获取解析自定义参数******************************");
        JSONObject input;
        try {
            input = JSONUtil.parseObj(arg);
        }catch (Exception e) {
            throw new Exception("json解析自定义参数错误,params:" + arg + e);
        }
        String sessionId = getSessionId(envClassEnum.getUserName(), envClassEnum.getPassword(), envClassEnum.getUrlPre());

        String inf = "";
        //PHP应用
        if ((input.get("lang")).equals(1)) {
            //描述接口文件上传并获取对应文件识别码
            inf = Utils.getInf(envClassEnum, sessionId);
        }

        //获取原先参数
        JSONObject updateData = Utils.getProjectByAppId(""+ input.get("appId"),sessionId, envClassEnum);

        //执行创建或者更新任务
        Utils.execute(updateData, input, EnvClassEnum.DEV, sessionId, inf);
    }

    /**
     * 模拟登陆获取SOA系统的sessionId
     * @param userName
     * @param password
     * @param urlPre
     * @return
     */
    private static String getSessionId(String userName, String password, String urlPre) throws Exception {
        log.info("模拟登录获取sessionId******************************");
        try {
            Map loginMap =  new HashMap<String, Object>();
            loginMap.put("userName", userName);
            loginMap.put("password",password);
            HttpRequest loginRequest = HttpUtil.createPost(urlPre + "login2");
            loginRequest.form(loginMap);
            loginRequest = loginRequest.enableDefaultCookie();
            HttpResponse loginResponse = loginRequest.execute();
            JSONObject jsonObject = JSONUtil.parseObj(loginResponse.body());
            Map map = (Map)jsonObject.get("datas");
            String sessionId = (String) map.get("sessionId");
            return sessionId;
        }catch (Exception e) {
            throw new Exception("模拟登录获取sessionId发生异常：" + e);
        }
    }

    public static void main(String[] args) throws Exception {
        if (DEV.equals(args[1])) {
            deploy(args[0], EnvClassEnum.DEV);
        }else if (TEST.equals(args[1])) {
            deploy(args[0], EnvClassEnum.TEST);
        }else if (PRODUCT.equals(args[1])) {
            deploy(args[0], EnvClassEnum.PRO);
        }else {
            //不做任何处理
            log.error("环境变量书写错误：" + args[1].toString());
        }
    }
}
