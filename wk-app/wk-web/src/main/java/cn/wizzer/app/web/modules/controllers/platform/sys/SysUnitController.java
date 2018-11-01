package cn.wizzer.app.web.modules.controllers.platform.sys;

import cn.wizzer.app.sys.modules.models.Sys_unit;
import cn.wizzer.app.sys.modules.services.SysUnitService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.sys.modules.services.SysRoleService;
import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.base.Result;
import cn.wizzer.framework.util.StringUtil;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.cri.SqlExpression;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.filter.CrossOriginFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wizzer on 2016/6/24.
 */
@IocBean
@At("/platform/sys/unit")
public class SysUnitController {
    private static final Log log = Logs.get();
    @Inject
    private SysUnitService sysUnitService;
    @Inject
    private BasePersonService basePersonService;
    @Inject
    private SysRoleService sysRoleService;

    @At("")
    @Ok("beetl:/platform/sys/unit/index.html")
    @RequiresPermissions("sys.manager.unit.select")
    public void index(HttpServletRequest req) {
        Cnd cnd = Cnd.NEW();
        cnd = sysRoleService.getPermission(cnd,"id","","creater",false);
//        req.setAttribute("obj", sysUnitService.query(cnd.and("parentId", "=", "").or("parentId", "is", null).asc("path")));
        req.setAttribute("obj", sysUnitService.query(cnd.asc("path")));
    }

    @At
    @Ok("beetl:/platform/sys/unit/add.html")
    @RequiresPermissions("sys.manager.unit")
    public Object add(@Param("pid") String pid) {
        return Strings.isBlank(pid) ? null : sysUnitService.fetch(pid);
    }

    @At
    @Ok("json")
    @RequiresPermissions("sys.manager.unit.add")
    @SLog(tag = "新建单位", msg = "单位名称:${args[0].name}")
    public Object addDo(@Param("..") Sys_unit unit, @Param("parentId") String parentId, HttpServletRequest req) {
        try {
            if(!Strings.isBlank(basePersonService.getPersonInfo().getAirportid())){
                unit.setUnitairport(basePersonService.getPersonInfo().getAirportid());
            }
            sysUnitService.save(unit, parentId);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At
    @Ok("json") // 忽略password和createAt属性,忽略空属性的json输出
    @RequiresPermissions("sys.manager.unit.select")
    public Object getdata(@Param("unitid") String unitid) {
        return sysUnitService.query(Cnd.where("id", "=", unitid));
    }

    @At("/child/?")
    @Ok("beetl:/platform/sys/unit/child.html")
    @RequiresPermissions("sys.manager.unit.select")
    public Object child(String id) {
        return sysUnitService.query(Cnd.where("parentId", "=", id).asc("path"));
    }

    @At("/detail/?")
    @Ok("beetl:/platform/sys/unit/detail.html")
    @RequiresAuthentication
    public Object detail(String id) {
        return sysUnitService.fetch(id);
    }

    @At("/edit/?")
    @Ok("beetl:/platform/sys/unit/edit.html")
    @RequiresPermissions("sys.manager.unit")
    public Object edit(String id, HttpServletRequest req) {
        Sys_unit unit = sysUnitService.fetch(id);
        if (unit != null) {
            req.setAttribute("parentUnit", sysUnitService.fetch(unit.getParentId()));
        }
        return unit;
    }

    @At
    @Ok("json")
    @RequiresPermissions("sys.manager.unit.edit")
    @SLog(tag = "编辑单位", msg = "单位名称:${args[0].name}")
    public Object editDo(@Param("..") Sys_unit unit, @Param("parentId") String parentId, HttpServletRequest req) {
        try {
            unit.setOpBy(StringUtil.getUid());
            unit.setOpAt((int) (System.currentTimeMillis() / 1000));
            if(!Strings.isBlank(basePersonService.getPersonInfo().getAirportid()))
                unit.setUnitairport(basePersonService.getPersonInfo().getAirportid());
            sysUnitService.updateIgnoreNull(unit);
            if(!Strings.isBlank(unit.getParentId())){
                Sys_unit parentunit = sysUnitService.fetch(unit.getParentId());
                parentunit.setHasChildren(true);
                sysUnitService.updateIgnoreNull(parentunit);
            }

            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/delete/?")
    @Ok("json")
    @RequiresPermissions("sys.manager.unit.delete")
    @SLog(tag = "删除单位", msg = "单位名称:${args[1].getAttribute('name')}")
    public Object delete(String id, HttpServletRequest req) {
        try {
            Sys_unit unit = sysUnitService.fetch(id);
            req.setAttribute("name", unit.getName());
            if ("0001".equals(unit.getPath())) {
                return Result.error("system.not.allow");
            }
            sysUnitService.deleteAndChild(unit);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At
    @Ok("json")
    @RequiresPermissions("sys.manager.unit.select")
    public Object tree(@Param("pid") String pid) {
        Cnd cnd = Cnd.NEW();
        List<Sys_unit> list;

        if (pid == null || pid == "") {
            cnd = sysRoleService.getPermission(cnd,"id","","creater",false);
            list = sysUnitService.query(cnd.and("parentId", "=", Strings.sBlank(pid)).asc("path"));
        }else {
            list = sysUnitService.query(cnd.where("parentId", "=", Strings.sBlank(pid)).asc("path"));
        }
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Sys_unit unit : list) {
            Map<String, Object> obj = new HashMap<>();
            obj.put("id", unit.getId());
            obj.put("text", unit.getName());
            obj.put("children", unit.isHasChildren());
            tree.add(obj);
        }
        return tree;
    }

    @At("/getUnit")
    @Ok("json")
    public Object getUnit(){
        try{
            Sql sql= Sqls.queryRecord("select * from sys_unit where unitcode <> 'XTGL' order by name ");
            List<Sys_unit> units = sysUnitService.dao().execute(sql).getList(Sys_unit.class);
            return units;

        }catch(Exception e){
            return  null;
        }
    }

    @At("/getPersonUnits")
    @Ok("json")
    public Object getPersonUnits(@Param("unitid") String unitid){
        try{
            Cnd cnd = Cnd.NEW();
            cnd = sysRoleService.getPermission(cnd,"id","","creater",false);

//            Cnd c=Cnd.NEW();
            if(!Strings.isBlank(unitid)){
                cnd.and("id","=",unitid);
//                c.or("parentId","=",unitid);
                return sysUnitService.query(cnd);
            }else{
                if(!Strings.isBlank(basePersonService.getPersonInfo().getAirportid())){
                    cnd.and("unitairport","=",basePersonService.getPersonInfo().getAirportid());
                    cnd.asc("path");
                }
                return sysUnitService.query(cnd);
            }
        }catch(Exception e){
            return null;
        }
    }

    @At("/getUnitByAirportId")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class)})
    public Object getUnitByAirportId(@Param("airportid") String airportid){
        try{
            if(!Strings.isBlank(airportid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("unitairport","=",airportid);
                cnd.and("unitcode","<>","XTGL");
                List<Sys_unit> units = sysUnitService.query(cnd);
                List<HashMap> maps=new ArrayList<HashMap>();
                for(int i=0;i<units.size();i++){
                    HashMap map =new HashMap();
                    map.put("value",units.get(i).getId());
                    map.put("text",units.get(i).getName());
                    maps.add(map);
                }

                return Result.success("system.success",maps);
            }
            return Result.error(2,"system.error");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }
}
