package cn.wizzer.app.sys.modules.services.impl;

import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.plugin.HttpClientUtil;
import cn.wizzer.app.web.commons.plugin.PostRun;
import cn.wizzer.app.web.modules.controllers.open.api.Datatime.newDataTime;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.sys.modules.models.Sys_wx;
import cn.wizzer.app.sys.modules.services.SysWxService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import java.text.SimpleDateFormat;
import java.util.*;

@IocBean(args = {"refer:dao"})
public class SysWxServiceImpl extends BaseServiceImpl<Sys_wx> implements SysWxService {

    private static final Log log = Logs.get();

    private  SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

    public SysWxServiceImpl(Dao dao) {
        super(dao);
    }

    /**
     * 获取token前需要现在系统参数中维护corpid，还要先在此model中设置要访问的应用及secret
     * 若是获取通讯录的access_token ,则本身没有agentid，应设置为"memo"
     * @param cnd
     * @return
     * @throws Exception
     */
    @Override
//    @Aop(TransAop.READ_COMMITTED)
    public String getTokenOrNew(Cnd cnd) throws Exception {
        String corpid = Globals.WxCorpID;
        if(StringUtils.isBlank(corpid)){
            throw new ValidatException("请先在系统中维护参数WxCorpID");
        }
        cnd.and("corpid","=",corpid);
        List<Sys_wx> list = this.query(cnd);
        if(list.size()!=1){
            throw new ValidatException("请设置需要访问的应用和访问密钥");
        }
        Sys_wx sysWx = list.get(0);
        String access_token =sysWx.getToken();
        if(!isTokenValid(sysWx)){
            access_token = updateToken(sysWx);
        }
        return access_token;
    }

    /**
     * 发送文本消息给用户
     * @param userIds  接收用户id值的数组
     * @param appnum   应用编号，目前固定:无动力设备10001
     * @param content  发送的内容
     * @return          返回接收的无效用户，包括两部分：1 系统中未维护用户人员对应关系或微信账号的用户；2 微信接口返回的无效用户账号（接收人无权限或不存在)
     * @throws Exception
     */
    @Override
    public String sendMessageToUser( String[]userIds,String appnum, String content) {
        Cnd cnd = Cnd.NEW();
        String corpid = Globals.WxCorpID;
        if(StringUtils.isBlank(corpid) || ".".equals(corpid)){
            throw new ValidatException("请先在系统中维护参数WxCorpID");
        }
        cnd.and("corpid","=",corpid);
        cnd.and("number","=",appnum);
        //校验及查询接收用户的微信账号
        if(userIds==null || userIds.length<=0){
            throw new ValidatException("请设置接收用户!");
        }
        Cnd usecnd = Cnd.NEW();
        Sql sql = Sqls.queryRecord("select t0.username,t1.wxUserid \n" +
                "from sys_user t0 LEFT JOIN base_cnctobj t on t0.id=t.userId LEFT JOIN base_person t1 on t.personId = t1.id \n" +
                " $condition");
        usecnd.and("t0.id","in",userIds);
        sql.setCondition(usecnd);
        dao().execute(sql);
        List<Record> userlist = sql.getList(Record.class);
        StringBuffer sb = new StringBuffer("");
        List<String>errorUserList = new ArrayList<String>();
        int i=0;
        for(Record re:userlist){
            String wxUserid = re.getString("wxUserid");
            String username = re.getString("username");
            if(StringUtils.isNotBlank(wxUserid)){
                if(i!=0){sb.append("|");}
                sb.append(wxUserid);
                i++;
            }else{
                errorUserList.add(username);
            }
        }
        //获取access_token
        List<Sys_wx> list = this.query(cnd);
        if(list.size()!=1){
            throw new ValidatException("请设置需要访问的应用和访问密钥");
        }
        Sys_wx sysWx = list.get(0);
        String access_token =sysWx.getToken();
        if(!isTokenValid(sysWx)){
            access_token = updateToken(sysWx);
        }
        //POST发送json格式的文本消息
        String sendUrl = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token="+access_token;
        Map<String,Object> paramMap = new HashMap<String,Object>();
        paramMap.put("touser",sb.toString());
        paramMap.put("msgtype","text");
        paramMap.put("agentid",Integer.valueOf(sysWx.getAgentid()));
        JSONObject textJsonObj = new JSONObject();
        textJsonObj.put("content",content);
        paramMap.put("text",textJsonObj);
        paramMap.put("safe",0);
        JSONObject jsonObject = new JSONObject(paramMap);
        log.debug("请求参数："+jsonObject.toJSONString());
        HttpClientUtil httpClientUtil = new HttpClientUtil();
        String retMsg = httpClientUtil.postJson(sendUrl,jsonObject.toJSONString());
        JSONObject jsonobject = JSONObject.parseObject(retMsg);
        int errcode = jsonobject.getIntValue("errcode");
        String errmsg = jsonobject.getString("errmsg");
        String invaliduser = jsonobject.getString("invaliduser");
        if (errcode != 0) {
            throw new ValidatException("消息发送失败：" + errcode + "," + errmsg);
        }
        if(errorUserList.size()>0){
            log.debug("发送消息未找到微信账号的用户："+errorUserList.toString());
        }
        log.debug("发送无效的微信账号："+invaliduser);
        return invaliduser+""+errorUserList.toString().replace("[","").replace("]","");
    }

    /**
     * 异步发送消息
     * @param userIds 用Set参数可以防止用户重复发送
     * @param content
     */
    @Override
    @Async
    public void sendWxMessageAsy(Set<String> userIds, String content) {
        try {

            if(userIds!=null && userIds.size()>0){
                log.debug("发送企业微信消息 content="+content+":userIds="+userIds);
                String [] userIdArray = new String[userIds.size()];
                Iterator it = userIds.iterator();
                int i=0;
                while(it.hasNext()){
                    userIdArray[i] = (String)it.next();
                    i++;
                }
                this.sendMessageToUser(userIdArray,"10001",content.toString());
            }else{
                log.debug("未传入任何用户信息，未发送微信消息！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //重新获取token
    private String updateToken(Sys_wx sysWx) {
        String access_token ="";
        //当前时间大于到期时间,说明需要获取新的token了
        String corpid = Globals.WxCorpID;
        String secret = sysWx.getSecret();
        String tokenurl="https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+secret;
        //"{\"errcode\":0,\"errmsg\":\"\",\"access_token\": \"accesstoken000001\",\"expires_in\": 7200}"
        PostRun pr=new PostRun();
        String  tokenJson=pr.WXTokenget(tokenurl);
        JSONObject jsonobject = JSONObject.parseObject(tokenJson);
        if(jsonobject!=null) {
            int errcode = jsonobject.getIntValue("errcode");
            String errmsg = jsonobject.getString("errmsg");
            access_token = jsonobject.getString("access_token");
            String expires_in = jsonobject.getString("expires_in");
            if (errcode != 0) {
                throw new ValidatException("获取token失败：" + errcode + "," + errmsg);
            }
            if (Integer.valueOf(Globals.WXExpire).intValue() > Integer.valueOf(expires_in).intValue()) {
                log.debug("System config param 'WXExpire' bigger than the Weixin Inteface gettoken value，need modify the system config!");
                Globals.WXExpire = expires_in;
            }
            long timeLose = System.currentTimeMillis() + Integer.parseInt(Globals.WXExpire) * 1000;
            sysWx.setToken(access_token);
            sysWx.setExpire(expires_in);
            sysWx.setStarttime(newDataTime.getDateYMDHMS());
            sysWx.setEndtime(sdf.format(new Date(timeLose)));
            dao().updateIgnoreNull(sysWx);
        }else {
            throw new ValidatException("请求微信接口返回空，请检查设置参数后再试！");
        }
        return access_token;
    }

    //判断当前表中的token是否有效
    private boolean isTokenValid (Sys_wx sysWx){
        Date enddate = null;
        try {
            if(StringUtils.isNotBlank(sysWx.getEndtime())) {
                enddate = sdf.parse(sysWx.getEndtime().toString());
            }
        }catch (Exception e1){
            e1.printStackTrace();
            return false;
        }
        Date day=new Date();
        if(enddate==null || day.getTime()>enddate.getTime()) {//已过期
            return false;
        }
        return true;
    }


}
