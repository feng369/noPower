package cn.wizzer.app.web.modules.controllers.eq.control;

import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.app.base.modules.services.BaseAirportService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.eq.modules.services.*;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.json.JSONObject;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xl on 2017/9/15.
 */
@IocBean
@At("/platform/eq/Map")
public class eqMapController {
    //专门针对硬件通讯接口getinfo方法定制日志文件
    private static Logger luckyLog = Logger.getLogger("LuckyLog");
    Log log = Logs.get();
    @Inject
    private BasePersonService basePersonService;
    @Inject
    private BaseAirportService baseAirportService;
    @Inject
    private EqUseService eqUseService;
    @Inject
    private EqUseinfoService eqUseinfoService;


    @At("")
    @Ok("beetl:/platform/eq/Map/index.html")
    @RequiresPermissions("eq.Map.index")
    public void index(HttpServletRequest req) {
        String position="";
        //获取当前登录人
        base_person person= basePersonService.getPersonInfo();
        if (!Strings.isBlank(person.getAirportid())) {
            base_airport baseairport = baseAirportService.fetch(person.getAirportid());
            position=baseairport.getPosition();
                req.setAttribute("obj", baseairport);
        }
        req.setAttribute("MapRef", Globals.MapRef);
    }

    @At("/data_charging")
    @Ok("beetl:/platform/eq/Map/charging.html")
    @RequiresPermissions("eq.Map.data.charging.sum")
    public void data_charging(HttpServletRequest req) {
        req.setAttribute("MapRef", Globals.MapRef);
    }

    @At("/charging_data")
    @Ok("beetl:/platform/eq/Map/charging_data.html")
    @RequiresPermissions("eq.Map.data.charging.detail")
    public void charging_data(HttpServletRequest req) {
        //获取当前登录人
        base_person person= basePersonService.getPersonInfo();
        if (!Strings.isBlank(person.getAirportid())) {
            base_airport baseairport = baseAirportService.fetch(person.getAirportid());
            req.setAttribute("obj", baseairport);
        }
        req.setAttribute("person",person);
        req.setAttribute("MapRef", Globals.MapRef);
    }

    @At("/equsedata")
    @Ok("beetl:/platform/eq/Map/eqtotal.html")
    @RequiresPermissions("eq.Map.data.reports.eqtotal")
    public void equsedata(HttpServletRequest req) {
        req.setAttribute("MapRef", Globals.MapRef);


    }

    @At("/geteqtotal_data")
    @Ok("json")
    public Object geteqtotal_data(@Param("units") String units,@Param("starttime") String starttime,@Param("endtime") String endtime,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns){

        return eqUseinfoService.geteqTotal(units,starttime,endtime,start,length,draw,order,columns);
    }

    @At("/geteqtypetotal_data")
    @Ok("json")
    public Object geteqtypetotal_data(@Param("types") String types,@Param("syear") String syear,@Param("aircorp") String aircorp, @Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns){

        return eqUseinfoService.geteqtypeTotal(types,syear,aircorp,start,length,draw);
    }



    @At("/getcharging_data")
    @Ok("json")
    public Object getcharging_data(@Param("unitid") String unitid,@Param("starttime") String starttime,@Param("endtime") String endtime, @Param("pstatus") int pstatus,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns){

        return eqUseinfoService.getDataList(unitid,starttime,endtime,pstatus,start,length,draw);
    }



    @At("/eqindex")
    @Ok("beetl:/platform/eq/Map/eqindex.html")
    @RequiresPermissions("eq.Map.eqindex")
    public void eqindex(HttpServletRequest req) {
        String position="";
        //获取当前登录人
        base_person person= basePersonService.getPersonInfo();
        if (!Strings.isBlank(person.getAirportid())) {
            base_airport baseairport = baseAirportService.fetch(person.getAirportid());
            position=baseairport.getPosition();
            req.setAttribute("obj", baseairport);
        }
        req.setAttribute("MapStyle",Globals.MapStyle);
        req.setAttribute("MapRef", Globals.MapRef);
    }

    /**
     * 此接口供硬件接口调用，不要轻易改动！！
     * @param type
     * @param ownerid
     * @param bikeid
     * @param lockstate
     * @param time
     * @param sign
     * @param power
     * @param lon
     * @param lat
     * @param gpstime
     * @param req
     * @return
     */
    @At("/getinfo")
    @Ok("json")
    public Object getinfo(@Param("type") String type, @Param("ownerid") String ownerid ,@Param("bikeid") String bikeid ,@Param("lockstate") String lockstate , @Param("time") String time , @Param("sign") String sign ,@Param("power") String power ,@Param("lon") String lon ,@Param("lat") String lat , @Param("gpstime") String gpstime ,HttpServletRequest req) {
        NutMap re = new NutMap();
        try{
            Map map= new HashMap();
            map.put("type",type);
            map.put("ownerid",ownerid);
            map.put("bikeid",bikeid);
            map.put("lockstate",lockstate);
            map.put("time",time);
            map.put("sign",sign);
            map.put("power",power);
            map.put("lon",lon);
            map.put("lat",lat);
            map.put("lgpstimeon",gpstime);

            JSONObject json=new JSONObject(map);
            luckyLog.info("getinfo-->收到的数据格式："+json);
            int ret = 0;
            if(!Strings.isBlank(type)){
                ret = eqUseService.getInfo(map);
            }

            if(json.isNull("type")){
                re.put("return_msg","ERROR");
                re.put("return_code","2");
            }else{
                re.put("return_msg","SUCCESS");
                re.put("return_code",ret);
            }
        }catch(Exception e){
            luckyLog.error("getinfo-->"+e.getMessage());
            re.put("return_msg","ERROR");
            re.put("return_code","0");
            e.printStackTrace();
        }
        JSONObject json=new JSONObject(re);
        luckyLog.info("getinfo-->返回数据："+json);
        return re;
    }

}
