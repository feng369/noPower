package cn.wizzer.app.web.modules.controllers.platform.eq;

import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.eq.modules.models.*;
import cn.wizzer.app.eq.modules.services.*;
import cn.wizzer.app.sys.modules.services.SysDictService;
import cn.wizzer.app.sys.modules.services.SysUnitService;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.map.LngLat;
import cn.wizzer.framework.map.MapUtils;
import cn.wizzer.framework.page.Pagination;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ResultTreeType;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.json.JSONObject;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.sql.Sql;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.filter.CrossOriginFilter;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@IocBean
@At("/platform/eq/materiel")
public class EqMaterielController{
    //专门针对硬件通讯接口checklockid方法定制日志文件
    private static Logger luckyLog = Logger.getLogger("LuckyLog");
    private static final Log log = Logs.get();
    @Inject
    private EqMaterielService eqMaterielService;
    @Inject
    private EqUseService eqUseService;
    @Inject
    private SysUnitService unitService;
    @Inject
    private SysDictService dictService;
    @Inject
    private EqStakeService eqStakeService;
    @Inject
    private EqLockService eqLockService;
    @Inject
    private BaseCnctobjService baseCnctobjService;
    @Inject
    private BasePersonService basePersonService;
    @Inject
    private Dao dao;

    @At("")
    @Ok("beetl:/platform/eq/materiel/index.html")
    @RequiresPermissions("platform.eq.materiel")
    public void index() {
    }

    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.eq.materiel.select")
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
		Cnd cnd = Cnd.NEW();
		//cnd.and("equsestatus","=","0");
    	return eqMaterielService.data(length, start, draw, order, columns, cnd, "unit|lock|stake");
    }

    /**
     * 此接口供硬件设备调用，勿轻易调整
     * @param lockid
     * @return
     */
    @At("/checklockid")
    @Ok("json")
    public Object checklockid(@Param("lockid") String lockid) {
        luckyLog.info("checklockid-->收到的数据："+lockid);
        Cnd cnd = Cnd.NEW();
        cnd.and("lockid","=",lockid);
        NutMap re = new NutMap();
        int count = eqMaterielService.count(cnd);
        if(count == 1){
            re.put("return_msg","SUCCESS");
            re.put("return_code","1");
        }else if(count == 0){
            re.put("return_msg","ERROR");
            re.put("return_code","0");
        }
        else {
            re.put("return_msg","ERROR");
            re.put("return_code","2");
        }
        JSONObject json=new JSONObject(re);
        luckyLog.info("checklockid-->返回数据："+json);
        return re;
    }

    @At("/add")
    @Ok("beetl:/platform/eq/materiel/add.html")
    @RequiresPermissions("platform.eq.materiel")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.materiel.add")
    @SLog(tag = "eq_materiel", msg = "${args[0].id}")
    public Object addDo(@Param("..")eq_materiel eqMateriel, HttpServletRequest req) {
		try {
            eqMateriel.setEqusestatus("0");
			eqMaterielService.insert(eqMateriel);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/eq/materiel/edit.html")
    @RequiresPermissions("platform.eq.materiel")
    public void edit(String id,HttpServletRequest req) {
        eq_materiel eq_mat = eqMaterielService.fetch(id);
		req.setAttribute("obj",eq_mat);
        if(StringUtils.isNotBlank(eq_mat.getEqunitid())){
            req.setAttribute("unit",unitService.fetch(eq_mat.getEqunitid()));
        }else{
            req.setAttribute("unit",null);
        }
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.materiel.edit")
    @SLog(tag = "eq_materiel", msg = "${args[0].id}")
    public Object editDo(@Param("..")eq_materiel eqMateriel, HttpServletRequest req) {
		try {
            eqMateriel.setOpBy(StringUtil.getUid());
			eqMateriel.setOpAt((int) (System.currentTimeMillis() / 1000));
			eqMaterielService.updateIgnoreNull(eqMateriel);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.eq.materiel.delete")
    @SLog(tag = "eq_materiel", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
			    List<String>errorList = new ArrayList<String>();
                List<eq_materiel> matList = eqMaterielService.query("equsestatus|eqnum",Cnd.where("id","in",ids));
                for(eq_materiel eqMateriel:matList){
                    if(!"0".equals(eqMateriel.getEqusestatus())){
                        errorList.add(eqMateriel.getEqnum());
                    }
                }
                if(errorList.size()>0){
                    throw new ValidatException("如下设备不是未上线状态，不能删除："+errorList.toArray());
                }
				eqMaterielService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
                eq_materiel eqMateriel = eqMaterielService.fetch(id);
                if(eqMateriel!=null &&!"0".equals(eqMateriel.getEqusestatus())){
                    throw new ValidatException("设备["+eqMateriel.getEqnum()+"]不是未上线状态，不能删除!");
                }
				eqMaterielService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
		    e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/unbunding")
    @Ok("json")
//    @RequiresPermissions("platform.eq.materiel.unbunding")
    public Object unbunding(@Param("id") String id, HttpServletRequest req) {
        try {
            if(StringUtils.isNotBlank(id)){
                eqMaterielService.unBindLock(id);
                return Result.success("system.success");
            }
            throw new ValidatException("输入参数不正确!");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/eq/materiel/detail.html")
    @RequiresAuthentication
	public void detail(String id, HttpServletRequest req) {
		if (!Strings.isBlank(id)) {
            eq_materiel eq_mat = eqMaterielService.fetch(id);
            req.setAttribute("obj",eq_mat);
            if(StringUtils.isNotBlank(eq_mat.getEqunitid())){
                req.setAttribute("unit",unitService.fetch(eq_mat.getEqunitid()));
            }else{
                req.setAttribute("unit",null);
            }
            if(StringUtils.isNotBlank(eq_mat.getTypeid())){
                req.setAttribute("type",dictService.fetch(eq_mat.getTypeid()));
            }else{
                req.setAttribute("type",null);
            }
            if(StringUtils.isNotBlank(eq_mat.getStakeid())){
                req.setAttribute("stake",eqStakeService.fetch(eq_mat.getStakeid()));
            }else{
                req.setAttribute("stake",null);
            }
		}else{
            req.setAttribute("obj", null);
        }
    }

    @At("/getlock/?")
    @Ok("beetl:/platform/eq/materiel/getlock.html")
    @RequiresPermissions("platform.eq.materiel")
    public void getlock(String id,HttpServletRequest req) {
        if (!Strings.isBlank(id)) {
            req.setAttribute("obj", eqMaterielService.fetch(id));
        }else{
            req.setAttribute("obj", null);
        }
    }

    @At("/getMaterialList")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getMaterialList(@Param("keyword") String keyword){
        try{
            if(!Strings.isBlank(keyword)){
                Cnd cnd=Cnd.NEW();
                cnd.and("equsestatus","=","1");
                cnd.and("eqname","like","%"+keyword.toLowerCase()+"%");
                List<eq_materiel> materiels=eqMaterielService.query(cnd);
                List<HashMap> mapList=new ArrayList<>();
                for(int i=0;i<materiels.size();i++){
                    HashMap map=new HashMap();
                    map.put("eqname",materiels.get(i).getEqname());
                    map.put("eqnum",materiels.get(i).getEqnum());
                    map.put("eqtype",materiels.get(i).getEqtype());
                    map.put("eqcode",materiels.get(i).getEqcode());
                    map.put("lockid",materiels.get(i).getLockid());
                    map.put("id",materiels.get(i).getId());
                    mapList.add(map);
                }

                return Result.success("system.success",mapList);
            }
            return Result.error(2,"null keyword");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getMaterielBySeatid")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getMaterielBySeatid(@Param("seatid") String seatid){
        try{
            if(Strings.isNotBlank(seatid)){
                Sql sql = Sqls.queryRecord("select s.stakenum,s.stakename,m.eqnum,m.eqcode,m.eqname,m.eqtype,m.lockid,u.pstatus,u.errstatus,u.position,m.id,d.`name` typeid "+
                        " from eq_stake s inner join eq_materiel m on m.stakeid=s.id " +
                        " inner JOIN eq_use u on u.stakeid=s.id and u.eqid=m.id " +
                        " left join sys_dict d on d.id=m.typeid " +
                        " where s.seatid='"+seatid+"'");
                dao.execute(sql);
                List<Record> res = sql.getList(Record.class);
                return Result.success("system.success",res);
            }else {
                return Result.error("输入去参数不完整！");
            }
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getAroundUsableEqList")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getAroundUsableEqList(@Param("personid") String personid,@Param("position") String position){
        try{
            if(!Strings.isBlank(personid)&&!Strings.isBlank(position)&& position.indexOf(",")>0){
                String[]spos = position.split(",");
                LngLat start = new LngLat(Double.valueOf(spos[0]),Double.valueOf(spos[1]));
                //查询用户所属组织
                Cnd cnd=Cnd.NEW();
                cnd.and("userId","=",personid);
                String npersonid = baseCnctobjService.fetch(cnd).getPersonId();
                base_person basePerson = basePersonService.fetch(npersonid);
                //查询所属组织的可用设备
                Cnd eqcnd=Cnd.NEW();
//                eqcnd.and("equnitid","=",basePerson.getUnitid());
                eqcnd.and("pstatus","=","0");
                eqcnd.and("errstatus","=","0");
                eqcnd.and("bizstatus","=","0");
                List<eq_use> eqUseList = eqUseService.query(eqcnd,"");
                List<HashMap> mapList=new ArrayList<>();
                for(eq_use use:eqUseList){
                    if(StringUtils.isNotBlank(use.getPosition())&& use.getPosition().indexOf(",")>0){
                        String[]epos = use.getPosition().split(",");
                        LngLat end = new LngLat(Double.valueOf(epos[0]),Double.valueOf(epos[1]));
                        double disDouble = MapUtils.calculateLineDistance(start, end);
                        int disInt = new BigDecimal(disDouble).setScale(0,java.math.BigDecimal.ROUND_HALF_UP).intValue();
                        if(disInt<=Integer.valueOf(Globals.QueryDis).intValue()) {
                            Cnd matcnd=Cnd.NEW();
                            matcnd.and("id","=",use.getEqid());
                            List<eq_materiel> matList = eqMaterielService.query(matcnd,"type");
                            if(matList.size()==1){
                                HashMap map=new HashMap();
                                eq_materiel eqMateriel = matList.get(0);
                                map.put("id",eqMateriel.getId());
                                map.put("eqnum",eqMateriel.getEqnum());
                                map.put("eqcode",eqMateriel.getEqcode());
                                map.put("eqname",eqMateriel.getEqname());
                                map.put("eqtype",eqMateriel.getEqtype());
                                map.put("lockid",eqMateriel.getLockid());
                                map.put("typecode",eqMateriel.getType()!=null?eqMateriel.getType().getCode():"");
                                map.put("typename",eqMateriel.getType()!=null?eqMateriel.getType().getName():"");
                                map.put("position",use.getPosition());
                                map.put("distance",disInt+"");
                                if(StringUtils.isNotBlank(eqMateriel.getStakeid())){
                                    Cnd stakecnd=Cnd.NEW();
                                    stakecnd.and("id","=",eqMateriel.getStakeid());
                                    List<eq_stake>  stakeList = eqStakeService.query(stakecnd,"planeseat");
                                    if(stakeList.size()==1){
                                        eq_stake stake = stakeList.get(0);
                                        map.put("stakenum",stake.getStakenum());
                                        map.put("stakename",stake.getStakename());
                                        if(stake.getPlaneseat()!=null){
                                            map.put("seatnum",stake.getPlaneseat().getSeatnum());
                                            map.put("seatname",stake.getPlaneseat().getSeatname());
                                        }
                                    }
                                }
                                //与getMaterielByParams接口保持一致，方便APP前端模型处理
                                map.put("pstatus",use.getPstatus());
                                map.put("bizstatus",use.getBizstatus());
                                mapList.add(map);
                            }
                        }
                    }
                }
                return Result.success("system.success",mapList);
            }else {
                throw  new ValidatException("输入去参数不完整！");
            }
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getMaterialBylockID")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getMaterialBylockID(@Param("lockID") String lockID){
        try{
            if(!Strings.isBlank(lockID)){
                Cnd cnd=Cnd.NEW();
                cnd.and("lockid","=",lockID);
                eq_materiel materiel= eqMaterielService.fetch(cnd);
                HashMap map=new HashMap();
                map.put("eqnum",materiel.getEqnum());
                map.put("eqcode",materiel.getEqcode());
                map.put("eqname",materiel.getEqname());
                map.put("id",materiel.getId());
                map.put("eqtype",materiel.getEqtype());
                return Result.success("system.success",map);
            }
            return Result.error(2,"lockID is null");
        }
        catch(Exception e){
            e.printStackTrace();
            return  Result.error("system.error",e);
        }
    }

    @At("/getLockIDByID")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getLockIDByID(@Param("id") String id){
        try{
            if(!Strings.isBlank(id)){
                eq_materiel materiel= eqMaterielService.fetch(id);
                HashMap map=new HashMap();
                map.put("lockid",materiel.getLockid());
                return Result.success("system.success",map);
            }
            return Result.error(2,"eqid is null");
        }
        catch(Exception e){
            e.printStackTrace();
            return  Result.error("system.error",e);
        }
    }

    @At("/getMaterialByNum")
    @Ok("jsonp:full")
    public Object getMaterialByNum(@Param("eqnum") String eqnum){
        try{
            if(!Strings.isBlank(eqnum)){
                Cnd cnd=Cnd.NEW();
                cnd.and("eqnum","=",eqnum);
                List<eq_materiel> materiel= eqMaterielService.query(cnd,"stake");
                return materiel;
            }
            return null;
        }
        catch(Exception e){
            return  null;
        }
    }


    @At("/eqmateriel")
    @Ok("json")
    @RequiresPermissions("platform.eq.materiel")
    public eq_materiel eqmateriel(@Param("id") String id, HttpServletRequest req) {
        if(!Strings.isBlank(id)){
            return eqMaterielService.fetch(id);
        }else {
            return null;
        }
    }


    @At("/geteqIDBylockID")
    @Ok("jsonp:full")
    public Object geteqIDBylockID(@Param("lockID") String lockID){
        try{
            if(!Strings.isBlank(lockID)){
                Cnd cnd=Cnd.NEW();
                cnd.and("lockid","=",lockID);
                eq_materiel materiel=eqMaterielService.fetch(cnd);
                return materiel;
            }
            return null;
        }catch(Exception e){
            return null;
        }
    }

    @At("/isLundang")
    @Ok("jsonp:full")
    public Object isLundang(@Param("id") String id){
        try{
            if(!Strings.isBlank(id)){
                Cnd cnd=Cnd.NEW();
                cnd.and("id","=",id);
                List<eq_materiel> materiel=eqMaterielService.query(cnd,"type");
                if(materiel.size()>0){
                    if(materiel.get(0).getType().getCode().equals("eqtype.ldc")){
                        return true;
                    }
                }
            }
            return false;
        }catch(Exception e){
            return false;
        }
    }

    //从给定的机位范围内搜索匹配关键字的设备
    @At("/getMaterielByParams")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getMaterielByParams(@Param("airportid") String airportid,@Param("position") String position,@Param("keyword") String keyword){
        try{
            if(!Strings.isBlank(airportid)&&!Strings.isBlank(position)&&!Strings.isBlank(keyword)&& position.indexOf(",")>0){
                Sql sql= Sqls.queryRecord("select a.eqcode,a.eqname,a.eqnum,a.id,a.eqtype,d.position,e.`code` typecode,b.stakename,b.stakenum," +
                        "c.seatname,c.seatnum,d.pstatus,d.bizstatus from eq_materiel a " +
                        "left join eq_stake b on a.stakeid=b.id " +
                        "left join eq_use d on a.id=d.eqid "+
                        "left join eq_planeseat c on b.seatid=c.id " +
                        "left join sys_dict e on a.typeid=e.id "+
                        " where b.airportId='"+airportid+"' and (c.seatnum = '"+keyword+"' or a.eqname like '%"+keyword+"%' )");// or a.eqnum like '%"+keyword+"%'
                dao.execute(sql);
                List<Record> res = sql.getList(Record.class);
                String[]spos = position.split(",");
                LngLat start = new LngLat(Double.valueOf(spos[0]),Double.valueOf(spos[1]));
                for(int i=0;i<res.size();){
//                for(Record record:res){
                    String pos = res.get(i).getString("position");
                    if(StringUtils.isNotBlank(pos)&& pos.indexOf(",")>0){
                        String[]epos = pos.split(",");
                        LngLat end = new LngLat(Double.valueOf(epos[0]),Double.valueOf(epos[1]));
                        double disDouble = MapUtils.calculateLineDistance(start, end);
                        int disInt = new BigDecimal(disDouble).setScale(0,java.math.BigDecimal.ROUND_HALF_UP).intValue();
                        if(disInt>Integer.valueOf(Globals.QueryDis).intValue()) {
                            res.remove(i);
                        }else{
                            res.get(i).put("distance",disInt+"");
                            i++;
                        }
                    }else{
                        res.remove(i);
                    }
                }
                Collections.sort(res, new Comparator<Record>() {
                    @Override
                    public int compare(Record o1, Record o2) {
                        int dis1 = o1.getInt("distance");
                        int dis2 = o2.getInt("distance");
                        int i = dis1 - dis2;
                        return i;
                    }
                });
                return Result.success("system.success",res);
            }
            return Result.error(2,"参数不能位空!");

        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getMaterialInfoByID")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getMaterialInfoByID(@Param("id") String id){
        try{
            Sql sql= Sqls.queryRecord("select  a.id,a.eqname,b.pstatus,c.stakename, (case b.pstatus when 0 then c.position when 1 then b.position else 'error' end) as position " +
                    "from eq_materiel a left join eq_use b on a.id=b.eqid " +
                    "left join eq_stake c on a.stakeid=c.id where a.id='"+id+"'");
            dao.execute(sql);
            List<Record> res = sql.getList(Record.class);

            return Result.success("system.success",res);
        }
        catch(Exception e){
            e.printStackTrace();
            return  Result.error("system.error",e);
        }
    }

    @At("/updateLockinfo")
    @Ok("json")
    public Object updateLockinfo(@Param("mid") String mid,@Param("lid") String lid,@Param("lnum") String lnum){
        try{
            if(!Strings.isBlank(mid)){
                eqMaterielService.bindLock(mid,lid);
                return Result.success("system.success");
            }
            throw new ValidatException("输入参数不正确!");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    /**
     * 设备分配桩位 koudepei 20180307
     * @param eqid
     * @param stakeid
     * @param req
     * @return
     */
    @At("/bindStake")
    @Ok("json")
    @RequiresPermissions("platform.eq.materiel")
    @SLog(tag = "eq_materiel", msg = "${req.getAttribute('eqid')}")
    public Object bindStake(@Param("eqid")String[] eqid,@Param("stakeid")String stakeid, HttpServletRequest req) {
        try {
            if(eqid!=null && StringUtils.isNotBlank(stakeid)){
                eqMaterielService.bindStake(eqid,stakeid);
                return Result.success("system.success");
            }
            return Result.error("system.error");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getCountByeqnum")
    @Ok("json")
    public Object getCountByeqnum(String eqnum){
        return eqMaterielService.count("eq_materiel", Cnd.where("eqnum", "=", eqnum));

    }
    //20180420zhf1003
    @At("/relieveStake")
    @Ok("json")
    public Object  relieveStake(@Param("id") String id){
        try {
            eqMaterielService.relieveStake(id);
            return Result.success("解除桩位成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/traceRecord/?")
    @Ok("beetl:/platform/eq/materiel/traceRecord.html")
    @RequiresPermissions("platform.eq.materiel.traceRecord")
    public void traceRecord(String id, HttpServletRequest req) {
        if(!Strings.isBlank(id)){
            req.setAttribute("id",id);
        }else{
            req.setAttribute("id",null);
        }
    }

    //需要进一步优化分页查询
    @At("/getEqTraceList")
    @Ok("json")
    @RequiresPermissions("platform.eq.materiel.traceRecord")
    public Pagination getEqTraceList(@Param("eqid") String eqid,@Param("pageNumber") Integer pageNumber){
        String sql="select a.lockid,a.position,a.time,a.lockstate,a.power,c.username,d.`name` unitname " +
                " from eq_trace a " +
                " left join eq_useinfo b on a.equseinfoid=b.id "+
                " left join sys_user c on c.id=b.personid "+
                " left join sys_unit d on d.id=b.personunitid "+
                " where a.eqid = '" + eqid+"' " + " ORDER BY a.time DESC ";
        Sql sqlstr= Sqls.queryRecord(sql);
        Pagination page = eqMaterielService.listPage(pageNumber,sqlstr);
        return page;
    }
}
