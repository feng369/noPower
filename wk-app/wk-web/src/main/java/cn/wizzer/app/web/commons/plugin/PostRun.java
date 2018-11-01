package cn.wizzer.app.web.commons.plugin;

import org.nutz.lang.Strings;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xl on 2017/9/18.
 */
public class PostRun {
    //private String url = "https://192.168.1.101/";
    private String charset = "utf-8";
    private HttpClientUtil httpClientUtil = null;

    public String post(String url,String serial,String type){
        httpClientUtil = new HttpClientUtil();
        String httpOrgCreateTest = url; //+ "httpOrg/create";
        Map<String,String> createMap = new HashMap<String,String>();
        if(!Strings.isBlank(type))
            createMap.put("type",type);
        if(!Strings.isBlank(serial));
        createMap.put("serial",serial);
        String httpOrgCreateTestRtn = httpClientUtil.doPost(httpOrgCreateTest,createMap,charset);
        return httpOrgCreateTestRtn;
    }

    public String WXTokenget(String url){
        httpClientUtil = new HttpClientUtil();
        String httptoken = url;
        String httpTokenRtn = httpClientUtil.doGet(httptoken,charset);
        return httpTokenRtn;
    }

    //微信发送消息Post 已失效
    public String WXMsgpost(String url,String tuser,String tparty,String ttag,String msgtype,String agentid,String content,String safe){
        httpClientUtil = new HttpClientUtil();
        String httpWxMsg = url;
        Map<String,String> createMap = new HashMap<String,String>();
        if(!Strings.isBlank(tuser))
            createMap.put("touser",tuser);
        if(!Strings.isBlank(tparty))
            createMap.put("toparty",tparty);
        if(!Strings.isBlank(ttag))
            createMap.put("totag",ttag);
        if(!Strings.isBlank(msgtype))
            createMap.put("msgtype",msgtype);
        if(!Strings.isBlank(agentid))
            createMap.put("agentid",agentid);
        if(!Strings.isBlank(content))
            createMap.put("content",content);
        if(!Strings.isBlank(safe))
            createMap.put("safe",safe);
        String httpMsgRtn = httpClientUtil.doPost(httpWxMsg,createMap,charset);
        return httpMsgRtn;
    }
}
