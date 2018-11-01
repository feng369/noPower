package cn.wizzer.app.web.modules.controllers.platform.sys;

import cn.wizzer.app.web.commons.plugin.PostRun;
import cn.wizzer.app.web.modules.controllers.open.api.Datatime.newDataTime;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.sys.modules.models.Sys_wx;
import cn.wizzer.app.sys.modules.services.SysWxService;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@IocBean
@At("/platform/sys/wx/token")
public class SysWxController{
    private static final Log log = Logs.get();
    @Inject
    private SysWxService sysWxService;

    @At("")
    @Ok("beetl:/platform/sys/wx/token/index.html")
    @RequiresPermissions("sys.wx.token")
    public void index() {
    }

    @At("/data")
    @Ok("json")
    @RequiresPermissions("sys.wx.token")
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
		Cnd cnd = Cnd.NEW();
    	return sysWxService.data(length, start, draw, order, columns, cnd, null);
    }

    @At("/add")
    @Ok("beetl:/platform/sys/wx/token/add.html")
    @RequiresPermissions("sys.wx.token")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("sys.wx.token.add")
    @SLog(tag = "Sys_wx", msg = "${args[0].id}")
    public Object addDo(@Param("..")Sys_wx sysWx, HttpServletRequest req) {
		try {
			sysWxService.insert(sysWx);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/sys/wx/token/edit.html")
    @RequiresPermissions("sys.wx.token")
    public void edit(String id,HttpServletRequest req) {
		req.setAttribute("obj", sysWxService.fetch(id));
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("sys.wx.token.edit")
    @SLog(tag = "Sys_wx", msg = "${args[0].id}")
    public Object editDo(@Param("..")Sys_wx sysWx, HttpServletRequest req) {
		try {
            sysWx.setOpBy(StringUtil.getUid());
			sysWx.setOpAt((int) (System.currentTimeMillis() / 1000));
			sysWxService.updateIgnoreNull(sysWx);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("sys.wx.token.delete")
    @SLog(tag = "Sys_wx", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				sysWxService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				sysWxService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/sys/wx/token/detail.html")
    @RequiresPermissions("sys.wx.token")
	public void detail(String id, HttpServletRequest req) {
		if (!Strings.isBlank(id)) {
            req.setAttribute("obj", sysWxService.fetch(id));
		}else{
            req.setAttribute("obj", null);
        }
    }

    @At("/postWXToken")
    @Ok("jsonp:full")
    public Object postWXToken(@Param("corpid")String corpid,@Param("corpsecret") String corpsecret){
        //获取当前位置
        String url="https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+corpsecret;
        PostRun pr=new PostRun();
        String Json=pr.WXTokenget(url);
        return Json;
    }

    @At("/postWXToken2")
    @Ok("json")
    public Object postWXToken2(@Param("corpid")String corpid,@Param("corpsecret") String corpsecret){
        //获取当前位置
        String url="https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+corpsecret;
        PostRun pr=new PostRun();
        String Json=pr.WXTokenget(url);
        return Json;
    }

    public Object isNewToken(@Param("newtoken") String newtoken){
        try{

            List<Sys_wx> wx = sysWxService.query();
            if(wx.size()>0){
                if(!Strings.isBlank(newtoken)){
                    if(wx.get(0).getToken()==newtoken){
                        return false;
                    }else{
                        return true;
                    }
                }
            }
            return null;
        }catch(Exception e){
            return null;
        }

    }

    @At("/checkTokenExpire")
    @Ok("jsonp:full")
    public Object checkTokenExpire(){
        try{
            List<Sys_wx> wxList=sysWxService.query();
            if(wxList.size()>0){
                SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
                Date date = sdf.parse(wxList.get(0).getEndtime().toString());
                Date day=new Date();
                if(day.getTime()>date.getTime()){//已过期
                    //当前时间大于到期时间,说明需要获取新的token了
                    return "off";
                }else
                {
                    return wxList.get(0).getToken();//未过期
                }
            }
            else{//没有token说明要新增token
                return "need";
            }
        }catch(Exception e){
            return null;
        }
    }

    @At("/checkTokenExpire2")
    @Ok("json")
    public Object checkTokenExpire2(){
        try{
            List<Sys_wx> wxList=sysWxService.query();
            if(wxList.size()>0){
                SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
                Date date = sdf.parse(wxList.get(0).getEndtime().toString());
                Date day=new Date();
                if(day.getTime()>date.getTime()){//已过期
                    //当前时间大于到期时间,说明需要获取新的token了
                    return "off";
                }else
                {
                    return wxList.get(0).getToken();//未过期
                }
            }
            else{//没有token说明要新增token
                return "need";
            }
        }catch(Exception e){
            return null;
        }
    }

    @At("/insertWxToken")
    @Ok("jsonp:full")
    public Object insertWxToken(@Param("token") String token,@Param("expire") String expire,@Param("flag") String flag){
        try{
            if(!Strings.isBlank(token)&&!Strings.isBlank(expire)){
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long timeLose=System.currentTimeMillis()+Integer.parseInt(expire)*1000;
                if(flag.equals("off")){
                    List<Sys_wx> list = sysWxService.query();
                    Sys_wx wx=list.get(0);
                    wx.setToken(token);
                    wx.setExpire(expire);
                    wx.setStarttime(newDataTime.getDateYMDHMS());
                    wx.setEndtime(sdf.format(new Date(timeLose)));
                    sysWxService.updateIgnoreNull(wx);
                    return Result.success("更新成功");
                }else if(flag.equals("need")){
                    Sys_wx wx=new Sys_wx();
                    wx.setToken(token);
                    wx.setExpire(expire);
                    wx.setStarttime(newDataTime.getDateYMDHMS());
                    wx.setEndtime(sdf.format(new Date(timeLose)));
                    sysWxService.insert(wx);
                    return Result.success("新增成功");
                }
            }
            return null;
        }catch (Exception e){
            return  null;
        }
    }

    @At("/insertWxToken2")
    @Ok("json")
    public Object insertWxToken2(@Param("token") String token,@Param("expire") String expire,@Param("flag") String flag){
        try{
            if(!Strings.isBlank(token)&&!Strings.isBlank(expire)){
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long timeLose=System.currentTimeMillis()+Integer.parseInt(expire)*1000;
                if(flag.equals("off")){
                    List<Sys_wx> list = sysWxService.query();
                    Sys_wx wx=list.get(0);
                    wx.setToken(token);
                    wx.setExpire(expire);
                    wx.setStarttime(newDataTime.getDateYMDHMS());
                    wx.setEndtime(sdf.format(new Date(timeLose)));
                    sysWxService.updateIgnoreNull(wx);
                    return Result.success("更新成功");
                }else if(flag.equals("need")){
                    Sys_wx wx=new Sys_wx();
                    wx.setToken(token);
                    wx.setExpire(expire);
                    wx.setStarttime(newDataTime.getDateYMDHMS());
                    wx.setEndtime(sdf.format(new Date(timeLose)));
                    sysWxService.insert(wx);
                    return Result.success("新增成功");
                }
            }
            return null;
        }catch (Exception e){
            return  null;
        }
    }

    //微信发送消息，现已失效 蔡巍20180207
    @At("/sendMsg")
    @Ok("jsonp:full")
    public Object sendMsg(@Param("touser") String touser,@Param("msgtype") String msgtype,@Param("agentid") String agentid,@Param("content") String content,@Param("wxtoken") String wxtoken){
        try{
            //获取当前位置
            String url="https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token="+wxtoken;
            PostRun pr=new PostRun();
            String Json=pr.WXMsgpost(url,"caiwei","","",msgtype,agentid,content,"0");
            return Json;
        }catch (Exception e){
            return null;
        }
    }

    @At("/updateToken")
    @Ok("json")
    @RequiresPermissions("sys.wx.token")
    public Object updateToken(@Param("id")String id,HttpServletRequest req){
        try{
            if(StringUtils.isNotBlank(id)){
                Sys_wx wx = sysWxService.fetch(id);
                Cnd cnd = Cnd.NEW();
                cnd.and("agentid","=",wx.getAgentid());
                sysWxService.getTokenOrNew(cnd);
                return Result.success("system.success");
            }
            throw new ValidatException("请求参数不合法！");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }


}
