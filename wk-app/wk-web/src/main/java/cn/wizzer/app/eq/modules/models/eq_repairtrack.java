package cn.wizzer.app.eq.modules.models;

import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

@Table("eq_repairtrack")
@Comment("维修历史对象")
public class eq_repairtrack extends BaseModel {
    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("维修对象ID")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String eqRepairId;

    @Column
    @Comment("操作时间")
    @ColDefine(type = ColType.VARCHAR, width = 300)
    private String operatetime;

    @Column
    @Comment("操作人")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String operaterId;


    @Column
    @Comment("状态,1报修中，2维修中，0完成，99草稿")
    @ColDefine(type = ColType.VARCHAR, width = 10)
    private String repairStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperatetime() {
        return operatetime;
    }

    public void setOperatetime(String operatetime) {
        this.operatetime = operatetime;
    }

    public String getOperaterId() {
        return operaterId;
    }

    public void setOperaterId(String operaterId) {
        this.operaterId = operaterId;
    }

    public String getRepairStatus() {
        return repairStatus;
    }

    public void setRepairStatus(String repairStatus) {
        this.repairStatus = repairStatus;
    }

    public String getEqRepairId() {
        return eqRepairId;
    }

    public void setEqRepairId(String eqRepairId) {
        this.eqRepairId = eqRepairId;
    }

}
