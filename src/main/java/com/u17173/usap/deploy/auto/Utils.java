package com.u17173.usap.deploy.auto;

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
 * @author Li Bang Zhu
 * @date 2018/12/3
 */
public class Utils {
    private static final Log log = LogFactory.get(Utils.class);


    /**
     * 用于获取maven仓库的url地址并填入发布信息
     * @param deployMap
     * @return
     */
    public static void SetmvnUrl(Map<String, Object> deployMap) throws Exception {
        log.info("开始根据您填入的GAV坐标信息从Maven中获取jar包信息填入发布参数********************************");
        try {
            //maven仓库
            String r;
            //groupId
            String g = deployMap.get("groupId").toString();
            //artifactId
            String a = deployMap.get("artifactId").toString();
            //version
            String v = deployMap.get("version").toString();
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
        }catch (Exception e) {
            //log.info("获取maven仓库的url地址并填入发布信息失败，请检查maven库中是否已存在JAR包或者您填入的GAV坐标是否与maven库中一致");
            throw new Exception("请检查maven库中是否已存在JAR包或者您填入的GAV坐标是否与maven库中一致" + e);
        }
    }

    /**
     * 获取指定appId的项目信息，若有则返回信息，无则返回空
     * @param appId
     * @param sessionId
     * @param envClassEnum
     * @return
     * @throws Exception
     */
    public static JSONObject getProjectByAppId(String appId, String sessionId, EnvClassEnum envClassEnum) throws Exception {
        log.info("开始从SOA系统中获取appId = " + appId + "的信息********************************");
        try {
            HttpRequest request = HttpUtil.createPost(envClassEnum.getUrlPre() + "shtml/project/loadProjects");
            request.cookie("JSESSIONID=" + sessionId);
            HttpResponse response = request.execute();
            String body = response.body();
            JSONObject appProject = JSONUtil.parseObj(body);
            JSONArray array = (JSONArray) appProject.get("datas");
            if (array != null) {
                for (JSONObject object : array.toList(JSONObject.class)) {
                    if (object.get("appId").equals(appId)) {
                        return object;
                    }
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
            request = HttpUtil.createPost(envClassEnum.getUrlPre() + "shtml/project/doUpload");

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
        if (updateData == null) {
            create(updateData, input, envClassEnum, sessionId, inf);
        }else {
            update(updateData, input, envClassEnum, sessionId, inf);
        }
    }

    /**
     * 执行usap项目的创建
     * @param updateData
     * @param input
     * @param envClassEnum
     * @param sessionId
     * @param inf
     * @throws Exception
     */
    public static void create(JSONObject updateData, JSONObject input, EnvClassEnum envClassEnum, String sessionId, String inf) throws Exception {
        //执行创建应用
        Map<String, Object> deployMap = new HashMap<String, Object>();
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
        //如果是java应用
        if (input.get("lang").equals(2)) {
            SetmvnUrl(deployMap);
        }

        HttpRequest updateRequest = HttpUtil.createPost(envClassEnum.getUrlPre() + "shtml/project/saveProject");
        updateRequest.form(deployMap);
        log.info("创建信息为：{}",deployMap);
        log.info("开始创建usap服务********************************" + deployMap.get("appId"));
        updateRequest.cookie("JSESSIONID=" + sessionId);
        HttpResponse updateResult = updateRequest.execute();
        log.info("创建结果：" + updateResult.body());
        try {
            JSONObject rs = JSONUtil.parseObj(updateResult.body());
            if (!"成功".equals(rs.get("msg"))) {
                throw new Exception("返回结果不成功");
            }
        }catch (Exception e) {
            throw new Exception("解析结果失败或者操作失败" + e.getMessage());
        }
        //SOA字典自動化添加该创建项目
        addDictItem(deployMap, envClassEnum, sessionId);

    }

    /**
     * 执行usap项目的更新
     * @param updateData
     * @param input
     * @param envClassEnum
     * @param sessionId
     * @param inf
     * @throws Exception
     */
    public static void update(JSONObject updateData, JSONObject input, EnvClassEnum envClassEnum, String sessionId, String inf) throws Exception {
        Map<String, Object> deployMap = new HashMap<String, Object>();
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
        if (input.get("lang").equals(2)) {
            Utils.SetmvnUrl(deployMap);
        }
        HttpRequest updateRequest = HttpUtil.createPost(envClassEnum.getUrlPre()+ "shtml/project/upgradeProject");
        updateRequest.form(deployMap);
        updateRequest.cookie("JSESSIONID=" + sessionId);
        log.info("更新信息为：{}",deployMap);
        log.info("开始更新usap服务********************************" + input.get("appId"));
        HttpResponse updateResult = updateRequest.execute();
        log.info("更新结果：" + updateResult.body());
        try {
            JSONObject rs = JSONUtil.parseObj(updateResult.body());
            if (!"成功".equals(rs.get("msg"))) {
                throw new Exception("返回结果不成功");
            }
        }catch (Exception e) {
            throw new Exception("解析结果失败或者操作失败" + e.getMessage());
        }
    }


    /**
     * SOA字典自動化添加
     * @param deployMap
     * @param envClassEnum
     */
    public static void addDictItem(Map<String, Object> deployMap, EnvClassEnum envClassEnum, String sessionId) {
        log.info("开始对字典元素进行自动化创建（文档映射-应用对坐标）********************************");
        Integer orderNo;
        Integer dictId = Integer.MIN_VALUE;
        //获取不同环境的对应字典ID（文档映射-应用对坐标）
        HttpRequest getdictIdRequest = HttpUtil.createGet(envClassEnum.getUrlPre() + "dict/doList?limit=30");
        getdictIdRequest.cookie("JSESSIONID=" + sessionId);
        HttpResponse getdictIdResponse = getdictIdRequest.execute();
        JSONObject getdictIdResult = JSONUtil.parseObj(getdictIdResponse.body());
        JSONArray array = (JSONArray) getdictIdResult.get("data");
        for (JSONObject object : array.toList(JSONObject.class)) {
            if ("DOC_APPID_ARTIFACTID".equals(object.get("dictcode"))) {
                dictId = (Integer) object.get("dictid");
                break;
            }
        }

        //获取要创建的字典元素orderNo
        HttpRequest getOrderNoRequest = HttpUtil.createPost(envClassEnum.getUrlPre() + "dictItem/doList?dictId=" + dictId);
        getOrderNoRequest.cookie("JSESSIONID=" + sessionId);
        HttpResponse getOrderNoResponse = getOrderNoRequest.execute();
        JSONObject getOrderNoResult = JSONUtil.parseObj(getOrderNoResponse.body());
        orderNo = ((Integer) getOrderNoResult.get("total")) + 1;

        //执行创建字典元素操作
        HttpRequest request = HttpUtil.createPost((envClassEnum.getUrlPre() + "dictItem/doSave"));
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("dictId", dictId);
        map.put("dictName", "文档映射-应用对坐标");
        map.put("itemName", deployMap.get("appName"));
        map.put("itemCode", deployMap.get("appId"));
        map.put("itemVal", deployMap.get("appId"));
        map.put("itemValExt", "");
        map.put("orderNo", orderNo);
        map.put("remark", "");
        request.form(map);
        request.cookie("JSESSIONID=" + sessionId);
        HttpResponse response = request.execute();
        JSONObject resultJson = JSONUtil.parseObj(response.body());
        log.info(resultJson.toString());
        if (Boolean.FALSE.equals(resultJson.get("rs"))) {
            log.info("创建字典元素失败");
        }else {
            log.info("创建字典元素成功");
        }
    }

}
