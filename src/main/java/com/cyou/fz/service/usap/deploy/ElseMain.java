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
public class ElseMain {
    private static final Log log = LogFactory.get(ElseMain.class);

    /**
     *
     * @param args
     *
     */
    public static void main(String[] args) throws Exception {
        log.info("获取解析自定义参数----------------------");
        JSONObject input = JSONUtil.parseObj(args[0]);
        if (input.get("url_pre") != null) {
            Constants.URL_PRE = "http://" + (String) input.get("url_pre") + "/";
        }


        /**
         * 1.两个环境的SOA数据库皆可不用，版本号等信息统一用开发环境下的信息为主
         * 2.整合SOA系统发布流程的1.5.6步骤既可
        **/



    }

    /**
     * 用于获取maven仓库的url地址并填入发布信息
     * @param updateData
     * @param input
     * @param deployMap
     * @return
     */
    public static Map<String, Object> SetmvnUrl(Map<String, Object> updateData, Map<String, Object> input, Map<String, Object> deployMap) {
        //maven仓库
        String r;
        //groupId
        String g = (updateData == null ? input : updateData).get("groupId").toString();
        //artifactId
        String a = (updateData == null ? input : updateData).get("artifactId").toString();
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
    }

}
