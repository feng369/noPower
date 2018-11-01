package cn.wizzer.app.eq.modules.models;

import cn.wizzer.app.sys.modules.models.Sys_unit;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.framework.base.dao.entity.annotation.Ref;
import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

/**
 * Created by xl on 2017/9/14.
 * 设备使用信息表
 */
@Comment("设备使用信息表")
@Table("eq_useinfo")
public class eq_useinfo extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("借用单编号")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String equsenum;

    @Column
    @Comment("设备id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(eq_materiel.class)
    private String eqid;

    @One(field = "eqid")
    private eq_materiel materiel;

    @Column
    @Comment("锁ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(eq_lock.class)
    private String lid;
    @One(field = "lid")
    private eq_lock lock;

    @Column
    @Comment("借用人id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_user.class)
    private String personid;
    @One(field = "personid")
    private Sys_user sysUser;

    @Column
    @Comment("借用人单位ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_unit.class)
    private String personunitid;
    @One(field = "personunitid")
    private Sys_unit sysUnit;

    @Column
    @Comment("设备单位id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String equnitid;

    @Column
    @Comment("客户端借用开始时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String starttime;

    @Column
    @Comment("客户端借用结束时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String endtime;

    @Column
    @Comment("服务器借用开始时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String sstarttime;

    @Column
    @Comment("服务器借用结束时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String sendtime;

    @Column
    @Comment("借用状态") //0 借用中 1 已关锁 2 借用完毕
    @ColDefine(type = ColType.INT)
    private String pstatus;

    @Column
    @Comment("转交标识")
    @ColDefine(type = ColType.VARCHAR)
    private String deliver;

    @Column
    @Comment("备注")
    @ColDefine(type = ColType.VARCHAR)
    private String remark;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEqusenum() {
        return equsenum;
    }

    public void setEqusenum(String equsenum) {
        this.equsenum = equsenum;
    }

    public String getEqid() {
        return eqid;
    }

    public void setEqid(String eqid) {
        this.eqid = eqid;
    }

    public String getPersonid() {
        return personid;
    }

    public void setPersonid(String personid) {
        this.personid = personid;
    }

    public String getPersonunitid() {
        return personunitid;
    }

    public void setPersonunitid(String personunitid) {
        this.personunitid = personunitid;
    }

    public String getEqunitid() {
        return equnitid;
    }

    public void setEqunitid(String equnitid) {
        this.equnitid = equnitid;
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

    public String getSstarttime() {
        return sstarttime;
    }

    public void setSstarttime(String sstarttime) {
        this.sstarttime = sstarttime;
    }

    public String getSendtime() {
        return sendtime;
    }

    public void setSendtime(String sendtime) {
        this.sendtime = sendtime;
    }

    public String getDeliver() {
        return deliver;
    }

    public void setDeliver(String deliver) {
        this.deliver = deliver;
    }

    public String getPstatus() {
        return pstatus;
    }

    public void setPstatus(String pstatus) {
        this.pstatus = pstatus;
    }

    public eq_materiel getMateriel() { return materiel;}

    public void setMateriel(eq_materiel materiel) { this.materiel=materiel; }

    public Sys_user getSysUser() {
        return sysUser;
    }

    public void setSysUser(Sys_user sysUser) {
        this.sysUser = sysUser;
    }

    public Sys_unit getSysUnit() {
        return sysUnit;
    }

    public void setSysUnit(Sys_unit sysUnit) {
        this.sysUnit = sysUnit;
    }

    public String getLid() {
        return lid;
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    public eq_lock getLock() {
        return lock;
    }

    public void setLock(eq_lock lock) {
        this.lock = lock;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
