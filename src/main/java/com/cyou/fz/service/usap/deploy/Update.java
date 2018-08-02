package com.cyou.fz.service.usap.deploy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

/**
 * @author created by BangZhuLi
 * @date 2018/2/5  10:21
 */
public class Update {

    private static final Log log = LogFactory.get(Update.class);

    public static void main(String[] args) {
        //JSONObject object = getProjectByAppId("newgame");
        //System.out.println(object);
    }

    public static JSONObject getProjectByAppId(String appId, String sessionId, String urlPre) {
        HttpRequest request = HttpUtil.createPost(urlPre + "shtml/project/loadProjects");
        request.cookie("JSESSIONID=" + sessionId);
        request.contentType("json");
        request.disableCache();
        HttpResponse response = request.execute();
        String body = response.body();
        JSONObject appProject = JSONUtil.parseObj(body);

        //log.info("从SOA系统获取到的appProject信息为" + appProject.toString());
        JSONArray array = (JSONArray) appProject.get("datas");
        if (array == null) {
            return null;
        }
        for (JSONObject object : array.toList(JSONObject.class)) {
            if (object.get("appId").equals(appId)) {
                return object;
            }
        }
        return null;
    }
}
