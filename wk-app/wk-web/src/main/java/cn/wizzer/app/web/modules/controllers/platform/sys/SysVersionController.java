package cn.wizzer.app.web.modules.controllers.platform.sys;

import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.sys.modules.models.Sys_version;
import cn.wizzer.app.sys.modules.services.SysVersionService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
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
import java.util.HashMap;
import java.util.List;

@IocBean
@At("/platform/sys/version")
public class SysVersionController{
    private static final Log log = Logs.get();
    @Inject
    private SysVersionService sysVersionService;

    @At("")
    @Ok("beetl:/platform/sys/version/index.html")
    @RequiresPermissions("platform.sys.version")
    public void index() {
    }

    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.sys.version")
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
		Cnd cnd = Cnd.NEW();
    	return sysVersionService.data(length, start, draw, order, columns, cnd, null);
    }

    @At("/add")
    @Ok("beetl:/platform/sys/version/add.html")
    @RequiresPermissions("platform.sys.version")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.sys.version.add")
    @SLog(tag = "Sys_version", msg = "${args[0].id}")
    public Object addDo(@Param("..")Sys_version sysVersion, HttpServletRequest req) {
		try {
			sysVersionService.insert(sysVersion);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/sys/version/edit.html")
    @RequiresPermissions("platform.sys.version")
    public void edit(String id,HttpServletRequest req) {
		req.setAttribute("obj", sysVersionService.fetch(id));
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.sys.version.edit")
    @SLog(tag = "Sys_version", msg = "${args[0].id}")
    public Object editDo(@Param("..")Sys_version sysVersion, HttpServletRequest req) {
		try {
            sysVersion.setOpBy(StringUtil.getUid());
			sysVersion.setOpAt((int) (System.currentTimeMillis() / 1000));
			sysVersionService.updateIgnoreNull(sysVersion);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.sys.version.delete")
    @SLog(tag = "Sys_version", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				sysVersionService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				sysVersionService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/sys/version/detail.html")
    @RequiresPermissions("platform.sys.version")
	public void detail(String id, HttpServletRequest req) {
		if (!Strings.isBlank(id)) {
            req.setAttribute("obj", sysVersionService.fetch(id));
		}else{
            req.setAttribute("obj", null);
        }
    }

    @At("/getVersion")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getVersion( @Param("name")String name){

        try{
            Cnd cnd = Cnd.NEW();
            cnd.and("name","=",name);
            List<Sys_version> svList = sysVersionService.query(cnd);
            HashMap map=new HashMap();
            if(svList.size()>0){
                return Result.success("system.success",svList.get(0));
            }
           throw new ValidatException("未找到版本号信息");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }


    @At("/editVersion")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class)})
    /*@Api(name="修改版本号",params = {
            @ApiParam(name = "name", type = "String", description = "版本名称")
            , @ApiParam(name = "newVersion", type = "String", description = "新版本号")
            , @ApiParam(name = "wgt", type = "boolean", description = "1大更新(IOS 强制更新)  0小更新")
    }, ok={@ReturnKey(key="code",description="code=0修改成功")})
    */
    public Object editVersion(@Param("name")String name,@Param("newVersion")String newVersion,@Param("wgt")boolean wgt){
        try{
            sysVersionService.editVersion(name,newVersion,wgt);
            return Result.success("system.success");
        }catch(Exception e){
            return Result.error(e.getMessage());
        }
    }

}
