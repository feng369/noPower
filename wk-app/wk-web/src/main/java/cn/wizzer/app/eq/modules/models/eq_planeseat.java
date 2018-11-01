package cn.wizzer.app.eq.modules.models;

import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;
import java.io.Serializable;

/**
 * Created by xl on 2017/9/25.
 * 机位表
 */
@Table("eq_planeseat")
public class eq_planeseat extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;


    @Column
    @Comment("机位编号")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String seatnum;

    @Column
    @Comment("机位名称")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String seatname;

    @Column
    @Comment("坐标")
    @ColDefine(type = ColType.VARCHAR, width = 128)
    private String position;

    @Column
    @Comment("所属机场")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String airportid;
    @One(field = "airportid")
    private base_airport baseAirport;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSeatnum() {
        return seatnum;
    }

    public void setSeatnum(String seatnum) {
        this.seatnum = seatnum;
    }

    public String getSeatname() {
        return seatname;
    }

    public void setSeatname(String seatname) {
        this.seatname = seatname;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getAirportid() {
        return airportid;
    }

    public void setAirportid(String airportid) {
        this.airportid = airportid;
    }

    public base_airport getBaseAirport() {
        return baseAirport;
    }

    public void setBaseAirport(base_airport baseAirport) {
        this.baseAirport = baseAirport;
    }
}
