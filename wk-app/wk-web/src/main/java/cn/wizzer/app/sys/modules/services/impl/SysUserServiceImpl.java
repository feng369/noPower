package cn.wizzer.app.sys.modules.services.impl;

import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.app.base.modules.models.base_cnctobj;
import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.app.base.modules.services.BaseAirportService;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.msg.modules.services.MsgAssignService;
import cn.wizzer.app.sys.modules.models.*;
import cn.wizzer.app.sys.modules.services.*;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.framework.base.Result;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.framework.util.StringUtil;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import java.util.*;

/**
 * Created by wizzer on 2016/12/22.
 */
@IocBean(args = {"refer:dao"})
public class SysUserServiceImpl extends BaseServiceImpl<Sys_user> implements SysUserService {
    private static final Log log = Logs.get();
    public SysUserServiceImpl(Dao dao) {
        super(dao);
    }
    @Inject
    private SysMenuService sysMenuService;
    @Inject
    private SysUseraddService sysUseraddService;
    @Inject
    private BasePersonService basePersonService;
    @Inject
    private BaseCnctobjService baseCnctobjService;
    @Inject
    private SysRegistauditService sysRegistauditService;
    @Inject
    private MsgAssignService msgAssignService;
    @Inject
    private BaseAirportService baseAirportService;
    @Inject
    private SysUnitService unitService;
    @Inject
    private SysRoleService sysRoleService;

    @Inject
    private  SysWxService sysWxService;
    /**
     * 查询用户角色code列表
     *
     * @param user
     * @return
     */
    public List<String> getRoleCodeList(Sys_user user) {
        dao().fetchLinks(user, "roles");
        List<String> roleNameList = new ArrayList<String>();
        for (Sys_role role : user.getRoles()) {
            if (!role.isDisabled())
                roleNameList.add(role.getCode());
        }
        return roleNameList;
    }

    /**
     * 查询用户角色列表
     *
     * @param user
     * @return
     */
    public List<Sys_role> getRolesList(Sys_user user) {
        List<Sys_role> rolesList = new ArrayList<Sys_role>();
        dao().fetchLinks(user, "roles");
        for (Sys_role role : user.getRoles()) {
            if (!role.isDisabled()) {
                dao().fetchLinks(role, "unit");
                rolesList.add(role);
            }
        }
        return rolesList;
    }


    /**
     * 获取用户菜单
     * @param user
     */
    public void fillMenu(Sys_user user) {
        user.setMenus(getMenus(user.getId()));
        //计算左侧菜单
        List<Sys_menu> firstMenus = new ArrayList<>();
        Map<String, List<Sys_menu>> secondMenus = new HashMap<>();
        for (Sys_menu menu : user.getMenus()) {
            if (menu.getPath().length() > 4) {
                List<Sys_menu> s = secondMenus.get(StringUtil.getParentId(menu.getPath()));
                if (s == null) s = new ArrayList<>();
                s.add(menu);
                secondMenus.put(StringUtil.getParentId(menu.getPath()), s);
            } else if (menu.getPath().length() == 4) {
                firstMenus.add(menu);
            }
        }
        user.setFirstMenus(firstMenus);
        user.setSecondMenus(secondMenus);
        if (!Strings.isBlank(user.getCustomMenu())) {
            user.setCustomMenus(sysMenuService.query(Cnd.where("id", "in", user.getCustomMenu().split(","))));
        }
    }

    /**
     * 查询用户菜单权限
     *
     * @param userId
     * @return
     */
    public List<Sys_menu> getMenus(String userId) {
        Sql sql = Sqls.create("select distinct a.* from sys_menu a,sys_role_menu b where a.id=b.menuId and " +
                " b.roleId in(select c.roleId from sys_user_role c,sys_role d where c.roleId=d.id and c.userId=@userId and d.disabled=@f) and a.disabled=@f and a.isShow=@t and a.type='menu' order by a.location ASC,a.path asc");
        sql.params().set("userId", userId);
        sql.params().set("f",false);
        sql.params().set("t",true);
        Entity<Sys_menu> entity = dao().getEntity(Sys_menu.class);
        sql.setEntity(entity);
        sql.setCallback(Sqls.callback.entities());
        dao().execute(sql);
        return sql.getList(Sys_menu.class);
    }

    /**
     * 查询用户菜单和按钮权限
     *
     * @param userId
     * @return
     */
    public List<Sys_menu> getMenusAndButtons(String userId) {
        Sql sql = Sqls.create("select distinct a.* from sys_menu a,sys_role_menu b where a.id=b.menuId and " +
                " b.roleId in(select c.roleId from sys_user_role c,sys_role d where c.roleId=d.id and c.userId=@userId and d.disabled=@f) and a.disabled=@f order by a.location ASC,a.path asc");
        sql.params().set("userId", userId);
        sql.params().set("f",false);
        Entity<Sys_menu> entity = dao().getEntity(Sys_menu.class);
        sql.setEntity(entity);
        sql.setCallback(Sqls.callback.entities());
        dao().execute(sql);
        return sql.getList(Sys_menu.class);
    }

    /**
     * 查询用户按钮权限
     *
     * @param userId
     * @return
     */
    public List<Sys_menu> getDatas(String userId) {
        Sql sql = Sqls.create("select distinct a.* from sys_menu a,sys_role_menu b where a.id=b.menuId  and " +
                " b.roleId in(select c.roleId from sys_user_role c,sys_role d where c.roleId=d.id and c.userId=@userId and d.disabled=@f) and a.disabled=@f and a.type='data' order by a.location ASC,a.path asc");
        sql.params().set("userId", userId);
        sql.params().set("f",false);
        Entity<Sys_menu> entity = dao().getEntity(Sys_menu.class);
        sql.setEntity(entity);
        sql.setCallback(Sqls.callback.entities());
        dao().execute(sql);
        return sql.getList(Sys_menu.class);
    }

    /**
     * 删除一个用户
     *
     * @param userId
     */
    @Aop(TransAop.READ_COMMITTED)
    public void deleteById(String userId) {
        String refValue = this.getEntityClass().getSimpleName();
        if(judgeRelervanceBeforeBatchDelete(refValue,new String[]{userId})) {
            dao().clear("sys_user_unit", Cnd.where("userId", "=", userId));
            dao().clear("sys_user_role", Cnd.where("userId", "=", userId));
            dao().clear("sys_user", Cnd.where("id", "=", userId));
        }
    }

    /**
     * 批量删除用户
     *
     * @param userIds
     */
    @Aop(TransAop.READ_COMMITTED)
    public void deleteByIds(String[] userIds) {
        String refValue = this.getEntityClass().getSimpleName();
        if(judgeRelervanceBeforeBatchDelete(refValue,userIds)) {
            dao().clear("sys_user_unit", Cnd.where("userId", "in", userIds));
            dao().clear("sys_user_role", Cnd.where("userId", "in", userIds));
            dao().clear("sys_user", Cnd.where("id", "in", userIds));
        }
    }

    @Aop(TransAop.READ_COMMITTED)
    public Map addUserByMobile(String airportid, String username, String loginname, String password, String unitid, String cardid, int sex, String pictureads, String tel, String deptid, String jobs) {

        Sys_user user=new Sys_user();
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        String salt = rng.nextBytes().toBase64();
        String hashedPasswordBase64 = new Sha256Hash(password, salt, 1024).toBase64();
        user.setLoginname(loginname);
        user.setUsername(username);
        user.setUnitid(unitid);
        user.setSalt(salt);
        user.setPassword(hashedPasswordBase64);
        user.setLoginPjax(true);
        user.setLoginCount(0);
        user.setLoginAt(0);
        //手机端注册必须
        user.setDisabled(true);
        Sys_user u = this.insert(user);

        //将unitid分配到sys_user_unit中

        this.insert("sys_user_unit", Chain.make("userId", u.getId()).add("unitId", unitid));


        //生成sys_useradd
        Sys_useradd useradd = new Sys_useradd();
        useradd.setUserid(user.getId());
        if (!Strings.isBlank(cardid)) {
            useradd.setCardid(cardid);
        }
        if (!Strings.isBlank(jobs)) {
            useradd.setJobs(jobs);
        }
        if (!Strings.isBlank(tel)) {
            useradd.setTel(tel);
        }
        useradd.setSex(sex);
        if(!Strings.isBlank(airportid)){
            useradd.setAirportid(airportid);
        }
        if(!Strings.isBlank(unitid)){
            useradd.setUnitid(unitid);
        }
        if(!Strings.isBlank(deptid)){
            useradd.setDeptid(deptid);
        }
        useradd.setNeedAudit(true);
        sysUseraddService.insert(useradd);
        //生成一条sys_registaudit对象
        Sys_registaudit sysRegistaudit = new Sys_registaudit();
        //id
        sysRegistaudit.setId(u.getId());
        //手机注册
        sysRegistaudit.setWay(0);
        sysRegistauditService.insert(sysRegistaudit);

        Map map = new HashMap();
        map.put("userId",u.getId());


        //系统内按角色发送，企业微信只能按用户发送
        Map<String,List<String>> roleAndUser = sysRoleService.getUsersByRole(null,null,"系统管理员");//(目前只有一个系统管理员,不分单位)
        Set<String> userIds = new HashSet<String>();
        //发送待办任务20180421
        for(Map.Entry<String,List<String>> entry:roleAndUser.entrySet()){
            String roleId = entry.getKey();
            List<String> userIdList = entry.getValue();
            userIds.addAll(userIdList);
            msgAssignService.addAssignToRole("用户注册",sysRegistaudit.getId(),"您收到用户["+loginname+"]的注册信息，请立即处理!",null,"1",roleId,userIdList,user.getId(),"/platform/sys/registaudit/phoneaudit/"+sysRegistaudit.getId()
                    ,"/platform/sys/registaudit/phonedetail/"+sysRegistaudit.getId(),"cn.wizzer.app.sys.modules.services.impl.SysRegistauditServiceImpl");
        }


        //发送企业微信消息给设备管理员角色的用户
        if("1".equals(Globals.WxCorpStart)) {
            if (userIds.size() > 0) {
       /*         Cnd cnd = Cnd.NEW();
                cnd.and("userId", "=", personid);
                String npersonid = baseCnctobjService.fetch(cnd).getPersonId();
                base_person basePerson = basePersonService.fetch(npersonid);*/
                StringBuffer content = new StringBuffer("");
                content.append("收到["+loginname+"]的注册信息,请及时处理!");

                log.debug("注册信息发送企业微信号消息,userIds="+userIds);
                sysWxService.sendWxMessageAsy(userIds, content.toString());
            }
        }
        return map;
    }

    @Override
    @Aop(TransAop.READ_COMMITTED)
    public Sys_user addUser(String airportid, String username, String loginname, String password, String unitid, String cardid, boolean needAuditFlag, boolean leader) {
        Sys_user user=new Sys_user();
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        String salt = rng.nextBytes().toBase64();
        String hashedPasswordBase64 = new Sha256Hash(password, salt, 1024).toBase64();
        user.setLoginname(loginname);
        user.setUsername(username);
        user.setUnitid(unitid);
        user.setSalt(salt);
        user.setPassword(hashedPasswordBase64);
        user.setLoginPjax(true);
        user.setLoginCount(0);
        user.setLoginAt(0);
        //20180326zhf1130
        if(needAuditFlag){
            //需要审核时
            user.setDisabled(true);
        }else{
            user.setCreater(this.fetch(Cnd.where("loginname", "=", "superadmin")).getId());
        }
        Sys_user u = this.insert(user);
        if(needAuditFlag){
            //生成一条sys_registaudit对象
            Sys_registaudit sysRegistaudit = new Sys_registaudit();
            //id
            sysRegistaudit.setId(u.getId());
            sysRegistaudit.setWay(1);
            sysRegistauditService.insert(sysRegistaudit);

            //系统内按角色发送，企业微信只能按用户发送
            Map<String,List<String>> roleAndUser = sysRoleService.getUsersByRole(null,null,"系统管理员");//(目前只有一个系统管理员,不分单位)
            Set<String> userIds = new HashSet<String>();
            //发送待办任务20180421
            for(Map.Entry<String,List<String>> entry:roleAndUser.entrySet()){
                String roleId = entry.getKey();
                List<String> userIdList = entry.getValue();
                userIds.addAll(userIdList);
                msgAssignService.addAssignToRole("用户注册",sysRegistaudit.getId(),"您收到用户["+loginname+"]的注册信息，请立即处理!",null,"1",roleId,userIdList,user.getId(),"/platform/sys/registaudit/audit/"+sysRegistaudit.getId()
                        ,"/platform/sys/registaudit/detail/"+sysRegistaudit.getId(),"cn.wizzer.app.sys.modules.services.impl.SysRegistauditServiceImpl");
            }


            //发送企业微信消息给设备管理员角色的用户
            if("1".equals(Globals.WxCorpStart)) {
                if (userIds.size() > 0) {
       /*         Cnd cnd = Cnd.NEW();
                cnd.and("userId", "=", personid);
                String npersonid = baseCnctobjService.fetch(cnd).getPersonId();
                base_person basePerson = basePersonService.fetch(npersonid);*/
                    StringBuffer content = new StringBuffer("");
                    content.append("收到["+loginname+"]的注册信息,请及时处理!");

                    log.debug("注册信息发送企业微信号消息,userIds="+userIds);
                    sysWxService.sendWxMessageAsy(userIds, content.toString());
                }
            }



        }
        //将unitid分配到sys_user_unit中

        this.insert("sys_user_unit", Chain.make("userId", u.getId()).add("unitId", unitid));
        //20180328zhf1530
        //分配角色
        if(!needAuditFlag){
            Cnd cnd =Cnd.NEW();
            if(leader){
                cnd.and("code","=","leader");
            }else{
                cnd.and("code","=","JW");
            }
            Sys_role role = sysRoleService.fetch(cnd);
            if(role!=null){
                this.insert("sys_user_role", Chain.make("userId", u.getId()).add("roleId", role.getId()));
            }
        }
        this.insert("sys_user_unit", Chain.make("userId", u.getId()).add("unitId", unitid));

        //处理base_person
        base_person basePerson = null;
        if(!needAuditFlag){
            if(leader && Strings.isBlank(cardid)){
                //此时肯定是没填证件号并且cardid不为空
                basePerson = basePersonService.fetch(Cnd.where("personname", "=", username).and("airportid","=",airportid));
            }else{
                basePerson = basePersonService.fetch(Cnd.where("personname", "=", username).and("cardid", "=", cardid).and("airportid", "=", airportid));
            }
            if(basePerson != null){
                //person已经存在
                basePerson.setPersonnum(basePersonService.getPersonNum(airportid,unitid));
                basePersonService.updateIgnoreNull(basePerson);
            }else{
                //person不存在
                basePerson =new base_person();
                basePerson.setAirportid(airportid);
                basePerson.setPersonname(username);
                basePerson.setPersonnum(basePersonService.getPersonNum(airportid,unitid));
                basePerson.setEmptypeId("empType.employee");
                basePerson.setUnitid(unitid);
                //20180323zhf1156
                if(!Strings.isBlank(cardid)){
                    basePerson.setCardid(cardid);
                }
                basePerson = basePersonService.insert(basePerson);
            }
        }
        if(basePerson != null){
            base_cnctobj cnctobj =new base_cnctobj();
            cnctobj.setUserId(u.getId());
            cnctobj.setPersonId(basePerson.getId());
            baseCnctobjService.insert(cnctobj);
        }
        return u;
    }

    public Map getUserInfo(Sys_user curUser) {
        Cnd cnd = Cnd.NEW();
        Map map = new HashMap();
        cnd.and("userId", "=", curUser.getId());
        base_cnctobj baseCnctobj = baseCnctobjService.fetch(cnd);
        if (baseCnctobj != null) {
            if (!Strings.isBlank(baseCnctobj.getPersonId())) {
                base_person basePerson = basePersonService.fetch(baseCnctobj.getPersonId());
                if (basePerson != null) {
                    if (!Strings.isBlank(basePerson.getAirportid())) {
                        base_airport baseAirport = baseAirportService.fetch(basePerson.getAirportid());
                        if (baseAirport != null) {
                            basePerson.setBase_airport(baseAirport);
                            map.put("airportid", basePerson.getAirportid());
                            map.put("airportname", baseAirport.getAirportname());
                            map.put("airportposition", baseAirport.getPosition());
                        }
                    }
                    if (!Strings.isBlank(basePerson.getUnitid())) {
                        Sys_unit sysUnit = unitService.fetch(basePerson.getUnitid());
                        if (sysUnit != null) {
                            basePerson.setUnit(sysUnit);
                            map.put("unitid", sysUnit.getId());
                        }
                    }
                    if (!Strings.isBlank(basePerson.getCustomerId())) {
                        map.put("customerid", basePerson.getCustomerId());
                    } else {
                        map.put("customerid", "");
                    }
                }
                baseCnctobj.setPerson(basePerson);
            }
        }
        baseCnctobj.setUser(curUser);
        map.put("userId", baseCnctobj.getUserId());
        map.put("personId", baseCnctobj.getPersonId());
        map.put("username", curUser.getUsername());
        Sys_user sysUser = dao().fetchLinks(dao().fetch(Sys_user.class, curUser.getId()), "roles");
        List<Sys_role> roles = sysUser.getRoles();
        map.put("roleName", roles.get(0).getName());
        String path = Globals.AppUploadPath+"/userImage/";
        map.put("path",path+sysUser.getUserImage());
        return map;
    }

}
