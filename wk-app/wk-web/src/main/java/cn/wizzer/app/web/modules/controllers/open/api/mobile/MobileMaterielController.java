package cn.wizzer.app.web.modules.controllers.open.api.mobile;

import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.eq.modules.models.eq_materiel;
import cn.wizzer.app.eq.modules.models.eq_stake;
import cn.wizzer.app.eq.modules.models.eq_use;
import cn.wizzer.app.eq.modules.services.EqMaterielService;
import cn.wizzer.app.eq.modules.services.EqStakeService;
import cn.wizzer.app.eq.modules.services.EqUseService;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.framework.base.Result;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.map.LngLat;
import cn.wizzer.framework.map.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
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
import java.util.*;

@IocBean
@At("/open/mobile/materiel")
@Filters({@By(type = TokenFilter.class)})
@Api(name = "设备档案相关API", match = ApiMatchMode.ALL,description="设备档案相关API接口")
public class MobileMaterielController {

    private static final Log log = Logs.get();

    @Inject
    private EqUseService eqUseService;
    @Inject
    private EqMaterielService eqMaterielService;
    @Inject
    private BaseCnctobjService baseCnctobjService;
    @Inject
    private BasePersonService basePersonService;
    @Inject
    private EqStakeService eqStakeService;
    @Inject
    private Dao dao;

    @At("/getMaterialBylockID")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="根据锁号获得维修设备信息",params ={@ApiParam(name = "lockID", type = "String", description = "锁编号,即IME码")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data.id",description="设备id"),
                    @ReturnKey(key="data.eqname",description="设备名称"),
                    @ReturnKey(key="data.eqnum",description="设备编号"),
                    @ReturnKey(key="data.eqtype",description="设备规格型号"),
                    @ReturnKey(key="data.eqcode",description="设备身份证牌照，即机场设备编码"),
                    @ReturnKey(key="data.lockid",description="锁编号IME码")
            } )
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

    @At("/getMaterialList")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="根据设备名称关键字检索使用中设备列表",params ={@ApiParam(name = "keyword", type = "String", description = "设备名称模糊搜索关键字")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data[i].id",description="设备id"),
                    @ReturnKey(key="data[i].eqname",description="设备名称"),
                    @ReturnKey(key="data[i].eqnum",description="设备编号"),
                    @ReturnKey(key="data[i].eqtype",description="设备规格型号"),
                    @ReturnKey(key="data[i].eqcode",description="设备身份证牌照，即机场设备编码"),
                    @ReturnKey(key="data[i].lockid",description="锁编号IME码")
            } )
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

    @At("/getAroundUsableEqList")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="获取用户周围设备列表",params ={@ApiParam(name = "personid", type = "String", description = "用户userid"),
                    @ApiParam(name = "position", type = "String", description = "用户当前经纬度")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data[i].id",description="设备id"),
                    @ReturnKey(key="data[i].eqname",description="设备名称"),
                    @ReturnKey(key="data[i].eqnum",description="设备编号"),
                    @ReturnKey(key="data[i].eqtype",description="设备规格型号"),
                    @ReturnKey(key="data[i].lockid",description="锁编号IME码"),
                    @ReturnKey(key="data[i].eqcode",description="设备身份证牌照，即机场设备编码"),
                    @ReturnKey(key="data[i].typecode",description="设备类型编号"),
                    @ReturnKey(key="data[i].typename",description="设备类型名称"),
                    @ReturnKey(key="data[i].position",description="设备地理位置"),
                    @ReturnKey(key="data[i].distance",description="设备距离(米)"),
                    @ReturnKey(key="data[i].stakenum",description="桩位编号"),
                    @ReturnKey(key="data[i].stakename",description="桩位名称"),
                    @ReturnKey(key="data[i].seatnum",description="机位编号"),
                    @ReturnKey(key="data[i].seatname",description="机位名称"),
                    @ReturnKey(key="data[i].pstatus",description="设备开关状态，0 关；1 开"),
                    @ReturnKey(key="data[i].bizstatus",description="设备业务状态：0 正常使用中； 1 报修中；2 维修中;....")
            },description = "bizstatus=0表示设备正常，否则不可借用，pstatus=0表示空闲(可借用) pstatus=1 表示借用中" )
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

    //从给定的机位范围内搜索匹配关键字的设备
    @At("/getMaterielByParams")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="关键字搜索可借用设备",params ={@ApiParam(name = "airportid", type = "String", description = "机场id"),
            @ApiParam(name = "position", type = "String", description = "用户当前经纬度"),
            @ApiParam(name = "keyword", type = "String", description = "搜索关键字")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data[i].id",description="设备id"),
                    @ReturnKey(key="data[i].eqname",description="设备名称"),
                    @ReturnKey(key="data[i].eqnum",description="设备编号"),
                    @ReturnKey(key="data[i].eqtype",description="设备规格型号"),
                    @ReturnKey(key="data[i].lockid",description="锁编号IME码"),
                    @ReturnKey(key="data[i].eqcode",description="设备身份证牌照，即机场设备编码"),
                    @ReturnKey(key="data[i].typecode",description="设备类型编号"),
                    @ReturnKey(key="data[i].typename",description="设备类型名称"),
                    @ReturnKey(key="data[i].position",description="设备地理位置"),
                    @ReturnKey(key="data[i].distance",description="设备距离(米)"),
                    @ReturnKey(key="data[i].stakenum",description="桩位编号"),
                    @ReturnKey(key="data[i].stakename",description="桩位名称"),
                    @ReturnKey(key="data[i].seatnum",description="机位编号"),
                    @ReturnKey(key="data[i].seatname",description="机位名称"),
                    @ReturnKey(key="data[i].pstatus",description="设备开关状态，0 关；1 开"),
                    @ReturnKey(key="data[i].bizstatus",description="设备业务状态：0 正常使用中； 1 报修中；2 维修中;....")
            },description = "bizstatus=0表示设备正常，否则不可借用，pstatus=0表示空闲(可借用) pstatus=1 表示借用中")
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

    //查询正在借用中的设备的信息，主要查询经纬度
    @At("/getUsebyuseinfoid")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="获取设备对应锁的经纬度",params ={@ApiParam(name = "useinfoid", type = "String", description = "设备使用记录id")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data.eqid",description="设备id"),
                    @ReturnKey(key="data.useid",description="设备的使用id"),
                    @ReturnKey(key="data.position",description="设备地址位置")
            } )
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


}
