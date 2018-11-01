package cn.wizzer.app.eq.modules.services;

import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.eq.modules.models.eq_planeseat;
import org.nutz.dao.entity.Record;
import org.nutz.lang.util.NutMap;

import java.util.List;

public interface EqPlaneseatService extends BaseService<eq_planeseat>{

    public List<Record> getPlaneSeatByAirport(String airportid,String borrow,String seatId);
    List<Record> getSeatsOfEqUse(String airportid, String borrow, String seatId);
//    List<List<Record>> getSeatsOfEqUse(String airportid, String borrow, String seatId);
    List<Record> getMarkerBySeatList(String[] seatList, String airportid, String borrow);

//    List<Record> getRightSeats(String airportid, String borrow);
}
