package cn.wizzer.app.web.modules.controllers.platform.eq;

import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.eq.modules.models.eq_power;
import cn.wizzer.app.eq.modules.services.EqPowerService;
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
import java.util.List;

@IocBean
@At("/platform/eq/power")
public class EqPowerController{
    private static final Log log = Logs.get();
    @Inject
    private EqPowerService eqPowerService;

    @At("")
    @Ok("beetl:/platform/eq/power/index.html")
    @RequiresPermissions("platform.eq.power")
    public void index() {
    }

    @At("/data")
    @Ok("json:full")
    @RequiresPermissions("platform.eq.power")
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
		Cnd cnd = Cnd.NEW();
    	return eqPowerService.data(length, start, draw, order, columns, cnd, null);
    }

    @At("/add")
    @Ok("beetl:/platform/eq/power/add.html")
    @RequiresPermissions("platform.eq.power")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.power.add")
    @SLog(tag = "eq_power", msg = "${args[0].id}")
    public Object addDo(@Param("..")eq_power eqPower, HttpServletRequest req) {
		try {
			eqPowerService.insert(eqPower);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/eq/power/edit.html")
    @RequiresPermissions("platform.eq.power")
    public void edit(String id,HttpServletRequest req) {
		req.setAttribute("obj", eqPowerService.fetch(id));
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.power.edit")
    @SLog(tag = "eq_power", msg = "${args[0].id}")
    public Object editDo(@Param("..")eq_power eqPower, HttpServletRequest req) {
		try {
            eqPower.setOpBy(StringUtil.getUid());
			eqPower.setOpAt((int) (System.currentTimeMillis() / 1000));
			eqPowerService.updateIgnoreNull(eqPower);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.eq.power.delete")
    @SLog(tag = "eq_power", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				eqPowerService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				eqPowerService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/eq/power/detail.html")
    @RequiresAuthentication
	public void detail(String id, HttpServletRequest req) {
        		if (!Strings.isBlank(id)) {
            req.setAttribute("obj", eqPowerService.fetch(id));
		}else{
            req.setAttribute("obj", null);
        }}

}
