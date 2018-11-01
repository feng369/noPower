package cn.wizzer.app.msg.modules.models;

import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.framework.base.dao.entity.annotation.Ref;
import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

@Comment("系统消息表")
@Table("msg_message")
public class Msg_message extends BaseModel implements Serializable {

    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("消息类型")  //0 即时消息；1 通知类消息(无详情); 2 通知类消息(有详情)； 3 待办任务类消息
    @ColDefine(type = ColType.INT)
    private String type;

    @Column
    @Comment("业务类型")  //0 即时消息、1 预警提醒、2 工作流等待办任务、3 异常通知、4 后台事务消息
    @ColDefine(type = ColType.INT)
    private String bizType;

    @Column
    @Comment("状态")  //0 未读 1 已读
    @ColDefine(type = ColType.INT)
    private String state;

    @Column
    @Comment("优先级")  //0 低 1 中 2 高
    @ColDefine(type = ColType.INT)
    private String priority;

    @Column
    @Comment("消息标题")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String title;

    @Column
    @Comment("消息体")
    @ColDefine(type = ColType.VARCHAR, width = 500)
    private String body;

    @Column
    @Comment("发送时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String sendtime;

    @Column
    @Comment("接收时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String receivetime;

    @Column
    @Comment("接收人id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_user.class)
    private String receiverId;
    @One(field = "receiverId")
    private Sys_user receiver;

    @Column
    @Comment("发送人id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_user.class)
    private String senderId;
    @One(field = "senderId")
    private Sys_user sender;

    @Column
    @Comment("所属机场")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String airportId;
    @One(field = "airportId")
    private base_airport airport;

    @Column
    @Comment("源对象id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String sourceid;

    @Column
    @Comment("查看链接")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String linkUrl;

    @Column
    @Comment("扩展参数")
    @ColDefine(type = ColType.VARCHAR, width = 150)
    private String extParam;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
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

    public String getSendtime() {
        return sendtime;
    }

    public void setSendtime(String sendtime) {
        this.sendtime = sendtime;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public Sys_user getReceiver() {
        return receiver;
    }

    public void setReceiver(Sys_user receiver) {
        this.receiver = receiver;
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

    public String getAirportId() {
        return airportId;
    }

    public void setAirportId(String airportId) {
        this.airportId = airportId;
    }

    public base_airport getAirport() {
        return airport;
    }

    public void setAirport(base_airport airport) {
        this.airport = airport;
    }

    public String getSourceid() {
        return sourceid;
    }

    public void setSourceid(String sourceid) {
        this.sourceid = sourceid;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getExtParam() {
        return extParam;
    }

    public void setExtParam(String extParam) {
        this.extParam = extParam;
    }

    public String getReceivetime() {
        return receivetime;
    }

    public void setReceivetime(String receivetime) {
        this.receivetime = receivetime;
    }
}
