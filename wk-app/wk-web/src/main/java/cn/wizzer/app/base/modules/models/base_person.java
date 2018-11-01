package cn.wizzer.app.base.modules.models;

import cn.wizzer.app.sys.modules.models.Sys_dict;
import cn.wizzer.app.sys.modules.models.Sys_unit;
import cn.wizzer.framework.base.dao.entity.annotation.Ref;
import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

/**
 * Created by xl on 2017/6/1.
 */
@Comment("人员表")
@Table("base_person")
public class base_person extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("人员编号")
    @ColDefine(type = ColType.VARCHAR, width = 120)
    private String personnum;

    @Column
    @Comment("人员姓名")
    @ColDefine(type = ColType.VARCHAR, width = 120)
    private String personname;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_unit.class)
    private String unitid;

    @One(field = "unitid")
    private Sys_unit unit;



    @Column
    @Comment("证件号")
    @ColDefine(type = ColType.VARCHAR, width = 255)
    private String cardid;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(base_airport.class)
    private String airportid;

    @One(field = "airportid")
    private base_airport base_airport;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String deptid;

    @One(field = "deptid")
    private base_dept base_dept;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String jobid;

    @One(field = "jobid")
    private base_job base_job;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String postid;
    @One(field = "postid")
    private base_post base_post;


    @Column
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String placeid;

    @One(field = "placeid")
    private base_place base_place;

    @Column
    @Comment("类型")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String emptypeId;
    @One(field = "emptypeId")
    private Sys_dict dict;

    @Column
    @Comment("客户ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String customerId;

    @One(field = "customerId")
    private base_customer customer;

    //20180320zhf1805
    @Column
    @Comment("是否是领导")
    @ColDefine(type = ColType.BOOLEAN)
    private boolean isLeader;

    //20180326zhf1043
    @Column
    @Comment("员工图片")
    @ColDefine(type = ColType.VARCHAR, width = 512)
    private String picture;

    @Column
    @Comment("企业微信用户ID")
    @ColDefine(type = ColType.VARCHAR, width = 64)
    private String wxUserid;

    //20180417zhf1033
    //0:男 1:女
    @Column
    @Comment("性别")
    @ColDefine(type = ColType.INT,width = 2)
    private int sex;

    //20180417zhf2055
    @Column
    @Comment("tel")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String tel;

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getWxUserid() {
        return wxUserid;
    }

    public void setWxUserid(String wxUserid) {
        this.wxUserid = wxUserid;
    }

    public Boolean getLeader() {
        return this.isLeader;
    }

    public boolean isLeader() {
        return isLeader;
    }
    public void setLeader(boolean leader) {
        isLeader = leader;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPersonnum() {
        return personnum;
    }

    public void setPersonnum(String personnum) {
        this.personnum = personnum;
    }

    public String getPersonname() {
        return personname;
    }

    public void setPersonname(String personname) {
        this.personname = personname;
    }

    public String getUnitid() {
        return unitid;
    }

    public void setUnitid(String unitid) {
        this.unitid = unitid;
    }

    public Sys_unit getUnit() {
        return unit;
    }

    public void setUnit(Sys_unit unit) {
        this.unit = unit;
    }

    public String getAirportid() {
        return airportid;
    }

    public void setAirportid(String airportid) {
        this.airportid = airportid;
    }

    public cn.wizzer.app.base.modules.models.base_airport getBase_airport() {
        return base_airport;
    }

    public void setBase_airport(cn.wizzer.app.base.modules.models.base_airport base_airport) {
        this.base_airport = base_airport;
    }

    public String getDeptid() {
        return deptid;
    }

    public void setDeptid(String deptid) {
        this.deptid = deptid;
    }

    public cn.wizzer.app.base.modules.models.base_dept getBase_dept() {
        return base_dept;
    }

    public void setBase_dept(cn.wizzer.app.base.modules.models.base_dept base_dept) {
        this.base_dept = base_dept;
    }

    public String getJobid() {
        return jobid;
    }

    public void setJobid(String jobid) {
        this.jobid = jobid;
    }

    public cn.wizzer.app.base.modules.models.base_job getBase_job() {
        return base_job;
    }

    public void setBase_job(cn.wizzer.app.base.modules.models.base_job base_job) {
        this.base_job = base_job;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPlaceid() {
        return placeid;
    }

    public void setPlaceid(String placeid) {
        this.placeid = placeid;
    }

    public cn.wizzer.app.base.modules.models.base_place getBase_place() {
        return base_place;
    }

    public void setBase_place(cn.wizzer.app.base.modules.models.base_place base_place) {
        this.base_place = base_place;
    }

    public cn.wizzer.app.base.modules.models.base_post getBase_post() {
        return base_post;
    }

    public void setBase_post(cn.wizzer.app.base.modules.models.base_post base_post) {
        this.base_post = base_post;
    }

    public String getEmptypeId() {
        return emptypeId;
    }

    public void setEmptypeId(String emptypeId) {
        this.emptypeId = emptypeId;
    }

    public Sys_dict getDict() {
        return dict;
    }

    public void setDict(Sys_dict dict) {
        this.dict = dict;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public base_customer getCustomer() {
        return customer;
    }

    public void setCustomer(base_customer customer) {
        this.customer = customer;
    }

    public String getCardid() {
        return cardid;
    }

    public void setCardid(String cardid) {
        this.cardid = cardid;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
