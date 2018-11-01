package cn.wizzer.app.eq.modules.models;

import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.app.sys.modules.models.Sys_dict;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.framework.base.dao.entity.annotation.Ref;
import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;
import cn.wizzer.app.base.modules.models.*;

import java.io.Serializable;

/**
 * Created by xl on 2017/9/14.
 * 轮档使用表
 */
@Table("eq_chockuse")
@Comment("轮挡")
public class eq_chockuse extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("useinfoID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String useinfoID;
    @One(field = "useinfoID")
    private eq_useinfo useinfo;

    @Column
    @Comment("设备ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String eqid;

    @Column
    @Comment("桩位ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String stakeid;


    @Column
    @Comment("机位ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String seatid;
    @One(field = "seatid")
    private eq_planeseat planeseat;

    @Column
    @Comment("客户端开始时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String starttime;

    @Column
    @Comment("客户端结束时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String endtime;

    @Column
    @Comment("服务端开始时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String sstarttime;

    @Column
    @Comment("服务端结束时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String sendtime;

    @Column
    @Comment("接航班使用人，记录userid")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_user.class)
    private String getpersonid;
    @One(field = "getpersonid")
    private Sys_user getperson;

    @Column
    @Comment("送航班使用人，记录userid")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Ref(Sys_user.class)
    private String gopersonid;
    @One(field = "gopersonid")
    private Sys_user goperson;

    @Column
    @Comment("工作类型")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String worktype;
    @One(field = "worktype")
    private Sys_dict work;

    @Column
    @Comment("状态")//1 使用中 0 使用完毕
    @ColDefine(type = ColType.VARCHAR, width = 32)
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

    public String getUseinfoID(){return useinfoID;}

    public void setUseinfoID(String useinfoID){this.useinfoID=useinfoID;}

    public eq_useinfo getUseinfo(){return useinfo;}

    public void setUseinfo(eq_useinfo useinfo){this.useinfo=useinfo;}

    public String getStakeid() { return stakeid; }

    public void setStakeid( String stakeid ) { this.stakeid = stakeid; }

    public String getEqid() { return eqid; }

    public void setEqid( String eqid ) { this.eqid = eqid; }

    public String getSeatid() { return seatid; }

    public void setSeatid( String seatid ) { this.seatid = seatid; }

    public String getStarttime() { return starttime; }

    public void setStarttime( String starttime ) { this.starttime = starttime; }

    public String getEndtime() { return endtime; }

    public void setEndtime( String endtime ) { this.endtime = endtime; }

    public String getSstarttime() { return sstarttime; }

    public void setSstarttime( String sstarttime ) { this.sstarttime = sstarttime; }

    public String getSendtime() { return sendtime; }

    public void setSendtime( String sendtime ) { this.sendtime = sendtime; }

    public String getGetpersonid() { return getpersonid; }

    public void setGetpersonid( String getpersonid ) { this.getpersonid = getpersonid; }

    public String getGopersonid() { return gopersonid; }

    public void setGopersonid( String gopersonid ) { this.gopersonid = gopersonid; }

    public String getWorktype() { return worktype; }

    public void setWorktype( String worktype ) { this.worktype = worktype; }

    public String getPstatus() { return pstatus; }

    public void setPstatus( String pstatus ) { this.pstatus = pstatus; }

    public eq_planeseat getPlaneseat() { return planeseat; }

    public void setPlaneseat( eq_planeseat planeseat ) { this.planeseat = planeseat; }

    public Sys_user getGetperson() { return getperson; }

    public void setGetperson( Sys_user getperson ) { this.getperson = getperson; }

    public Sys_user getGoperson() { return goperson; }

    public void setGoperson( Sys_user goperson ) { this.goperson = goperson; }

    public Sys_dict getWork() { return work; }

    public void setWork( Sys_dict work ) { this.work = work; }


}
