package cn.wizzer.app.web.modules.controllers.platform.eq;

import cn.wizzer.app.eq.modules.models.eq_use;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.app.web.modules.controllers.open.api.Datatime.newDataTime;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.map.LngLat;
import cn.wizzer.framework.map.MapUtils;
import cn.wizzer.framework.page.OffsetPager;
import cn.wizzer.framework.page.Pagination;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.DateUtil;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.eq.modules.models.eq_useinfo;
import cn.wizzer.app.eq.modules.services.EqUseinfoService;
import cn.wizzer.app.eq.modules.models.eq_stake;
import cn.wizzer.app.eq.modules.services.EqStakeService;
import jdk.nashorn.internal.objects.Global;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Daos;
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
@At("/platform/eq/useinfo")
public class EqUseinfoController{
    private static final Log log = Logs.get();
    @Inject
    private EqUseinfoService eqUseinfoService;
    @Inject
    private EqStakeService eqStakeService;
    @Inject
    private Dao dao;

    @At("")
    @Ok("beetl:/platform/eq/useinfo/index.html")
    @RequiresPermissions("platform.eq.useinfo")
    public void index() {
    }

    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.eq.useinfo.select")
    public Object data(@Param("aircorp") String aircorp,@Param("syear") String syear,@Param("types") String types, @Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        Cnd cnd = Cnd.NEW();

    	return eqUseinfoService.geteqtypeList(types,syear,aircorp, start,length, draw,order,columns);
    }

    @At("/add")
    @Ok("beetl:/platform/eq/useinfo/add.html")
    @RequiresPermissions("platform.eq.useinfo")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.useinfo.add")
    @SLog(tag = "eq_useinfo", msg = "${args[0].id}")
    public Object addDo(@Param("..")eq_useinfo eqUseinfo, HttpServletRequest req) {
		try {
			eqUseinfoService.insert(eqUseinfo);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/eq/useinfo/edit.html")
    @RequiresPermissions("platform.eq.useinfo")
    public void edit(String id,HttpServletRequest req) {
		req.setAttribute("obj", eqUseinfoService.fetch(id));
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.useinfo.edit")
    @SLog(tag = "eq_useinfo", msg = "${args[0].id}")
    public Object editDo(@Param("..")eq_useinfo eqUseinfo, HttpServletRequest req) {
		try {
            eqUseinfo.setOpBy(StringUtil.getUid());
			eqUseinfo.setOpAt((int) (System.currentTimeMillis() / 1000));
			eqUseinfoService.updateIgnoreNull(eqUseinfo);
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.eq.useinfo.delete")
    @SLog(tag = "eq_useinfo", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				eqUseinfoService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				eqUseinfoService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
		    e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/eq/useinfo/detail.html")
    @RequiresAuthentication
	public void detail(String id, HttpServletRequest req) {
		if (!Strings.isBlank(id)) {
            req.setAttribute("obj", eqUseinfoService.fetch(id));
		}else{
            req.setAttribute("obj", null);
        }
    }

    @At("/getUseList")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getUseList(@Param("personid") String personid,@Param("pagenumber") Integer pagenumber,@Param("pagesize") Integer pagesize){
        try{
            if(!Strings.isBlank(personid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("personid","=",personid);
                cnd.desc("createTime");
//                List<eq_useinfo> useinfo = eqUseinfoService.query(cnd,"materiel");
                //改为分页20180402
//                Pagination page = eqUseinfoService.listPage(pagenumber,pagesize,cnd,"eqid");//此方法只能指定fieldName，无法指定外键linkName
//                List<eq_useinfo> useinfo = (List<eq_useinfo>) page.getList();
                Pager pager = new Pager(1);
                if(pagenumber!=null && pagenumber.intValue()>0){
                    pager.setPageNumber(pagenumber.intValue());
                }
                if(pagesize!=null){
                    pager.setPageSize(pagesize.intValue());
                }

                List<eq_useinfo> useinfo = eqUseinfoService.query(cnd,"materiel",pager);

                List<HashMap> mapList=new ArrayList<>();
                for(int i=0;i<useinfo.size();i++){
                    HashMap map=new HashMap();
                    map.put("eqnum",useinfo.get(i).getMateriel()==null?"": useinfo.get(i).getMateriel().getEqnum());
                    map.put("starttime",useinfo.get(i).getStarttime());
                    map.put("eqname",useinfo.get(i).getMateriel()==null?"":useinfo.get(i).getMateriel().getEqname());
                    map.put("pstatus",useinfo.get(i).getPstatus());
                    map.put("endtime",useinfo.get(i).getEndtime());
                    map.put("eqcode",useinfo.get(i).getMateriel() == null ? "":useinfo.get(i).getMateriel().getEqcode());
                    map.put("id",useinfo.get(i).getId());
                    mapList.add(map);
                }

                return Result.success("system.success",mapList);
            }
            return Result.error(2,"personid is null");

        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getUseStatus")
    @Ok("jsonp:full")
    public Object getUseStatus(@Param("id") String id){
        try{
            if(!Strings.isBlank(id)){
                eq_useinfo useinfo = eqUseinfoService.fetch(id);
                return useinfo.getPstatus();
            }
            return null;
        }catch(Exception e){
            return null;
        }
    }

    @At("/setUseStatus")
    @Ok("jsonp:full")
    public Object setUseStatus(@Param("id") String id,@Param("pstatus") String pstatus){
        try{
            if(!Strings.isBlank(id)){

                eq_useinfo useinfo = eqUseinfoService.fetch(id);
                useinfo.setPstatus(pstatus);
                useinfo.setEndtime(newDataTime.getDateYMDHMS());
                return eqUseinfoService.updateIgnoreNull(useinfo);
            }
            return null;
        }catch(Exception e){
            return null;
        }
    }

    @At("/getUseNum")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getUseNum(@Param("personid") String personid){
        try{
            if(!Strings.isBlank(personid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("personid","=",personid);
                int num = eqUseinfoService.count(cnd);
                HashMap map= new HashMap();
                map.put("number",num);
                return Result.success("system.success",map);
            }
            return Result.error(2,"system.error");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/insertInfo")
    @Ok("jsonp:full")
    public Object insertInfo(@Param("equsenum") String equsenum,@Param("equipid") String equipid,@Param("personid") String personid,@Param("personunitid") String personunitid,@Param("starttime") String starttime,@Param("endtime") String endtime,@Param("pstatus") String pstatus,@Param("equnitid") String equnitid)
    {
        try{
            if(!Strings.isBlank(equipid)){
                eq_useinfo useinfo=new eq_useinfo();
                    useinfo.setEqid(equipid);
                if(!Strings.isBlank(equsenum))
                    useinfo.setEqusenum(equsenum);
                if(!Strings.isBlank(personid))
                    useinfo.setPersonid(personid);
                if(!Strings.isBlank(personunitid))
                    useinfo.setPersonunitid(personunitid);
                if(!Strings.isBlank(starttime))
                    useinfo.setStarttime(starttime);
                if(!Strings.isBlank(endtime))
                    useinfo.setEndtime(endtime);
                if(!Strings.isBlank(pstatus))
                    useinfo.setPstatus(pstatus);
                if(!Strings.isBlank(equnitid))
                    useinfo.setEqunitid(equnitid);
                eq_useinfo ui = eqUseinfoService.insert(useinfo);
                return ui.getId();
            }
            return null;
        }catch(Exception e){
            return null;
        }
    }

    @At("/getUsingEqList")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getUsingEqList(@Param("userid") String userid){
        try{
            if(!Strings.isBlank(userid)){
                Sql sql = Sqls.queryRecord("select  a.id,a.starttime,a.sstarttime,b.eqname,b.eqnum,b.typeid,b.lockid,b.eqcode,d.seatnum,c.position,c.stakename,c.stakenum,e.`code`"+
                        ",f.position eqposition,g.starttime starttime2,g.sstarttime sstarttime2,g.endtime endtime2,g.sendtime sendtime2,g.pstatus cpstatus "+
                        " from eq_useinfo a " +
                        "left join eq_materiel b on a.eqid=b.id " +
                        "left join eq_stake c on b.stakeid=c.id " +
                        "left join eq_planeseat d on c.seatid=d.id " +
                        "left join sys_dict e on b.typeid = e.id "+
                        "left join eq_use f on a.id = f.equseinfoid "+
                        "left join eq_chockuse g on a.id = g.useinfoID "+
                        " where a.personid='"+userid+"' and f.pstatus=1");
                dao.execute(sql);
                List<Record> res = sql.getList(Record.class);
                for(Record record: res){
                    //计算设备使用时长
                    String starttime = record.getString("starttime");
                    String sstarttime = record.getString("sstarttime");
                    long diseq = 0;
                    if(StringUtils.isNotBlank(sstarttime)){
                        diseq = DateUtil.getMinuteBetweens(sstarttime,DateUtil.getDateTime());
                        long h = diseq/60;
                        long m = diseq % 60;
                        record.put("usedtimes",(h<10?("0"+h):(h+""))+":"+(m<10?("0"+m):(m+"")));
                    }
                    //计算设备到桩位的距离
                    String sposition = record.getString("position");
                    String eqposition = record.getString("eqposition");
                    if(StringUtils.isNotBlank(sposition)&& StringUtils.isNotBlank(eqposition)&& sposition.indexOf(",")>0&& eqposition.indexOf(",")>0){
                        String[]spos = sposition.split(",");
                        LngLat start = new LngLat(Double.valueOf(spos[0]),Double.valueOf(spos[1]));
                        String[]epos = eqposition.split(",");
                        LngLat end = new LngLat(Double.valueOf(epos[0]),Double.valueOf(epos[1]));
                        double disDouble = MapUtils.calculateLineDistance(start, end);
                        int disInt = new BigDecimal(disDouble).setScale(0,java.math.BigDecimal.ROUND_HALF_UP).intValue();
                        record.put("distance",disInt+"");
                    }
                    //计算轮挡使用时长
                    String code = record.getString("code");
                    long disld = 0;
                    if("eqtype.ldc".equals(code)){
                        String sstarttime2 = record.getString("sstarttime2");
                        String sendtime2 = record.getString("sendtime2");
                        if(StringUtils.isNotBlank(sstarttime2)){
                            if(StringUtils.isNotBlank(sendtime2)){
                                disld = DateUtil.getMinuteBetweens(sstarttime2,sendtime2);
                            }else {
                                disld = DateUtil.getMinuteBetweens(sstarttime2, DateUtil.getDateTime());
                            }
                            long h = disld/60;
                            long m = disld % 60;
                            record.put("usedtimesld",(h<10?("0"+h):(h+""))+":"+(m<10?("0"+m):(m+"")));
                        }
                    }
                    record.put("eqnum",record.getString("stakenum"));//为不改前端，临时把设备编码改为桩位编码，为了归还方便核对
                    record.remove("eqposition");
                    record.remove("sstarttime");
                    record.remove("sstarttime2");
                    record.remove("endtime2");
                    record.remove("sendtime2");
                }
                return Result.success("system.success",res);
            }
            return Result.error(2,"userid is null");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error", e);
        }
    }

    @At("/getBackDistance")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getBackDistance(){
        try{
            HashMap map=new HashMap();
            map.put("backdis",Globals.BackDis);
            return Result.success("system.success",map);
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getCtrlDistance")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getCtrlDistance(){
        try{
            HashMap map =new HashMap();
            map.put("ctrldis",Globals.CtrlDis);
            return Result.success("system.success",map);
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/setDeliver")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object setDeliver(@Param("userid") String userid,@Param("useinfoids") String [] useinfoids){
        try{
            if(!Strings.isBlank(userid)){
                for(String useinfoid : useinfoids){
                    eq_useinfo useinfo = eqUseinfoService.fetch(useinfoid);
                    useinfo.setDeliver(userid);
                    eqUseinfoService.updateIgnoreNull(useinfo);
                }
                return Result.success("system.success");
            }
            return  Result.error(2,"system.error");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/DeliverMateriel")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object DeliverMateriel(@Param("olduserid") String olduserid,@Param("newuserid") String newuserid){
        try{
            if(!Strings.isBlank(olduserid)&&!Strings.isBlank(newuserid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("deliver","=",olduserid);
                List<eq_useinfo> useinfos = eqUseinfoService.query(cnd);
                for(eq_useinfo useinfo : useinfos){
                    useinfo.setPersonid(newuserid);
                    useinfo.setDeliver("");
                    eqUseinfoService.updateIgnoreNull(useinfo);
                }
                return Result.success("system.success");
            }
            return Result.error(2,"system.error");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/ClearDeliver")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object ClearDeliver(@Param("userid") String userid){
        try{
            if(!Strings.isBlank(userid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("deliver","=",userid);
                List<eq_useinfo> useinfos = eqUseinfoService.query(cnd);
                for(eq_useinfo useinfo : useinfos){
                    useinfo.setDeliver("");
                    eqUseinfoService.updateIgnoreNull(useinfo);
                }
                return Result.success("system.success");
            }
            return Result.error(2,"system.error");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/ListenDeliver")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object ListenDeliver(@Param("userid") String userid){
        try{
            if(!Strings.isBlank(userid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("deliver","=",userid);
                int number = eqUseinfoService.count(cnd);
                HashMap map=new HashMap();
                map.put("number",number);
                return Result.success("system.success",map);
            }
            return Result.error(2,"system.error");
        }catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }


}
