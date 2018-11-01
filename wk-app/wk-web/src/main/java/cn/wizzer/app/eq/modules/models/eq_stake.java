package cn.wizzer.app.eq.modules.models;

import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

/**
 * Created by xl on 2017/9/14.
 * 桩位表
 */
@Table("eq_stake")
public class eq_stake extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("桩位编码")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String stakenum;

    @Column
    @Comment("桩位名称")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String stakename;


    @Column
    @Comment("所属机场")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String airportId;
    @One(field = "airportId")
    private base_airport airport;

    @Column
    @Comment("所属机位")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String seatid;
    @One(field = "seatid")
    private eq_planeseat planeseat;

    @Column
    @Comment("地理位置")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String position;

    //20180422zhf1308
    @Column
    @Comment("是否已经绑定设备 0:没有,1.有")
    @ColDefine(type = ColType.BOOLEAN)
    private boolean linkEqMaterial;

    public boolean isLinkEqMaterial() {
        return linkEqMaterial;
    }
    public void setLinkEqMaterial(boolean linkEqMaterial) {
        this.linkEqMaterial = linkEqMaterial;
    }
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStakenum() {
        return stakenum;
    }

    public void setStakenum(String stakenum) {
        this.stakenum = stakenum;
    }

    public String getStakename() {
        return stakename;
    }

    public void setStakename(String stakename) {
        this.stakename = stakename;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getAirportId() {
        return airportId;
    }

    public void setAirportId(String airportId) {
        this.airportId = airportId;
    }

    public base_airport getAirport() {
        return airport;
    }

    public void setAirport(base_airport airport) {
        this.airport = airport;
    }

    public String getSeatid(){ return seatid;}

    public void setSeatid(String seatid){this.seatid=seatid;}

    public eq_planeseat getPlaneseat(){ return planeseat;}

    public void setPlaneseat(eq_planeseat planeseat){this.planeseat=planeseat;}
}
