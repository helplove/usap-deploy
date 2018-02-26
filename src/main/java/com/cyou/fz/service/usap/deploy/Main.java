package com.cyou.fz.service.usap.deploy;

import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.log.StaticLog;
import org.w3c.dom.Document;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author created by BangZhuLi
 * @date 2018/2/1  14:26
 * usap自动化部署（php）,
 */

public class Main {

    private static final Log log = LogFactory.get(Main.class);

    /**
     *
     * @param args
     *
     */
    public static void main(String[] args) {
        //模拟登录获取sessionId
        log.info("模拟登录获取sessionId---------------------");
        Map loginMap =  new HashMap<String, Object>();
        loginMap.put("userName", "admin");
        loginMap.put("password","111111");
        HttpRequest loginRequest = HttpUtil.createPost("http://10.5.121.174/login2");
        loginRequest.form(loginMap);
        HttpResponse loginResponse = loginRequest.execute();
        JSONObject jsonObject = JSONUtil.parseObj(loginResponse.body());
        Map map = (Map)jsonObject.get("datas");
        String sessionId = (String) map.get("sessionId");


        //
        log.info("获取解析自定义参数----------------------");
        JSONObject input = JSONUtil.parseObj(args[0]);
        //"{\"appId\":\"sea\",\"appName\":\"default\",\"version\":\"2.6.3-SNAPSHOT\",\"port\":\"default\",\"threadNum\":\"default\",\"serveiceVersion\":\"default\",\"timeout\":\"default\",\"lang\":2,\"phpUrl\":\"default\",\"userNames\":\"default\"}"
        String inf = "";
        //PHP应用
        if (input.get("lang").equals(1)) {
            //描述接口文件上传并获取对应文件识别码
            log.info("描述接口文件上传并获取对应文件识别码---------------------");
            HttpRequest request = HttpUtil.createPost("http://10.5.121.174/shtml/project/doUpload");
            request.contentType("multipart/form-data");
            request.disableCache();
            request.cookie("JSESSIONID=" + sessionId);
            File file = new File("api/" + input.get("appId"));
            for (File file1 : file.listFiles()) {
                if (file1.getName().endsWith("json.txt")){
                    request.form("file",file1);
                    log.info("接口描述文件目录为：" + file1.getAbsolutePath());
                }
            }
            JSONObject uploadMsg = JSONUtil.parseObj(request.execute().body());
            inf = (String) uploadMsg.get("msg");
        }




        //进行服务usap服务发布请求
        //log.info("开始进行服务usap服务发布请求---------------------");

        //原先参数
        JSONObject updateData = Update.getProjectByAppId(""+ input.get("appId"));
        //需要发布的参数
        Map<String, Object> deployMap = new HashMap<String, Object>();
        if (updateData == null) {
            //执行创建应用
            log.info("开始创建usap服务---------------------" + input.get("appId"));
            deployMap.put("id", input.get("id"));
            deployMap.put("appName", input.get("appName"));
            deployMap.put("groupId", "com.cyou.fz.services");
            deployMap.put("artifactId", input.get("artifactId"));
            deployMap.put("version", input.get("version"));
            deployMap.put("pkg", "com.cyou.fz.services." + input.get("artifactId"));
            deployMap.put("port", input.get("port"));
            deployMap.put("threadNum", input.get("threadNum"));
            deployMap.put("serveiceVersion", input.get("serveiceVersion"));
            deployMap.put("timeout", input.get("timeout"));
            deployMap.put("phpUrl", input.get("phpUrl"));
            deployMap.put("inf", inf);
            deployMap.put("userNames", input.get("userNames"));
            if (input.get("lang").equals(2)) {
                deployMap = SetmvnUrl(updateData, input, deployMap);
            }
        }else {
            log.info("开始更新usap服务---------------------" + input.get("appId"));
            deployMap.put("id", updateData.get("id"));
            deployMap.put("appName", input.get("appName").equals("default")?updateData.get("appName"):input.get("appName"));
            deployMap.put("version", input.get("version").equals("default")?updateData.get("version"):input.get("version"));
            deployMap.put("port", input.get("port").equals("default")?updateData.get("port"):input.get("port"));
            deployMap.put("threadNum", input.get("threadNum").equals("default")?updateData.get("threadNum"):input.get("threadNum"));
            deployMap.put("serveiceVersion", input.get("serveiceVersion").equals("default")?updateData.get("serveiceVersion"):input.get("serveiceVersion"));
            deployMap.put("timeout", input.get("timeout").equals("default")?updateData.get("timeout"):input.get("timeout"));
            deployMap.put("phpUrl", input.get("phpUrl").equals("default")?updateData.get("phpUrl"):input.get("phpUrl"));
            deployMap.put("inf", inf);
            deployMap.put("userNames", input.get("userNames").equals("default")?updateData.get("userNames"):input.get("userNames"));
            if (input.get("lang").equals(2)) {
                deployMap = SetmvnUrl(updateData, input, deployMap);
            }
        }

        HttpRequest updateRequest = HttpUtil.createPost("http://10.5.121.174/shtml/project/upgradeProject");
        updateRequest.disableCache();
        updateRequest.form(deployMap);
        updateRequest.cookie("JSESSIONID=" + sessionId);
        HttpResponse updateResult = updateRequest.execute();
        log.info("" + JSONUtil.parseObj(updateResult.body()));
    }

    public static Map<String, Object> SetmvnUrl(Map<String, Object> updateData, Map<String, Object> input, Map<String, Object> deployMap) {
        //maven仓库
        String r;
        //groupId
        String g = updateData.get("groupId").toString();
        //artifactId
        String a = updateData.get("artifactId").toString();
        //version
        String v = input.get("version").toString();
        if (v.toLowerCase().endsWith("snapshot")) {
            r = "snapshots";
        }else {
            r = "releases";
        }
        HttpRequest mavenUrlReq = HttpUtil.createGet("http://mvnrepos.dev.17173.com:8081/nexus/service/local/artifact/maven/resolve?r=" + r +
                "&g=" + g +
                "&a=" + a +
                "&v=" + v);
        //所获得的maven信息
        String rs = mavenUrlReq.execute().body();
        Document document = XmlUtil.readXML(rs);
        //所求的部分maven url地址
        String mavenUrl = document.getElementsByTagName("repositoryPath").item(0).getTextContent();
        mavenUrl = mavenUrl.substring(0,mavenUrl.length()-4);
        //最后所需要填入soa进行发布的maven地址
        String jarDeployUrl = "http://mvnrepos.dev.17173.com:8081/nexus/content/repositories/" + r  +  mavenUrl + ".jar";
        String jarSourceDeployUrl = "http://mvnrepos.dev.17173.com:8081/nexus/content/repositories/" + r  +  mavenUrl + "-sources.jar";
        String jarDocDeployUrl = "http://mvnrepos.dev.17173.com:8081/nexus/content/repositories/" + r  +  mavenUrl + "-javadoc.jar";
        deployMap.put("mavenJarUrl", jarDeployUrl);
        deployMap.put("mavenJavaSourceUrl", jarSourceDeployUrl);
        deployMap.put("mavenJavaDocUrl", jarDocDeployUrl);
        return deployMap;
    }
}
