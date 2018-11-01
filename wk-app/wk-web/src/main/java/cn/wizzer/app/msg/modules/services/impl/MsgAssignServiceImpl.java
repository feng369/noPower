package cn.wizzer.app.msg.modules.services.impl;

import cn.wizzer.app.msg.modules.models.Msg_assign_his;
import cn.wizzer.app.msg.modules.models.Msg_option;
import cn.wizzer.app.msg.modules.services.MsgMessageService;
import cn.wizzer.app.sys.modules.models.Sys_role;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.app.sys.modules.services.SysUserService;
import cn.wizzer.app.sys.modules.services.SysWxService;
import cn.wizzer.app.sys.modules.services.impl.SysUserServiceImpl;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.msg.modules.models.Msg_assign;
import cn.wizzer.app.msg.modules.services.MsgAssignService;
import cn.wizzer.framework.util.ClassReflection;
import cn.wizzer.framework.util.DateUtil;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.framework.websocket.MyWebsocket;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Each;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@IocBean(args = {"refer:dao"})
public class MsgAssignServiceImpl extends BaseServiceImpl<Msg_assign> implements MsgAssignService {

    private static final Log log = Logs.get();

    @Inject
    private SysUserService userService;
    @Inject
    private MsgMessageService msgMessageService;
    @Inject
    private SysWxService sysWxService;

    @Inject
    protected MyWebsocket myWebsocket;

    public MsgAssignServiceImpl(Dao dao) {
        super(dao);
    }

    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void addAssignToUser(String bizObject, String bizObjectId, String title, String body, String state, String userid, String personid,String sendid, String linkurl,String linkViewUrl,String bizObjectServiceImpl) {
        log.debug("按用户发送待办任务："+userid);
        Msg_assign assign = new Msg_assign();
        assign.setBizObject(bizObject);
        assign.setBizObjectId(bizObjectId);
//        assign.setAccepttime(DateUtil.getDateTime());
        assign.setBody(body);
        assign.setTitle(title);
        assign.setState(state);
        assign.setStarttime(DateUtil.getDateTime());
        assign.setAccepttime(DateUtil.getDateTime());
            assign.setPerformerUserId(userid);
            assign.setPerformerPersonId(personid);
//        assign.setPerformerRoleId(roleId);
        assign.setSenderId(sendid);
        assign.setLinkUrl(linkurl);
        assign.setLinkViewUrl(linkViewUrl);
        assign.setBizObjectServiceImpl(bizObjectServiceImpl);
        this.insert(assign);

        //发送消息中心更新消息数量,为减少数据库查询，只有前端收到此消息时主动查询
//        int [] gstate = {1,2};
//        final int assignCount = this.getTodoCount(userid,gstate);
        myWebsocket.each("home-msg-"+userid, new Each<Session>() {
            public void invoke(int index, Session ele, int length) {
                myWebsocket.sendJson(ele.getId(), new NutMap("action", "assign").setv("msg", "ok"));
            }
        });
    }

    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void addAssignToRole(String bizObject, String bizObjectId, String title, String body, String state, String roleId, List<String> userIdList, String sendid, String linkurl,String linkViewUrl,String bizObjectServiceImpl) {
        log.debug("按角色发送待办任务："+roleId);
        Msg_assign assign = new Msg_assign();
        assign.setBizObject(bizObject);
        assign.setBizObjectId(bizObjectId);
//        assign.setAccepttime(DateUtil.getDateTime());
        assign.setBody(body);
        assign.setTitle(title);
        assign.setState(state);
        assign.setStarttime(DateUtil.getDateTime());
        assign.setAccepttime(DateUtil.getDateTime());
//            assign.setPerformerUserId(userId);
//            assign.setPerformerPersonId("");
        assign.setPerformerRoleId(roleId);
        assign.setSenderId(sendid);
        assign.setLinkUrl(linkurl);
        assign.setLinkViewUrl(linkViewUrl);
        assign.setBizObjectServiceImpl(bizObjectServiceImpl);
        this.insert(assign);

        //发送消息中心更新该角色对应的用户的消息数量
        for(String userid :userIdList){
            myWebsocket.each("home-msg-"+userid, new Each<Session>() {
                public void invoke(int index, Session ele, int length) {
                    myWebsocket.sendJson(ele.getId(), new NutMap("action", "assign").setv("msg", "ok" ));
                }
            });
        }
    }

    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void complete(Msg_assign assign, Msg_option option) {
        log.debug("审批完成添加审批意见，assign:"+assign+",option:"+option);
        if(assign==null||option==null)
            return;
        String assignid = assign.getId();
        try {

            if(assign!=null){
                //写到历史表,删除当前表中数据
                Msg_assign_his his  = new Msg_assign_his();
                ClassReflection.reflectionAttr(assign,his);
                his.setState("9");
                his.setEndtime(DateUtil.getDateTime());
                his.setPerformerUserId(StringUtil.getUid());
                his.setPerformerPersonId(StringUtil.getPersonid());
                dao().insert(his);
                this.delete(assignid);
            }
            boolean isNotice= false;//是否有后续处理人（即为转交或者跳转下一步）
            if("2".equals(option.getType())|| "3".equals(option.getType())) {
                isNotice = true;
            }
            if(option !=null){
                //添加审批意见
                option.setOpBy(StringUtil.getUid());
                option.setOpAt((int) (System.currentTimeMillis() / 1000));
                option.setOptime(DateUtil.getDateTime());
                option.setAssignid(assignid);
                option.setBillid(assign.getBizObjectId());
                dao().insert(option);
            }

            Set userIds = new HashSet();
            StringBuffer content = new StringBuffer("");
            if(isNotice){
                Msg_assign assignNew = new Msg_assign();
                ClassReflection.reflectionAttr(assign, assignNew);
                assignNew.setId(null);
                assignNew.setPerformerRoleId(null);
                assignNew.setPerformerUserId(option.getNextHandlerId());
                assignNew.setState("1");
                assignNew.setAccepttime(DateUtil.getDateTime());
                this.insert(assignNew);

                //发送消息中心更新下一步处理人用户的消息数量
                myWebsocket.each("home-msg-"+option.getNextHandlerId(), new Each<Session>() {
                    public void invoke(int index, Session ele, int length) {
                        myWebsocket.sendJson(ele.getId(), new NutMap("action", "assign").setv("msg", "ok" ));
                    }
                });
                userIds.add(option.getNextHandlerId());
                content.append("您有新的待办任务{"+assign.getBizObject()+"}，请处理！");
            }else{//流程结束发送通知给发起人
                log.debug("流程处理结束--发送通知消息给流程发起人 ");
                userIds.add(assign.getSenderId());
                content.append("您的流程{"+assign.getBizObject()+"}处理结束，请查看单据信息！");
                msgMessageService.addMessage("2","2","0","1",content.toString(),null,assign.getSenderId(),assign.getBizObjectId(),assign.getLinkViewUrl());
            }
            //发送企业微信消息给设备管理员角色的用户
            if("1".equals(Globals.WxCorpStart)) {
                log.debug("流程处理发送企业微信消息");
                sysWxService.sendWxMessageAsy(userIds, content.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ValidatException("消息处理失败！");
        }
    }

    @Override
    public int getTodoCount(String userid, int[] state) {
        Sys_user user =userService.fetch(userid);
        userService.fetchLinks(user, "roles");
        List<String> roleIdList = new ArrayList<String>();
        for(Sys_role role:user.getRoles()){
            roleIdList.add(role.getId());
        }
        Cnd cnd = Cnd.NEW();
        cnd.and("performerUserId","=",userid);
        cnd.or("performerRoleId","in",roleIdList);
        return this.count(cnd);
    }

    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void handler(String assignid, Msg_option msgOption, NutMap objcetMap) {
        Msg_assign assign = this.fetch(assignid);
        if (assign == null) {
            throw new ValidatException("该待办任务不存在，可能已被处理！");
        }
        if ("2".equals(msgOption.getType())) {
            if (Strings.isBlank(msgOption.getNextHandlerId())) {
                throw new ValidatException("未找到被转交人！");
            }
        }
        //确定操作结果类型
        String type = msgOption.getType();
        if("0".equals(msgOption.getType())) {
            if (Strings.isNotBlank(msgOption.getNextHandlerId())) {
                type="3";
                msgOption.setType("3");//改变审批操作类型
            }
        }

        //无需后续审批，即不为转交，也未指定下一步处理人
        String serviceClassName = assign.getBizObjectServiceImpl();
        Class serviceClass = null;
        try {
            serviceClass = Class.forName(serviceClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new ValidatException("找不到服务类：" + serviceClassName);
        }
        //调用具体业务对象得auditHandler方法
        Ioc ioc = Mvcs.getIoc();
        BaseService baseService = (BaseService) ioc.get(serviceClass);
        baseService.auditHandler(objcetMap,type);

        this.complete(assign, msgOption);
    }



}
