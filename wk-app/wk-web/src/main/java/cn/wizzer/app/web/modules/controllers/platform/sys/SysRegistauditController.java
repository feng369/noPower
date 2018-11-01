package cn.wizzer.app.web.modules.controllers.platform.sys;

import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.app.base.modules.models.base_cnctobj;
import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.app.base.modules.services.BaseAirportService;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.sys.modules.models.*;
import cn.wizzer.app.sys.modules.services.SysUnitService;
import cn.wizzer.app.sys.modules.services.SysUserService;
import cn.wizzer.app.sys.modules.services.SysUseraddService;
import cn.wizzer.framework.base.Result;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.app.sys.modules.services.SysRegistauditService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.beetl.ext.nutz.BeetlView;
import org.beetl.ext.nutz.BeetlViewMaker;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.entity.Record;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.view.JspView;
import org.nutz.mvc.view.ViewWrapper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * zhf
 */
@IocBean
@At("/platform/sys/registaudit")
public class SysRegistauditController {
    private static final Log log = Logs.get();
    @Inject
    private SysRegistauditService sysRegistauditService;
    @Inject
    private SysUserService sysUserService;
    @Inject
    private SysUseraddService sysUseraddService;
    @Inject
    private SysUnitService sysUnitService;
    @Inject
    private BaseAirportService baseAirportService;
    @Inject
    private BaseCnctobjService baseCnctobjService;
    @Inject
    private BasePersonService basePersonService;
    @Inject
    Dao dao;

    @At("")
    @Ok("beetl:/platform/sys/registaudit/index.html")
    @RequiresPermissions("sys.manager.registaudit")
    public void index() {

    }

    @At("/data")
    @Ok("json:full")
    @RequiresPermissions("sys.manager.registaudit")
    public Object data(@Param("result")int result,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        return sysUseraddService.getRegistAuditList(result,length, start, draw, order, columns);
    }

    @At("/audit/?")
    @Ok("beetl:/platform/sys/registaudit/audit.html")
    @RequiresAuthentication
    public void audit(String uid,@Param("assignid")String assignid, HttpServletRequest req) {
        Sys_registaudit sysRegistaudit = sysRegistauditService.fetch(uid);
        if(sysRegistaudit!=null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sysRegistaudit.setAuditTime(sdf.format(new Date()));
            //auditor
            Sys_user currentUser = (Sys_user) SecurityUtils.getSubject().getPrincipal();
            sysRegistaudit.setAuditor(currentUser);
            sysRegistaudit.setAuditorId(currentUser.getId());
            sysRegistauditService.updateIgnoreNull(sysRegistaudit);
            req.setAttribute("sysRegistaudit", sysRegistaudit);
            getRegistInfo(uid,true,req);
            req.setAttribute("assignid", assignid);
        }
    }

    //进入手机注册审核页面
    @At("/phoneaudit/?")
    @Ok("beetl:/platform/sys/registaudit/phoneaudit.html")
    @RequiresAuthentication
    public void phoneaudit(String uid, HttpServletRequest req) {
        Sys_registaudit sysRegistaudit = sysRegistauditService.fetch(uid);
        if(sysRegistaudit!=null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sysRegistaudit.setAuditTime(sdf.format(new Date()));
            //auditor
            Sys_user currentUser = (Sys_user) SecurityUtils.getSubject().getPrincipal();
            sysRegistaudit.setAuditor(currentUser);
            sysRegistaudit.setAuditorId(currentUser.getId());
            sysRegistauditService.updateIgnoreNull(sysRegistaudit);
            req.setAttribute("sysRegistaudit", sysRegistaudit);
            getRegistInfo(uid,true,req);
        }
    }




//    @At("/auditDo")
//    @Ok("json:full")
//    @RequiresPermissions("sys.manager.registaudit")
//    public Object auditDo(@Param("sysRegistauditForm") String sysRegistauditForm,@Param("roleName")String roleCode, HttpServletRequest req) {
//        Sys_registaudit sysRegistaudit = Json.fromJson(Sys_registaudit.class,sysRegistauditForm);
//        try {
//            if (sysRegistaudit != null ) {
//                Sys_registaudit registaudit = sysRegistauditService.fetch(sysRegistaudit.getId());
//                if(registaudit != null){
//                    //已经存在
//                    if(!Strings.isBlank(sysRegistaudit.getAuditTime())){
//                        //处理时间
//                        sysRegistaudit.setAuditTime(sysRegistaudit.getAuditTime().replace("+", " "));
//                    }
//                     sysRegistaudit.setCreater(((Sys_user)SecurityUtils.getSubject().getPrincipal()).getId());
//                    sysRegistaudit.setWay(registaudit.getWay());
//                    sysRegistauditService.updateIgnoreNull(sysRegistaudit);
//                    sysRegistauditService.completeAudit(sysRegistaudit.getId(),sysRegistaudit.getResult(),roleCode,sysRegistaudit.getPersonId(),false);
//                }
//            }
//            return Result.success("完成审核");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return Result.error("审核错误");
//        }
//    }

    //手机注册点击审核操作
    @At("/auditDo")
    @Ok("json:full")
    @RequiresPermissions("sys.manager.registaudit")
    public Object auditDo(@Param("..") Sys_registaudit sysRegistaudit,@Param("assignid")String assignid, HttpServletRequest req) {
        try {
            if (sysRegistaudit != null ) {
                
//                sysRegistauditService.audit(sysRegistaudit,null,assignid);
            }
            return Result.success("完成审核");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("审核错误");
        }
    }

//    //手机注册点击审核操作
//    @At("/phoneauditDo")
//    @Ok("json:full")
//    @RequiresPermissions("sys.manager.registaudit")
////    public Object phoneauditDo(@Param("sysRegistauditForm") String sysRegistauditForm,@Param("assignid")String assignid, HttpServletRequest req) {
//    public Object phoneauditDo(@Param("..") Sys_registaudit sysRegistaudit,@Param("assignid")String assignid, HttpServletRequest req) {
////        Sys_registaudit sysRegistaudit = Json.fromJson(Sys_registaudit.class,sysRegistauditForm);
//        try {
//            if (sysRegistaudit != null ) {
//                   sysRegistauditService.audit(sysRegistaudit,null,assignid);
//            }
//            return Result.success("完成审核");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return Result.error("审核错误");
//        }
//    }

    @At("/detail/?")
//    @Ok("beetl:/platform/sys/registaudit/detail.html")
    @RequiresAuthentication
    public View detail(String uid, HttpServletRequest req) {
        Sys_registaudit sysRegistaudit = sysRegistauditService.fetch(uid);
        try {
            if(sysRegistaudit.getWay()==0){
                if(sysRegistaudit != null){
                    req.setAttribute("sysRegistaudit",sysRegistaudit);
                }
                Sys_useradd sysUseradd = sysUseraddService.fetch(Cnd.where("userid", "=", uid));
                if(sysUseradd != null){
                    req.setAttribute("sysUseradd",sysUseradd);
                }

                if(sysRegistaudit != null){
                    req.setAttribute("sysRegistaudit", sysRegistaudit);
                    if(!Strings.isBlank(sysRegistaudit.getAuditorId())){
                        req.setAttribute("auditor",sysUserService.fetch(sysRegistaudit.getAuditorId()));
                    }
                }
                getRegistInfo(uid,true,req);

                return new ViewWrapper(new BeetlView(new BeetlViewMaker().render,"/platform/sys/registaudit/phonedetail.html"),"");
            }else  if(sysRegistaudit.getWay()==1){
                Sys_user sysUser = dao.fetchLinks(dao.fetch(Sys_user.class, uid), "roles");
                List<Sys_role> roles = sysUser.getRoles();
                if(roles.size() > 0){
                    Sys_role sysRole = roles.get(0);
                    req.setAttribute("sysRole",sysRole);
                }
                if(sysRegistaudit != null){
                    req.setAttribute("sysRegistaudit", sysRegistaudit);
                    if(!Strings.isBlank(sysRegistaudit.getAuditorId())){
                        req.setAttribute("auditor",sysUserService.fetch(sysRegistaudit.getAuditorId()));
                    }
                    String personId = sysRegistaudit.getPersonId();
                    if(!Strings.isBlank(personId)){
                        base_person basePerson = basePersonService.fetch(personId);
                        req.setAttribute("basePerson",basePerson);
                    }
                }
                getRegistInfo(uid,false,req);

                return new ViewWrapper(new BeetlView(new BeetlViewMaker().render,"/platform/sys/registaudit/detail.html"),"");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    //进入手机注册查看页面
    @At("/phonedetail/?")
    @Ok("beetl:/platform/sys/registaudit/phonedetail.html")
    @RequiresAuthentication
    public void phonedetail(String uid, HttpServletRequest req) {
        Sys_registaudit sysRegistaudit = sysRegistauditService.fetch(uid);
        /*Sys_user sysUser = dao.fetchLinks(dao.fetch(Sys_user.class, uid), "roles");
        List<Sys_role> roles = sysUser.getRoles();
        if(roles.size() > 0){
            Sys_role sysRole = roles.get(0);
            req.setAttribute("sysRole",sysRole);
        }*/
        if(sysRegistaudit != null){
            req.setAttribute("sysRegistaudit",sysRegistaudit);
        }
        Sys_useradd sysUseradd = sysUseraddService.fetch(Cnd.where("userid", "=", uid));
        if(sysUseradd != null){
            req.setAttribute("sysUseradd",sysUseradd);
        }

        if(sysRegistaudit != null){
            req.setAttribute("sysRegistaudit", sysRegistaudit);
            if(!Strings.isBlank(sysRegistaudit.getAuditorId())){
                req.setAttribute("auditor",sysUserService.fetch(sysRegistaudit.getAuditorId()));
            }
        }
       getRegistInfo(uid,true,req);
    }

    //注册信息
    private void getRegistInfo(String uid,boolean flag,HttpServletRequest req) {
        if (!Strings.isBlank(uid)) {
            Sys_user sysUser = sysUserService.fetch(uid);
            req.setAttribute("sysUser", sysUser);
            Sys_useradd sysUseradd = sysUseraddService.fetch(Cnd.where("userid", "=", uid));
            if(flag){
                if(sysUseradd != null){
                    String pictureads = sysUseradd.getPictureads();
                    if(!Strings.isBlank(pictureads)){
                        req.setAttribute("imgPath",pictureads);
                    }
                }
            }
                req.setAttribute("sysUseradd",sysUseradd);
            if (sysUser != null) {
                if (!Strings.isBlank(sysUser.getUnitid())) {
                    Sys_unit sysUnit = sysUnitService.fetch(sysUser.getUnitid());
                    req.setAttribute("sysUnit", sysUnit);
                    if (sysUnit != null && !Strings.isBlank(sysUnit.getUnitairport())) {

                        base_airport baseAirport = baseAirportService.fetch(sysUnit.getUnitairport());
                        req.setAttribute("baseAirport", baseAirport);
                    }
                }
            }
        }
    }

    @At
    @Ok("beetl:/platform/sys/registaudit/selectPerson.html")
    @RequiresPermissions("sys.manager.registaudit")
    public void selectPerson(HttpServletRequest req,@Param("personname")String personname) {
         req.setAttribute("personname",personname);
    }
}
