package cn.wizzer.app.web.modules.controllers.platform.eq;

import cn.wizzer.app.web.modules.controllers.platform.base.BaseCnctobjController;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.eq.modules.models.eq_lock;
import cn.wizzer.app.eq.modules.services.EqLockService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@IocBean
@At("/platform/eq/lock")
public class EqLockController{
    private static final Log log = Logs.get();
    @Inject
    private EqLockService eqLockService;
    @Inject
    private BaseCnctobjService baseCnctobjService;
    @Inject
    private BasePersonService basePersonService;

    @At("")
    @Ok("beetl:/platform/eq/lock/index.html")
    @RequiresPermissions("platform.eq.lock")
    public void index() {
    }

    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.eq.lock")
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
		Cnd cnd = Cnd.NEW();
    	return eqLockService.data(length, start, draw, order, columns, cnd, null);
    }

    @At("/add")
    @Ok("beetl:/platform/eq/lock/add.html")
    @RequiresPermissions("platform.eq.lock")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.lock.add")
    @SLog(tag = "eq_lock", msg = "${args[0].id}")
    public Object addDo(@Param("..")eq_lock eqLock, HttpServletRequest req) {
		try {
			eqLockService.insert(eqLock);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/eq/lock/edit.html")
    @RequiresPermissions("platform.eq.lock")
    public void edit(String id,HttpServletRequest req) {
        Cnd cnd=Cnd.NEW();
        cnd.and("id","=",id);
        List<eq_lock> locks=eqLockService.query(cnd,"airport");
		req.setAttribute("obj", locks.get(0));
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.lock.edit")
    @SLog(tag = "eq_lock", msg = "${args[0].id}")
    public Object editDo(@Param("..")eq_lock eqLock, HttpServletRequest req) {
		try {
            eqLock.setOpBy(StringUtil.getUid());
			eqLock.setOpAt((int) (System.currentTimeMillis() / 1000));
			eqLockService.updateIgnoreNull(eqLock);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.eq.lock.delete")
    @SLog(tag = "eq_lock", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				eqLockService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				eqLockService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/eq/lock/detail.html")
    @RequiresAuthentication
	public void detail(String id, HttpServletRequest req) {
        Cnd cnd = Cnd.NEW();
		if (!Strings.isBlank(id)) {
		    cnd.and("id","=",id);
		    List<eq_lock> locks = eqLockService.query(cnd,"airport");
            req.setAttribute("obj", locks.get(0));
		}else{
            req.setAttribute("obj", null);
        }
    }

    @At({"/unlock/?"})
    @Ok("json")
    @RequiresPermissions("platform.eq.lock.unlock")
    @SLog(tag = "eq_lock", msg = "${req.getAttribute('id')}")
    public Object unlock(String id, @Param("type")String type, HttpServletRequest req) {
        try {
            int ret = eqLockService.unLock(id,type);
            if(ret==1) {
                return Result.success("system.success");
            }else if(ret==0){
                return Result.error("设备信号终断!");
            }else{
                return Result.error("发送操作失败!");
            }
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/getlockData")
    @Ok("json")
    public Object getlockData(@Param("id") String id){
        try{
            if(!Strings.isBlank(id)){
                Cnd cnd = Cnd.NEW();
                cnd.and("id","=",id);
                return eqLockService.query(cnd,"airport");
            }
            return null;
        }
        catch (Exception e){
            return null;
        }
    }

    @At("/getLockbyParam")
    @Ok("json")
    public Object getLockbyParam(@Param("locknum")String locknum,@Param("lockstatus") String lockstatus,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns){
        try{
            String airportid="";
            if(!Strings.isBlank(lockstatus)){
                String personid = baseCnctobjService.getPersonIDByUserID();
                if(!Strings.isBlank(personid))
                    airportid = basePersonService.fetch(personid).getAirportid();

                Cnd cnd=Cnd.NEW();
                cnd.and("lockstatus","=",lockstatus);
                if(!airportid.equals("")){
                    cnd.and("airportId","=",airportid);
                }
                if(!Strings.isBlank(locknum)){
                    locknum = locknum.replace("'","");
                    cnd.and("locknum","like","%" + locknum + "%");
                }
                return eqLockService.data(length, start, draw, order, columns,cnd,"airport");
            }
            return null;
        }catch (Exception e){
            return null;
        }

    }


    @At("/getCountBylocknum")
    @Ok("json")
    public Object getCountBylocknum(String locknum){
        return eqLockService.count("eq_lock", Cnd.where("locknum", "=", locknum));

    }
}
