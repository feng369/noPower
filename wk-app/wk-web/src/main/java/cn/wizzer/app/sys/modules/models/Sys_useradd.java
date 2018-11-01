package cn.wizzer.app.sys.modules.models;

import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.app.base.modules.models.base_dept;
import cn.wizzer.app.base.modules.models.base_job;
import cn.wizzer.framework.base.dao.entity.annotation.Ref;
import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by cw on 2018/1/5.
 */
@Table("sys_useradd")
public class Sys_useradd extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("userid")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String userid;
    @One(field = "userid")
    private Sys_user user;

    @Column
    @Comment("tel")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String tel;

    @Column
    @Comment("jobs")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String jobs;

    @Column
    @Comment("cardid")
    @ColDefine(type = ColType.VARCHAR, width = 255)
    private String cardid;

    @Column
    @Comment("pictureads")
    @ColDefine(type = ColType.VARCHAR, width = 512)
    private String pictureads;

    //20180326zhf1039
    @Column
    @Comment("注册是否需要审核状态 0:不需要审核,1.需要审核")
    @ColDefine(type = ColType.BOOLEAN)
    private boolean needAudit;

    //20180417zhf1033
    //0:男 1:女
    @Column
    @Comment("性别")
    @ColDefine(type = ColType.INT,width = 2)
    private int sex;
    //1
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String airportid;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String deptid;
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String jobid;
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String unitid;
    //2.

    public String getAirportid() {
        return airportid;
    }

    public void setAirportid(String airportid) {
        this.airportid = airportid;
    }

    public String getDeptid() {
        return deptid;
    }

    public void setDeptid(String deptid) {
        this.deptid = deptid;
    }

    public String getJobid() {
        return jobid;
    }

    public void setJobid(String jobid) {
        this.jobid = jobid;
    }

    public String getUnitid() {
        return unitid;
    }

    public void setUnitid(String unitid) {
        this.unitid = unitid;
    }

    public boolean isNeedAudit() {
        return needAudit;
    }

    public void setNeedAudit(boolean needAudit) {
        this.needAudit = needAudit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getJobs() {
        return jobs;
    }

    public void setJobs(String jobs) {
        this.jobs = jobs;
    }

    public String getCardid() {
        return cardid;
    }

    public void setCardid(String cardid) {
        this.cardid = cardid;
    }

    public String getPictureads() {
        return pictureads;
    }

    public void setPictureads(String pictureads) {
        this.pictureads = pictureads;
    }

    public Sys_user getUser() {
        return user;
    }

    public void setUser(Sys_user user) {
        this.user = user;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }
}
