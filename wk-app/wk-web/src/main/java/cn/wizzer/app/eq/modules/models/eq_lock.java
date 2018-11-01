package cn.wizzer.app.eq.modules.models;

import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

/**
 * Created by cw on 2017/12/20.
 * 智能锁表
 */
@Table("eq_lock")
public class eq_lock extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column
    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("锁编码")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String locknum;

    //20180424zhf1523
    @Column
    @Comment("锁编号")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String lockcode;

    @Column
    @Comment("锁状态（0 空闲 1 使用中 2 下线）")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String lockstatus;


    @Column
    @Comment("锁参数")
    @ColDefine(type = ColType.VARCHAR, width = 255)
    private String lockparam;


    @Column
    @Comment("描述")
    @ColDefine(type = ColType.VARCHAR, width = 255)
    private String describ;

    @Column
    @Comment("所在机场")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String airportId;
    @One(field = "airportId")
    private base_airport airport;

    //20180823 如下两字段是再getinfo接口中对每次锁的命令操作进行临时记录，用于重复操作判断，特别是网络延迟收到的命令的顺序与操作不一致
    @Column
    @Comment("最新的操作命令")
    @ColDefine(type = ColType.VARCHAR, width = 2)
    private String lastopType;

    @Column
    @Comment("最新的操作时间戳")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String lastopTime;

    public String getLockcode() {
        return lockcode;
    }

    public void setLockcode(String lockcode) {
        this.lockcode = lockcode;
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

    public String getLocknum() {
        return locknum;
    }

    public void setLocknum(String locknum) {
        this.locknum = locknum;
    }

    public String getLockstatus() {
        return lockstatus;
    }

    public void setLockstatus(String lockstatus) {
        this.lockstatus = lockstatus;
    }

    public String getLockparam() {
        return lockparam;
    }

    public void setLockparam(String lockparam) {
        this.lockparam = lockparam;
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

    public String getDescrib() {
        return describ;
    }

    public void setDescrib(String describ) {
        this.describ = describ;
    }

    public String getLastopType() {
        return lastopType;
    }

    public void setLastopType(String lastopType) {
        this.lastopType = lastopType;
    }

    public String getLastopTime() {
        return lastopTime;
    }

    public void setLastopTime(String lastopTime) {
        this.lastopTime = lastopTime;
    }
}
