package cn.wizzer.app.eq.modules.models;

import cn.wizzer.app.sys.modules.models.Sys_dict;
import cn.wizzer.app.sys.modules.models.Sys_unit;
import cn.wizzer.framework.base.dao.entity.annotation.Ref;
import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

/**
 * Created by xl on 2017/9/25.
 * 计费表
 */
@Table("eq_charging")
@Comment("计费表")
public class eq_charging extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("单位ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_unit.class)
    private String unitid;
    @One(field = "unitid")
    private Sys_unit sysUnit;

    @Column
    @Comment("计费模式")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String chargingtype;
    @One(field = "chargingtype")
    private Sys_dict sysDict;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUnitid() {
        return unitid;
    }

    public void setUnitid(String unitid) {
        this.unitid = unitid;
    }

    public String getChargingtype() {
        return chargingtype;
    }

    public void setChargingtype(String chargingtype) {
        this.chargingtype = chargingtype;
    }

    public Sys_dict getSysDict() {
        return sysDict;
    }

    public void setSysDict(Sys_dict sysDict) {
        this.sysDict = sysDict;
    }
}
