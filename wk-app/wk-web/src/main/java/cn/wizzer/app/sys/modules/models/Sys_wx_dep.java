package cn.wizzer.app.sys.modules.models;

import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

@Comment("企业微信部门表")
@Table("sys_wx_dep")
public class Sys_wx_dep extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column
    @Name
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("企业ID")
    @ColDefine(type = ColType.VARCHAR, width = 64)
    private String corpid;

    @Column
    @Comment("部门ID")
    @ColDefine(type = ColType.VARCHAR, width = 16)
    private String depid;

    @Column
    @Comment("部门名称")
    @ColDefine(type = ColType.VARCHAR, width = 64)
    private String name;

    @Column
    @Comment("上级部门ID")
    @ColDefine(type = ColType.VARCHAR, width = 16)
    private String parentid;

    //order值大的排序靠前，值范围是[0, 2^32)
    @Column
    @Comment("排序")
    @ColDefine(type = ColType.VARCHAR, width = 16)
    private String orderNum;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCorpid() {
        return corpid;
    }

    public void setCorpid(String corpid) {
        this.corpid = corpid;
    }

    public String getDepid() {
        return depid;
    }

    public void setDepid(String depid) {
        this.depid = depid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }
}
