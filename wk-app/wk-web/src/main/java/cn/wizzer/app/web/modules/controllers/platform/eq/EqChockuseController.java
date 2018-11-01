package cn.wizzer.app.web.modules.controllers.platform.eq;

import cn.wizzer.app.eq.modules.services.EqUseService;
import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.app.web.modules.controllers.open.api.Datatime.newDataTime;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.eq.modules.models.eq_chockuse;
import cn.wizzer.app.eq.modules.models.eq_use;
import cn.wizzer.app.eq.modules.services.EqChockuseService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.bouncycastle.jce.exception.ExtCertPathBuilderException;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@IocBean
@At("/platform/eq/chockuse")
public class EqChockuseController{
    private static final Log log = Logs.get();
    @Inject
    private EqChockuseService eqChockuseService;
    @Inject
    private EqUseService eqUseService;

    @At("")
    @Ok("beetl:/platform/eq/chockuse/index.html")
    @RequiresPermissions("platform.eq.chockuse")
    public void index() {
    }

    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.eq.chockuse")
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
		Cnd cnd = Cnd.NEW();
    	return eqChockuseService.data(length, start, draw, order, columns, cnd, null);
    }

    @At("/add")
    @Ok("beetl:/platform/eq/chockuse/add.html")
    @RequiresPermissions("platform.eq.chockuse")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.chockuse.add")
    @SLog(tag = "eq_chockuse", msg = "${args[0].id}")
    public Object addDo(@Param("..")eq_chockuse eqChockuse, HttpServletRequest req) {
		try {
			eqChockuseService.insert(eqChockuse);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/eq/chockuse/edit.html")
    @RequiresPermissions("platform.eq.chockuse")
    public void edit(String id,HttpServletRequest req) {
		req.setAttribute("obj", eqChockuseService.fetch(id));
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.chockuse.edit")
    @SLog(tag = "eq_chockuse", msg = "${args[0].id}")
    public Object editDo(@Param("..")eq_chockuse eqChockuse, HttpServletRequest req) {
		try {
            eqChockuse.setOpBy(StringUtil.getUid());
			eqChockuse.setOpAt((int) (System.currentTimeMillis() / 1000));
			eqChockuseService.updateIgnoreNull(eqChockuse);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.eq.chockuse.delete")
    @SLog(tag = "eq_chockuse", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				eqChockuseService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				eqChockuseService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/eq/chockuse/detail.html")
    @RequiresAuthentication
	public void detail(String id, HttpServletRequest req) {
		if (!Strings.isBlank(id)) {
            req.setAttribute("obj", eqChockuseService.fetch(id));
		}else{
            req.setAttribute("obj", null);
        }
    }

    @At("/insertChock")
    @Ok("jsonp:full")
    public Object insertChock(@Param("eqid") String eqid,@Param("seatid") String seatid,@Param("starttime") String starttime,@Param("personid") String personid,@Param("pstatus") String pstatus){
        try{
            if(!Strings.isBlank(eqid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("eqid","=",eqid);
                eq_use use = eqUseService.fetch(cnd);
                if(!use.equals(null)){
                    eq_chockuse chockuse=new eq_chockuse();
                    chockuse.setEqid(eqid);
                    if(!Strings.isBlank(personid))
                        chockuse.setGetpersonid(personid);
                    if(!Strings.isBlank(seatid))
                        chockuse.setSeatid(seatid);
                    if(!Strings.isBlank(starttime))
                        chockuse.setStarttime(starttime);
                    if(!Strings.isBlank(pstatus))
                        chockuse.setPstatus(pstatus);
                    chockuse.setStakeid(use.getStakeid());
                    eqChockuseService.insert(chockuse);
                    return Result.success("OK");
                }

            }
            return  Result.error("fail");

        }catch(Exception e){
            return  Result.error("fail");
        }

    }

    @At("/insertChockbyUseinfo")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object insertChockbyUseinfo(@Param("useinfoid") String useinfoid,@Param("userid") String userid,@Param("starttime") String starttime){
        try{
            if(!Strings.isBlank(useinfoid)){
                    eq_chockuse chockuse=new eq_chockuse();
                    chockuse.setUseinfoID(useinfoid);
                    if(!Strings.isBlank(userid))
                        chockuse.setGetpersonid(userid);
                        chockuse.setStarttime(starttime);
                        chockuse.setSstarttime(newDataTime.getDateYMDHMS());
                        chockuse.setPstatus("1");
                    eqChockuseService.insert(chockuse);
                    return Result.success("OK");
                }
            return  Result.error(-1,"fail");
        }catch(Exception e){
            e.printStackTrace();
            return  Result.error(-1,"fail");
        }

    }

    @At("/updateChock")
    @Ok("jsonp:full")
    public Object updateChock(@Param("eqid") String eqid,@Param("seatid") String seatid,@Param("endtime") String endtime,@Param("pstatus") String pstatus,@Param("personid") String personid)
    {
        try{
            if(!Strings.isBlank(eqid)&&!Strings.isBlank(seatid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("eqid","=",eqid);
//                cnd.and("seatid","=",seatid);
                cnd.and("pstatus","=","0");
                List<eq_chockuse> chockuseList = eqChockuseService.query(cnd);
                if(chockuseList.size()>0){
                    chockuseList.get(0).setEndtime(endtime);
                    chockuseList.get(0).setSendtime(newDataTime.getDateYMDHMS());
                    chockuseList.get(0).setSeatid(seatid);
                    chockuseList.get(0).setPstatus(pstatus);
                    chockuseList.get(0).setGopersonid(personid);
                    eqChockuseService.updateIgnoreNull(chockuseList.get(0));
                    Result.success("OK");
                }
            }
            return  Result.error("fail");
        }catch(Exception e){
            return  Result.error("fail");
        }
    }

    @At("/updateChockbyUseinfo")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object updateChockbyUseinfo(@Param("useinfoid") String useinfoid,@Param("userid") String userid,@Param("endtime") String endtime)
    {
        try{
            if(!Strings.isBlank(useinfoid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("useinfoID","=",useinfoid);
                cnd.and("pstatus","=","1");
                List<eq_chockuse> chockuseList = eqChockuseService.query(cnd);
                if(chockuseList.size()>0){
                    chockuseList.get(0).setEndtime(endtime);
                    chockuseList.get(0).setSendtime(newDataTime.getDateYMDHMS());
                    chockuseList.get(0).setGopersonid(userid);
                    chockuseList.get(0).setPstatus("0");
                    eqChockuseService.updateIgnoreNull(chockuseList.get(0));
                    String date="";
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    try
//                    {
                        Date d1 = df.parse(chockuseList.get(0).getStarttime());
                        Date d2 = df.parse(endtime);
                        long diff = d2.getTime() - d1.getTime();//这样得到的差值是微秒级别
                        long days = diff / (1000 * 60 * 60 * 24);

                        long hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
                        long minutes = (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60))/(1000* 60);
                        long seconds = ((diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60)-minutes*(1000*60))/1000);

                        date=((days*24+hours)>9?(days*24+hours):"0"+(days*24+hours))+":"+(minutes>9?minutes:"0"+minutes)+":"+(seconds>9?seconds:"0"+seconds);
//                    }catch (Exception e)
//                    {
//
//                    }

                    return  Result.success(date);
                }
            }
            return  Result.error(-1,"fail");
        }catch(Exception e){
            e.printStackTrace();
            return  Result.error(-1,"fail");
        }
    }


    @At("/getChockstatus")
    @Ok("jsonp:full")
    public Object getChockstatus(@Param("eqid") String eqid){
        try{
            if(!Strings.isBlank(eqid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("eqid","=",eqid);
                cnd.and("pstatus","=","0");
                List<eq_chockuse> chockuses = eqChockuseService.query(cnd,"planeseat");
                if(chockuses.size()>0)
                {
                    return chockuses.get(0);
                }
            }
            return null;
        }catch(Exception e){
            return null;
        }
    }

    @At("/getChockbyuiID")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getChockbyuiID(@Param("useinfoid") String useinfoid){
        try{
            if(!Strings.isBlank(useinfoid)){
                Cnd cnd =Cnd.NEW();
                cnd.and("useinfoid","=",useinfoid);
//                cnd.and("pstatus","=","1");
                eq_chockuse chockuse=eqChockuseService.fetch(cnd);
                HashMap map=new HashMap();
                if(chockuse!=null){
                    map.put("pstatus",chockuse.getPstatus());
                    map.put("starttime",chockuse.getStarttime());
                    map.put("endtime",chockuse.getEndtime());
                    map.put("id",chockuse.getId());
                }
                return Result.success("system.success",map);
            }
            return Result.error(2,"useinfo is null");
        }
        catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

}
