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

    private static final Log log = LogFactory.get(DevMain.class);

    public static void main(String[] args) {
        JSONObject object = getProjectByAppId("newgame");
        System.out.println(object);
    }

    public static JSONObject getProjectByAppId(String appId) {
        String sessionId = DevMain.getSessionId();
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
