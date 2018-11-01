package cn.wizzer.app.web.modules.controllers.open.api.mobile;

import cn.wizzer.app.eq.modules.models.eq_chockuse;
import cn.wizzer.app.eq.modules.models.eq_useinfo;
import cn.wizzer.app.eq.modules.services.EqChockuseService;
import cn.wizzer.app.eq.modules.services.EqUseinfoService;
import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.app.web.modules.controllers.open.api.Datatime.newDataTime;
import cn.wizzer.framework.base.Result;
import cn.wizzer.framework.map.LngLat;
import cn.wizzer.framework.map.MapUtils;
import cn.wizzer.framework.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.*;
import org.nutz.plugins.apidoc.annotation.Api;
import org.nutz.plugins.apidoc.annotation.ApiMatchMode;
import org.nutz.plugins.apidoc.annotation.ApiParam;
import org.nutz.plugins.apidoc.annotation.ReturnKey;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@IocBean
@At("/open/mobile/useinfo")
@Filters({@By(type = TokenFilter.class)})
@Api(name = "设备使用情况相关API", match = ApiMatchMode.ALL,description="设备使用情况业务处理相关API接口")
public class MobileUseinfoController {
    private static final Log log = Logs.get();

    @Inject
    private EqUseinfoService eqUseinfoService;
    @Inject
    private EqChockuseService eqChockuseService;
    @Inject
    private Dao dao;

    @At("/getUseNum")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="获取用户借用次数",params ={@ApiParam(name = "personid", type = "String", description = "用户userid")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data.number",description="借用次数")} )
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

    @At("/getUseList")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="获取借用历史数据",params ={@ApiParam(name = "personid", type = "String", description = "用户userid"),
            @ApiParam(name = "pagenumber", type = "String", description = "当前页"),
            @ApiParam(name = "pagesize", type = "String", description = "每页条数(默认为10)",optional = true)},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data[i].id",description="使用记录id"),
                    @ReturnKey(key="data[i].eqnum",description="设备编号"),
                    @ReturnKey(key="data[i].eqname",description="设备名称"),
                    @ReturnKey(key="data[i].starttime",description="设备借用开始时间"),
                    @ReturnKey(key="data[i].pstatus",description="0 借用中 1 借用完毕"),
                    @ReturnKey(key="data[i].endtime",description="设备使用归还时间"),
                    @ReturnKey(key="data[i].eqcode",description="设备身份证牌照")} )
    public Object getUseList(@Param("personid") String personid, @Param("pagenumber") Integer pagenumber, @Param("pagesize") Integer pagesize){
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

    @At("/setDeliver")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="设置转交设备的标识",params ={@ApiParam(name = "userid", type = "String", description = "用户id"),
                    @ApiParam(name = "useinfoids", type = "String[]", description = "转交设备id数组")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)")} )
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

    @At("/ListenDeliver")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="监听转交结果",params ={@ApiParam(name = "userid", type = "String", description = "转交用户id"),
            @ApiParam(name = "useinfoids", type = "String[]", description = "转交设备id数组")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data.number",description="待转交设备数量，为0则表示已经转交成功")
            } )
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

    @At("/DeliverMateriel")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="设备转交",params ={@ApiParam(name = "olduserid", type = "String", description = "转交用户id"),
            @ApiParam(name = "newuserid", type = "String", description = "被转交用户id")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)")
            } )
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
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="撤销转交",params ={@ApiParam(name = "userid", type = "String", description = "转交用户id")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)")
            } )
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

    @At("/getUsingEqList")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="获取正在借用设备列表",params ={@ApiParam(name = "userid", type = "String", description = "用户id")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data[i].id",description="借用记录id"),
                    @ReturnKey(key="data[i].eqnum",description="设备编号"),
                    @ReturnKey(key="data[i].eqname",description="设备名称"),
                    @ReturnKey(key="data[i].typeid",description="设备类型id"),
                    @ReturnKey(key="data[i].code",description="设备类型编号"),
                    @ReturnKey(key="data[i].stakenum",description="桩位编号"),
                    @ReturnKey(key="data[i].stakename",description="桩位名称"),
                    @ReturnKey(key="data[i].usedtimes",description="设备使用时常(小时/分钟)"),
                    @ReturnKey(key="data[i].usedtimesld",description="轮挡使用时常(小时/分钟)"),
                    @ReturnKey(key="data[i].cpstatus",description="轮挡使用状态"),
                    @ReturnKey(key="data[i].lockid",description="锁编号"),
                    @ReturnKey(key="data[i].seatnum",description="机位"),
                    @ReturnKey(key="data[i].distance",description="距离"),
                    @ReturnKey(key="data[i].position",description="设备经纬度")} )
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
//                    record.put("eqnum",record.getString("stakenum"));//为不改前端，临时把设备编码改为桩位编码，为了归还方便核对
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

    @At("/insertChockbyUseinfo")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="轮挡开始计时",params ={@ApiParam(name = "useinfoid", type = "String", description = "设备借用记录id"),
                    @ApiParam(name = "userid", type = "String", description = "用户id"),
                    @ApiParam(name = "starttime", type = "String", description = "开始时间:yyyy-MM-dd hh:mm:ss")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)")
            } )
    public Object insertChockbyUseinfo(@Param("useinfoid") String useinfoid,@Param("userid") String userid,@Param("starttime") String starttime){
        try{
            if(!Strings.isBlank(useinfoid)){
                eq_chockuse chockuse=new eq_chockuse();
                chockuse.setUseinfoID(useinfoid);
                if(!Strings.isBlank(userid))
                    chockuse.setGetpersonid(userid);
                chockuse.setStarttime(starttime);
                chockuse.setSstarttime(newDataTime.getDateYMDHMS());
                chockuse.setPstatus("1");
                eqChockuseService.insert(chockuse);
                return Result.success("system.success");
            }
            return  Result.error("useinfoid is null");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error", e);
        }

    }


    @At("/updateChockbyUseinfo")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="轮挡开始计时",params ={@ApiParam(name = "useinfoid", type = "String", description = "设备借用记录id"),
            @ApiParam(name = "userid", type = "String", description = "用户id"),
            @ApiParam(name = "endtime", type = "String", description = "结束时间:yyyy-MM-dd hh:mm:ss")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息,成功返回借用时长00:00:02)")
            } )
    public Object updateChockbyUseinfo(@Param("useinfoid") String useinfoid,@Param("userid") String userid,@Param("endtime") String endtime)
    {
        try{
            if(!Strings.isBlank(useinfoid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("useinfoID","=",useinfoid);
                cnd.and("pstatus","=","1");
                List<eq_chockuse> chockuseList = eqChockuseService.query(cnd);
                if(chockuseList.size()>0){
                    chockuseList.get(0).setEndtime(endtime);
                    chockuseList.get(0).setSendtime(newDataTime.getDateYMDHMS());
                    chockuseList.get(0).setGopersonid(userid);
                    chockuseList.get(0).setPstatus("0");
                    eqChockuseService.updateIgnoreNull(chockuseList.get(0));
                    String date="";
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    try
//                    {
                    Date d1 = df.parse(chockuseList.get(0).getStarttime());
                    Date d2 = df.parse(endtime);
                    long diff = d2.getTime() - d1.getTime();//这样得到的差值是微秒级别
                    long days = diff / (1000 * 60 * 60 * 24);

                    long hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
                    long minutes = (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60))/(1000* 60);
                    long seconds = ((diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60)-minutes*(1000*60))/1000);

                    date=((days*24+hours)>9?(days*24+hours):"0"+(days*24+hours))+":"+(minutes>9?minutes:"0"+minutes)+":"+(seconds>9?seconds:"0"+seconds);
//                    }catch (Exception e)
//                    {
//
//                    }

                    return  Result.success(date);
                }
            }
            return  Result.error(-1,"fail");
        }catch(Exception e){
            e.printStackTrace();
            return  Result.error(-1,"fail");
        }
    }

}
