package cn.wizzer.app.web.modules.controllers.platform.eq;

import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.eq.modules.models.eq_order;
import cn.wizzer.app.eq.modules.services.EqOrderService;
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
@At("/platform/eq/order")
public class EqOrderController{
    private static final Log log = Logs.get();
    @Inject
    private EqOrderService eqOrderService;

    @At("")
    @Ok("beetl:/platform/eq/order/index.html")
    @RequiresPermissions("platform.eq.order")
    public void index() {
    }

    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.eq.order")
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
		Cnd cnd = Cnd.NEW();
    	return eqOrderService.data(length, start, draw, order, columns, cnd, null);
    }

    @At("/add")
    @Ok("beetl:/platform/eq/order/add.html")
    @RequiresPermissions("platform.eq.order")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.order.add")
    @SLog(tag = "eq_order", msg = "${args[0].id}")
    public Object addDo(@Param("..")eq_order eqOrder, HttpServletRequest req) {
		try {
			eqOrderService.insert(eqOrder);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/eq/order/edit.html")
    @RequiresPermissions("platform.eq.order")
    public void edit(String id,HttpServletRequest req) {
		req.setAttribute("obj", eqOrderService.fetch(id));
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.order.edit")
    @SLog(tag = "eq_order", msg = "${args[0].id}")
    public Object editDo(@Param("..")eq_order eqOrder, HttpServletRequest req) {
		try {
            eqOrder.setOpBy(StringUtil.getUid());
			eqOrder.setOpAt((int) (System.currentTimeMillis() / 1000));
			eqOrderService.updateIgnoreNull(eqOrder);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.eq.order.delete")
    @SLog(tag = "eq_order", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				eqOrderService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				eqOrderService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/eq/order/detail.html")
    @RequiresAuthentication
	public void detail(String id, HttpServletRequest req) {
		if (!Strings.isBlank(id)) {
            req.setAttribute("obj", eqOrderService.fetch(id));
		}else{
            req.setAttribute("obj", null);
        }
    }

}
