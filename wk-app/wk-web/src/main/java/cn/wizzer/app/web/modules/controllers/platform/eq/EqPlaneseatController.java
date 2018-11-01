package cn.wizzer.app.web.modules.controllers.platform.eq;

import cn.wizzer.app.base.modules.models.base_cnctobj;
import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.map.LngLat;
import cn.wizzer.framework.map.MapUtils;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.eq.modules.models.eq_planeseat;
import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.app.base.modules.services.BaseAirportService;
import cn.wizzer.app.eq.modules.services.EqPlaneseatService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.filter.CrossOriginFilter;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@IocBean
@At("/platform/eq/planeseat")
public class EqPlaneseatController{
    private static final Log log = Logs.get();
    @Inject
    private EqPlaneseatService eqPlaneseatService;
    @Inject
    private BaseAirportService baseAirportService;
    @Inject
    private BaseCnctobjService baseCnctobjService;
    @Inject
    private BasePersonService basePersonService;

    @At("")
    @Ok("beetl:/platform/eq/planeseat/index.html")
    @RequiresPermissions("platform.eq.planeseat")
    public void index() {
    }

    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.eq.planeseat.select")
    public Object data(@Param("name")String name,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
		Cnd cnd = Cnd.NEW();
		//20180404zhf0951
		if(!Strings.isBlank(name)){
            name = name.replace("'", "");
            cnd.and("seatname","LIKE","%"+name+"%");
            cnd.or("seatnum","LIKE","%"+name+"%");
		}
    	return eqPlaneseatService.data(length, start, draw, order, columns, cnd, "baseAirport");
    }

    @At("/add")
    @Ok("beetl:/platform/eq/planeseat/add.html")
    @RequiresPermissions("platform.eq.planeseat")
    public void add(HttpServletRequest req) {
//20180403zzhf1638
        //回显登陆人所属机场
        Sys_user sysUser = (Sys_user) SecurityUtils.getSubject().getPrincipal();
        if(sysUser != null ){
            base_cnctobj baseCnctobj = baseCnctobjService.fetch(Cnd.where("userId", "=", sysUser.getId()));
            String personId = baseCnctobj.getPersonId();
            if(!Strings.isBlank(personId)){
                base_person basePerson = basePersonService.fetch(Cnd.where("id", "=", personId));
                if(basePerson != null){
                    if(!Strings.isBlank(basePerson.getAirportid())){
                        base_airport baseAirport = baseAirportService.fetch(Cnd.where("id", "=", basePerson.getAirportid()));
                        req.setAttribute("baseAirport",baseAirport);
                    }
                }
            }

        }
    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.planeseat.add")
    @SLog(tag = "eq_planeseat", msg = "${args[0].id}")
    public Object addDo(@Param("..")eq_planeseat eqPlaneseat, HttpServletRequest req) {
		try {
			eqPlaneseatService.insert(eqPlaneseat);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/eq/planeseat/edit.html")
    @RequiresPermissions("platform.eq.planeseat")
    public void edit(String id,HttpServletRequest req) {
        eq_planeseat eqPlaneseat = eqPlaneseatService.fetch(id);
        if(!Strings.isBlank(eqPlaneseat.getAirportid())){
            req.setAttribute("baseAirport", baseAirportService.fetch(eqPlaneseat.getAirportid()));
        }
        req.setAttribute("obj",eqPlaneseat);
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.planeseat.edit")
    @SLog(tag = "eq_planeseat", msg = "${args[0].id}")
    public Object editDo(@Param("..")eq_planeseat eqPlaneseat, HttpServletRequest req) {
		try {
            eqPlaneseat.setOpBy(StringUtil.getUid());
			eqPlaneseat.setOpAt((int) (System.currentTimeMillis() / 1000));
			eqPlaneseatService.updateIgnoreNull(eqPlaneseat);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.eq.planeseat.delete")
    @SLog(tag = "eq_planeseat", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				eqPlaneseatService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				eqPlaneseatService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/eq/planeseat/detail.html")
    @RequiresAuthentication
	public void detail(String id, HttpServletRequest req) {
		if (!Strings.isBlank(id)) {
		    eq_planeseat planeseat=eqPlaneseatService.fetch(id);
		    if(!Strings.isBlank(planeseat.getAirportid())){
		        base_airport airport = baseAirportService.fetch(planeseat.getAirportid());
		        planeseat.setBaseAirport(airport);
            }
            req.setAttribute("obj", planeseat);
		}else{
            req.setAttribute("obj", null);
        }
    }

    @At("/getAroundPlaneseat")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getAroundPlaneseat(@Param("airportid") String airportid,@Param("position") String position){
        try{
            if(!Strings.isBlank(airportid) &&!Strings.isBlank(position)&& position.indexOf(",")>0){
                Cnd cnd=Cnd.NEW();
                cnd.and("airportid","=",airportid);
                List<eq_planeseat> planeseats = eqPlaneseatService.query(cnd);
                List<HashMap> mapList=new ArrayList<>();
                if(planeseats.size()>0){
                    String[]spos = position.split(",");
                    LngLat start = new LngLat(Double.valueOf(spos[0]),Double.valueOf(spos[1]));
                    for(int i=0;i<planeseats.size();i++){
                        eq_planeseat planeseat = planeseats.get(i);
                        if(StringUtils.isNotBlank(planeseat.getPosition())&& planeseat.getPosition().indexOf(",")>0) {
                            String[] epos = planeseat.getPosition().split(",");
                            LngLat end = new LngLat(Double.valueOf(epos[0]), Double.valueOf(epos[1]));
                            double disDouble = MapUtils.calculateLineDistance(start, end);
                            int disInt = new BigDecimal(disDouble).setScale(0, java.math.BigDecimal.ROUND_HALF_UP).intValue();
                            if (disInt <= Integer.valueOf(Globals.QueryDis).intValue()) {
                                HashMap map=new HashMap();
                                map.put("position",planeseat.getPosition());
                                map.put("seatname",planeseat.getSeatname());
                                map.put("seatnum",planeseat.getSeatnum());
                                map.put("id",planeseat.getId());
                                mapList.add(map);
                            }
                        }
                    }
                }
                return Result.success("system.success",mapList);
            }
            return Result.error("输入参数不正确");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }


    @At("/getPlaneseat")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getPlaneseat(@Param("airportid") String airportid){
        try{
            if(!Strings.isBlank(airportid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("airportid","=",airportid);
                List<eq_planeseat> planeseats = eqPlaneseatService.query(cnd);
                List<HashMap> mapList=new ArrayList<>();
                if(planeseats.size()>0){
                    for(int i=0;i<planeseats.size();i++){
                        HashMap map=new HashMap();
                        map.put("position",planeseats.get(i).getPosition());
                        map.put("seatname",planeseats.get(i).getSeatname());
                        map.put("seatnum",planeseats.get(i).getSeatnum());
                        map.put("id",planeseats.get(i).getId());
                        mapList.add(map);
                    }
                }
                return Result.success("system.success",mapList);
            }
            return Result.error(2,"airportid is null");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getCountByseatnum")
    @Ok("json")
    public Object getCountByseatnum(String seatnum){
        return eqPlaneseatService.count("eq_planeseat", Cnd.where("seatnum", "=", seatnum));

    }

    @At("/getPlaneSeatByAirport")
    @Ok("json")
    public Object getPlaneSeatByAirport(@Param("airportid") String airportid,  @Param("seatId")String seatId, @Param("borrow")String borrow){
        return eqPlaneseatService.getPlaneSeatByAirport(airportid,borrow,seatId);

    }

    @At("/getSeatsOfEqUse")
    @Ok("json")
    public Object getSeatsOfEqUse(@Param("airportid") String airportid,  @Param("seatId")String seatId, @Param("borrow")String borrow){
        return eqPlaneseatService.getSeatsOfEqUse(airportid,borrow,seatId);

    }
    /*@At("/getRightSeats")
    @Ok("json")
    public Object getRightSeats(@Param("airportid") String airportid, @Param("borrow")String borrow){
        return eqPlaneseatService.getRightSeats(airportid,borrow);
    }*/

    //20180413zhf1020 //首页监控使用暂不要session过期
    @At("/getMarkerBySeatList")
    @Ok("json")
    public Object getMarkerBySeatList(@Param("seatList") String[] seatList,@Param("airportid") String airportid,@Param("borrow")String borrow){
        return eqPlaneseatService.getMarkerBySeatList(seatList,airportid,borrow);
    }

    @At("/selectPlaneSeat")
    @Ok("beetl:/platform/eq/stake/selectPlaneSeat.html")
    @RequiresPermissions("platform.eq.planeseat")
    public void selectPlaneSeat() {
    }
}
