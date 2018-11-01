package cn.wizzer.app.eq.modules.models;

import cn.wizzer.app.sys.modules.models.Sys_unit;
import cn.wizzer.framework.base.dao.entity.annotation.Ref;
import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

/**
 * Created by xl on 2017/9/14.
 * 设备使用表
 */
@Comment("设备使用表")
@Table("eq_use")
public class eq_use extends BaseModel implements Serializable {
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
    @Ref(eq_materiel.class)
    private String eqid;
    @One(field = "eqid")
    private eq_materiel eqMateriel;


    @Column
    @Comment("设备单位id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_unit.class)
    private String equnitid;
    @One(field = "equnitid")
    private Sys_unit sysUnit;

    @Column
    @Comment("借用状态") //只表示锁的开关状态 0 未使用(即关锁中) 1 使用中(即开锁中) 20180515 kdp 调整
    @ColDefine(type = ColType.INT)
    private String pstatus;

    @Column
    @Comment("操作失败状态") //临时记录锁是否开关操作失败 0 无 1 开锁失败 2 关锁失败  add by koudepei 20180823
    @ColDefine(type = ColType.INT)
    private String failStatus;

    @Column
    @Comment("业务状态") //0 正常使用中； 1 报修中；2 维修中；  其它如保养等业务待定。。。//20180515 kdp 追加（非正常使用状态时，只有特定权限的用户才能解锁）
    @ColDefine(type = ColType.INT)
    @Default("0")
    private String bizstatus;

    @Column
    @Comment("异常状态")//0 正常 1 告警（如电量不足等） 2 超时借用(业务实现待定) 3 错误异常（硬件开关状态和系统中不一致） 4 设备信号失联(通过任务定时来判断)
    @ColDefine(type = ColType.INT)
    private String errstatus;

    @Column
    @Comment("固定桩位")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(eq_stake.class)
    private String stakeid;
    @One(field = "stakeid")
    private eq_stake eqStake;

    @Column
    @Comment("地理位置")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String position;

    @Column
    @Comment("电量")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String power;

    @Column
    @Comment("关联使用信息")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(eq_useinfo.class)
    private String equseinfoid;
    @One(field = "equseinfoid")
    private eq_useinfo eqUseinfo;

    @Column
    @Comment("锁ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(eq_lock.class)
    private String lid;
    @One(field = "lid")
    private eq_lock lock;

    private String personname;
    private String personunit;
    private String eqname;
    private String equnit;


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


    public String getEqunitid() {
        return equnitid;
    }

    public void setEqunitid(String equnitid) {
        this.equnitid = equnitid;
    }

    public String getPstatus() {
        return pstatus;
    }

    public void setPstatus(String pstatus) {
        this.pstatus = pstatus;
    }

    public String getStakeid() {
        return stakeid;
    }

    public void setStakeid(String stakeid) {
        this.stakeid = stakeid;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
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

    public Sys_unit getSysUnit() {
        return sysUnit;
    }

    public void setSysUnit(Sys_unit sysUnit) {
        this.sysUnit = sysUnit;
    }

    public eq_stake getEqStake() {
        return eqStake;
    }

    public void setEqStake(eq_stake eqStake) {
        this.eqStake = eqStake;
    }

    public String getErrstatus() {
        return errstatus;
    }

    public void setErrstatus(String errstatus) {
        this.errstatus = errstatus;
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

    public String getPersonname() {
        return personname;
    }

    public void setPersonname(String personname) {
        this.personname = personname;
    }

    public String getPersonunit() {
        return personunit;
    }

    public void setPersonunit(String personunit) {
        this.personunit = personunit;
    }

    public String getEqname() {
        return eqname;
    }

    public void setEqname(String eqname) {
        this.eqname = eqname;
    }

    public String getEqunit() {
        return equnit;
    }

    public void setEqunit(String equnit) {
        this.equnit = equnit;
    }

    public String getBizstatus() {
        return bizstatus;
    }

    public void setBizstatus(String bizstatus) {
        this.bizstatus = bizstatus;
    }

    public String getFailStatus() {
        return failStatus;
    }

    public void setFailStatus(String failStatus) {
        this.failStatus = failStatus;
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
}
