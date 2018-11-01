package cn.wizzer.app.msg.modules.models;

import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.framework.base.dao.entity.annotation.Ref;
import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

@Comment("任务处理意见表")
@Table("msg_option")
public class Msg_option extends BaseModel implements Serializable {

    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("单据id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String billid;

    @Column
    @Comment("任务id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String assignid;

    @Column
    @Comment("操作类型")  //0 同意(然后结束流程)；1 不同意(打回或者终止由业务auditHandler方法决定); 2 转交(其他人处理)； 3 跳转下一步人(同意，并且指定了下一步处理人)
    @ColDefine(type = ColType.INT)
    private String type;

    @Column
    @Comment("处理意见")
    @ColDefine(type = ColType.VARCHAR, width = 255)
    private String content;

    @Column
    @Comment("操作时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String optime;

    @Column
    @Comment("下一个处理人id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_user.class)
    private String nextHandlerId;
    @One(field = "nextHandlerId")
    private Sys_user nextHandler;

    @One(field = "opBy")
    private Sys_user opByUser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBillid() {
        return billid;
    }

    public void setBillid(String billid) {
        this.billid = billid;
    }

    public String getAssignid() {
        return assignid;
    }

    public void setAssignid(String assignid) {
        this.assignid = assignid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOptime() {
        return optime;
    }

    public void setOptime(String optime) {
        this.optime = optime;
    }

    public String getNextHandlerId() {
        return nextHandlerId;
    }

    public void setNextHandlerId(String nextHandlerId) {
        this.nextHandlerId = nextHandlerId;
    }

    public Sys_user getNextHandler() {
        return nextHandler;
    }

    public void setNextHandler(Sys_user nextHandler) {
        this.nextHandler = nextHandler;
    }

    public Sys_user getOpByUser() {
        return opByUser;
    }

    public void setOpByUser(Sys_user opByUser) {
        this.opByUser = opByUser;
    }
}
