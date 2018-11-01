package cn.wizzer.app.eq.modules.models;

import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

/**
 * Created by kdp on 2018/3/11
 * 设备跟踪记录表
 */
@Table("eq_trace")
@TableIndexes({@Index(name = "INDEX_EQ_TRACE_EQID", fields = {"eqid"}, unique = false)})
public class eq_trace extends BaseModel implements Serializable {
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
    @Comment("锁号")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String lockid;

//    @One(field = "lid")
//    private eq_lock lock;

    @Column
    @Comment("地理位置")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String position;

    @Column
    @Comment("电量")
    @ColDefine(type = ColType.VARCHAR, width = 5)
    private String power;

    @Column
    @Comment("关联使用信息表")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String equseinfoid;
    @One(field = "equseinfoid")
    private eq_useinfo eqUseinfo;

    @Column
    @Comment("关联使用表")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String equseid;
    @One(field = "equseid")
    private eq_use eqUse;

    @Column
    @Comment("上报时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String time;

    @Column
    @Comment("锁状态")  //lockstate=1关锁状态,lockstate=0,开锁状态
    @ColDefine(type = ColType.INT)
    private String lockstate;

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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public eq_materiel getEqMateriel() {
        return eqMateriel;
    }

    public void setEqMateriel(eq_materiel eqMateriel) {
        this.eqMateriel = eqMateriel;
    }

    public String getEquseinfoid() {
        return equseinfoid;
    }

    public void setEquseinfoid(String equseinfoid) {
        this.equseinfoid = equseinfoid;
    }

    public eq_useinfo getEqUseinfo() {
        return eqUseinfo;
    }

    public void setEqUseinfo(eq_useinfo eqUseinfo) {
        this.eqUseinfo = eqUseinfo;
    }

    public String getLockid() {
        return lockid;
    }

    public void setLockid(String lockid) {
        this.lockid = lockid;
    }

//    public eq_lock getLock() {
//        return lock;
//    }
//
//    public void setLock(eq_lock lock) {
//        this.lock = lock;
//    }

    public String getEquseid() {
        return equseid;
    }

    public void setEquseid(String equseid) {
        this.equseid = equseid;
    }

    public eq_use getEqUse() {
        return eqUse;
    }

    public void setEqUse(eq_use eqUse) {
        this.eqUse = eqUse;
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

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }
}
