package cn.wizzer.app.web.modules.controllers.platform.eq;

import cn.wizzer.app.base.modules.services.BaseAirportService;
import cn.wizzer.app.eq.modules.models.eq_materiel;
import cn.wizzer.app.eq.modules.models.eq_useinfo;
import cn.wizzer.app.eq.modules.services.EqMaterielService;
import cn.wizzer.app.eq.modules.services.EqUseinfoService;
import cn.wizzer.app.sys.modules.models.Sys_role;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.app.sys.modules.services.SysUnitService;
import cn.wizzer.app.sys.modules.services.SysUserService;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.app.web.commons.plugin.PostRun;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.eq.modules.models.eq_use;
import cn.wizzer.app.eq.modules.services.EqUseService;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.filter.CrossOriginFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@IocBean
@At("/platform/eq/use")
public class EqUseController{
    //专门针对硬件通讯接口getinfo方法定制日志文件
    private static Logger luckyLog = Logger.getLogger("LuckyLog");
    private static final Log log = Logs.get();
    @Inject
    private EqUseService eqUseService;
    @Inject
    private EqMaterielService eqMaterielService;
    @Inject
    private SysUserService sysUserService;
    @Inject
    private SysUnitService sysUnitService;
    @Inject
    private EqUseinfoService eqUseinfoService;
    @Inject
    private BaseAirportService baseAirportService;

    @At("")
    @Ok("beetl:/platform/eq/use/index.html")
    @RequiresPermissions("platform.eq.use")
    public void index() {
    }

    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.eq.use")
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
            //20180419zhf1600
            return eqUseService.getList(length, start, draw, order, columns, Cnd.NEW());
    }


    @At("/getEqUsePowerInfo")
    @Ok("beetl:/platform/eq/use/power.html")
    @RequiresPermissions("platform.eq.use.power")
    public void getEqUsePowerInfo() {
    }
    @At("/getEqUsePowerData")
    @Ok("json")
    @RequiresPermissions("platform.eq.use.power")

    public Object getEqUsePowerData(@Param("stakenum")String stakenum,@Param("locknum")String locknum,@Param("length")int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
            return eqUseService.getEqUsePowerData(stakenum,locknum,length, start, draw, order, columns, Cnd.NEW());
    }

    @At("/getEqUseList")
    @Ok("json")
//    @RequiresPermissions("platform.eq.use")//首页监控使用暂不要session过期
    public Object getEqUseList(@Param("seatList") String[] seatList,@Param("airportid") String airportid ){
            return eqUseService.getEqUseList(seatList,airportid);
    }

    @At("/getEqUseListForDataTable")
    @Ok("json")
//    @RequiresPermissions("platform.eq.use")//首页监控使用暂不要session过期
    public Object getEqUseListForDataTable(@Param("seatId") String seatId,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns){
            return eqUseService.getEqUseListForDataTable(seatId,length, start, draw, order, columns);
    }
/*
    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.eq.use")
    public Object data(@Param("seatId") String seatId,@Param("status")String status,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        if (!Strings.isBlank(seatId)) {

            return eqUseService.getEqUseList(status,seatId, length, start, draw, order, columns);
        } else {
            //20180419zhf1600
            return eqUseService.getList(length, start, draw, order, columns, Cnd.NEW());
        }
    }
*/

    @At("/getstakecount")
    @Ok("json")
    @RequiresPermissions("platform.eq.use")
    public Object getstakecount(@Param("stakeid") String stakeid) {
        Cnd cnd = Cnd.NEW();
        cnd.and("stakeid", "=", stakeid);
        cnd.and("pstatus", "=", "1");
        int count = eqUseService.count(cnd);
        //获取桩位异常的状态数量
        int errcount = eqUseService.count(Cnd.where("errstatus", "=", "3").and("stakeid", "=", stakeid));
        int overtime = eqUseService.count(Cnd.where("errstatus", "=", "2").and("stakeid", "=", stakeid));
        int wncount = eqUseService.count(Cnd.where("errstatus", "=", "1").and("stakeid", "=", stakeid));
        Map map = new HashMap();
        map.put("use", count);
        map.put("errcount", errcount);
        map.put("overtime", overtime);
        map.put("wncount", wncount);

        return map;
    }

    @At("/getstakestatus")
    @Ok("json")
    @RequiresPermissions("platform.eq.use")
    public Object getstakestatus(@Param("stakeid") String stakeid) {
        //检查桩位状态
        List errlist = new ArrayList();
        Sql sql = Sqls.queryRecord("select errstatus from eq_use where errstatus<>0 and stakeid='" + stakeid + "'");
        List<eq_use> list = eqUseService.dao().execute(sql).getList(eq_use.class);
        for (eq_use eqUse : list) {
            errlist.add(eqUse.getErrstatus());
        }
        return errlist;
    }

    @At("/add")
    @Ok("beetl:/platform/eq/use/add.html")
    @RequiresPermissions("platform.eq.use")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.use.add")
    @SLog(tag = "eq_use", msg = "${args[0].id}")
    public Object addDo(@Param("..") eq_use eqUse, HttpServletRequest req) {
        try {
            eqUseService.insert(eqUse);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error", e);
        }
    }

    /**
     * 设备上线 koudepei 20180307
     *
     * @param eqid
     * @param req
     * @return
     */
    @At("/useAddOn")
    @Ok("json")
    @RequiresPermissions("platform.eq.use")
    @SLog(tag = "eq_use", msg = "${req.getAttribute('eqid')}")
    public Object useAddOn(@Param("eqid") String[] eqid, HttpServletRequest req) {
        try {
            eqUseService.useOnline(eqid);
            return Result.success("system.success");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error", e);
        }
    }

    /**
     * 设备下线 koudepei 20180320
     *
     * @param eqid
     * @param req
     * @return
     */
    @At("/useAddOff")
    @Ok("json")
    @RequiresPermissions("platform.eq.use")
    @SLog(tag = "eq_use", msg = "${req.getAttribute('eqid')}")
    public Object useAddOff(@Param("eqid") String[] eqid, HttpServletRequest req) {
        try {
            eqUseService.useOffline(eqid);
            return Result.success("system.success");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error", e);
        }
    }


    @At("/edit/?")
    @Ok("beetl:/platform/eq/use/edit.html")
    @RequiresPermissions("platform.eq.use")
    public void edit(String id, HttpServletRequest req) {
        req.setAttribute("obj", eqUseService.fetch(id));
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.use.edit")
    @SLog(tag = "eq_use", msg = "${args[0].id}")
    public Object editDo(@Param("..") eq_use eqUse, HttpServletRequest req) {
        try {
            eqUse.setOpBy(StringUtil.getUid());
            eqUse.setOpAt((int) (System.currentTimeMillis() / 1000));
            eqUseService.updateIgnoreNull(eqUse);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.eq.use.delete")
    @SLog(tag = "eq_use", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids") String[] ids, HttpServletRequest req) {
        try {
            if (ids != null && ids.length > 0) {
                eqUseService.delete(ids);
                req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
            } else {
                eqUseService.delete(id);
                req.setAttribute("id", id);
            }
            return Result.success("system.success");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error", e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/eq/use/detail.html")
    @RequiresAuthentication
    public void detail(String id, HttpServletRequest req) {
        if (!Strings.isBlank(id)) {
            req.setAttribute("obj", eqUseService.fetch(id));
        } else {
            req.setAttribute("obj", null);
        }
    }

    //20180310zhf1239
    @At("/getposition")
    @Ok("json")
//    public Object getposition(String pstatus, String errstatus) {
    public Object getposition(@Param("key") String key, HttpServletRequest req) {
        //获取地点标记
        Cnd cnd = Cnd.NEW();
//        cnd.where("id", "IS NOT", null);
//        if (!Strings.isBlank(pstatus)) {
//            cnd.and("pstatus", "= ", pstatus);
//        }
//        if (!Strings.isBlank(errstatus)) {
//            cnd.and("errstatus", "=", errstatus);
//        }
        cnd.andNot("position", "is", null);
        cnd.andNot("position", "=", "");

        if ("all".equals(key)) {

        }else if ("normal".equals(key)) {
            cnd.and("pstatus", "=", "1");
            cnd.and("errstatus", "=", "0");
            cnd.and("bizstatus", "=", "0");
        }else  if ("warning".equals(key)) {
            cnd.and("bizstatus", "=", "0");
            cnd.and("errstatus", "=", "1");
        }else  if ("overtime".equals(key)) {
            cnd.and("pstatus", "=", "1");
            cnd.and("errstatus", "=", "2");
            cnd.and("bizstatus", "=", "0");
        }else  if ("error".equals(key)) {
            cnd.and("errstatus", "=", "3");
            String[]bs = new String[]{"1","2"};
            cnd.or("bizstatus", "in", bs);
        }else {
            return 0;
        }

        return eqUseService.query(cnd);
    }

    @At("/posturl")
    @Ok("jsonp:full")
    public Object posturl(@Param("serial") String serial, @Param("type") String type) {
        //获取地点标记
        String url = Globals.luckyUrl+ Globals.unLock;
        PostRun pr = new PostRun();
        String Json = pr.post(url, serial, type);
        return Json;
    }

    @At("/postPosition")
    @Ok("json")
    @Filters({@By(type = CrossOriginFilter.class), @By(type = TokenFilter.class)})
    public Object postPosition(@Param("serial") String serial) {
        try {
            //获取当前位置
            String url = Globals.luckyUrl + Globals.QueryLock;
            PostRun pr = new PostRun();
            String json = pr.post(url, serial, null);
            com.alibaba.fastjson.JSONObject jsonobject = com.alibaba.fastjson.JSONObject.parseObject(json);
            return Result.success("system.success", jsonobject);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getequseinfo")
    @Ok("json")
    @RequiresPermissions("platform.eq.use")
    public eq_use getequseinfo(@Param("useid") String useid, HttpServletRequest req) {
        eq_use eqUse = eqUseService.fetch(useid);
        eqUseService.fetchLinks(eqUse,"eqUseinfo|eqMateriel|sysUnit|eqStake");
        if(eqUse.getEqUseinfo()!=null){
            eq_useinfo eqUseinfo = eqUse.getEqUseinfo();
            eqUseinfoService.fetchLinks(eqUseinfo,"sysUser|sysUnit");
            eqUse.setEqUseinfo(eqUseinfo);
        }
        return eqUse;
    }

    @At("/getusecount")
    @Ok("json")
    @RequiresPermissions("platform.eq.use")
    public int getusecount(@Param("key") String key, HttpServletRequest req) {
        Cnd cnd = Cnd.NEW();
        if ("all".equals(key)) {

        }else if ("normal".equals(key)) {
            cnd.and("pstatus", "=", "1");
            cnd.and("errstatus", "=", "0");
            cnd.and("bizstatus", "=", "0");
        }else  if ("warning".equals(key)) {
            cnd.and("bizstatus", "=", "0");
            cnd.and("errstatus", "=", "1");
        }else  if ("overtime".equals(key)) {
            cnd.and("pstatus", "=", "1");
            cnd.and("errstatus", "=", "2");
            cnd.and("bizstatus", "=", "0");
        }else  if ("error".equals(key)) {
            String[]es = new String[]{"3","4"};
            cnd.and("errstatus", "in", es);
            String[]bs = new String[]{"1","2"};
            cnd.or("bizstatus", "in", bs);
        }else {
            return 0;
        }
        int count = eqUseService.count(cnd);
        return count;

    }

    @At("/updateUseErrstatus")
    @Ok("json")
    @Filters({@By(type = CrossOriginFilter.class), @By(type = TokenFilter.class)})
    public Object updateUseErrstatus(@Param("equipid") String equipid, @Param("errstatus") String errstatus, @Param("pstatus") String pstatus) {
        try {
            if (!Strings.isBlank(equipid)) {
                Cnd cnd = Cnd.NEW();
                cnd.and("eqid", "=", equipid);
                eq_use use = eqUseService.fetch(cnd);
                use.setErrstatus(errstatus);
                use.setPstatus(pstatus);
                eqUseService.updateIgnoreNull(use);
                return Result.success("OK");
            }
            return Result.error(-1, "system.error");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(-1, "system.error");
        }

    }

    private Boolean getIsSpecial() {
        Subject subject = SecurityUtils.getSubject();
        Sys_user curUser = (Sys_user) subject.getPrincipal();
        String Punitid = curUser.getUnitid();
        List<Sys_role> role = curUser.getRoles();
        //获取超级管理员权限标识，超级管理员权限标识为sysadmin可以查看所有数据
        for(Sys_role r:role){
            if (r.getCode().toString().equals("sysadmin")) {
                return true;
            }
        }

        return false;
    }

    ///获取未完成订单
    @At("/getOrderList")
    @Ok("json")
    @Filters({@By(type = CrossOriginFilter.class), @By(type = TokenFilter.class)})
    public Object getOrderList(@Param("personid") String personid) {
        try {
            if (!Strings.isBlank(personid)) {
                Sql sql = Sqls.queryRecord("select * from eq_use a left join eq_useinfo b on a.equseinfoid=b.id " +
                        "where b.id is not null and a.pstatus=1 and b.personid='" + personid + "'");
                List<eq_use> use = eqUseService.dao().execute(sql).getList(eq_use.class);
                HashMap map = new HashMap();
                if (use.size() > 0) {
                    map.put("isOrder", true);
                } else {
                    map.put("isOrder", false);
                }
                return Result.success("system.success", map);
            }
            return Result.error(2, "personid is null");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.success", e);
        }
    }

    //查询正在借用中的设备的信息，主要查询经纬度
    @At("/getUsebyuseinfoid")
    @Ok("json")
    @Filters({@By(type = CrossOriginFilter.class), @By(type = TokenFilter.class)})
    public Object getUsebyuseinfoid(@Param("useinfoid") String useinfoid) {
        try {
            if (!Strings.isBlank(useinfoid)) {
                Cnd cnd = Cnd.NEW();
                cnd.and("equseinfoid", "=", useinfoid);
                cnd.and("pstatus", "=", "1");
                List<eq_use> useList = eqUseService.query(cnd);
                if (useList.size() > 0) {
                    HashMap map = new HashMap();
                    map.put("useid", useList.get(0).getId());
                    map.put("eqid", useList.get(0).getEqid());
                    map.put("position", useList.get(0).getPosition());
                    return Result.success("system.success", map);
                }
            }
            return Result.error(2, "useinfoid is null");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.success", e);
        }
    }

    @At("/getData")
    @Ok("json")
    public Object getData(@Param("id") String id) {
        try {
            if (!Strings.isBlank(id)) {
                Cnd cnd = Cnd.NEW();
                cnd.and("id", "=", id);
                List<eq_use> eq_uses = eqUseService.query(cnd, "eqMateriel|sysUnit|eqStake");
                return eq_uses;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @At("/getEqPositonbyeqid")
    @Ok("json")
    @Filters({@By(type = CrossOriginFilter.class), @By(type = TokenFilter.class)})
    public Object getEqPositonbyeqid(@Param("eqid") String eqid) {
        try {
            Cnd cnd = Cnd.NEW();
            cnd.and("eqid", "=", eqid);
            List<eq_use> use = eqUseService.query(cnd, "eqMateriel");
            HashMap map = new HashMap();
            map.put("position", use.get(0).getPosition());
            map.put("eqid", eqid);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.success", e);
        }
    }

//    //去掉维修单修改前端单独调用，融合到维修单的修改service中 20180412 update by koudepei
//    @At("/updateUsepstatus")
//    @Ok("json")
//    public Object updateUsepstatus(@Param("pstatus") String pstatus,@Param("eqid") String eqid){
//        try{
//            Cnd cnd=Cnd.NEW();
//            cnd.and("eqid","=",eqid);
//            eq_use use=eqUseService.fetch(cnd);
//            if(use!=null){
//                if(pstatus.equals("2"))//维修中
//                {
//                    use.setPstatus("2");//锁定
//                }else if(pstatus.equals("0"))//已完成
//                {
//                    use.setPstatus("0");//正常
//                }
//                eqUseService.updateIgnoreNull(use);
//                return Result.success("OK");
//            }
//            return Result.error(-1,"system.error");
//        }catch(Exception e){
//            return Result.error(-1,"system.error");
//        }
//    }

    @At("/openLock")
    @Ok("json")
    @Filters({@By(type = CrossOriginFilter.class), @By(type = TokenFilter.class)})
    public Object openLock(@Param("lockID") String lockID, @Param("personid") String personid, @Param("personunitid") String personunitid, @Param("starttime") String starttime) {
        try {
            luckyLog.info("APP用户："+personid+",试图开锁，锁号："+lockID);
            //第一步，从锁ID得到设备，从设备ID得到USE表
            Cnd cnd=Cnd.NEW();
            cnd.and("lockid","=",lockID);
            eq_materiel materiel=eqMaterielService.fetch(cnd);
            if(materiel==null){
                return Result.error( "系统中未找到"+lockID+"对应的设备信息，请稍后再试");
            }
            Cnd cnd1 = Cnd.NEW();
            cnd1.and("eqid", "=", materiel.getId());
            eq_use use = eqUseService.fetch(cnd1);
            if(use==null){
                return Result.error(4, "设备未初始化，无法借用");
            }else{
                if (!"0".equals(use.getBizstatus())) {//如果锁定，则判断是否是管理员，如果是管理员，也可以解锁（后期修改为专业维修人员）
                    if (getIsSpecial() == true) {
                        int status = eqUseService.unLockProcess(lockID, use, materiel, personid, personunitid, starttime);
                        if(status==1) {
                            Map map = new HashMap();
                            map.put("equseid", use.getId());
                            return Result.success("设备开始借用", map);
                        }else if(status==0){
                            return Result.error("设备失去信号连接，请稍后再试或联系设备管理员!");
                        }
                    }
                    return Result.error(2, "设备维护中，无法解锁");
                } else if (use.getPstatus().equals("1")) {//已使用
                    return Result.error(1, "设备已被使用，无法再次解锁");
                } else {//0,未使用
                    int status = eqUseService.unLockProcess(lockID, use, materiel, personid, personunitid, starttime);
                    if(status==1) {
                        Map map = new HashMap();
                        map.put("equseid", use.getId());
                        return Result.success("设备开始借用", map);
                    }else if(status==0){
                        return Result.error("设备失去信号连接，请稍后再试或联系设备管理员!");
                    }
                }
                return Result.error("设备借用失败，请联系系统管理员!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            luckyLog.error("APP用户："+personid+",开锁["+lockID+"]失败："+e.getMessage());
            return Result.error("system.error",e);
        }
    }



    /**
     * //判断锁是否打开，必须在开锁指令(即openLock接口)后调用此接口查询此状态
     * @param equseid 必须值为openLock接口正常返回的值
     * @param lockID  不用
     * @param personid 必须
     * @param personunitid 必须
     * @param starttime 不用
     * @return
     */
    @At("/getUnLockStatus")
    @Ok("json")
    @Filters({@By(type = CrossOriginFilter.class), @By(type = TokenFilter.class)})
    public Object getUnLockStatus(@Param("equseid") String equseid,@Param("lockID") String lockID, @Param("personid") String personid, @Param("personunitid") String personunitid, @Param("starttime") String starttime) {
        try {
            eq_use use = eqUseService.fetch(equseid);
            if(use==null){
                return Result.error("参数[equseid]不正确，未找到对应信息！");
            }
            eq_materiel materiel = eqMaterielService.fetch(use.getEqid());
            if(materiel==null){
                return Result.error( "参数不正确，未找到对应的设备信息！");
            }
            if("1".equals(use.getPstatus())){
                //更新或插入useinfo表，更新eq_use表,  只需要处理一次
                eqUseService.unlockSuccess(use,materiel, personid, personunitid);
                return Result.success("设备已解锁！");
            }else  if("1".equals(use.getFailStatus())){
                return Result.error(3, "设备借用失败，请稍后重试或通知设备管理员!");
            }else  if("0".equals(use.getPstatus())){
                return Result.error(2, "设备还未打开，请稍后...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error( "系统异常，请管理员检查！");
    }

    @At("/returnLock")
    @Ok("json")
    @Filters({@By(type = CrossOriginFilter.class), @By(type = TokenFilter.class)})
    public Object returnLock(@Param("lockID") String lockID, @Param("personid") String personid) {
        try {
            luckyLog.info("APP用户:"+personid+",试图归还设备，锁号："+lockID);
            if (!Strings.isBlank(lockID)) {
                Cnd cnd=Cnd.NEW();
                cnd.and("lockid","=",lockID);
                eq_materiel materiel=eqMaterielService.fetch(cnd);
                if(materiel==null){
                    return Result.error( "系统中未找到"+lockID+"对应的设备信息，请稍后再试");
                }
                Cnd cnd1 = Cnd.NEW();
                cnd1.and("eqid", "=", materiel.getId());
                eq_use use = eqUseService.fetch(cnd1);
                if(use==null){
                    return Result.error( "系统中未找到设备的使用信息，请联系系统管理员！");
                }
                int status = eqUseService.lockProcess(lockID,personid,materiel,use);
                if(status==1) {
                        Map map = new HashMap();
                        map.put("equseid",use.getId());
                        return Result.success("归还申请成功，请手动上锁设备",map);
                }else if(status==0){
                    return Result.error("设备失去信号连接，请稍后再试或联系设备管理员!");
                }
                return Result.error("设备归还失败，请联系系统管理员!");
            }
            return Result.error(-2, "归还失败，参数获取失败，请稍后再试");
        } catch (Exception e) {
            e.printStackTrace();
            luckyLog.error("APP用户:"+personid+",归还设备失败，锁号["+lockID+"],失败原因:"+e.getMessage());
            return Result.error( "system.error",e);
        }
    }

    //判断锁是否关闭，必须在关锁指令(即returnLock接口)后调用此接口查询此状态
    @At("/getLockStatus")
    @Ok("json")
    @Filters({@By(type = CrossOriginFilter.class), @By(type = TokenFilter.class)})
    public Object getLockStatus(@Param("equseid") String equseid) {
        try {
            eq_use use = eqUseService.fetch(equseid);
            if(use==null){
                return Result.error("参数[equseid]不正确，未找到对应信息！");
            }
            if("0".equals(use.getPstatus())){
                return Result.success("设备已归还！");
            }else if("2".equals(use.getFailStatus())){
                return Result.error(3, "设备归还失败，请稍后重试或通知设备管理员!");
            }else  if("1".equals(use.getPstatus())){
                return Result.error(2, "设备还未归还，请稍后...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error( "系统异常，请管理员检查！");
    }


}
