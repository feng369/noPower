package cn.wizzer.app.eq.modules.models;

import cn.wizzer.app.sys.modules.models.Sys_dict;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.framework.base.dao.entity.annotation.Ref;
import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

/**
 * Created by xl on 2017/9/19.
 * 维修表
 */
@Table("eq_repair")
@Comment("维修表")
public class eq_repair extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("编号")
    @ColDefine(type = ColType.VARCHAR, width = 50)
    private String repnum;

    @Column
    @Comment("设备ID")
    @ColDefine(type = ColType.VARCHAR, width = 50)
    private String eqid;
    @One(field = "eqid")
    private eq_materiel eqMateriel;

    @Column
    @Comment("类型")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String reptype;
    @One(field = "reptype")
    private Sys_dict sysDict;

    @Column
    @Comment("内容")
    @ColDefine(type = ColType.VARCHAR, width = 500)
    private String reptext;


    @Column
    @Comment("上报人ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_user.class)
    private String personid;
    @One(field = "personid")
    private Sys_user sysUser;

    @Column
    @Comment("维修负责人ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_user.class)
    private String wxuserid;
    @One(field = "wxuserid")
    private Sys_user wxUser;


    @Column
    @Comment("图片路径")
    @ColDefine(type = ColType.VARCHAR, width = 255)
    private String imgpath;

    @Column
    @Comment("图片名称")
    @ColDefine(type = ColType.VARCHAR, width = 200)
    private String imgname;


    @Column
    @Comment("图片原名称")
    @ColDefine(type = ColType.VARCHAR, width = 200)
    private String oldimgname;

    @Column
    @Comment("状态,1报修中，2维修中，0完成，99草稿")
    @ColDefine(type = ColType.VARCHAR, width = 10)
    private String pstatus;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRepnum() {
        return repnum;
    }

    public void setRepnum(String repnum) {
        this.repnum = repnum;
    }

    public String getReptype() {
        return reptype;
    }

    public void setReptype(String reptype) {
        this.reptype = reptype;
    }

    public Sys_dict getSysDict() {
        return sysDict;
    }

    public void setSysDict(Sys_dict sysDict) {
        this.sysDict = sysDict;
    }

    public String getReptext() {
        return reptext;
    }

    public void setReptext(String reptext) {
        this.reptext = reptext;
    }

    public String getPersonid() {
        return personid;
    }

    public void setPersonid(String personid) {
        this.personid = personid;
    }

    public String getImgpath() {
        return imgpath;
    }

    public void setImgpath(String imgpath) {
        this.imgpath = imgpath;
    }

    public String getImgname() {
        return imgname;
    }

    public void setImgname(String imgname) {
        this.imgname = imgname;
    }

    public String getOldimgname() {
        return oldimgname;
    }

    public void setOldimgname(String oldimgname) {
        this.oldimgname = oldimgname;
    }

    public String getEqid() {
        return eqid;
    }

    public void setEqid(String eqid) {
        this.eqid = eqid;
    }

    public eq_materiel getEqMateriel() {
        return eqMateriel;
    }

    public void setEqMateriel(eq_materiel eqMateriel) {
        this.eqMateriel = eqMateriel;
    }

    public String getPstatus() {
        return pstatus;
    }

    public void setPstatus(String pstatus) {
        this.pstatus = pstatus;
    }

    public Sys_user getSysUser() {
        return sysUser;
    }

    public void setSysUser(Sys_user sysUser) {
        this.sysUser = sysUser;
    }

    public String getWxuserid() {
        return wxuserid;
    }

    public void setWxuserid(String wxuserid) {
        this.wxuserid = wxuserid;
    }

    public Sys_user getWxUser() {
        return wxUser;
    }

    public void setWxUser(Sys_user wxUser) {
        this.wxUser = wxUser;
    }
}
