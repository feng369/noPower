package cn.wizzer.app.web.modules.controllers.platform.base;

import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.sys.modules.services.SysUnitService;
import cn.wizzer.app.sys.modules.services.SysUserService;
import cn.wizzer.app.sys.modules.services.SysRoleService;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.base.modules.models.base_dept;
import cn.wizzer.app.base.modules.services.BaseDeptService;
import org.apache.commons.lang3.StringUtils;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IocBean
@At("/platform/base/dept")
public class BaseDeptController{
    private static final Log log = Logs.get();
    @Inject
    private BaseDeptService baseDeptService;

    @Inject
    private SysUnitService unitService;

    @Inject
    private SysUserService sysUserService;

    @Inject
    private BaseCnctobjService baseCnctobjService;

    @Inject
    private  SysRoleService sysRoleService;

    @At("")
    @Ok("beetl:/platform/base/dept/index.html")
    @RequiresPermissions("platform.base.dept")
    public Object index() {
        Cnd cnd=Cnd.NEW();
        cnd.exps("parentId", "=", "").or("parentId", "is", null);
        cnd= sysRoleService.getPermission(cnd,"unitid","","creater",true);
        cnd.asc("path");
        return baseDeptService.query(cnd,"unit");

    }

    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.base.dept.select")
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        Cnd cnd = Cnd.NEW();

        return baseDeptService.data(length, start, draw, order, columns, cnd, "unit");
    }

    @At("/add")
    @Ok("beetl:/platform/base/dept/add.html")
    @RequiresPermissions("platform.base.dept")
    public Object add(@Param("pid") String pid) {
        if(Strings.isBlank(pid)){
            return null;
        }else{
            base_dept dept = baseDeptService.fetch(pid);
            return baseDeptService.fetchLinks(dept,"unit");
        }

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.base.dept.add")
    @SLog(tag = "base_dept", msg = "${args[0].id}")
    public Object addDo(@Param("..") base_dept baseDept, @Param("parentId") String parentId, HttpServletRequest req) {
        try {
            baseDeptService.save(baseDept, parentId);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/edit/?")
    @Ok("beetl:/platform/base/dept/edit.html")
    @RequiresPermissions("platform.base.dept")
    public Object edit(String id,HttpServletRequest req) {
        //req.setAttribute("obj", baseDeptService.fetch(id));
        base_dept dept = baseDeptService.fetch(id);
        if (dept != null) {
            req.setAttribute("unit", unitService.fetch(dept.getUnitid()));
            req.setAttribute("parentDept", baseDeptService.fetch(dept.getParentId()));
            req.setAttribute("creater",sysUserService.fetch(dept.getCreater()));
            req.setAttribute("opby",sysUserService.fetch(dept.getOpBy()));
        }

        return dept;
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.base.dept.edit")
    @SLog(tag = "base_dept", msg = "${args[0].id}")
    public Object editDo(@Param("..")base_dept baseDept, HttpServletRequest req) {
        try {
            baseDept.setOpBy(StringUtil.getUid());
            baseDept.setOpAt((int) (System.currentTimeMillis() / 1000));
            baseDeptService.update(baseDept);
            //baseDeptService.updateIgnoreNull(baseDept);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }
    @At("/child/?")
    @Ok("beetl:/platform/base/dept/child.html")
    @RequiresPermissions("platform.base.dept")
    public Object child(String id) {
        return baseDeptService.query(Cnd.where("parentId", "=", id).asc("path"),"unit");
    }



    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.base.dept.delete")
    @SLog(tag = "base_dept", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
        try {
            base_dept dept = baseDeptService.fetch(id);
            req.setAttribute("name", dept.getDeptname());

            baseDeptService.deleteAndChild(dept);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/base/dept/detail.html")
    @RequiresAuthentication
    public Object detail(String id, HttpServletRequest req) {

        base_dept dept = baseDeptService.fetch(id);
        if(dept!=null){
            req.setAttribute("unit", unitService.fetch(dept.getUnitid()));
            req.setAttribute("parentDept", baseDeptService.fetch(dept.getParentId()));
            req.setAttribute("creater",sysUserService.fetch(dept.getCreater()));
            req.setAttribute("opby",sysUserService.fetch(dept.getOpBy()));
        }
        return dept;
    }

    @At
    @Ok("json")
    @RequiresPermissions("sys.manager.unit")
    public Object tree(@Param("pid") String pid) {
        List<base_dept> list = baseDeptService.query(Cnd.where("parentId", "=", Strings.sBlank(pid)).asc("path"));
        List<Map<String, Object>> tree = new ArrayList<>();
        for (base_dept dept : list) {
            Map<String, Object> obj = new HashMap<>();
            obj.put("id", dept.getId());
            obj.put("text", dept.getDeptname());
            obj.put("children", dept.isHasChildren());
            tree.add(obj);
        }
        return tree;
    }

    //20180320zhf1117
    @At("/getCountBydeptnum")
    @Ok("json:full")
    public Object getCountBydeptnum(String deptnum){
        return baseDeptService.count("base_dept", Cnd.where("deptnum", "=", deptnum));
    }

}
