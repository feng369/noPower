package cn.wizzer.app.web.modules.controllers.platform.msg;

import cn.wizzer.app.msg.modules.services.MsgAssignService;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.msg.modules.models.Msg_message;
import cn.wizzer.app.msg.modules.services.MsgMessageService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
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
@At("/platform/msg/message")
public class MsgMessageController{
    private static final Log log = Logs.get();
    @Inject
    private MsgMessageService msgMessageService;
    @Inject
    private MsgAssignService msgAssignService;

    @At("")
    @Ok("beetl:/platform/msg/message/index.html")
    @RequiresAuthentication
    public void index() {
    }

    @At("/data")
    @Ok("json:full")
    @RequiresAuthentication
    public Object data(@Param("condition")  String condition,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns, HttpServletRequest req) {
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
        cnd.and("receiverId","=",StringUtil.getUid());
    	return msgMessageService.data(length, start, draw, order, columns, cnd, "sender");
    }

    @At("/add")
    @Ok("beetl:/platform/msg/message/add.html")
    @RequiresPermissions("platform.msg.message")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.msg.message.add")
    @SLog(tag = "系统消息表", msg = "${args[0].id}")
    public Object addDo(@Param("..")Msg_message msgMessage, HttpServletRequest req) {
		try {
			msgMessageService.insert(msgMessage);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/msg/message/edit.html")
    @RequiresPermissions("platform.msg.message")
    public void edit(String id,HttpServletRequest req) {
		req.setAttribute("obj", msgMessageService.fetch(id));
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.msg.message.edit")
    @SLog(tag = "系统消息表", msg = "${args[0].id}")
    public Object editDo(@Param("..")Msg_message msgMessage, HttpServletRequest req) {
		try {
            msgMessage.setOpBy(StringUtil.getUid());
			msgMessage.setOpAt((int) (System.currentTimeMillis() / 1000));
			msgMessageService.updateIgnoreNull(msgMessage);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.msg.message.delete")
    @SLog(tag = "系统消息表", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				msgMessageService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				msgMessageService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/msg/message/detail.html")
    @RequiresAuthentication
	public void detail(String id, HttpServletRequest req) {
        if (!Strings.isBlank(id)) {
            Msg_message message = msgMessageService.fetch(id);
            message = msgMessageService.fetchLinks(message,"sender");
            req.setAttribute("obj",message);
		}else{
            req.setAttribute("obj", null);
        }
    }

    //即时消息列表
    @At("/im")
    @Ok("beetl:/platform/msg/message/im.html")
    @RequiresAuthentication
    public void im() {

    }

    //查询消息中心显示的数量：即未处理的待办任务数量、未读的系统通知数量和未读的即时消息数量
    @At({"/getUnhandleMsgCount/?"})
    @Ok("json")
    @RequiresAuthentication
    public Object getUnhandleMsgCount(String userid, HttpServletRequest req){
        try {
            int [] gstate = {1,2};
            int cn = msgAssignService.getTodoCount(userid,gstate);
            Map map = new HashMap();
            map.put("assign",cn);
            int []type = {1,2,3};
            int []bizType = {1,2,3,4};
            int noticeCn = msgMessageService.getMessageCount(userid,type,bizType,"0");
            map.put("message",noticeCn);
            int[]typeIm = {0};
            int[]bizTypeIm = {0};
            int imCn = msgMessageService.getMessageCount(userid,typeIm,bizTypeIm,"0");
            map.put("im",imCn);
            map.put("messagecenter",cn+noticeCn+imCn);
            return Result.success("system.success",map);
        }catch (Exception e){
            return Result.error("system.error",e);
        }
    }

    //查询消息中心未读的系统通知数量和未读的即时消息数量
    @At({"/getUnreadNoticeCount/?"})
    @Ok("json")
    @RequiresAuthentication
    public Object getUnreadNoticeCount(String userid, HttpServletRequest req){
        try {
            Map map = new HashMap();
            int []type = {1,2,3};
            int []bizType = {1,2,3,4};
            int noticeCn = msgMessageService.getMessageCount(userid,type,bizType,"0");
            map.put("message",noticeCn);
            int[]typeIm = {0};
            int[]bizTypeIm = {0};
            int imCn = msgMessageService.getMessageCount(userid,typeIm,bizTypeIm,"0");
            map.put("im",imCn);
            return Result.success("system.success",map);
        }catch (Exception e){
            return Result.error("system.error",e);
        }
    }

    @At({"/getAssignCount/?"})
    @Ok("json")
    @RequiresAuthentication
    public Object getAssignCount(String userid, HttpServletRequest req){
        try {
            int [] gstate = {1,2};
            int cn = msgAssignService.getTodoCount(userid,gstate);
            Map map = new HashMap();
            map.put("assign",cn);
            int []type = {0,1,2,3};
            int []bizType = {0,1,2,3,4};
            int msgCn = msgMessageService.getMessageCount(userid,type,bizType,"0");
            map.put("messagecenter",cn+msgCn);
            return Result.success("system.success",map);
        }catch (Exception e){
            return Result.error("system.error",e);
        }
    }

    //更改消息已读未读状态
    @At({"/updateState"})
    @Ok("json")
    @RequiresAuthentication
    public Object updateState(@Param("id")String id,@Param("state")String state, HttpServletRequest req){
        try {
            if(Strings.isNotBlank(id) && Strings.isNotBlank(state)){
                if("0".equals(state) ||  "1".equals(state)){
                    msgMessageService.update(Chain.make("state",state),Cnd.where("id","=",id));
                    return Result.success("system.success");
                }
                throw new ValidatException("传入状态值不合法！");
            }
            throw new ValidatException("传入参数不完整！");
        }catch (Exception e){
            return Result.error("system.error",e);
        }
    }



}
