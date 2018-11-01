package cn.wizzer.app.web.modules.controllers.platform.sys;

import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.sys.modules.models.Sys_mobile;
import cn.wizzer.app.sys.modules.services.SysMobileService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import cn.wizzer.app.web.modules.controllers.open.api.Datatime.newDataTime;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.filter.CrossOriginFilter;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@IocBean
@At("/platform/sys/mobile")
public class SysMobileController{
    private static final Log log = Logs.get();
    @Inject
    private SysMobileService sysMobileService;

    @At("")
    @Ok("beetl:/platform/sys/mobile/index.html")
    @RequiresPermissions("platform.sys.mobile")
    public void index() {
    }

    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.sys.mobile")
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
		Cnd cnd = Cnd.NEW();
    	return sysMobileService.data(length, start, draw, order, columns, cnd, null);
    }

    @At("/add")
    @Ok("beetl:/platform/sys/mobile/add.html")
    @RequiresPermissions("platform.sys.mobile")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.sys.mobile.add")
    @SLog(tag = "Sys_mobile", msg = "${args[0].id}")
    public Object addDo(@Param("..")Sys_mobile sysMobile, HttpServletRequest req) {
		try {
			sysMobileService.insert(sysMobile);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/sys/mobile/edit.html")
    @RequiresPermissions("platform.sys.mobile")
    public void edit(String id,HttpServletRequest req) {
		req.setAttribute("obj", sysMobileService.fetch(id));
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.sys.mobile.edit")
    @SLog(tag = "Sys_mobile", msg = "${args[0].id}")
    public Object editDo(@Param("..")Sys_mobile sysMobile, HttpServletRequest req) {
		try {
            sysMobile.setOpBy(StringUtil.getUid());
			sysMobile.setOpAt((int) (System.currentTimeMillis() / 1000));
			sysMobileService.updateIgnoreNull(sysMobile);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.sys.mobile.delete")
    @SLog(tag = "Sys_mobile", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				sysMobileService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				sysMobileService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/sys/mobile/detail.html")
    @RequiresPermissions("platform.sys.mobile")
	public void detail(String id, HttpServletRequest req) {
		if (!Strings.isBlank(id)) {
            req.setAttribute("obj", sysMobileService.fetch(id));
		}else{
            req.setAttribute("obj", null);
        }
    }


    @At("/isExpire")
    @Ok("jsonp:full")
    public Object isExpire(@Param("deviceid") String deviceid){
        int i=0;
        Cnd cnd=Cnd.NEW();
        cnd.and("deviceid","=",deviceid);
        List<Sys_mobile> mobile=sysMobileService.query(cnd,"user");
        if(mobile.size()>0){
            //说明存在此数据
            cnd.and("pstatus","=","1");
            mobile=sysMobileService.query(cnd,"user");
            if(mobile.size()>0){
                //用户打开了自动登录
                if(!Strings.isBlank(mobile.get(0).getStartDate())&&!Strings.isBlank(mobile.get(0).getEndDate())){
                    if(java.sql.Date.valueOf(mobile.get(0).getEndDate()).after(java.sql.Date.valueOf(newDataTime.getDateYMDHMS()))
                            &&java.sql.Date.valueOf(newDataTime.getDateYMDHMS()).after(java.sql.Date.valueOf(mobile.get(0).getStartDate()))){
                        //结束时间大于当前时间，当前时间大于开始时间，说明在有效期内
                        return mobile.get(0);
                    }
                    else{//过期
                        return 2;
                    }
                }else{//过期
                    return 2;
                }

            }else{
                //自动登录已经被用户关掉
                return 1;
            }
        }else
        {
            //不存在的数据
            return 0;
        }
    }

    @At("/isExpire2")
    @Ok("json")
    public Object isExpire2(@Param("deviceid") String deviceid){
        int i=0;
        Cnd cnd=Cnd.NEW();
        cnd.and("deviceid","=",deviceid);
        List<Sys_mobile> mobile=sysMobileService.query(cnd,"user");
        if(mobile.size()>0){
            //说明存在此数据
            cnd.and("pstatus","=","1");
            mobile=sysMobileService.query(cnd,"user");
            if(mobile.size()>0){
                //用户打开了自动登录
                if(!Strings.isBlank(mobile.get(0).getStartDate())&&!Strings.isBlank(mobile.get(0).getEndDate())){
                    if(java.sql.Date.valueOf(mobile.get(0).getEndDate()).after(java.sql.Date.valueOf(newDataTime.getDateYMDHMS()))
                            &&java.sql.Date.valueOf(newDataTime.getDateYMDHMS()).after(java.sql.Date.valueOf(mobile.get(0).getStartDate()))){
                        //结束时间大于当前时间，当前时间大于开始时间，说明在有效期内
                        return mobile.get(0);
                    }
                    else{//过期
                        return 2;
                    }
                }else{//过期
                    return 2;
                }

            }else{
                //自动登录已经被用户关掉
                return 1;
            }
        }else
        {
            //不存在的数据
            return 0;
        }
    }

    @At("/setDeviceLogin")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object setDeviceLogin(@Param("deviceid") String deviceid,@Param("deviceOS") String deviceOS,@Param("deviceModel") String deviceModel,@Param("userid") String userid,@Param("expire") String expire,@Param("pstatus") String pstatus,@Param("account") String account,@Param("password") String password)
    {
        boolean insert=false;
        try {
            Cnd cnd=Cnd.NEW();
            cnd.and("deviceid","=",deviceid);
            Sys_mobile mobile=sysMobileService.fetch(cnd);
            if(mobile==null){
                mobile=new Sys_mobile();
                insert=true;
            }
            if(!Strings.isBlank(deviceid))
                mobile.setDeviceid(deviceid);
            if(!Strings.isBlank(deviceModel))
                mobile.setDeviceModel(deviceModel);
            if(!Strings.isBlank(deviceOS))
                mobile.setDeviceOS(deviceOS);
            if(!Strings.isBlank(pstatus))
                mobile.setPstatus(pstatus);
            if(!Strings.isBlank(userid))
                mobile.setUserid(userid);
            if(!Strings.isBlank(account))
                mobile.setAccount(account);
            if(!Strings.isBlank(password))
                mobile.setPassword(password);
            if(!Strings.isBlank(expire)){
                mobile.setStartDate(newDataTime.getDateYMDHMS());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DATE, Integer.parseInt(expire));
                String endDate = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(calendar.getTime());
                mobile.setEndDate(endDate);
            }

            if(insert==false){
                sysMobileService.update(mobile);

            }else{
                sysMobileService.insert(mobile);
            }
            return Result.success("设置成功");
        }catch(Exception e){
            return Result.error(-1,"设置失败");
        }
    }

    @At("/getDeviceInfo")
    @Ok("json")
    @Filters(@By(type=CrossOriginFilter.class))
    public Object getDeviceInfo(@Param("deviceid") String deviceid){
        try{
            Cnd cnd=Cnd.NEW();
            cnd.and("deviceid","=",deviceid).desc("createTime");
//            List<Sys_mobile> mobile=sysMobileService.query(cnd,"user");
            Sys_mobile mobile=sysMobileService.fetch(cnd);
            if(mobile!=null){
                HashMap map=new HashMap();
                map.put("account",mobile.getAccount());
                map.put("password",mobile.getPassword());
                map.put("enddate",mobile.getEndDate());
                return Result.success("system.success",map);
            }
            return Result.error(2,"设备信息为空!");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getQueryDistance")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getQueryDistance(){
        try{
            HashMap map =new HashMap();
            map.put("dis",Globals.QueryDis);
            return Result.success("system.success",map);
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getMobileExpire2")
    @Ok("json")
    public Object getMobileExpire2(){
        try{
            HashMap map=new HashMap();
            map.put("code","0");
            map.put("mexpire",Globals.MobileExpire);
            return map;
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getAppZoom")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getAppZoom(){
        try{
            HashMap map =new HashMap();
            map.put("zoom",Globals.AppZoom);
            return Result.success("system.success",map);
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getH5AppZoom")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getH5AppZoom(){
        try{
            HashMap map =new HashMap();
            map.put("zoom",Globals.H5AppZoom);
            return Result.success("system.success",map);
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getShowCircle")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getShowCircle(){
        try{
            HashMap map =new HashMap();
            map.put("circle",Globals.ShowCircle);
            return Result.success("system.success",map);
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getDefaultAirport")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class)})
    public Object getDefaultAirport(){
        try{
            HashMap map =new HashMap();
            map.put("airportid",Globals.defaultAirport);
            return Result.success("system.success",map);
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }
}
