package cn.wizzer.app.web.modules.controllers.platform.eq;

import cn.wizzer.app.sys.modules.services.SysDictService;
import cn.wizzer.app.sys.modules.services.SysUnitService;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.eq.modules.models.eq_charging;
import cn.wizzer.app.eq.modules.services.EqChargingService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@IocBean
@At("/platform/eq/charging")
public class EqChargingController{
    private static final Log log = Logs.get();
    @Inject
    private EqChargingService eqChargingService;

    @Inject
    private SysUnitService sysUnitService;

    @Inject
    private SysDictService sysDictService;
    @At("")
    @Ok("beetl:/platform/eq/charging/index.html")
    @RequiresPermissions("platform.eq.charging")
    public void index() {
    }

    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.eq.charging")
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        Cnd cnd = Cnd.NEW();
        //return  eqChargingService.getList(length,start,draw,order,columns);
        return eqChargingService.data(length, start, draw, order, columns, cnd, "sysUnit|sysDict");
    }

    @At("/add")
    @Ok("beetl:/platform/eq/charging/add.html")
    @RequiresPermissions("platform.eq.charging")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.charging.add")
    @SLog(tag = "eq_charging", msg = "${args[0].id}")
    public Object addDo(@Param("..")eq_charging eqCharging, HttpServletRequest req) {
		try {
			eqChargingService.insert(eqCharging);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/eq/charging/edit.html")
    @RequiresPermissions("platform.eq.charging")
    public void edit(String id,HttpServletRequest req) {
        eq_charging eqCharging= eqChargingService.fetch(id);

        req.setAttribute("obj", eqCharging);
        if(!Strings.isBlank(eqCharging.getUnitid())){
            req.setAttribute("sysUnit",sysUnitService.fetch(eqCharging.getUnitid()));
        }
        if(!Strings.isBlank(eqCharging.getChargingtype())){
            req.setAttribute("sysDict",sysDictService.fetch(eqCharging.getChargingtype()));
        }

    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.charging.edit")
    @SLog(tag = "eq_charging", msg = "${args[0].id}")
    public Object editDo(@Param("..")eq_charging eqCharging, HttpServletRequest req) {
		try {
            eqCharging.setOpBy(StringUtil.getUid());
			eqCharging.setOpAt((int) (System.currentTimeMillis() / 1000));
			eqChargingService.updateIgnoreNull(eqCharging);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.eq.charging.delete")
    @SLog(tag = "eq_charging", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				eqChargingService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				eqChargingService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/eq/charging/detail.html")
    @RequiresAuthentication
	public void detail(String id, HttpServletRequest req) {
		if (!Strings.isBlank(id)) {
            eq_charging eqCharging = eqChargingService.fetch(id);
            req.setAttribute("obj", eqCharging);
            if(!Strings.isBlank(eqCharging.getUnitid())){
                req.setAttribute("sysUnit",sysUnitService.fetch(eqCharging.getUnitid()));
            }
            if(!Strings.isBlank(eqCharging.getChargingtype())){
                req.setAttribute("sysDict",sysDictService.fetch(eqCharging.getChargingtype()));
            }
            req.setAttribute("obj", eqChargingService.fetch(id));
		}else{
            req.setAttribute("obj", null);
        }
    }

}
