package cn.wizzer.app.eq.modules.models;

import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

/**
 * Created by caiw on 2018/3/19
 * 设备电量记录表
 */
@Deprecated  //废弃，已整合到eq_trace 20180505 koudepei
@Table("eq_power")
public class eq_power extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("设备id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String eqid;
    @One(field = "eqid")
    private eq_materiel eqMateriel;

    @Column
    @Comment("锁ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String lockid;
    @One(field = "lockid")
    private eq_lock lock;

    @Column
    @Comment("电量")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String power;

    @Column
    @Comment("上报时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String time;

    @Column
    @Comment("锁状态")  //lockstate=1关锁状态,lockstate=0,开锁状态
    @ColDefine(type = ColType.INT)
    private String lockstate;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEqid() {
        return eqid;
    }

    public void setEqid(String eqid) {
        this.eqid = eqid;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }


    public eq_materiel getEqMateriel() {
        return eqMateriel;
    }

    public void setEqMateriel(eq_materiel eqMateriel) {
        this.eqMateriel = eqMateriel;
    }

    public String getLockid() {
        return lockid;
    }

    public void setLockid(String lockid) {
        this.lockid = lockid;
    }

    public eq_lock getLock() {
        return lock;
    }

    public void setLock(eq_lock lock) {
        this.lock = lock;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLockstate() {
        return lockstate;
    }

    public void setLockstate(String lockstate) {
        this.lockstate = lockstate;
    }
}
