package com.cyou.fz.service.usap.deploy;


import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.w3c.dom.Document;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author created by BangZhuLi
 * @date 2018/6/13
 * usap自动化部署（测试，线上环境）,
 */
public class TestMain {
    private static final Log log = LogFactory.get(TestMain.class);

    /**
     *
     * @param args
     *
     */
    public static void main(String[] args) throws Exception {
        log.info("获取解析自定义参数******************************");
        JSONObject input = JSONUtil.parseObj(args[0]);
        String sessionId = getSessionId();

        String inf = "";
        //PHP应用
        if ((input.get("lang")).equals(1)) {
            //描述接口文件上传并获取对应文件识别码
            inf = Utils.getInf(EnvClassEnum.TEST, sessionId);
        }

        //进行服务usap服务发布请求

        //原先参数
        JSONObject updateData = Utils.getProjectByAppId(""+ input.get("appId"),sessionId, Constants.getTestUrlPre(), EnvClassEnum.TEST);

        //执行创建或者更新任务
        Utils.execute(updateData, input, EnvClassEnum.TEST, sessionId, inf);
    }


    /**
     * 模拟登录获取sessionId
     * @return
     */
    public static String getSessionId() {
        log.info("模拟登录获取sessionId******************************");
        Map loginMap =  new HashMap<String, Object>();
        loginMap.put("userName", "admin");
        loginMap.put("password","111111");//线上密码soa@173,测试开发111111
        HttpRequest loginRequest = HttpUtil.createPost(Constants.getTestUrlPre() + "login2");
        loginRequest.form(loginMap);
        loginRequest = loginRequest.enableDefaultCookie();
        HttpResponse loginResponse = loginRequest.execute();
        JSONObject jsonObject = JSONUtil.parseObj(loginResponse.body());
        Map map = (Map)jsonObject.get("datas");
        String sessionId = (String) map.get("sessionId");
        return sessionId;
    }


}
