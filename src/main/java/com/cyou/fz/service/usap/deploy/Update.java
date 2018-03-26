package com.cyou.fz.service.usap.deploy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author created by BangZhuLi
 * @date 2018/2/5  10:21
 */
public class Update {

    private static final Log log = LogFactory.get(Main.class);

    public static void main(String[] args) {
        JSONObject object = getProjectByAppId("1");
        System.out.println(object);
    }

    public static JSONObject getProjectByAppId(String appId) {
        Map loginMap = new HashMap<String, Object>();
        loginMap.put("userName", "admin");
        loginMap.put("password", "111111");
        HttpRequest loginRequest = HttpUtil.createPost(Constants.URL_PRE + "login2");
        loginRequest.form(loginMap);
        HttpResponse loginResponse = loginRequest.execute();
        JSONObject jsonObject = JSONUtil.parseObj(loginResponse.body());
        Map map = (Map) jsonObject.get("datas");
        String sessionId = (String) map.get("sessionId");
        while ("未登录".equals(jsonObject.get("msg"))) {
           log.info("获取登录信息重试中---");
            sessionId = (String) ((Map)JSONUtil.parseObj(loginRequest.execute().body()).get("datas")).get("sessionId");
        }
        //System.out.println("sessionId = " + sessionId);
        HttpRequest request = HttpUtil.createPost(Constants.URL_PRE + "shtml/project/loadProjects");
        request.cookie("JSESSIONID=" + sessionId);
        request.contentType("json");
        request.disableCache();
        HttpResponse response = request.execute();
        String body = response.body();
        JSONObject appProject = JSONUtil.parseObj(body);

        //System.out.println(appProject);
        JSONArray array = (JSONArray) appProject.get("datas");
        for (JSONObject object : array.toList(JSONObject.class)) {
            if (object.get("appId").equals(appId)) {
                return object;
            }
        }
        return null;
    }
}
