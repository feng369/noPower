package cn.wizzer.app.eq.modules.models;

import cn.wizzer.app.sys.modules.models.Sys_dict;
import cn.wizzer.app.sys.modules.models.Sys_unit;
import cn.wizzer.framework.base.dao.entity.annotation.Ref;
import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

/**
 * Created by xl on 2017/9/14.
 * 设备表
 */
@Comment("设备信息表")
@Table("eq_materiel")
public class eq_materiel extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("设备编号")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String eqnum;

    @Column
    @Comment("身份牌照编号")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String eqcode;

    @Column
    @Comment("设备名称")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String eqname;

    @Column
    @Comment("规格型号")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String eqtype;

    @Column
    @Comment("开始使用时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String starttime;


    @Column
    @Comment("设备所属单位")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_unit.class)
    private String equnitid;
    @One(field = "equnitid")
    private Sys_unit unit;

    @Column
    @Comment("设备所属单位联系电话")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String equnitphone;


    @Column
    @Comment("厂牌型号")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String eqfac;

    @Column
    @Comment("设备颜色")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String eqcolor;

    @Column
    @Comment("固定桩位")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String stakeid;
    @One(field = "stakeid")
    private eq_stake stake;


    @Column
    @Comment("设备运行状态")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String eqrunstatus;


    @Column
    @Comment("设备使用状态")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String equsestatus;

    @Column
    @Comment("设备类型")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_dict.class)
    private String typeid;
    @One(field = "typeid")
    private Sys_dict type;

    @Column
    @Comment("锁号")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String lockid;

    @Column
    @Comment("锁ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(eq_lock.class)
    private String lid;
    @One(field = "lid")
    private eq_lock lock;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEqnum() {
        return eqnum;
    }

    public void setEqnum(String eqnum) {
        this.eqnum = eqnum;
    }

    public String getEqcode() {
        return eqcode;
    }

    public void setEqcode(String eqcode) {
        this.eqcode = eqcode;
    }

    public String getEqname() {
        return eqname;
    }

    public void setEqname(String eqname) {
        this.eqname = eqname;
    }

    public String getEqtype() {
        return eqtype;
    }

    public void setEqtype(String eqtype) {
        this.eqtype = eqtype;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getEqunitid() {
        return equnitid;
    }

    public void setEqunitid(String equnitid) {
        this.equnitid = equnitid;
    }

    public String getEqunitphone() {
        return equnitphone;
    }

    public void setEqunitphone(String equnitphone) {
        this.equnitphone = equnitphone;
    }

    public String getEqfac() {
        return eqfac;
    }

    public void setEqfac(String eqfac) {
        this.eqfac = eqfac;
    }

    public String getEqcolor() {
        return eqcolor;
    }

    public void setEqcolor(String eqcolor) {
        this.eqcolor = eqcolor;
    }

    public String getStakeid() {
        return stakeid;
    }

    public void setStakeid(String stakeid) {
        this.stakeid = stakeid;
    }

    public String getEqusestatus() {
        return equsestatus;
    }

    public void setEqusestatus(String equsestatus) {
        this.equsestatus = equsestatus;
    }

    public Sys_unit getUnit() {
        return unit;
    }

    public void setUnit(Sys_unit unit) {
        this.unit = unit;
    }

    public String getEqrunstatus() {
        return eqrunstatus;
    }

    public void setEqrunstatus(String eqrunstatus) {
        this.eqrunstatus = eqrunstatus;
    }

    public String getTypeid(){return typeid;}
    public void setTypeid(String typeid){ this.typeid=typeid; }

    public Sys_dict getType(){return type;}

    public void setType(Sys_dict type){this.type=type;}

    public eq_stake getStake(){return stake;}

    public void setStake(eq_stake stake){this.stake=stake;}

    public String getLockid(){return lockid;}

    public void setLockid(String lockid){this.lockid=lockid;}

    public String getLid(){return lid;}

    public void setLid(String lid){this.lid=lid;}

    public eq_lock getLock(){return lock;}

    public void setLock(eq_lock lock){this.lock=lock;}

}
