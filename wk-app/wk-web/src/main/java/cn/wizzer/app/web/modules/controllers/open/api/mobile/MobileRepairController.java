package cn.wizzer.app.web.modules.controllers.open.api.mobile;

import cn.wizzer.app.eq.modules.models.eq_materiel;
import cn.wizzer.app.eq.modules.models.eq_repair;
import cn.wizzer.app.eq.modules.services.EqMaterielService;
import cn.wizzer.app.eq.modules.services.EqRepairService;
import cn.wizzer.app.eq.modules.services.EqUseService;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.framework.base.Result;
import cn.wizzer.framework.util.DateUtil;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.pager.Pager;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.impl.AdaptorErrorContext;
import org.nutz.plugins.apidoc.annotation.Api;
import org.nutz.plugins.apidoc.annotation.ApiMatchMode;
import org.nutz.plugins.apidoc.annotation.ApiParam;
import org.nutz.plugins.apidoc.annotation.ReturnKey;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

@IocBean
@At("/open/mobile/repair")
@Filters({@By(type = TokenFilter.class)})
@Api(name = "设备维修申报相关API", match = ApiMatchMode.ALL,description="设备维修申报相关API接口")
public class MobileRepairController {

    private static final Log log = Logs.get();
    @Inject
    private EqRepairService eqRepairService;
    @Inject
    private EqUseService eqUseService;
    @Inject
    private EqMaterielService eqMaterielService;
    @Inject
    private Dao dao;

    @At("/getRepairList")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="获取设备维修列表",params ={@ApiParam(name = "userid", type = "String", description = "用户id"),
                @ApiParam(name = "pagenumber", type = "String", description = "当前页"),
                @ApiParam(name = "pagesize", type = "String", description = "没页显示数量(默认为10)",optional = true)},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data[i].id",description="维修单id"),
                    @ReturnKey(key="data[i].eqname",description="设备名称"),
                    @ReturnKey(key="data[i].eqnum",description="设备编号"),
                    @ReturnKey(key="data[i].eqtype",description="设备规格型号"),
                    @ReturnKey(key="data[i].eqcode",description="设备身份证牌照，即机场设备编码"),
                    @ReturnKey(key="data[i].reptype",description="维修类型"),
                    @ReturnKey(key="data[i].pstatus",description="维修单状态 0:正常使用 1:报修中 2:维修中")
            } )
    public Object getRepairList(@Param("userid") String userid,@Param("pagenumber") Integer pagenumber,@Param("pagesize") Integer pagesize){
        try{
            if(!Strings.isBlank(userid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("pstatus","<>","99");
                cnd.and("personid","=",userid);
                Pager pager = new Pager(1);
                if(pagenumber!=null && pagenumber.intValue()>0){
                    pager.setPageNumber(pagenumber.intValue());
                }
                if(pagesize!=null){
                    pager.setPageSize(pagesize.intValue());
                }
                List<eq_repair> repairList = eqRepairService.query(cnd,"eqMateriel|sysDict",pager);
                List<HashMap> mapList =new ArrayList<>();
                for(int i=0;i<repairList.size();i++){
                    HashMap map=new HashMap();
                    map.put("eqname",repairList.get(i).getEqMateriel()==null?null:repairList.get(i).getEqMateriel().getEqname());
                    map.put("eqtype",repairList.get(i).getEqMateriel()==null?null:repairList.get(i).getEqMateriel().getEqtype());
                    map.put("pstatus",repairList.get(i).getPstatus());
                    map.put("id",repairList.get(i).getId());
                    map.put("eqnum",repairList.get(i).getEqMateriel()==null?null:repairList.get(i).getEqMateriel().getEqnum());
                    map.put("reptype",repairList.get(i).getReptype());
                    map.put("eqcode",repairList.get(i).getEqMateriel() == null ?"": repairList.get(i).getEqMateriel().getEqcode());
                    mapList.add(map);
                }
                return  Result.success("system.success",mapList);
            }
            return Result.error(2,"userid is null");
        }
        catch (Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/insertRepair")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="新建维修申报",params ={@ApiParam(name = "eqid", type = "String", description = "设备id"),
            @ApiParam(name = "reptype", type = "String", description = "维修类型:默认紧急"),
            @ApiParam(name = "reptext", type = "String", description = "问题描述"),
            @ApiParam(name = "personid", type = "String", description = "用户userid"),
            @ApiParam(name = "pstatus", type = "String", description = "默认传”1”")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data",description="维修单id")
            } )
    public Object insertRepair(@Param("reptype") String reptype,@Param("eqid") String eqid,@Param("reptext") String reptext,@Param("personid") String personid,@Param("pstatus") String pstatus)
    {
        try{
            eq_repair repair = eqRepairService.insertRepair(reptype,eqid,reptext,personid,pstatus);
            return Result.success("保存成功",repair.getId());
        }catch(Exception e){
            e.printStackTrace();
            return Result.error(-1,"保存失败");
        }

    }

    @At("/img")
    @Ok("json")
    //AdaptorErrorContext必须是最后一个参数
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="新建维修申报上传图片",params ={@ApiParam(name = "filename", type = "String", description = "图片文件名"),
            @ApiParam(name = "isnew", type = "boolean", description = "默认false",optional = true),
            @ApiParam(name = "eqid", type = "String", description = "设备id",optional = true),
            @ApiParam(name = "base64", type = "String", description = "图片base64字符串"),
            @ApiParam(name = "repairid", type = "String", description = "维修单id")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)")
            } )
    public Object img(@Param("filename") String filename, @Param("base64") String base64, @Param("eqid") String eqid, @Param("isnew") boolean isnew, @Param("repairid") String repairid, HttpServletRequest req, AdaptorErrorContext err) {
        byte[] buffer = null;
        try {
            if (err != null && err.getAdaptorErr() != null) {
                return NutMap.NEW().addv("code", 1).addv("msg", "文件不合法");
            } else if (base64 == null) {
                return Result.error("空文件");
            } else {
                String p = Globals.AppRoot;
                String fn= R.UU32()+filename.substring(filename.lastIndexOf("."));
                String path=Globals.AppUploadPath + "/image/" + DateUtil.format(new Date(), "yyyyMMdd") + "/";
                String f = Globals.AppUploadPath + "/image/" + DateUtil.format(new Date(), "yyyyMMdd") + "/" + fn ;
                File file=new File(p+Globals.AppUploadPath + "/image/" + DateUtil.format(new Date(), "yyyyMMdd"));
                if(!file.exists()){
                    file.mkdirs();
                }
                if(base64.indexOf(",")>=0){//兼容H5
                    buffer = Base64.getDecoder().decode(base64.split(",")[1]);
                }else{
                    buffer = Base64.getDecoder().decode(base64);
                }

                FileOutputStream out = new FileOutputStream(p + f);
                out.write(buffer);
                out.close();
                /********************
                 * 将上传的文件路径修改到数据库中
                 * 1.新增一行
                 * 2.得到picpath及picname、oldpicname
                 * 3.将新的合并到这些字段
                 * 4.修改
                 *******************/
                eq_repair repair;
                eq_repair r;
                if(isnew==true){
                    repair=new eq_repair();
//                    repair.setEqid(eqid);
                }else
                {
                    Cnd cnd=Cnd.NEW();
                    cnd.and("id","=",repairid);
                    repair=eqRepairService.fetch(cnd);
                }
                if(repair!=null){

                    String picpath=repair.getImgpath();
                    String picname=repair.getImgname();
                    String oldpicname=repair.getOldimgname();
                    if(!Strings.isBlank(picpath)){
                        picpath+=","+path+fn;
                    }else{
                        picpath=path+fn;
                    }
                    if(!Strings.isBlank(picname)){
                        picname+=","+fn;
                    }else{
                        picname=fn;
                    }
                    if(!Strings.isBlank(oldpicname)){
                        oldpicname+=","+filename;
                    }else{
                        oldpicname=filename;
                    }
                    repair.setImgpath(picpath);
                    repair.setImgname(picname);
                    repair.setOldimgname(oldpicname);
                    repair.setPstatus("1");

                    if(isnew==true){
                        r = eqRepairService.insert(repair);
                        return Result.success("上传成功","{\"id\":\""+r.getId()+"\",\"path\":\""+Globals.AppBase+f+"\"}");
                    }else{
                        eqRepairService.updateIgnoreNull(repair);
                        return Result.success("上传成功","{\"id\":\""+repairid+"\",\"path\":\""+Globals.AppBase+f+"\"}");
                    }
                }
                return Result.success("系统繁忙，请重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        } catch (Throwable e) {
            e.printStackTrace();
            return Result.error(-2,"图片格式错误");
        }
    }

    @At("/getRepairInfo")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="获取维修详情数据",params ={@ApiParam(name = "repairid", type = "String", description = "维修单id")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data.eqname",description="设备名称"),
                    @ReturnKey(key="data.eqnum",description="设备编号"),
                    @ReturnKey(key="data.eqcode",description="设备身份证牌照，即机场设备编码"),
                    @ReturnKey(key="data.reptext",description="申报详情内容"),
                    @ReturnKey(key="data.reptype",description="维修类型"),
                    @ReturnKey(key="data.pstatus",description="维修单状态 0:正常使用 1:报修中 2:维修中"),
                    @ReturnKey(key="data.imgpath",description="申报图片路径")
            } )
    public Object getRepairInfo(@Param("repairid") String repairid){
        try{
            if(!Strings.isBlank(repairid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("id","=",repairid);

                List<eq_repair> repairs = eqRepairService.query(cnd,"eqMateriel");
                HashMap map = new HashMap();
                if(repairs.size()>0){
                    map.put("eqnum",repairs.get(0).getEqMateriel()==null?null:repairs.get(0).getEqMateriel().getEqnum());
                    map.put("eqcode",repairs.get(0).getEqMateriel()==null?null:repairs.get(0).getEqMateriel().getEqcode());
                    map.put("eqname",repairs.get(0).getEqMateriel()==null?null:repairs.get(0).getEqMateriel().getEqname());
                    map.put("reptype",repairs.get(0).getReptype());
                    map.put("pstatus",repairs.get(0).getPstatus());
                    map.put("reptext",repairs.get(0).getReptext());
                    map.put("imgpath",repairs.get(0).getImgpath());
                }
                return Result.success("system.success",map);
            }
            return Result.error(2,"repairid is null");
        }catch(Exception e){
            e.printStackTrace();
            return  Result.error("system.error",e);
        }
    }

    //通过lockid判断当前设备的维修状态
    @At("/updateRepairByLockID")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="判断设备是否在维修",params ={@ApiParam(name = "lockID", type = "String", description = "锁IME号")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data.isUse",description="true:设备正在维修 ;false:正常")
            } )
    public Object updateRepairByLockID(@Param("lockID") String lockID)
    {
        try{
            if(!Strings.isBlank(lockID)){
                Cnd cnd0=Cnd.NEW();
                cnd0.and("lockid","=",lockID);
                eq_materiel materiel=eqMaterielService.fetch(cnd0);
                if(materiel!=null){
                    Cnd cnd=Cnd.NEW();
                    cnd.and("eqid","=",materiel.getId());
                    cnd.and("pstatus","=","1");
                    cnd.or("pstatus","=","2");
                    List<eq_repair> repairs = eqRepairService.query(cnd);
                    HashMap map=new HashMap();
                    if(repairs.size()>0){
                        map.put("isUse",false);
                    }else{
                        map.put("isUse",true);
                    }
                    return Result.success("system.success",map);
                }

            }
            return Result.error(2,"lockID or materiel is null");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error", e);
        }

    }


}
