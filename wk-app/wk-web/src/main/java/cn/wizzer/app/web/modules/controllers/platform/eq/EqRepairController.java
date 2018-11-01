package cn.wizzer.app.web.modules.controllers.platform.eq;

import cn.wizzer.app.base.modules.services.BaseAirportService;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.eq.modules.models.eq_materiel;
import cn.wizzer.app.eq.modules.models.eq_use;
import cn.wizzer.app.eq.modules.services.EqMaterielService;
import cn.wizzer.app.eq.modules.services.EqRepairtrackService;
import cn.wizzer.app.eq.modules.services.EqUseService;
import cn.wizzer.app.msg.modules.models.Msg_option;
import cn.wizzer.app.sys.modules.models.Sys_dict;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.app.sys.modules.services.SysDictService;
import cn.wizzer.app.sys.modules.services.SysRoleService;
import cn.wizzer.app.sys.modules.services.SysUserService;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.DateUtil;
import cn.wizzer.app.eq.modules.models.eq_repair;
import cn.wizzer.app.eq.modules.services.EqRepairService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.filter.CrossOriginFilter;
import org.nutz.mvc.impl.AdaptorErrorContext;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

@IocBean
@At("/platform/eq/repair")
public class EqRepairController{
    private static final Log log = Logs.get();
    @Inject
    private EqRepairService eqRepairService;
    @Inject
    private EqMaterielService eqMaterielService;
    @Inject
    private EqUseService eqUseService;
    @Inject
    private SysUserService sysUserService;
    @Inject
    private SysRoleService sysRoleService;
    @Inject
    private SysDictService sysDictService;
    @Inject
    private BaseAirportService baseAirportService;
    @Inject
    private EqRepairtrackService eqRepairtrackService;
    @Inject
    Dao dao;
    @Inject
    private BasePersonService basePersonService;
    @Inject
    private BaseCnctobjService baseCnctobjService;


    @At("")
    @Ok("beetl:/platform/eq/repair/index.html")
    @RequiresPermissions("platform.eq.repair")
    public void index() {
    }

    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.eq.repair.select")
    public Object data(@Param("selectForm") String selectForm,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        String repnum="",personname="",pstatus="",eqname="",eqnum="";
        if(selectForm!=null){
		    Object o=Json.fromJson(selectForm);
		    if(o!=null){
                if(!Strings.isBlank(((LinkedHashMap) o).get("repnum").toString())){
                    repnum=((LinkedHashMap) o).get("repnum").toString();
                }
		        if(!Strings.isBlank(((LinkedHashMap) o).get("personname").toString())){
                    personname=((LinkedHashMap) o).get("personname").toString();
                }
                if(!Strings.isBlank(((LinkedHashMap) o).get("pstatus").toString())){
                    pstatus=((LinkedHashMap) o).get("pstatus").toString();
                }
                if(!Strings.isBlank(((LinkedHashMap) o).get("eqname").toString())){
                    eqname=((LinkedHashMap) o).get("eqname").toString();
                }
                if(!Strings.isBlank(((LinkedHashMap) o).get("eqnum").toString())){
                    eqnum=((LinkedHashMap) o).get("eqnum").toString();
                }

            }
        }
    	return eqRepairService.getRepairList(repnum,personname,pstatus,eqname,eqnum,start,length,draw);
    }

    @At("/add")
    @Ok("beetl:/platform/eq/repair/add.html")
    @RequiresPermissions("platform.eq.repair")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.repair.add")
    @SLog(tag = "eq_repair", msg = "${args[0].id}")
    public Object addDo(@Param("..")eq_repair eqRepair, HttpServletRequest req) {
		try{
            eqRepair = eqRepairService.insert(eqRepair);
            //20180329zhf1806
//            addRepairTrack(eqRepair);
			return Result.success("system.success");
		}catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/edit/?")
    @Ok("beetl:/platform/eq/repair/edit.html")
    @RequiresPermissions("platform.eq.repair")
    public void edit(String id,HttpServletRequest req) {
        eq_repair eqRepair =eqRepairService.fetch(id);
        if(!Strings.isBlank(eqRepair.getEqid())){
            eq_materiel eqMateriel=eqMaterielService.fetch(eqRepair.getEqid());
            eqRepair.setEqMateriel(eqMateriel);
        }
        if(!Strings.isBlank(eqRepair.getPersonid())){
            Sys_user sysUser=sysUserService.fetch(eqRepair.getPersonid());
            eqRepair.setSysUser(sysUser);
        }
        if(!Strings.isBlank(eqRepair.getWxuserid())){
            Sys_user sysUser=sysUserService.fetch(eqRepair.getWxuserid());
            eqRepair.setWxUser(sysUser);
        }
        if("1".equals(eqRepair.getPstatus())){
            eqRepair.setPstatus("2");
            req.setAttribute("submitButton", "分 配");
        }else{
            req.setAttribute("submitButton", "提 交");
        }
		req.setAttribute("obj", eqRepair);
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.eq.repair.edit")
    @SLog(tag = "eq_repair", msg = "${args[0].id}")
    public Object editDo(@Param("..")eq_repair eqRepair, HttpServletRequest req) {
		try {
		    if("2".equals(eqRepair.getPstatus())||"0".equals(eqRepair.getPstatus())){
                String eqid = eqRepair.getEqid();
                Cnd cnd=Cnd.NEW();
                cnd.and("eqid","=",eqid);
                eq_use eqUse = eqUseService.fetch(cnd);
                if(eqUse!=null ){
                    if("1".equals(eqUse.getBizstatus())) {
                        throw new ValidatException("设备已锁定，请确认设备状态！");
                    }
//                    else if(!"0".equals(eqUse.getPstatus())) {
//                        throw new ValidatException("设备未归还，请先归还设备才能进行维修！");
//                    }
                }
            }
            eqRepairService.editDo(eqRepair);

			return Result.success("system.success");
		} catch (Exception e) {
		    e.printStackTrace();
			return Result.error("system.error",e);
		}
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.eq.repair.delete")
    @SLog(tag = "eq_repair", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				eqRepairService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				eqRepairService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/eq/repair/detail.html")
    @RequiresAuthentication
	public void detail(String id, HttpServletRequest req) {
		if (!Strings.isBlank(id)) {
		    eq_repair eqRepair =eqRepairService.fetch(id);
		    if(!Strings.isBlank(eqRepair.getEqid())){
		        eq_materiel eqMateriel=eqMaterielService.fetch(eqRepair.getEqid());
                eqRepair.setEqMateriel(eqMateriel);
            }
            if(!Strings.isBlank(eqRepair.getReptype())){
                Sys_dict dict= sysDictService.fetch(eqRepair.getReptype());
                eqRepair.setSysDict(dict);
            }
            if(!Strings.isBlank(eqRepair.getPersonid())){
                Sys_user user=sysUserService.fetch(eqRepair.getPersonid());
                eqRepair.setSysUser(user);
            }
            if(!Strings.isBlank(eqRepair.getWxuserid())){
                Sys_user user=sysUserService.fetch(eqRepair.getWxuserid());
                eqRepair.setWxUser(user);
            }

            req.setAttribute("obj", eqRepair);
		}else{
            req.setAttribute("obj", null);
        }
    }

    @At("/audit/?")
    @Ok("beetl:/platform/eq/repair/audit.html")
    @RequiresAuthentication
    public void audit(String id,HttpServletRequest req) {
        eq_repair eqRepair =eqRepairService.fetch(id);
        if(!Strings.isBlank(eqRepair.getEqid())){
            eq_materiel eqMateriel=eqMaterielService.fetch(eqRepair.getEqid());
            eqRepair.setEqMateriel(eqMateriel);
        }
        if(!Strings.isBlank(eqRepair.getPersonid())){
            Sys_user sysUser=sysUserService.fetch(eqRepair.getPersonid());
            eqRepair.setSysUser(sysUser);
        }
        if(!Strings.isBlank(eqRepair.getWxuserid())){
            Sys_user sysUser=sysUserService.fetch(eqRepair.getWxuserid());
            eqRepair.setWxUser(sysUser);
        }
        if("1".equals(eqRepair.getPstatus())){
            eqRepair.setPstatus("2");
//            req.setAttribute("submitButton", "分 配");
        }else{
//            req.setAttribute("submitButton", "提 交");
        }
        req.setAttribute("obj", eqRepair);
    }

//    @At("/auditDo")
//    @Ok("json")
//    @RequiresPermissions("platform.eq.repair")
//    @SLog(tag = "eq_repair", msg = "${args[0].id}")
//    public Object auditDo(@Param("..")eq_repair eqRepair, @Param("::option.")Msg_option option, @Param("assignid")String assignid, HttpServletRequest req) {
//        try {
//            if("2".equals(eqRepair.getPstatus())||"0".equals(eqRepair.getPstatus())){
//                String eqid = eqRepair.getEqid();
//                Cnd cnd=Cnd.NEW();
//                cnd.and("eqid","=",eqid);
//                eq_use eqUse = eqUseService.fetch(cnd);
//                if(eqUse!=null ){
//                    if("2".equals(eqUse.getPstatus())) {
//                        throw new ValidatException("设备已锁定，请确认设备状态！");
//                    }else if(!"0".equals(eqUse.getPstatus())) {
//                        throw new ValidatException("设备未归还，请先归还设备才能进行维修！");
//                    }
//                }
//            }
//            eqRepairService.audit(eqRepair,option,assignid);
//
//            return Result.success("system.success");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return Result.error("system.error",e);
//        }
//    }


    @At("/selectWxUser")
    @Ok("beetl:/platform/eq/repair/selectWxUser.html")
    @RequiresPermissions("platform.eq.repair")
    public void selectWxUser(String eqid,HttpServletRequest req) {
        //查找维修人员角色数据
//        Sys_role sysRole = sysRoleService.fetch(Cnd.where("code","=","wx"));
        eq_materiel eqMateriel = eqMaterielService.fetch(eqid);
        Map<String,List<String>>  rolemap = sysRoleService.getUsersByRole(eqMateriel.getEqunitid(),"","维修人员");
        req.setAttribute("roleid", rolemap.size()>0?rolemap.keySet().iterator().next():"");//rolemap.entrySet().iterator().next().getKey()
    }

    //根据角色查找用户
    @At("/selectData")
    @Ok("json:full")
    @RequiresPermissions("platform.eq.repair")
    public Object selectData(@Param("roleid") String roleid, @Param("name") String name, @Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        String sql = "SELECT a.* FROM sys_user a WHERE 1=1 ";
        sql += " and a.id IN(SELECT b.userId FROM sys_user_role b WHERE b.roleId='" + roleid + "')";
        name = name.replace("'", "");
        if (!Strings.isBlank(name)) {
            sql += " and (a.loginname like '%" + name + "%' or a.username like '%" + name + "%') ";
        }
        String s = sql;
        if (order != null && order.size() > 0) {
            for (DataTableOrder o : order) {
                DataTableColumn col = columns.get(o.getColumn());
                s += " order by a." + Sqls.escapeSqlFieldValue(col.getData()).toString() + " " + o.getDir();
            }
        }
        return eqRepairService.data(length, start, draw, Sqls.create(sql), Sqls.create(s));
    }


    /**
     * 维修完成 koudepei 20180413
     *
     * @param ids
     * @param req
     * @return
     */
    @At("/complete")
    @Ok("json")
    @RequiresPermissions("platform.eq.repair.complete")
    @SLog(tag = "维修完成", msg = "${req.getAttribute('ids')}")
    public Object complete(@Param("ids") String[] ids, HttpServletRequest req) {
        try {
            eqRepairService.complete(ids);
            return Result.success("system.success");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error", e);
        }
    }


    @At("/getRepairList")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
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

    @At("/img")
    @Ok("json")
    //AdaptorErrorContext必须是最后一个参数
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object img(@Param("filename") String filename, @Param("base64") String base64,@Param("eqid") String eqid,@Param("isnew") boolean isnew,@Param("repairid") String repairid,HttpServletRequest req, AdaptorErrorContext err) {
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

    @At("/delUpload")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object delUpload(@Param("filename") String filename,@Param("repairid") String repairid){
        try {
            if (!Strings.isBlank(filename)) {
                Cnd cnd = Cnd.NEW();
                cnd.and("id", "=", repairid);
                eq_repair repair = eqRepairService.fetch(cnd);
                if (repair != null) {
                    String oldpicname = repair.getOldimgname();
                    String picname = repair.getImgname();
                    String picpath = repair.getImgpath();
                    if (oldpicname.indexOf(",") > -1) {
                        String[] op = oldpicname.split(",");
                        List<String> oplist = java.util.Arrays.asList(op);
                        ArrayList opArray = new ArrayList<>(oplist);
                        String[] pn = picname.split(",");
                        List<String> pnlist = java.util.Arrays.asList(pn);
                        ArrayList pnArray = new ArrayList<>(pnlist);
                        String[] pp = picpath.split(",");
                        List<String> pplist = java.util.Arrays.asList(pp);
                        ArrayList ppArray = new ArrayList<>(pplist);
                        int number = -1;
                        for (int i = 0; i < opArray.size(); i++) {
                            if (ppArray.get(i).toString().equals(filename)) {
                                number = i;
                                File file = new File(Globals.AppRoot + ppArray.get(i));
                                if (file.exists()) {
                                    file.delete();
                                    break;
                                }
                            }
                        }

                        /**************
                         * 修改字段
                         * picpath,picname,oldpicname
                         */
                        if (number > -1) {
                            String newoldpn = "";
                            String newpn = "";
                            String newpp = "";
                            opArray.remove(number);
                            pnArray.remove(number);
                            ppArray.remove(number);
                            newoldpn = org.apache.commons.lang.StringUtils.join(opArray.toArray(), ",");
                            newpn = org.apache.commons.lang.StringUtils.join(pnArray.toArray(), ",");
                            newpp = org.apache.commons.lang.StringUtils.join(ppArray.toArray(), ",");

                            repair.setOldimgname(newoldpn);
                            repair.setImgname(newpn);
                            repair.setImgpath(newpp);
                            eqRepairService.updateIgnoreNull(repair);
                            return Result.success("已删除");
                        }
                    } else {
                        if ((picpath).equals(filename)) {
                            File file = new File(Globals.AppRoot + picpath);
                            if (file.exists()) {
                                file.delete();
                                /**************
                                 * 修改字段
                                 * picpath,picname,oldpicname
                                 */
                                repair.setOldimgname("");
                                repair.setImgname("");
                                repair.setImgpath("");
                                eqRepairService.updateIgnoreNull(repair);
                                return Result.success("已删除");
                            }
                        }
                    }


                }
            }
            return Result.error(-1, "没有此文件");
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(-1,"system.error");
        }
    }

    @At("/insertRepair")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
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

    @At("/updateRepair")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object updateRepair(@Param("repairid") String repairid, @Param("repnum") String repnum,@Param("reptype") String reptype,@Param("eqid") String eqid,@Param("reptext") String reptext,@Param("personid") String personid,@Param("pstatus") String pstatus){
        try{
            if(!Strings.isBlank(repairid)){
                eqRepairService.updateRepair(repairid,repnum,reptype,eqid,reptext,personid,pstatus);
                return Result.success("保存成功");
            }
            return Result.error(-1,"保存失败");
        }
        catch(Exception e){
            return Result.error(-1,"保存失败");
        }
    }

    @At("/getRepairInfo")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
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
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
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
            return Result.error(2,"lockID is null");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error", e);
        }

    }

    @At("/trackRecord/?")
    @Ok("beetl:/platform/eq/repairtrack/detail.html")
    @RequiresPermissions("platform.eq.repair.select")
    public void trackRecord(String id, HttpServletRequest req) {
        if(!Strings.isBlank(id)){
            req.setAttribute("id",id);
        }else{
            req.setAttribute("id",null);
        }
    }



}
