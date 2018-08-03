package com.cyou.fz.service.usap.deploy;

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
 * create by Li Bang Zhu on 2018/8/1
 */
public class ProMain {

    private static final Log log = LogFactory.get(ProMain.class);

    /**
     *
     * @param args
     *
     */
    public static void main(String[] args) throws Exception {
        log.info("获取解析自定义参数----------------------");
        JSONObject input = JSONUtil.parseObj(args[0]);
        if (input.get("url_pre") != null) {
            Constants.setProUrlPre("http://" + (String) input.get("url_pre") + "/");
        }
        String sessionId = getSessionId();

        String inf = "";
        //PHP应用
        //当处于测试线上环境时不需要进行描述文件打成JAR包上传
        if ((input.get("lang")).equals(1)) {
            //描述接口文件上传并获取对应文件识别码
            inf = Utils.getInf(EnvClassEnum.PRO, sessionId);
        }


        //进行服务usap服务发布请求

        //原先参数
        JSONObject updateData = Utils.getProjectByAppId(""+ input.get("appId"),sessionId, Constants.getProUrlPre(), EnvClassEnum.PRO);

        //执行创建或者更新任务
        Utils.execute(updateData, input, EnvClassEnum.PRO, sessionId, inf);

    }


    /**
     * 模拟登录获取sessionId
     * @return
     */
    public static String getSessionId() {
        log.info("模拟登录获取sessionId---------------------");
        Map loginMap =  new HashMap<String, Object>();
        loginMap.put("userName", "admin");
        loginMap.put("password","soa@173");//线上密码soa@173,测试开发111111
        HttpRequest loginRequest = HttpUtil.createPost(Constants.getProUrlPre() + "login2");
        loginRequest.form(loginMap);
        loginRequest = loginRequest.enableDefaultCookie();
        HttpResponse loginResponse = loginRequest.execute();
        JSONObject jsonObject = JSONUtil.parseObj(loginResponse.body());
        Map map = (Map)jsonObject.get("datas");
        String sessionId = (String) map.get("sessionId");
        return sessionId;
    }
}
