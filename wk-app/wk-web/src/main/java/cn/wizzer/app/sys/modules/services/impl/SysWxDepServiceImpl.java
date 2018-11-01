package cn.wizzer.app.sys.modules.services.impl;

import cn.wizzer.app.sys.modules.models.Sys_wx;
import cn.wizzer.app.sys.modules.services.SysWxService;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.plugin.PostRun;
import cn.wizzer.app.web.modules.controllers.open.api.Datatime.newDataTime;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.sys.modules.models.Sys_wx_dep;
import cn.wizzer.app.sys.modules.services.SysWxDepService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@IocBean(args = {"refer:dao"})
public class SysWxDepServiceImpl extends BaseServiceImpl<Sys_wx_dep> implements SysWxDepService {

    @Inject
    private SysWxService sysWxService;


    public SysWxDepServiceImpl(Dao dao) {
        super(dao);
    }

    /**
     * 下载部门信息
     */
    @Override
    public void download() throws Exception {
        Cnd cnd = Cnd.NEW();
        cnd.and("agentid","=","memo");
        String access_token = sysWxService.getTokenOrNew(cnd);
        //获取全量组织架构
        //https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token=ACCESS_TOKEN&id=ID
        String memourl = "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token="+access_token;
        PostRun pr=new PostRun();
        String memoJson=pr.WXTokenget(memourl);
        JSONObject jsonobject = JSONObject.parseObject(memoJson);
        int errcode = jsonobject.getIntValue("errcode");
        String errmsg = jsonobject.getString("errmsg");
        if(errcode!=0){
            throw new ValidatException("获取token失败："+errcode+","+errmsg);
        }
        JSONArray depArray = jsonobject.getJSONArray("department");
        List<Sys_wx_dep> wxDepList = new ArrayList<Sys_wx_dep>();
        Sys_wx_dep depTemp =null;
        for(int i=0;i<depArray.size();i++){
            JSONObject ob = depArray.getJSONObject(i);
            int id = ob.getIntValue("id");
            String name = ob.getString("name");
            int parentid = ob.getIntValue("parentid");
            int order = ob.getIntValue("order");

            depTemp = new Sys_wx_dep();
            depTemp.setCorpid(Globals.WxCorpID);
            depTemp.setDepid(String.valueOf(id));
            depTemp.setName(name);
            depTemp.setParentid(String.valueOf(parentid));
            depTemp.setOrderNum(String.valueOf(order));
            wxDepList.add(depTemp);
        }

        insertList(wxDepList);
    }

    @Aop(TransAop.READ_COMMITTED)
    private void insertList(List<Sys_wx_dep>wxDepList){
        Cnd cnd = Cnd.NEW();
        cnd.and("corpid","=",Globals.WxCorpID);
        this.clear(cnd);
        for(Sys_wx_dep dep:wxDepList){
            this.insert(dep);
        }
    }

}
