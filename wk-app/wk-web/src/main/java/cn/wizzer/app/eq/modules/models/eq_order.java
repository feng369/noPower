package cn.wizzer.app.eq.modules.models;

import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

/**
 * Created by xl on 2017/9/14.
 * 借用订单表
 */
@Table("eq_order")
public class eq_order extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("借用人ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String personid;

    @Column
    @Comment("设备ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String eqid;

    @Column
    @Comment("开始时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String starttime;


    @Column
    @Comment("结束时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String endtime;


    @Column
    @Comment("备注")
    @ColDefine(type = ColType.VARCHAR, width = 200)
    private String note;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPersonid() {
        return personid;
    }

    public void setPersonid(String personid) {
        this.personid = personid;
    }

    public String getEqid() {
        return eqid;
    }

    public void setEqid(String eqid) {
        this.eqid = eqid;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
