package cn.wizzer.app.sys.modules.services.impl;

import cn.wizzer.app.sys.modules.services.SysWxService;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.plugin.PostRun;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.sys.modules.models.Sys_wx_user;
import cn.wizzer.app.sys.modules.services.SysWxUserService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.ArrayList;
import java.util.List;

@IocBean(args = {"refer:dao"})
public class SysWxUserServiceImpl extends BaseServiceImpl<Sys_wx_user> implements SysWxUserService {

    @Inject
    private SysWxService sysWxService;

    public SysWxUserServiceImpl(Dao dao) {
        super(dao);
    }

    /**
     * 下载微信用户信息
     * @throws Exception
     */
    @Override
    public void download() throws Exception {
        Cnd cnd = Cnd.NEW();
        cnd.and("agentid","=","memo");
        String access_token = sysWxService.getTokenOrNew(cnd);
        //获取用户
        //https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token=ACCESS_TOKEN&department_id=DEPARTMENT_ID&fetch_child=FETCH_CHILD
        String memourl = "https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token="+access_token+"&department_id=1&fetch_child=1";
        PostRun pr=new PostRun();
        String memoJson=pr.WXTokenget(memourl);
        JSONObject jsonobject = JSONObject.parseObject(memoJson);
        int errcode = jsonobject.getIntValue("errcode");
        String errmsg = jsonobject.getString("errmsg");
        if(errcode!=0){
            throw new ValidatException("获取token失败："+errcode+","+errmsg);
        }
        JSONArray userArray = jsonobject.getJSONArray("userlist");
        List<Sys_wx_user> wxUserList = new ArrayList<Sys_wx_user>();
        Sys_wx_user userTemp =null;
        for(int i=0;i<userArray.size();i++){
            JSONObject ob = userArray.getJSONObject(i);
            userTemp = new Sys_wx_user();
            userTemp.setCorpid(Globals.WxCorpID);
            userTemp.setUserid(ob.getString("userid"));
            userTemp.setName(ob.getString("name"));
            userTemp.setDepartment(ob.getString("department"));
            userTemp.setOrderNum(ob.getString("order"));
            userTemp.setPosition(ob.getString("position"));
            userTemp.setMobile(ob.getString("mobile"));
            userTemp.setGender(ob.getString("gender"));
            userTemp.setEmail(ob.getString("email"));
            userTemp.setIsleader(ob.getString("isleader"));
            userTemp.setAvatar(ob.getString("avatar"));
            userTemp.setTelephone(ob.getString("telephone"));
            userTemp.setEnglish_name(ob.getString("english_name"));
            userTemp.setStatus(ob.getString("status"));
            userTemp.setExtattr(ob.getString("extattr"));
            userTemp.setIsBindPerson(false);
            wxUserList.add(userTemp);
        }
        insertList(wxUserList);
    }

    @Aop(TransAop.READ_COMMITTED)
    private void insertList(List<Sys_wx_user>wxUserList){
        Cnd cnd = Cnd.NEW();
        cnd.and("corpid","=",Globals.WxCorpID);
        this.clear(cnd);
        for(Sys_wx_user dep:wxUserList){
            this.insert(dep);
        }
    }

}
