package cn.wizzer.app.web.modules.controllers.platform.sys;

import cn.wizzer.app.sys.modules.models.*;
import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.app.base.modules.models.base_cnctobj;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.base.modules.services.BaseAirportService;
import cn.wizzer.app.sys.modules.services.*;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.base.Result;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.DateUtil;
import cn.wizzer.framework.util.PasswordStrengthCheck;
import cn.wizzer.framework.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.filter.CrossOriginFilter;
import org.nutz.mvc.impl.AdaptorErrorContext;
import org.nutz.mvc.upload.TempFile;
import org.nutz.mvc.upload.UploadAdaptor;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

/**
 * Created by wizzer on 2016/6/23.
 */
@IocBean
@At("/platform/sys/user")
public class SysUserController {
    private static final Log log = Logs.get();
    @Inject
    private SysUserService userService;
    @Inject
    private SysMenuService menuService;
    @Inject
    private SysUnitService unitService;
    @Inject
    private SysRoleService roleService;
    @Inject
    private Dao dao;
    @Inject
    private BasePersonService basePersonService;
    @Inject
    private BaseCnctobjService baseCnctobjService;
    @Inject
    private BaseAirportService baseAirportService;
    @Inject
    private SysRegistauditService sysRegistauditService;
    @Inject
    private SysUseraddService sysUseraddService;

    @Inject
    private SysUserService sysUserService;

    @At("")
    @Ok("beetl:/platform/sys/user/index.html")
    @RequiresPermissions("sys.manager.user")
    public void index() {

    }

    @At
    @Ok("beetl:/platform/sys/user/add.html")
    @RequiresPermissions("sys.manager.user")
    public Object add(@Param("unitid") String unitid) {
        return Strings.isBlank(unitid) ? null : unitService.fetch(unitid);
    }

    @At
    @Ok("json")
    @RequiresPermissions("sys.manager.user.add")
    @SLog(tag = "新建用户", msg = "用户名:${args[0].loginname}")
    public Object addDo(@Param("..") Sys_user user, HttpServletRequest req) {
        try {
            RandomNumberGenerator rng = new SecureRandomNumberGenerator();
            String salt = rng.nextBytes().toBase64();
            String hashedPasswordBase64 = new Sha256Hash(user.getPassword(), salt, 1024).toBase64();
            user.setSalt(salt);
            user.setPassword(hashedPasswordBase64);
            user.setLoginPjax(true);
            user.setLoginCount(0);
            user.setLoginAt(0);
            userService.insert(user);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/edit/?")
    @Ok("beetl:/platform/sys/user/edit.html")
    @RequiresPermissions("sys.manager.user")
    public Object edit(String id,HttpServletRequest req) {
        Sys_user user= userService.fetch(id);
        req.setAttribute("creater",userService.fetch(user.getCreater()));
        return userService.fetchLinks(user, "unit");
    }

    @At
    @Ok("json")
    @RequiresPermissions("sys.manager.user.edit")
    @SLog(tag = "修改用户", msg = "用户名:${args[1]}->${args[0].loginname}")
    public Object editDo(@Param("..") Sys_user user, @Param("oldLoginname") String oldLoginname, HttpServletRequest req) {
        try {

            user.setOpBy(StringUtil.getUid());
            user.setOpAt((int) (System.currentTimeMillis() / 1000));
            userService.updateIgnoreNull(user);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/resetPwd/?")
    @Ok("json")
    @RequiresPermissions("sys.manager.user.edit")
    @SLog(tag = "重置密码", msg = "用户名:${args[1].getAttribute('loginname')}")
    public Object resetPwd(String id, HttpServletRequest req) {
        try {
            Sys_user user = userService.fetch(id);
            RandomNumberGenerator rng = new SecureRandomNumberGenerator();
            String salt = rng.nextBytes().toBase64();
            String hashedPasswordBase64 = new Sha256Hash("888888", salt, 1024).toBase64();
            userService.update(Chain.make("salt", salt).add("password", hashedPasswordBase64), Cnd.where("id", "=", id));
            req.setAttribute("loginname", user.getLoginname());
            return Result.success("system.success", "888888");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/delete/?")
    @Ok("json")
    @RequiresPermissions("sys.manager.user.delete")
    @SLog(tag = "删除用户", msg = "用户名:${args[1].getAttribute('loginname')}")
    public Object delete(String userId, HttpServletRequest req) {
        try {
            Sys_user user = userService.fetch(userId);
            if ("superadmin".equals(user.getLoginname())) {
                return Result.error("system.not.allow");
            }
            userService.deleteById(userId);
            req.setAttribute("loginname", user.getLoginname());
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/delete")
    @Ok("json")
    @RequiresPermissions("sys.manager.user.delete")
    @SLog(tag = "批量删除用户", msg = "用户ID:${args[1].getAttribute('ids')}")
    public Object deletes(@Param("ids") String[] userIds, HttpServletRequest req) {
        try {
            Sys_user user = userService.fetch(Cnd.where("loginname", "=", "superadmin"));
            StringBuilder sb = new StringBuilder();
            for (String s : userIds) {
                if (s.equals(user.getId())) {
                    return Result.error("system.not.allow");
                }
                sb.append(s).append(",");
            }
            userService.deleteByIds(userIds);
            req.setAttribute("ids", sb.toString());
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/enable/?")
    @Ok("json")
    @RequiresPermissions("sys.manager.user.edit")
    @SLog(tag = "启用用户", msg = "用户名:${args[1].getAttribute('loginname')}")
    public Object enable(String userId, HttpServletRequest req) {
        try {
            req.setAttribute("loginname", userService.fetch(userId).getLoginname());
            userService.update(Chain.make("disabled", false), Cnd.where("id", "=", userId));
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/disable/?")
    @Ok("json")
    @RequiresPermissions("sys.manager.user.edit")
    @SLog(tag = "禁用用户", msg = "用户名:${args[1].getAttribute('loginname')}")
    public Object disable(String userId, HttpServletRequest req) {
        try {
            String loginname = userService.fetch(userId).getLoginname();
            if ("superadmin".equals(loginname)) {
                return Result.error("system.not.allow");
            }
            req.setAttribute("loginname", loginname);
            userService.update(Chain.make("disabled", true), Cnd.where("id", "=", userId));
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/sys/user/detail.html")
    @RequiresAuthentication
    public Object detail(String id,HttpServletRequest req) {
        if (!Strings.isBlank(id)) {
            Sys_user user = userService.fetch(id);
            req.setAttribute("creater",userService.fetch(user.getCreater()));
            return userService.fetchLinks(user, "roles");
        }
        return null;
    }

    @At("/menu/?")
    @Ok("beetl:/platform/sys/user/menu.html")
    @RequiresPermissions("sys.manager.user.select")
    public Object menu(String id, HttpServletRequest req) {
        Sys_user user = userService.fetch(id);
        List<Sys_menu> menus = userService.getMenusAndButtons(id);
        List<Sys_menu> datas = userService.getDatas(id);
        List<Sys_menu> firstMenus = new ArrayList<>();
        List<Sys_menu> secondMenus = new ArrayList<>();
        for (Sys_menu menu : menus) {
            for (Sys_menu bt : datas) {
                if (menu.getPath().equals(bt.getPath().substring(0, bt.getPath().length() - 4))) {
                    menu.setHasChildren(true);
                    break;
                }
            }
            if (menu.getPath().length() == 4) {
                firstMenus.add(menu);
            } else {
                secondMenus.add(menu);
            }
        }
        req.setAttribute("userFirstMenus", firstMenus);
        req.setAttribute("userSecondMenus", secondMenus);
        req.setAttribute("jsonSecondMenus", Json.toJson(secondMenus));
        return user;
    }

    @At("/role/?")
    @Ok("beetl:/platform/sys/user/role.html")
    @RequiresPermissions("sys.manager.user.select")
    public Object role(String id, HttpServletRequest req) {
        Sys_user user = userService.fetch(id);
        List<Sys_role> roles = userService.getRolesList(user);
        req.setAttribute("userRoles", roles);
        return user;
    }

    @At("/editUnit/?")
    @Ok("beetl:/platform/sys/user/editUnit.html")
    @RequiresPermissions("sys.manager.user.editUnit")
    public Object editUnit(String id, HttpServletRequest req) {
        Sys_user user = userService.fetch(id);
        return user;
    }

    @At("/unitData")
    @Ok("json")
    @RequiresPermissions("sys.manager.user.editUnit")
    public Object unitData(@Param("userid") String userid,@Param("isin") String isin, @Param("name") String name, @Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        String sql = "SELECT a.* FROM sys_unit a WHERE 1=1 ";
        if (!Strings.isBlank(name)) {
            //20180330zhf1734
            name = name.replace("'", "");
            sql += " and ( a.name like '" + name + "%' or a.aliasname like '" + name + "%') ";
        }
        if(Strings.isNotBlank(userid) && Strings.isNotBlank(isin)){
            sql += " and a.id ";
            if("yes".equals(isin)){
                sql+=" in ";
            }else{
                sql+=" not in ";
            }
            sql+=" (SELECT b.unitId FROM sys_user_unit b WHERE b.userid='" + userid + "')";
        }
        String s = sql;
        if (order != null && order.size() > 0) {
            for (DataTableOrder o : order) {
                DataTableColumn col = columns.get(o.getColumn());
                s += " order by a." + Sqls.escapeSqlFieldValue(col.getData()).toString() + " " + o.getDir();
            }
        }
        return userService.data(length, start, draw, Sqls.create(sql), Sqls.create(s));
    }

    @At("/selectUnit")
    @Ok("beetl:/platform/sys/user/selectUnit.html")
    @RequiresPermissions("sys.manager.user.editUnit")
    public void selectUnit(HttpServletRequest req) {
    }

    @At("/pushUnit")
    @Ok("json")
    @RequiresPermissions("sys.manager.user.editUnit")
    @SLog(tag = "添加组织到用户", msg = "用户名称:${args[2].getAttribute('name')},用户ID:${args[0]}")
    public Object pushUnit(@Param("unitIds") String unitIds, @Param("userid") String userid, HttpServletRequest req) {
        try {
            String[] ids = StringUtils.split(unitIds, ",");
            for (String s : ids) {
                if (!Strings.isEmpty(s)) {
                    int cn = userService.count("sys_user_unit",Cnd.where("userId","=",userid).and("unitId","=",s));
                    if(cn<=0){
                        userService.insert("sys_user_unit", org.nutz.dao.Chain.make("userId", userid).add("unitId", s));
                    }
                }
            }
            Sys_user user = userService.fetch(userid);
            req.setAttribute("name", user.getLoginname());
            return Result.success("system.success");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/delUnit")
    @Ok("json")
    @RequiresPermissions("sys.manager.user.editUnit")
    @SLog(tag = "从用户中删除组织", msg = "用户账号:${args[2].getAttribute('name')},用户ID:${args[0]}")
    public Object delUnit(@Param("unitIds") String unitIds, @Param("userid") String userid, HttpServletRequest req) {
        try {
            String[] ids = StringUtils.split(unitIds, ",");
            userService.clear("sys_user_unit", Cnd.where("unitId", "in", ids).and("userId", "=", userid));
            Sys_user user = userService.fetch(userid);
            req.setAttribute("name", user.getLoginname());
            return Result.success("system.success");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At
    @Ok("json:{locked:'password|salt',ignoreNull:false}") // 忽略password和createAt属性,忽略空属性的json输出
    @RequiresPermissions("sys.manager.user.select")
    public Object data(@Param("unitid") String unitid, @Param("loginname") String loginname, @Param("username") String username, @Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        Cnd cnd1 = Cnd.NEW();
        Cnd cnd2 = Cnd.NEW();

        //查看数据权限范围内的用户
        Subject subject = SecurityUtils.getSubject();
        Sys_user curUser = (Sys_user) subject.getPrincipal();
        String curUserId = curUser.getId();
        List<Sys_role> roles = curUser.getRoles();
        boolean adminFlag=false;
        for (int i=0;i<roles.size();i++){
            if(roles.get(i).getCode().toString().equals("sysadmin"))
            {
                adminFlag=true;
                break;
            }
        }


        if(adminFlag==false) {
            cnd1.and("userId", "=", curUser.getId());
            base_cnctobj cnctobj = baseCnctobjService.fetch(cnd1);
            if (cnctobj != null) {
                base_person person = basePersonService.fetch(cnctobj.getPersonId());
                String uid = person.getUnitid();
                Cnd c = Cnd.NEW();
                c.and("unitid", "=", uid);
                List<base_person> personList = basePersonService.query(c);
                String userids = "";
                for (int i = 0; i < personList.size(); i++) {
                    Cnd cn = Cnd.NEW();
                    cn.and("personId", "=", personList.get(i).getId());
                    base_cnctobj cnctobj1 = baseCnctobjService.fetch(cn);
                    if (cnctobj1 != null) {
                        if (i > 0)
                            userids += ",'" + cnctobj1.getUserId() + "'";
                        else
                            userids += "'" + cnctobj1.getUserId() + "'";
                    }

                }

                cnd2.and("id", "in",  userids );


            }
        }
        cnd2.and("delFlag", "=", "0");
        if (!Strings.isBlank(loginname))
            cnd2.and("loginname", "like", "%" + loginname + "%");
        if (!Strings.isBlank(username))
            cnd2.and("username", "like", "%" + username + "%");

//        if (!Strings.isBlank(unitid) && !"root".equals(unitid))
//            cnd.and("unitid", "=", unitid);
//        if (!Strings.isBlank(loginname))
//            cnd.and("loginname", "like", "%" + loginname + "%");
//        if (!Strings.isBlank(username))
//            cnd.and("username", "like", "%" + username + "%");
        return userService.data(length, start, draw, order, columns, cnd2, null);
    }

    @At
    @Ok("json")
    @RequiresPermissions("sys.manager.user.select")
    public Object tree(@Param("pid") String pid) {
        List<Sys_unit> list = unitService.query(Cnd.where("parentId", "=", Strings.sBlank(pid)).asc("path"));
        List<Map<String, Object>> tree = new ArrayList<>();
        Map<String, Object> obj = new HashMap<>();
        if (Strings.isBlank(pid)) {
            obj.put("id", "root");
            obj.put("text", "所有用户");
            obj.put("children", false);
            tree.add(obj);
        }
        for (Sys_unit unit : list) {
            obj = new HashMap<>();
            obj.put("id", unit.getId());
            obj.put("text", unit.getName());
            obj.put("children", unit.isHasChildren());
            tree.add(obj);
        }
        return tree;
    }

    @At
    @Ok("beetl:/platform/sys/user/pass.html")
    @RequiresAuthentication
    public void pass() {

    }

    @At
    @Ok("beetl:/platform/sys/user/custom.html")
    @RequiresAuthentication
    public void custom() {

    }
    @At("/registration")
    @Ok("beetl:/platform/sys/registration.html")
    public void regist() {

    }

    @At
    @Ok("beetl:/platform/sys/user/mode.html")
    @RequiresAuthentication
    public void mode() {

    }

    @At
    @Ok("json")
    @RequiresAuthentication
    public Object modeDo(@Param("mode") String mode, HttpServletRequest req) {
        try {
            userService.update(Chain.make("loginPjax", "true".equals(mode)), Cnd.where("id", "=", req.getAttribute("uid")));
            Subject subject = SecurityUtils.getSubject();
            Sys_user user = (Sys_user) subject.getPrincipal();
            if ("true".equals(mode)) {
                user.setLoginPjax(true);
            } else {
                user.setLoginPjax(false);
            }
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }


    @At
    @Ok("json")
    @RequiresAuthentication
    public Object customDo(@Param("ids") String ids, HttpServletRequest req) {
        try {
            userService.update(Chain.make("customMenu", ids), Cnd.where("id", "=",StringUtil.getUid()));
            Subject subject = SecurityUtils.getSubject();
            Sys_user user = (Sys_user) subject.getPrincipal();
            if (!Strings.isBlank(ids)) {
                user.setCustomMenu(ids);
                user.setCustomMenus(menuService.query(Cnd.where("id", "in", ids.split(","))));
            } else {
                user.setCustomMenu("");
                user.setCustomMenus(new ArrayList<>());
            }
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At
    @Ok("json")
    @RequiresAuthentication
    public Object doChangePassword(@Param("oldPassword") String oldPassword, @Param("newPassword") String newPassword, HttpServletRequest req) {
        Subject subject = SecurityUtils.getSubject();
        Sys_user user = (Sys_user) subject.getPrincipal();
        String old = new Sha256Hash(oldPassword, user.getSalt(), 1024).toBase64();
        if (old.equals(user.getPassword())) {
            RandomNumberGenerator rng = new SecureRandomNumberGenerator();
            String salt = rng.nextBytes().toBase64();
            String hashedPasswordBase64 = new Sha256Hash(newPassword, salt, 1024).toBase64();
            user.setSalt(salt);
            user.setPassword(hashedPasswordBase64);
            userService.update(Chain.make("salt", salt).add("password", hashedPasswordBase64), Cnd.where("id", "=", user.getId()));
            return Result.success("修改成功");
        } else {
            return Result.error("原密码不正确");
        }
    }

    @At("/changePasswordByMobile")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object changePasswordByMobile(@Param("userid") String userid,@Param("oldpassword") String oldpassword, @Param("newpassword") String newpassword, HttpServletRequest req) {
        try {
//        Subject subject = SecurityUtils.getSubject();
//        Sys_user user = (Sys_user) subject.getPrincipal();
            if (StringUtils.isBlank(userid) || StringUtils.isBlank(oldpassword) || StringUtils.isBlank(newpassword)) {
                return Result.error("输入参数不能为空！");
            }
            Sys_user user = userService.fetch(userid);
            if (user == null) {
                return Result.error("用户不存在！");
            }
            String old = new Sha256Hash(oldpassword, user.getSalt(), 1024).toBase64();
            if (old.equals(user.getPassword())) {
                //校验新密码强弱
//            String pwd = new String((char[])token.getCredentials());//((CaptchaToken)token).getPassword()也可得到密码,本项目采用继承UsernamePasswordToken的自定义类CaptchaToken
                String[] dictionary = new String[]{user.getLoginname(), user.getUsername()};
                int level = PasswordStrengthCheck.showPassstrength(dictionary, newpassword, 8, 20);
                if (level < 1) {
                    throw new ValidatException("新密码太弱，请重新设置新密码！");
                }

                RandomNumberGenerator rng = new SecureRandomNumberGenerator();
                String salt = rng.nextBytes().toBase64();
                String hashedPasswordBase64 = new Sha256Hash(newpassword, salt, 1024).toBase64();
                user.setSalt(salt);
                user.setPassword(hashedPasswordBase64);
                userService.update(Chain.make("salt", salt).add("password", hashedPasswordBase64), Cnd.where("id", "=", user.getId()));
                return Result.success("修改成功");
            } else {
                return Result.error("原密码不正确");
            }
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("system.error", e);
        }
    }

    @At("/checkPassword")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object checkPassword(@Param("userid") String userid,@Param("password") String password, HttpServletRequest req) {
//        Subject subject = SecurityUtils.getSubject();
//        Sys_user user = (Sys_user) subject.getPrincipal();
        try {
            if (StringUtils.isBlank(userid) || StringUtils.isBlank(password)) {
                return Result.error("输入参数不能为空！");
            }
            Sys_user user = userService.fetch(userid);
            if (user == null) {
                return Result.error("用户不存在！");
            }
            String old = new Sha256Hash(password, user.getSalt(), 1024).toBase64();
            Map map = new HashMap();
            if (old.equals(user.getPassword())) {
                map.put("isok", "1");
            } else {
                map.put("isok", "0");
            }
            return Result.success("成功", map);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("系统运行异常", e);
        }
    }

    @At("/addUser")
    @Ok("json")
    public Object addUser(@Param("airportid") String airportid,  @Param("username") String username, @Param("loginname") String loginname,@Param("password") String password,@Param("unitid") String unitid,@Param("cardid")String cardid, @Param("needAuditFlag")boolean needAuditFlag,@Param("leader")boolean leader, HttpServletRequest req) {
        try {
            if(Globals.IsRegister.equals("0")){
                throw new ValidatException("目前不允许进行用户注册！");
            }
            Sys_user user = sysUserService.addUser(airportid,username,loginname,password,unitid,cardid,needAuditFlag,leader);
            return Result.success("system.success", user);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }




    @At("/upload")
    @AdaptBy(type = UploadAdaptor.class, args = { "ioc:imageUpload" })
    @Ok("raw:json")
    public String upload(@Param("uploads")TempFile tf){
        Map<String, Object> map = new HashMap<>();
        if(tf == null || "".equals( tf )){
            map.put( "result", "上传失败!" );
            return Json.toJson( map );
        }
        try {
            System.out.println(tf.getSubmittedFileName());
            System.out.println(tf.getSize());
            String[] postfix = tf.getSubmittedFileName().split("\\.");
            String path = Globals.AppRoot+Globals.AppUploadPath+"/userPhoto/";
            File temp = new File( path );
            if(!temp.exists()){
                temp.mkdirs();
            }
            String userPhoto=R.UU32()+ "." + postfix[postfix.length - 1];
            tf.write( temp.getPath() + "\\" + userPhoto );
            System.out.println(temp.getPath());
            tf.delete();
            map.put("uPhoto",userPhoto);
            map.put( "result", "上传成功!" );
        }
        catch( Exception e ) {
            e.printStackTrace();
            map.put( "result", "上传失败!" );
        }
        return Json.toJson( map );
    }

    @At("/uploadUserImage")
    @Ok("json")
    //AdaptorErrorContext必须是最后一个参数
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object uploadUserImage(@Param("filename") String filename, @Param("base64") String base64,@Param("userid") String userid,HttpServletRequest req, AdaptorErrorContext err) {
        byte[] buffer=null;
        try {
            if (StringUtils.isBlank(base64)) {
                return Result.error("传入文件不能为空！");
            }else if (err != null && err.getAdaptorErr() != null) {
                return Result.error("传入文件不能为空！");
            } else if (StringUtils.isBlank(userid)) {
                return Result.error("传入用户信息不能为空!");
            } else {
                Sys_user sysUser = userService.fetch(userid);
                if(sysUser==null){
                    return Result.error("系统未找到相关用户信息!");
                }
                String fn= R.UU32()+filename.substring(filename.lastIndexOf("."));
                String path = Globals.AppUploadPath+"/userImage/";
                String pathfile = Globals.AppUploadPath+"/userImage/" + fn ;
                File file=new File(Globals.AppRoot+path);
                if(!file.exists()){
                    file.mkdirs();
                }
                if(base64.indexOf(",")>=0){//兼容H5
                    buffer = Base64.getDecoder().decode(base64.split(",")[1]);
                }else{
                    buffer = Base64.getDecoder().decode(base64);
                }
                FileOutputStream out = new FileOutputStream(Globals.AppRoot+pathfile);
                out.write(buffer);
                out.close();
                //将上传的文件修改对应用户照片名称
                if(StringUtils.isNotBlank(sysUser.getUserImage())){
                    String oldfile = Globals.AppRoot+path+sysUser.getUserImage();
                    File file0=new File(oldfile);
                    if(file0.exists()){
                        file0.delete();
                    }
                }
                sysUser.setUserImage(fn);
//                userService.updateIgnoreNull(sysUser);
                userService.update(Chain.make("userImage", fn), Cnd.where("id", "=", sysUser.getId()));
                return Result.success("上传成功","{\"id\":\""+userid+"\",\"path\":\""+(Globals.AppBase+pathfile)+"\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        } catch (Throwable e) {
            e.printStackTrace();
            return Result.error("图片错误:"+e.getMessage());
        }
    }

    @At("/getUserImage")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class),@By(type = TokenFilter.class)})
    public Object getUserImage(@Param("userid") String userid,HttpServletRequest req) {
        byte[] buffer=null;
        try {
            if (StringUtils.isBlank(userid)) {
                return Result.error("传入用户信息不能为空!");
            } else {
                Sys_user sysUser = userService.fetch(userid);
                if(sysUser==null){
                    return Result.error("系统未找到相关用户信息!");
                }
                String path = Globals.AppUploadPath+"/userImage/";
                Map map = new HashMap();
                map.put("userid",userid);
                map.put("path",path+sysUser.getUserImage());
                return Result.success("成功",map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/checkLogin")
    @Ok("json")
    public Object checkLoginname(@Param("loginname") String loginname){
        try{
            Cnd cnd=Cnd.NEW();
            cnd.and("loginname","=",loginname);
            cnd.and("delFlag","=",0);
            Sys_user user=userService.fetch(cnd);
            if(user==null){
                return "OK";
            }
            else{
                return "fail";
            }
        }catch (Exception e){
            return "error";
        }
    }

    @At("/getUsername")
    @Ok("json")
    @RequiresAuthentication
    public Object getUsername(@Param("userid") String userid){
        try{
            if(!Strings.isBlank(userid)){
                Sys_user user = userService.fetch(userid);
                return user.getUsername();
            }
            return null;
        }catch(Exception e){
            return  null;
        }
    }

    @At("/getUserunit")
    @Ok("json")
    @RequiresAuthentication
    public Object getUserunit(@Param("userid") String userid){
        try{
            if(!Strings.isBlank(userid)){
                Sql sql = Sqls.queryRecord("select b.personname,c.`name` name from base_cnctobj a left join " +
                        " base_person b on a.personId=b.id left JOIN " +
                        " sys_unit c on b.unitid=c.id where a.userid='"+userid+"' ");
                List<base_cnctobj> cnctobjs = baseCnctobjService.dao().execute(sql).getList(base_cnctobj.class);
                return  cnctobjs;
//                System.out.println(sql);
//                dao.execute(sql);
//                List<Record> res = sql.getList(Record.class);
//                return res;
            }
            return null;
        }catch(Exception e){
            return  null;
        }
    }

    @At("/getCurrentUser")
    @Ok("json")
    @RequiresAuthentication
    public Object getCurrentUser(){
        Subject currentUser = SecurityUtils.getSubject();
        Sys_user user = (Sys_user) currentUser.getPrincipal();
        return user;
    }

    //2080423zhf1217
    @At("/addUserByMobile")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class)})
    public Object addUserByMobile(@Param("airportid") String airportid,  @Param("username") String username, @Param("loginname") String loginname,@Param("password") String password,@Param("unitid") String unitid,@Param("cardid")String cardid,@Param("sex")int sex,@Param("pictureads") String pictureads,@Param("tel") String tel,@Param("deptid")String  deptid,@Param("jobs") String jobs,HttpServletRequest req) {
        try{
            if(Globals.IsRegister.equals("0")){
                return Result.error("目前不允许进行用户注册！");
            }
            if(Strings.isBlank(airportid)){
                return Result.error("未选择所属机场");
            }
            if(Strings.isBlank(username)){
                return Result.error("未填写真实姓名");
            }
            if(Strings.isBlank(loginname)){
                return Result.error("未填写用户名");
            }
            if(Strings.isBlank(loginname)){
                return Result.error("未填写用户名");
            }
            if(Strings.isBlank(password)){
                return Result.error("未填写注册密码");
            }
            if(Strings.isBlank(unitid)){
                return Result.error("未选择所属单位");
            }
            if(Strings.isBlank(tel)){
                return Result.error("未填写电话");
            }
            Sys_user sys_user = userService.fetch(Cnd.where("loginname", "=", loginname).and("delFlag","=",0));
            if(sys_user !=null){
                return Result.error(3,"该用户已被使用");
            }
            List<base_person> basePersonList = basePersonService.query(Cnd.where("cardid", "=", cardid).and("airportid", "=", airportid));
            if(basePersonList.size() > 0 ){
                int count = baseCnctobjService.count(Cnd.where("personId", "=", basePersonList.get(0).getId()));
                if(count > 0){
                    //该证件号已经注册过
                    return Result.error(2,"该员工已经注册过");
                }
            }
            return  Result.success("system.success",sysUserService.addUserByMobile(airportid, username, loginname,password,unitid,cardid,sex,pictureads,tel,deptid,jobs));
        } catch (Exception e) {
            e.printStackTrace();

            return Result.error("system.error",e);
        }

    }




    @At("/uploadCardImage")
    @Ok("json")
    //AdaptorErrorContext必须是最后一个参数
    @Filters({@By(type=CrossOriginFilter.class)})
    public Object uploadCardImage(@Param("filename") String filename, @Param("base64") String base64,@Param("userid") String userid,HttpServletRequest req, AdaptorErrorContext err) {
        byte[] buffer=null;
        try {
            if (StringUtils.isBlank(base64)) {
                return Result.error("传入文件不能为空！");
            }else if (err != null && err.getAdaptorErr() != null) {
                return Result.error("传入文件不能为空！");
            } else if (StringUtils.isBlank(userid)) {
                return Result.error("传入用户信息不能为空!");
            } else {
                Sys_user sysUser = userService.fetch(userid);
                if(sysUser==null){
                    return Result.error("系统未找到相关用户信息!");
                }
                String fn= R.UU32()+filename.substring(filename.lastIndexOf("."));
                String path = Globals.AppUploadPath+"/userCard/";
                String pathfile = Globals.AppUploadPath+"/userCard/" + fn ;
                File file=new File(Globals.AppRoot+path);
                if(!file.exists()){
                    file.mkdirs();
                }
                if(base64.indexOf(",")>=0){//兼容H5
                    buffer = Base64.getDecoder().decode(base64.split(",")[1]);
                }else{
                    buffer = Base64.getDecoder().decode(base64);
                }
                FileOutputStream out = new FileOutputStream(Globals.AppRoot+pathfile);
                out.write(buffer);
                out.close();
                //将上传的文件修改对应用户照片名称
                Cnd c=Cnd.NEW();
                c.and("userid","=",sysUser.getId());
                Sys_useradd useradd = sysUseraddService.fetch(c);
                String picPath=useradd.getPictureads();
                if(!Strings.isBlank(picPath)){
                    picPath+=","+pathfile;
                }else{
                    picPath=pathfile;
                }
                useradd.setPictureads(picPath);
                sysUseraddService.updateIgnoreNull(useradd);
                return Result.success("上传成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        } catch (Throwable e) {
            e.printStackTrace();
            return Result.error("图片错误:"+e.getMessage());
        }
    }
    //20180424zhf1814
    @At("/checkLoginname")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class)})
    public Object checkLoginnameByMobile(@Param("loginname") String loginname){
        try{
            Cnd cnd=Cnd.NEW();
            Map map = new HashMap();
            cnd.and("loginname","=",loginname);
            cnd.and("delFlag","=",0);
            Sys_user user=userService.fetch(cnd);
            if(user==null){
                map.put("result","OK");
            }
            else{
                map.put("result","fail");
            }
            return Result.success("system.success",map);
        }catch (Exception e){
            return Result.error("system.error",e);
        }
    }
    //20180424zhf1814
    @At("/checkPersonByCarid")
    @Ok("json")
    @Filters({@By(type=CrossOriginFilter.class)})
    public Object checkPersonByCarid(@Param("carid") String carid,@Param("airportid")String airportid){
        try{
            Cnd cnd=Cnd.NEW();
            Map map = new HashMap();
            /*List<base_person> basePersonList = basePersonService.query(Cnd.where("cardid", "=", cardid).and("airportid", "=", airportid));*/
            cnd.and("carid","=",carid);
            cnd.and("airportid","=",airportid);
            List<base_person> basePersonList = basePersonService.query(cnd);
            if(basePersonList.size() == 0 ){
                map.put("result","OK");
            }
            else{
                map.put("result","fail");
            }
            return Result.success("system.success",map);
        }catch (Exception e){
            return Result.error("system.error",e);
        }
    }


}
