package com.cyou.fz.service.usap.deploy;

import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.w3c.dom.Document;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * create by Li Bang Zhu on 2018/8/3
 * 公共方法封装
 */
public class Utils {
    private static final Log log = LogFactory.get(Utils.class);


    /**
     * 用于获取maven仓库的url地址并填入发布信息
     * @param updateData
     * @param input
     * @param deployMap
     * @return
     */
    public static Map<String, Object> SetmvnUrl(Map<String, Object> updateData, Map<String, Object> input, Map<String, Object> deployMap) throws Exception {
        log.info("开始根据您填入的GAV坐标信息从Maven中获取jar包信息填入发布参数********************************");
        try {
            //maven仓库
            String r;
            //groupId
            String g = (updateData == null ? deployMap : updateData).get("groupId").toString();
            //artifactId
            String a = (updateData == null ? deployMap : updateData).get("artifactId").toString();
            //version
            String v = input.get("version").toString().equals("default")?updateData.get("version").toString():input.get("version").toString();
            if (v.toLowerCase().endsWith("snapshot")) {
                r = "snapshots";
            }else {
                r = "releases";
            }
            HttpRequest mavenUrlReq = HttpUtil.createGet("http://10.5.117.126:8081/nexus/service/local/artifact/maven/resolve?r=" + r +
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
            String jarDeployUrl = "http://10.5.117.126:8081/nexus/content/repositories/" + r  +  mavenUrl + ".jar";
            String jarSourceDeployUrl = "http://10.5.117.126:8081/nexus/content/repositories/" + r  +  mavenUrl + "-sources.jar";
            String jarDocDeployUrl = "http://10.5.117.126:8081/nexus/content/repositories/" + r  +  mavenUrl + "-javadoc.jar";
            deployMap.put("mavenJarUrl", jarDeployUrl);
            deployMap.put("mavenJavaSourceUrl", jarSourceDeployUrl);
            deployMap.put("mavenJavaDocUrl", jarDocDeployUrl);
            return deployMap;
        }catch (Exception e) {
            //log.info("获取maven仓库的url地址并填入发布信息失败，请检查maven库中是否已存在JAR包或者您填入的GAV坐标是否与maven库中一致");
            throw new Exception("获取maven仓库的url地址并填入发布信息失败，请检查maven库中是否已存在JAR包或者您填入的GAV坐标是否与maven库中一致" + e);
        }
    }

    /**
     * 获取指定appId的项目信息，若有则返回信息，无则返回空
     * @param appId
     * @param sessionId
     * @param urlPre
     * @param envClassEnum
     * @return
     * @throws Exception
     */
    public static JSONObject getProjectByAppId(String appId, String sessionId, String urlPre, EnvClassEnum envClassEnum) throws Exception {
        log.info("开始从SOA系统中获取appId = " + appId + "的信息********************************");
        try {
            HttpRequest request = HttpUtil.createPost(urlPre + "shtml/project/loadProjects");
            request.cookie("JSESSIONID=" + sessionId);
            HttpResponse response = request.execute();
            String body = response.body();
            JSONObject appProject = JSONUtil.parseObj(body);
            String msg = (String) appProject.get("msg");
            while ("未登录".equals(msg)) {
                if (EnvClassEnum.TEST.equals(envClassEnum)) {
                    sessionId = TestMain.getSessionId();
                }else if (EnvClassEnum.DEV.equals(envClassEnum)) {
                    sessionId = DevMain.getSessionId();
                }else {
                    sessionId = ProMain.getSessionId();
                }
                appProject = JSONUtil.parseObj(request.cookie("JSESSIONID=" + sessionId).execute().body());
                msg = (String) appProject.get("msg");
            }
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
        }catch (Exception e) {
            throw new Exception("从SOA系统获取appId = " + appId + "的信息出现异常" + e);
        }
    }

    /**
     * php项目描述文件上传并获取识别码inf
     * @param envClassEnum
     * @param sessionId
     * @return
     * @throws Exception
     */
    public static String getInf(EnvClassEnum envClassEnum, String sessionId) throws Exception {
        try {
            //描述接口文件上传并获取对应文件识别码
            log.info("描述接口文件上传并获取对应文件识别码********************************");
            HttpRequest request;
            if (EnvClassEnum.DEV.equals(envClassEnum)) {
                request = HttpUtil.createPost(Constants.getDevUrlPre() + "shtml/project/doUpload");
            }else if (EnvClassEnum.TEST.equals(envClassEnum)) {
                request = HttpUtil.createPost(Constants.getTestUrlPre() + "shtml/project/doUpload");
            }else {
                request = HttpUtil.createPost(Constants.getProUrlPre() + "shtml/project/doUpload");
            }

            request.contentType("multipart/form-data");
            request.cookie("JSESSIONID=" + sessionId);
            File file = new File("./");
            log.info("主目录为：{}",file.getAbsolutePath());
            for (File file1 : file.listFiles()) {
                if (file1.getName().endsWith("json.txt")){
                    request.form("file",file1);
                    log.info("接口描述文件目录为：" + file1.getAbsolutePath());
                    break;
                }
            }
            JSONObject uploadMsg = JSONUtil.parseObj(request.execute().body());
            String inf = (String) uploadMsg.get("msg");
            while ("用户未登入，请重新登入".equals(inf)) {
                if (EnvClassEnum.TEST.equals(envClassEnum)) {
                    sessionId = TestMain.getSessionId();
                }else if (EnvClassEnum.DEV.equals(envClassEnum)) {
                    sessionId = DevMain.getSessionId();
                }else {
                    sessionId = ProMain.getSessionId();
                }
                inf = (String) JSONUtil.parseObj(request.cookie("JSESSIONID=" + sessionId).execute().body()).get("msg");
            }
            return inf;
        } catch (Exception e) {
            throw new Exception("描述接口文件上传出现异常：" + e);
        }
    }

    /**
     * 执行更新或者创建操作
     * @param updateData
     * @param input
     * @param envClassEnum
     * @param sessionId
     * @param inf
     * @throws Exception
     */
    public static void execute(JSONObject updateData, JSONObject input, EnvClassEnum envClassEnum, String sessionId, String inf) throws Exception {
        Map<String, Object> deployMap = new HashMap<String, Object>();
        if (updateData == null) {
            //执行创建应用
            deployMap.put("appId", input.get("appId"));
            deployMap.put("appName", input.get("appName"));
            deployMap.put("groupId", input.get("groupId")!=null?input.get("groupId"):"com.cyou.fz.services");
            deployMap.put("artifactId", input.get("artifactId"));
            deployMap.put("version", input.get("version"));
            deployMap.put("pkg", input.get("pkg"));
            deployMap.put("port", input.get("port"));
            deployMap.put("threadNum", input.get("threadNum"));
            deployMap.put("serveiceVersion", input.get("serveiceVersion"));
            deployMap.put("timeout", input.get("timeout"));
            deployMap.put("lang", input.get("lang"));
            deployMap.put("phpUrl", input.get("phpUrl"));
            deployMap.put("inf", inf);
            deployMap.put("userNames", input.get("userNames"));
            if (input.get("lang").equals(2)) {
                deployMap = SetmvnUrl(updateData, input, deployMap);
            }
            HttpRequest updateRequest = HttpUtil.createPost(Constants.getDevUrlPre() + "shtml/project/saveProject");
            updateRequest.disableCache();
            updateRequest.form(deployMap);
            log.info("创建信息为：{}",deployMap);
            log.info("开始创建usap服务********************************" + input.get("appId"));
            updateRequest.cookie("JSESSIONID=" + sessionId);
            HttpResponse updateResult = updateRequest.execute();
            JSONObject rs = JSONUtil.parseObj(updateResult.body());
            while ("未登录".equals(rs.get("msg"))) {
                if (EnvClassEnum.TEST.equals(envClassEnum)) {
                    sessionId = TestMain.getSessionId();
                }else if (EnvClassEnum.DEV.equals(envClassEnum)) {
                    sessionId = DevMain.getSessionId();
                }else {
                    sessionId = ProMain.getSessionId();
                }
                rs = JSONUtil.parseObj(updateRequest.cookie("JSESSIONID=" + sessionId).execute().body());
            }
            log.info(rs.toString());
        }else {
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
                deployMap = Utils.SetmvnUrl(updateData, input, deployMap);
            }
            if (input.get("artifactId") != null) {
                deployMap.put("artifactId", input.get("artifactId"));
            }else {
                deployMap.put("artifactId", updateData.get("artifactId"));
            }
            if (input.get("groupId") != null) {
                deployMap.put("groupId", input.get("groupId"));
            }else {
                deployMap.put("groupId", updateData.get("groupId"));
            }
            if (input.get("pkg") != null) {
                deployMap.put("pkg", input.get("pkg"));
            }else {
                deployMap.put("pkg", updateData.get("pkg"));
            }
            HttpRequest updateRequest = HttpUtil.createPost(Constants.getDevUrlPre() + "shtml/project/upgradeProject");
            updateRequest.disableCache();
            updateRequest.form(deployMap);
            updateRequest.cookie("JSESSIONID=" + sessionId);
            log.info("更新信息为：{}",deployMap);
            log.info("开始更新usap服务********************************" + input.get("appId"));
            HttpResponse updateResult = updateRequest.execute();
            JSONObject rs = JSONUtil.parseObj(updateResult.body());
            while ("未登录".equals(rs.get("msg"))) {
                if (EnvClassEnum.TEST.equals(envClassEnum)) {
                    sessionId = TestMain.getSessionId();
                }else if (EnvClassEnum.DEV.equals(envClassEnum)) {
                    sessionId = DevMain.getSessionId();
                }else {
                    sessionId = ProMain.getSessionId();
                }
                rs = JSONUtil.parseObj(updateRequest.cookie("JSESSIONID=" + sessionId).execute().body());
            }
            if (!"成功".equals(rs.get("msg"))) {
                throw new Exception((String) rs.get("msg"));
            }
            log.info(rs.toString());
        }
    }
}
