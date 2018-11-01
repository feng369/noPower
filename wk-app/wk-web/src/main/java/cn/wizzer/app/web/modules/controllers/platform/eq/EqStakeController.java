package cn.wizzer.app.web.modules.controllers.platform.eq;

import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.app.base.modules.models.base_cnctobj;
import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.app.base.modules.models.base_place;
import cn.wizzer.app.base.modules.services.BaseAirportService;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.sys.modules.models.Sys_unit;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.app.sys.modules.services.SysUnitService;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.eq.modules.models.eq_stake;
import cn.wizzer.app.eq.modules.services.EqStakeService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@IocBean
@At("/platform/eq/stake")
public class EqStakeController{
    private static final Log log = Logs.get();
    @Inject
    private EqStakeService eqStakeService;
    @Inject
    private BasePersonService basePersonService;
    @Inject
    private BaseAirportService baseAirportService;
    @Inject
    private SysUnitService sysUnitService;
    @Inject
    private BaseCnctobjService baseCnctobjService;

    @At("")
    @Ok("beetl:/platform/eq/stake/index.html")
    @RequiresPermissions("platform.eq.stake")
    public void index() {
    }

    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.eq.stake.select")
    public Object data(@Param("flag")boolean flag,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns,HttpServletRequest req) {
		Cnd cnd = Cnd.NEW();
		//用于分配桩位过滤
        String name = req.getParameter("name");
        if(StringUtils.isNotBlank(name)){
            name = name.trim();
            cnd = cnd.where(Cnd.exps("stakenum","LIKE","%"+name+"%").or("stakename","LIKE","%"+name+"%"));
        }
        //20180403zhf1626
    	if(flag){
            //只显示未绑定设备的桩位
            cnd.and(" linkEqMaterial "," = ",0);
        }
        return eqStakeService.data(length, start, draw, order, columns, cnd, "airport|planeseat");

    }

    @At("/add")
    @Ok("beetl:/platform/eq/stake/add.html")
    @RequiresPermissions("platform.eq.stake")
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
    @RequiresPermissions("platform.eq.stake.add")
    @SLog(tag = "eq_stake", msg = "${args[0].id}")
    public Object addDo(@Param("..")eq_stake eqStake, HttpServletRequest req) {
		try {
			eqStakeService.insert(eqStake);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/eq/stake/edit.html")
    @RequiresPermissions("platform.eq.stake")
    public void edit(String id,HttpServletRequest req) {
        Cnd cnd=Cnd.NEW();
        cnd.and("id","=",id);
        List<eq_stake> stakes = eqStakeService.query(cnd,"airport|planeseat");
		req.setAttribute("obj", stakes.get(0));
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.stake.edit")
    @SLog(tag = "eq_stake", msg = "${args[0].id}")
    public Object editDo(@Param("..")eq_stake eqStake, HttpServletRequest req) {
		try {
            eqStake.setOpBy(StringUtil.getUid());
			eqStake.setOpAt((int) (System.currentTimeMillis() / 1000));
			if(!Strings.isBlank(eqStake.getAirportId())){
                base_airport airport=baseAirportService.fetch(eqStake.getAirportId());
            }
			eqStakeService.updateIgnoreNull(eqStake);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.eq.stake.delete")
    @SLog(tag = "eq_stake", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				eqStakeService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				eqStakeService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/eq/stake/detail.html")
    @RequiresAuthentication
	public void detail(String id, HttpServletRequest req) {
		if (!Strings.isBlank(id)) {
            eq_stake eqStake = eqStakeService.fetch(id);
            req.setAttribute("obj",eqStakeService.fetchLinks(eqStake,"airport|planeseat"));

		}else{
            req.setAttribute("obj", null);
        }
    }

    @At("/getposition")
    @Ok("json")
    public Object getPlace(){
        //获取地点标记
        base_person person= basePersonService.getPersonInfo();
        Cnd cnd = Cnd.NEW();
        cnd.and("airportId","=",person.getAirportid());
        List<eq_stake> shakes= eqStakeService.query(cnd);
        return shakes;
    }

    @At("/getStakeList")
    @Ok("jsonp:full")
    public Object getStakeList(@Param("airportid") String airportid){
        try{
            if(!Strings.isBlank(airportid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("airportid","=",airportid);
                List<eq_stake> stakes = eqStakeService.query(cnd);
                return stakes;
            }
            return null;
        }
        catch (Exception e)
        {
            return null;
        }

    }

    @At("/getCountBystakenum")
    @Ok("json")
    public Object getCountBystakenum(String stakenum){
        return baseAirportService.count("eq_stake", Cnd.where("stakenum", "=", stakenum));
    }
}
