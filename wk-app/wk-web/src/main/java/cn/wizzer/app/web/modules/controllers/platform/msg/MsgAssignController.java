package cn.wizzer.app.web.modules.controllers.platform.msg;

import cn.wizzer.app.msg.modules.models.Msg_option;
import cn.wizzer.app.msg.modules.services.MsgAssignHisService;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.msg.modules.models.Msg_assign;
import cn.wizzer.app.msg.modules.services.MsgAssignService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.nutz.dao.Cnd;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@IocBean
@At("/platform/msg/assign")
public class MsgAssignController{
    private static final Log log = Logs.get();
    @Inject
    private MsgAssignService msgAssignService;

    @Inject
    private MsgAssignHisService msgAssignHisService;

    @At("")
    @Ok("beetl:/platform/msg/assign/index.html")
    @RequiresAuthentication
    public void index() {
    }

    @At("/data")
    @Ok("json:full")
    @RequiresAuthentication
    public Object data(@Param("toDo")String toDo,@Param("condition")  String condition,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
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
        //获取登录用户基本信息
//        Subject subject = SecurityUtils.getSubject();
//        Sys_user curUser = (Sys_user) subject.getPrincipal();
        cnd.and("performerUserId","=",StringUtil.getUid());
        if("1".endsWith(toDo)){//查询历史消息
            return msgAssignHisService.data(length, start, draw, order, columns, cnd, "sender");
        }else if("0".endsWith(toDo)){//查询当前消息
            cnd.or("performerRoleId","in",StringUtil.getRoleid());
            return msgAssignService.data(length, start, draw, order, columns, cnd, "sender");
        }
        return null;
    }

    @At("/add")
    @Ok("beetl:/platform/msg/assign/add.html")
    @RequiresPermissions("platform.msg.assign")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.msg.assign.add")
    @SLog(tag = "待办任务表", msg = "${args[0].id}")
    public Object addDo(@Param("..")Msg_assign msgAssign, HttpServletRequest req) {
		try {
			msgAssignService.insert(msgAssign);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/msg/assign/edit.html")
    @RequiresPermissions("platform.msg.assign")
    public void edit(String id,HttpServletRequest req) {
		req.setAttribute("obj", msgAssignService.fetch(id));
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.msg.assign.edit")
    @SLog(tag = "待办任务表", msg = "${args[0].id}")
    public Object editDo(@Param("..")Msg_assign msgAssign, HttpServletRequest req) {
		try {
            msgAssign.setOpBy(StringUtil.getUid());
			msgAssign.setOpAt((int) (System.currentTimeMillis() / 1000));
			msgAssignService.updateIgnoreNull(msgAssign);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.msg.assign.delete")
    @SLog(tag = "待办任务表", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				msgAssignService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				msgAssignService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/msg/assign/detail.html")
    @RequiresAuthentication
	public void detail(String id, HttpServletRequest req) {
        		if (!Strings.isBlank(id)) {
            req.setAttribute("obj", msgAssignService.fetch(id));
		}else{
            req.setAttribute("obj", null);
        }
    }

    @At({"/getTodoCount/?"})
    @Ok("json")
    @RequiresAuthentication
    public Object getTodoCount(String userid, HttpServletRequest req){
        try {
            int [] gstate = {1,2};
            int cn = msgAssignService.getTodoCount(userid,gstate);
            return Result.success("system.success",cn);
        }catch (Exception e){
            return Result.error("system.error",e);
        }
    }


    @At("/handler/?")
    @Ok("beetl:/platform/msg/assign/handler.html")
    @RequiresAuthentication
    public void handler(String id,HttpServletRequest req) {
        req.setAttribute("obj", msgAssignService.fetch(id));
    }

    @At("/handlerDo")
    @Ok("json")
    @RequiresAuthentication
    @SLog(tag = "待办任务表", msg = "${args[0].id}")
    public Object handlerDo(@Param("..") NutMap map, @Param("::option.")Msg_option msgOption, @Param("assignid")String assignid, HttpServletRequest req) {
        try {
            msgAssignService.handler(assignid,msgOption,map);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/selectUser")
    @Ok("beetl:/platform/msg/assign/selectUser.html")
    @RequiresAuthentication
    public void selectUser(HttpServletRequest req) {
    }

    @At("/selectUser2")
    @Ok("beetl:/platform/msg/assign/selectUser2.html")
    @RequiresAuthentication
    public void selectUser2(HttpServletRequest req) {
    }


}
