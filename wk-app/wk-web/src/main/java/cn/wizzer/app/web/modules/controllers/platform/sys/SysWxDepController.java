package cn.wizzer.app.web.modules.controllers.platform.sys;

import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.sys.modules.models.Sys_wx_dep;
import cn.wizzer.app.sys.modules.services.SysWxDepService;
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
@At("/platform/sys/wx/dep")
public class SysWxDepController{
    private static final Log log = Logs.get();
    @Inject
    private SysWxDepService sysWxDepService;

    @At("")
    @Ok("beetl:/platform/sys/wx/dep/index.html")
    @RequiresPermissions("sys.wx.dep")
    public void index() {
    }

    @At("/data")
    @Ok("json:full")
    @RequiresPermissions("sys.wx.dep")
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
		Cnd cnd = Cnd.NEW();
    	return sysWxDepService.data(length, start, draw, order, columns, cnd, null);
    }

    @At("/add")
    @Ok("beetl:/platform/sys/wx/dep/add.html")
    @RequiresPermissions("sys.wx.dep")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("sys.wx.dep.add")
    @SLog(tag = "企业微信部门表", msg = "${args[0].id}")
    public Object addDo(@Param("..")Sys_wx_dep sysWxDep, HttpServletRequest req) {
		try {
			sysWxDepService.insert(sysWxDep);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/sys/wx/dep/edit.html")
    @RequiresPermissions("sys.wx.dep")
    public void edit(String id,HttpServletRequest req) {
		req.setAttribute("obj", sysWxDepService.fetch(id));
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("sys.wx.dep.edit")
    @SLog(tag = "企业微信部门表", msg = "${args[0].id}")
    public Object editDo(@Param("..")Sys_wx_dep sysWxDep, HttpServletRequest req) {
		try {
            sysWxDep.setOpBy(StringUtil.getUid());
			sysWxDep.setOpAt((int) (System.currentTimeMillis() / 1000));
			sysWxDepService.updateIgnoreNull(sysWxDep);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("sys.wx.dep.delete")
    @SLog(tag = "企业微信部门表", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				sysWxDepService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				sysWxDepService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/sys/wx/dep/detail.html")
    @RequiresPermissions("sys.wx.dep")
	public void detail(String id, HttpServletRequest req) {
        		if (!Strings.isBlank(id)) {
            req.setAttribute("obj", sysWxDepService.fetch(id));
		}else{
            req.setAttribute("obj", null);
        }
    }

    @At("/download")
    @Ok("json")
    @RequiresPermissions("sys.wx.dep.download")
    @SLog(tag = "企业微信部门", msg = "下载部门信息")
    public Object download(HttpServletRequest req) {
        try {
            sysWxDepService.download();
            return Result.success("system.success");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

}
