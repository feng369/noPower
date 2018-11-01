package cn.wizzer.app.msg.modules.models;

import cn.wizzer.app.sys.modules.models.Sys_role;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.framework.base.dao.entity.annotation.Ref;
import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

@Comment("待办任务历史表")
@Table("msg_assign_his")
public class Msg_assign_his extends BaseModel implements Serializable {

    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
//    @Prev(els = {@EL("uuid()")}) //与源表保持一致,不自动生成
    private String id;

    @Column
    @Comment("业务对象")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String bizObject;

    @Column
    @Comment("业务对象id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String bizObjectId;

    @Column
    @Comment("主题")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String title;

    @Column
    @Comment("内容")
    @ColDefine(type = ColType.VARCHAR, width = 200)
    private String body;

    @Column
    @Comment("状态")  //0 未分配；1 已分配; 2 处理中; 3 已取消 ; 9 已完成
    @ColDefine(type = ColType.INT)
    private String state;

    @Column
    @Comment("结束时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String endtime;

    @Column
    @Comment("开始时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String starttime;

    @Column
    @Comment("接收时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String accepttime;

    @Column
    @Comment("当前处理用户id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String performerUserId;

    @Column
    @Comment("当前处理员工id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String performerPersonId;

    @Column
    @Comment("当前处理角色id") //可以按登录用户角色查询待办，同一个角色的任意人员处理完即完成
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_role.class)
    private String performerRoleId;
    @One(field = "performerRoleId")
    private Sys_role perfomerRole;

    @Column
    @Comment("发起人id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_user.class)
    private String senderId;
    @One(field = "senderId")
    private Sys_user sender;

    @Column
    @Comment("业务处理URL")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String linkUrl;

    @Column
    @Comment("业务对象查看URL")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String linkViewUrl;

    @Column
    @Comment("业务对象服务名")//即业务对象serviceImpl类的全名称
    @ColDefine(type = ColType.VARCHAR, width = 128)
    private String bizObjectServiceImpl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBizObject() {
        return bizObject;
    }

    public void setBizObject(String bizObject) {
        this.bizObject = bizObject;
    }

    public String getBizObjectId() {
        return bizObjectId;
    }

    public void setBizObjectId(String bizObjectId) {
        this.bizObjectId = bizObjectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getAccepttime() {
        return accepttime;
    }

    public void setAccepttime(String accepttime) {
        this.accepttime = accepttime;
    }

    public String getPerformerUserId() {
        return performerUserId;
    }

    public void setPerformerUserId(String performerUserId) {
        this.performerUserId = performerUserId;
    }

    public String getPerformerPersonId() {
        return performerPersonId;
    }

    public void setPerformerPersonId(String performerPersonId) {
        this.performerPersonId = performerPersonId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Sys_user getSender() {
        return sender;
    }

    public void setSender(Sys_user sender) {
        this.sender = sender;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getPerformerRoleId() {
        return performerRoleId;
    }

    public void setPerformerRoleId(String performerRoleId) {
        this.performerRoleId = performerRoleId;
    }

    public Sys_role getPerfomerRole() {
        return perfomerRole;
    }

    public void setPerfomerRole(Sys_role perfomerRole) {
        this.perfomerRole = perfomerRole;
    }

    public String getLinkViewUrl() {
        return linkViewUrl;
    }

    public void setLinkViewUrl(String linkViewUrl) {
        this.linkViewUrl = linkViewUrl;
    }

    public String getBizObjectServiceImpl() {
        return bizObjectServiceImpl;
    }

    public void setBizObjectServiceImpl(String bizObjectServiceImpl) {
        this.bizObjectServiceImpl = bizObjectServiceImpl;
    }
}
