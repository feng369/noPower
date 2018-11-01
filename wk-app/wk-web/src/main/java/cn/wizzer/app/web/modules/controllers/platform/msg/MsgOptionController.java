package cn.wizzer.app.web.modules.controllers.platform.msg;

import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.msg.modules.models.Msg_option;
import cn.wizzer.app.msg.modules.services.MsgOptionService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@IocBean
@At("/platform/msg/option")
public class MsgOptionController{
    private static final Log log = Logs.get();
    @Inject
    private MsgOptionService msgOptionService;

    @At("")
    @Ok("beetl:/platform/msg/option/index.html")
    @RequiresPermissions("platform.msg.option")
    public void index() {
    }

    @At("/data")
    @Ok("json:full")
//    @RequiresPermissions("platform.msg.option")
    @RequiresAuthentication
    public Object data(@Param("condition")  String condition,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        Cnd cnd = Cnd.NEW();
        if(Strings.isNotBlank(condition)) {
            LinkedHashMap<String,Object> pm = (LinkedHashMap) Json.fromJson(condition);
            for(Map.Entry<String,Object> entry: pm.entrySet()){
                String key = entry.getKey();
                Object vo = entry.getValue();
                if(vo instanceof String || vo instanceof Integer){
                    cnd.and(key,"=",vo);
                }else if(vo instanceof List){
                    cnd.and(key,"in",vo);
                }
            }
        }
    	return msgOptionService.data(length, start, draw, order, columns, cnd, "opByUser|nextHandler");
    }

    @At("/add")
    @Ok("beetl:/platform/msg/option/add.html")
    @RequiresPermissions("platform.msg.option")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.msg.option.add")
    @SLog(tag = "任务处理意见表", msg = "${args[0].id}")
    public Object addDo(@Param("..")Msg_option msgOption, HttpServletRequest req) {
		try {
			msgOptionService.insert(msgOption);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/msg/option/edit.html")
    @RequiresPermissions("platform.msg.option")
    public void edit(String id,HttpServletRequest req) {
		req.setAttribute("obj", msgOptionService.fetch(id));
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.msg.option.edit")
    @SLog(tag = "任务处理意见表", msg = "${args[0].id}")
    public Object editDo(@Param("..")Msg_option msgOption, HttpServletRequest req) {
		try {
            msgOption.setOpBy(StringUtil.getUid());
			msgOption.setOpAt((int) (System.currentTimeMillis() / 1000));
			msgOptionService.updateIgnoreNull(msgOption);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.msg.option.delete")
    @SLog(tag = "任务处理意见表", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				msgOptionService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				msgOptionService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/msg/option/detail.html")
    @RequiresPermissions("platform.msg.option")
	public void detail(String id, HttpServletRequest req) {
        		if (!Strings.isBlank(id)) {
            req.setAttribute("obj", msgOptionService.fetch(id));
		}else{
            req.setAttribute("obj", null);
        }}

}
