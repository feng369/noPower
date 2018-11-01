package cn.wizzer.app.web.modules.controllers.open.api.mobile;

import cn.wizzer.app.eq.modules.models.eq_materiel;
import cn.wizzer.app.eq.modules.models.eq_use;
import cn.wizzer.app.eq.modules.services.EqMaterielService;
import cn.wizzer.app.eq.modules.services.EqUseService;
import cn.wizzer.app.sys.modules.models.Sys_role;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.framework.base.Result;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
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

import java.util.*;

@IocBean
@At("/open/mobile/use")
@Filters({@By(type = TokenFilter.class)})
@Api(name = "设备借用归还API", match = ApiMatchMode.ALL,description="设备借用归还相关API接口")
public class MobileUseController {
    //专门针对硬件通讯接口getinfo方法定制日志文件
    private static Logger luckyLog = Logger.getLogger("LuckyLog");
    private static final Log log = Logs.get();

    @Inject
    private EqUseService eqUseService;
    @Inject
    private EqMaterielService eqMaterielService;
    @Inject
    private Dao dao;

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


    @At("/openLock")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="扫码解锁指令",params ={@ApiParam(name = "lockID", type = "String", description = "锁编号"),
            @ApiParam(name = "personid", type = "String", description = "用户userid"),
            @ApiParam(name = "personunitid", type = "String", description = "用户所属单位id"),
            @ApiParam(name = "starttime", type = "String", description = "开始时间: yyyy-MM-dd hh:mm:ss")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data.equseid",description="锁的使用id")
            },description = "扫码解锁—此接口仅仅向硬件发送指令，是否开锁成功需要通过返回的equseid值调用接口getUnLockStatus来判断")
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
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="判断锁是否打开",params ={@ApiParam(name = "equseid", type = "String", description = "锁在使用id，openLock接口正常返回的值"),
                @ApiParam(name = "lockID", type = "String", description = "锁编号",optional = true),
                @ApiParam(name = "personid", type = "String", description = "借用用户id"),
                @ApiParam(name = "personunitid", type = "String", description = "借用用户所属单位"),
                @ApiParam(name = "starttime", type = "String", description = "借用开始时间: yyyy-MM-dd hh:mm:ss",optional = true)},
            ok={@ReturnKey(key = "code", description = "0 锁已打开；1 系统或校验异常见msg；2 锁还未打开，请稍后…;3 设备借用失败,见msg"),@ReturnKey(key = "msg", description = "(提示信息)")
            },description = "在扫描开锁后用此接口判断是否开锁成功，前端需要在一定时间内循环调用")
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
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="归还设备指令",params ={@ApiParam(name = "lockID", type = "String", description = "锁编号"),
            @ApiParam(name = "personid", type = "String", description = "用户userid")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data.equseid",description="锁使用的id")
            },description = "此接口仅仅向锁发送关锁指令，需要在发送后，手工关闭锁，然后通过返回的参数equseid调用getLockStatus接口判断锁是否关闭完成")
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
    @Filters({@By(type = TokenFilter.class)})
    @Api(name="判断锁是否关闭",params ={@ApiParam(name = "equseid", type = "String", description = "锁的使用id，returnLock接口正常返回的值")},
            ok={@ReturnKey(key = "code", description = "0 锁已归还；1 系统或校验异常见msg;2 锁还未归还，请稍后…;3 设备归还失败见msg"),@ReturnKey(key = "msg", description = "(提示信息)")
            },description = "在APP点归还设备后用此接口判断是否关锁完成，前端需要在一定时间内循环调用")
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
