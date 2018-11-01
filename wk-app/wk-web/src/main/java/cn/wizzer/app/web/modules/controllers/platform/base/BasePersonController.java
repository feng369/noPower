package cn.wizzer.app.web.modules.controllers.platform.base;

import cn.wizzer.app.base.modules.models.base_cnctobj;
import cn.wizzer.app.base.modules.services.*;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.app.sys.modules.services.SysUnitService;
import cn.wizzer.app.sys.modules.services.SysUserService;
import cn.wizzer.app.sys.modules.services.SysUseraddService;
import cn.wizzer.app.sys.modules.services.SysRoleService;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.base.modules.models.base_person;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.upload.TempFile;
import org.nutz.mvc.upload.UploadAdaptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@IocBean
@At("/platform/base/person")
public class BasePersonController {
    private static final Log log = Logs.get();
    @Inject
    private BasePersonService basePersonService;

    @Inject
    private BasePostService basePostService;

    @Inject
    private BaseJobService baseJobService;

    @Inject
    private SysUnitService unitService;

    @Inject
    private BaseDeptService baseDeptService;

    @Inject
    private BaseAirportService baseAirportService;

    @Inject
    private SysUserService sysUserService;
    @Inject
    private BasePlaceService basePlaceService;

    @Inject
    private BaseCnctobjService baseCnctobjService;
    @Inject
    private BaseCustomerService baseCustomerService;
    @Inject
    private SysUseraddService sysUseraddService;
    @Inject
    private SysRoleService sysRoleService;


    @At("")
    @Ok("beetl:/platform/base/person/index.html")
    @RequiresPermissions("platform.base.person")
    public void index() {
    }

    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.base.person.select")
    public Object data(@Param("selectForm") String selectForm, @Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        Cnd cnd = Cnd.NEW();
        if (selectForm != null) {
            base_person select = Json.fromJson(base_person.class, selectForm);
            if (!Strings.isBlank(select.getPersonnum()))
                cnd.and("personnum", "like", select.getPersonnum() + "%");
            if (!Strings.isBlank(select.getPersonname()))
                cnd.and("personname", "like", select.getPersonname() + "%");
            if (!Strings.isBlank(select.getUnitid())){
                cnd.and("unitid", "=", select.getUnitid());
            }
            if (!Strings.isBlank(select.getCardid())){
                cnd.and("cardid", "like", select.getCardid()+"%");
            }
            if (!Strings.isBlank(select.getTel())){
                cnd.and("tel", "like", select.getTel()+"%");
            }
        }

        cnd = sysRoleService.getPermission(cnd,"unitid","","creater",true);
        return basePersonService.data(length, start, draw, order, columns, cnd, "unit|base_airport|base_dept|base_post|base_job|base_place");
    }

    @At("/add")
    @Ok("beetl:/platform/base/person/add.html")
    @RequiresPermissions("platform.base.person")
    public void add() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.base.person.add")
    @SLog(tag = "base_person", msg = "${args[0].id}")
    public Object addDo(@Param("..") base_person basePerson, HttpServletRequest req) {
        try {
            Cnd cnd = Cnd.NEW();
            cnd.and("airportid","=",basePerson.getAirportid());
            if(Strings.isNotBlank(basePerson.getCardid())){
                Cnd cnd1 = cnd;
                cnd1.and("cardid","=",basePerson.getCardid());
                List<base_person>list = basePersonService.query("id",cnd1);
                if(list.size()>0){
                    throw new ValidatException("该员工证件号已经存在!");
                }
            }else{
                throw new ValidatException("该员工证件号不能为空!");
            }
            if(Strings.isNotBlank(basePerson.getPersonnum())){
                Cnd cnd2 = cnd;
                cnd2.and("personnum", "=", basePerson.getPersonnum());
                int c = basePersonService.count("base_person", cnd2);
                if(c>0){
                    throw new ValidatException("该员工编号已经存在!");
                }
            }else{
                throw new ValidatException("该员工编号不能为空!");
            }
            if(Strings.isNotBlank(basePerson.getTel())){
                Cnd cnd3 = cnd;
                cnd3.and("tel", "=", basePerson.getTel());
                int c = basePersonService.count("base_person",cnd3);
                if(c>0){
                    throw new ValidatException("该员工手机号码已经存在!");
                }
            }else{
                throw new ValidatException("该员工手机号码不能为空!");
            }
            basePersonService.insert(basePerson);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/edit/?")
    @Ok("beetl:/platform/base/person/edit.html")
    @RequiresPermissions("platform.base.person")
    public void edit(String id, HttpServletRequest req) {
        this.getInfo(id, req);
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.base.person.edit")
    @SLog(tag = "base_person", msg = "${args[0].id}")
    public Object editDo(@Param("..") base_person basePerson, HttpServletRequest req) {
        try {
            Cnd cnd = Cnd.NEW();
            cnd.and("airportid","=",basePerson.getAirportid());
            cnd.and("id","!=",basePerson.getId());
            if(Strings.isNotBlank(basePerson.getCardid())){
                Cnd cnd1 = cnd;
                cnd1.and("cardid","=",basePerson.getCardid());
                List<base_person>list = basePersonService.query("id",cnd1);
                if(list.size()>0){
                    throw new ValidatException("该员工证件号已经存在!");
                }
            }else{
                throw new ValidatException("该员工证件号不能为空!");
            }
            if(Strings.isNotBlank(basePerson.getPersonnum())){
                Cnd cnd2 = cnd;
                cnd2.and("personnum", "=", basePerson.getPersonnum());
               int c = basePersonService.count("base_person",cnd2);
               if(c>0){
                   throw new ValidatException("该员工编号已经存在!");
               }
            }else{
                throw new ValidatException("该员工编号不能为空!");
            }
            if(Strings.isNotBlank(basePerson.getTel())){
                Cnd cnd3 = cnd;
                cnd3.and("tel", "=", basePerson.getTel());
                int c = basePersonService.count("base_person",cnd3);
                if(c>0){
                    throw new ValidatException("该员工手机号码已经存在!");
                }
            }else{
                throw new ValidatException("该员工手机号码不能为空!");
            }

            basePerson.setOpBy(StringUtil.getUid());
            basePerson.setOpAt((int) (System.currentTimeMillis() / 1000));
            basePersonService.updateIgnoreNull(basePerson);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/bindWxuser")
    @Ok("json")
    @RequiresPermissions("platform.base.person.bindWxuser")
    public Object bindWxuser(@Param("id")String id,@Param("wxuserid")String wxuserid,HttpServletRequest req) {
        try {
            if(StringUtils.isNotBlank(id) && StringUtils.isNotBlank(wxuserid)){
                basePersonService.bindWxuser(id,wxuserid);
                return Result.success("system.success");
            }
            throw new ValidatException("请求参数不合法!");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/autoBindWxuser")
    @Ok("json")
    @RequiresPermissions("platform.base.person.bindWxuser")
    public Object autoBindWxuser(HttpServletRequest req) {
        try {
            basePersonService.autoBindWxuser();
            return Result.success("system.success");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.base.person.delete")
    @SLog(tag = "base_person", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids") String[] ids, HttpServletRequest req) {
        try {
            if (ids != null && ids.length > 0) {
                basePersonService.delete(ids);
                req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
            } else {
                basePersonService.delete(id);
                req.setAttribute("id", id);
            }
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/base/person/detail.html")
    @RequiresAuthentication
    public void detail(String id, HttpServletRequest req) {
        this.getInfo(id, req);
    }

    @At
    @Ok("beetl:/platform/base/person/selectAirport.html")
    @RequiresPermissions("platform.base.person.select")
    public void getInfo(String id, HttpServletRequest req) {
        base_person person = basePersonService.fetch(id);
        req.setAttribute("obj", basePersonService.fetch(id));
        if (person.getUnitid() != null) {
            req.setAttribute("unit", unitService.fetch(person.getUnitid()));
        } else {
            req.setAttribute("unit", null);
        }
        if (person.getPostid() != null) {
            req.setAttribute("post", basePostService.fetch(person.getPostid()));
        } else {
            req.setAttribute("post", null);
        }
        if (person.getDeptid() != null) {
            req.setAttribute("dept", baseDeptService.fetch(person.getDeptid()));
        } else {
            req.setAttribute("dept", null);
        }

        if (person.getJobid() != null) {
            req.setAttribute("job", baseJobService.fetch(person.getJobid()));
        } else {
            req.setAttribute("job", null);
        }
        if (person.getAirportid() != null) {
            req.setAttribute("airport", baseAirportService.fetch(person.getAirportid()));
        } else {
            req.setAttribute("airport", null);
        }
        if (person.getPlaceid() != null) {
            req.setAttribute("place", basePlaceService.fetch(person.getPlaceid()));
        } else {
            req.setAttribute("place", null);
        }
        if (person.getCustomerId() != null) {
            req.setAttribute("customer", baseCustomerService.fetch(person.getCustomerId()));
        } else {
            req.setAttribute("customer", null);
        }
        req.setAttribute("creater", sysUserService.fetch(person.getCreater()));
        req.setAttribute("opby", sysUserService.fetch(person.getOpBy()));
    }

    @At
    @Ok("beetl:/platform/base/person/selectAirport.html")
    @RequiresPermissions("platform.base.person.select")
    public void selectAirport(HttpServletRequest req) {

    }
//    @At
//    @Ok("beetl:/platform/sys/role/selectUser.html")
//    @RequiresPermissions("sys.manager.role")
//    public void selectAirport(HttpServletRequest req) {
//
//    }


    @At("/selectData")
    @Ok("json:full")
    @RequiresPermissions("platform.base.person.select")
    public Object selectData(@Param("name") String name, @Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        String sql = "select a.*,b.`name` as unitname,c.airportname,d.deptname,e.jobname,f.postname from base_person a left join ";
        sql += " sys_unit b on a.unitid=b.id left JOIN ";
        sql += " base_airport c on a.airportid=c.id left join ";
        sql += " base_dept d on a.deptid=d.id left JOIN ";
        sql += " base_job e on a.jobid=e.id left JOIN ";
        sql += " base_post f on a.postid=f.id ";
        //20180330zhf1734
        name = name.replace("'", "");
        if (!Strings.isBlank(name)) {
            sql += " WHERE  a.personname LIKE '%" + name + "%' OR b.name Like '%" + name + "%'";
        }
        String s = sql;
        if (order != null && order.size() > 0) {
            for (DataTableOrder o : order) {
                DataTableColumn col = columns.get(o.getColumn());
                s += " order by " + Sqls.escapeSqlFieldValue(col.getData()).toString() + " " + o.getDir();
            }
        }
        return basePersonService.data(length, start, draw, Sqls.create(sql), Sqls.create(s));
    }

    @At("/autocomplete")
    @Ok("json")
    @RequiresPermissions("platform.base.person.select")
    public Object autocomplete(@Param("personnum") String personnum) {
        Cnd cnd = Cnd.NEW();
        List<base_person> List;
        if (!Strings.isBlank(personnum))
            cnd.and("personnum", "like", "%" + personnum + "%");
        int count = basePersonService.count(cnd);
        if (count == 0) {
            List = null;
        } else {
            List = basePersonService.query(cnd, "unit|base_airport|base_dept|base_job|base_post");
        }
        return List;
    }

    @At("/getPersonbyM")
    @Ok("jsonp:full")
    public Object getPersonbyM(@Param("airportid") String airportid) {
        try {
            if (!Strings.isBlank(airportid)) {
                return basePersonService.query(Cnd.where("airportid", "=", airportid));
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }

    //20180320zhf1117
    @At("/getCountByPersonnum")
    @Ok("json:full")
    @RequiresPermissions("platform.base.person")
    public Object getCountByPersonnum(@Param("personnum")String personnum,@Param("id")String id) {
        Cnd cnd = Cnd.NEW();
        cnd.and("personnum", "=", personnum);
        if(Strings.isNotBlank(id)){
            cnd.and("id", "=", id);
        }
        return basePersonService.count("base_person", cnd);

    }
    //20180320zhf1117
    @At("/getCountByPersonName")
    @Ok("json:full")
    public Object getCountByPersonName(String personname) {
        personname = personname.replace(" ","");
        return basePersonService.count("base_person", Cnd.where("personname", "=", personname));

    }

    //20180320zhf1117
    @At("/judgeLeaderByPersonNameAndCardId")
    @Ok("json:full")
    public Object judgeLeaderByPersonNameAndCardId(String personname, String cardid, String unitid) {
        return basePersonService.judgeLeaderByPersonNameAndCardId(personname, cardid, unitid);
    }
    /**
     * 20180419zhf1428
     * 上传Excel文件
     */
    @At("/uploadFile")
    @AdaptBy(type = UploadAdaptor.class, args = { "ioc:myUpload" })
    @Ok("raw:json")
    @RequiresPermissions("platform.base.person.import")
    public Object uploadFile(@Param("uploads") TempFile tf){
        return basePersonService.uploadFile(tf);
    }
    /**
     * 20180419zhf1428
     * 下载错误信息文档
     */
    @At("/downText")
    @Ok("raw")
    @RequiresPermissions("platform.base.person.import")
    public HttpServletResponse  downText(@Param("path") String path, HttpServletResponse response){
        try{
            //取得文件
            File file = new File(path);
            //文件名称
            String fileName  = file.getName();
            //取得文件的后缀名
            //以流的形式下载文件
            //读进来
            InputStream fis = new BufferedInputStream(new FileInputStream(path));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes()));
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }

    @At("/createuser")
    @Ok("json")
    @RequiresPermissions("platform.base.person.createuser")
    public Object createuser(@Param("loginType") String loginType,@Param("roleId")String roleId, @Param("ids") String[] ids, HttpServletRequest req) {
        try {
            Map map = basePersonService.createuser(loginType, roleId, ids);
            if(map==null||map.keySet().size()==0) {
                return Result.success("system.success");
            }else{
//                if(map.containsKey("cnctobj")){
//                    List<String>cncObjList = (List<String>) map.get("cnctobj");
//                }
//                if(map.containsKey("user")){
//                    List<String>cncObjList = (List<String>) map.get("user");
//                }
                return new Result(2, "system.success", map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/checkToUser")
    @Ok("json")
    @RequiresPermissions("platform.base.person.createuser")
    public Object checkToUser(@Param("loginType") String loginType, @Param("ids") String[] ids, HttpServletRequest req) {
        try {
            List<base_person>objList =  basePersonService.query(Cnd.where("id","in",ids));
            List<String> isNull = new ArrayList<String>();//账号字段未空的
            Map<String,base_person>allPersonMap = new HashMap<String,base_person>();
            for(base_person person:objList){
                allPersonMap.put(person.getId(),person);
                if("phone".equals(loginType) && Strings.isBlank(person.getTel())){
                    isNull.add(person.getPersonnum());
                }else if("personNum".equals(loginType) && Strings.isBlank(person.getPersonnum())){
                    isNull.add(person.getPersonnum());
                }
            }
            //已经存在联系对象
            List<base_cnctobj>objList2 =  baseCnctobjService.query(Cnd.where("personId","in",ids));
            List<String> isExist = new ArrayList<String>();
            if(objList2.size()>0){
                for(base_cnctobj obj:objList2){
                    base_person person = allPersonMap.get(obj.getPersonId());
                    isExist.add(person.getPersonnum());
                }
            }

            Map map = new HashMap();
            if(isNull.size()>0){
                map.put("isnull",isNull);
            }
            if(isExist.size()>0){
                map.put("isexist",isExist);
            }
            if(map.keySet().size()==0) {
                return Result.success("system.success");
            }else{
                return new Result(2, "system.success", map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }

    }

}
