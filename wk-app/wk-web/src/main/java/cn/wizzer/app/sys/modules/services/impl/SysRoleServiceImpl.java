package cn.wizzer.app.sys.modules.services.impl;

import cn.wizzer.app.sys.modules.models.Sys_menu;
import cn.wizzer.app.sys.modules.models.Sys_role;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.app.sys.modules.services.SysRoleService;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.entity.Record;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;

import java.util.*;

/**
 * Created by wizzer on 2016/12/22.
 */
@IocBean(args = {"refer:dao"})
public class SysRoleServiceImpl extends BaseServiceImpl<Sys_role> implements SysRoleService {
    @Inject
    private BaseCnctobjService baseCnctobjService;

    public SysRoleServiceImpl(Dao dao) {
        super(dao);
    }

    public List<Sys_menu> getMenusAndButtons(String roleId) {
        Sql sql = Sqls.create("select distinct a.* from sys_menu a,sys_role_menu b where a.id=b.menuId and" +
                " b.roleId=@roleId and a.disabled=@f order by a.location ASC,a.path asc");
        sql.params().set("roleId", roleId);
        sql.params().set("f",false);
        Entity<Sys_menu> entity = dao().getEntity(Sys_menu.class);
        sql.setEntity(entity);
        sql.setCallback(Sqls.callback.entities());
        dao().execute(sql);
        return sql.getList(Sys_menu.class);
    }

    public List<Sys_menu> getDatas(String roleId) {
        Sql sql = Sqls.create("select distinct a.* from sys_menu a,sys_role_menu b where a.id=b.menuId and" +
                " b.roleId=@roleId and a.type='data' and a.disabled=@f order by a.location ASC,a.path asc");
        sql.params().set("roleId", roleId);
        sql.params().set("f",false);
        Entity<Sys_menu> entity = dao().getEntity(Sys_menu.class);
        sql.setEntity(entity);
        sql.setCallback(Sqls.callback.entities());
        dao().execute(sql);
        return sql.getList(Sys_menu.class);
    }

    public List<Sys_menu> getDatas() {
        Sql sql = Sqls.create("select distinct a.* from sys_menu a,sys_role_menu b where a.id=b.menuId and a.type='data' order by a.location ASC,a.path asc");
        Entity<Sys_menu> entity = dao().getEntity(Sys_menu.class);
        sql.setEntity(entity);
        sql.setCallback(Sqls.callback.entities());
        dao().execute(sql);
        return sql.getList(Sys_menu.class);
    }

    /**
     * 查询权限
     *
     * @param role
     * @return
     */
    public List<String> getPermissionNameList(Sys_role role) {
        dao().fetchLinks(role, "menus");
        List<String> list = new ArrayList<String>();
        for (Sys_menu menu : role.getMenus()) {
            if (!Strings.isEmpty(menu.getPermission()) && !menu.isDisabled()) {
                list.add(menu.getPermission());
            }
        }
        return list;
    }

    @Aop(TransAop.READ_COMMITTED)
    public void del(String roleid) {
        this.dao().clear("sys_user_role", Cnd.where("roleId", "=", roleid));
        this.dao().clear("sys_role_menu", Cnd.where("roleId", "=", roleid));
        this.delete(roleid);
    }

    @Aop(TransAop.READ_COMMITTED)
    public void del(String[] roleids) {
        this.dao().clear("sys_user_role", Cnd.where("roleId", "in", roleids));
        this.dao().clear("sys_role_menu", Cnd.where("roleId", "in", roleids));
        this.delete(roleids);
    }

    @Aop(TransAop.READ_COMMITTED)
    public int updateDataRight(String roleid,String isdept,String isunit)
    {
        //this.dao().update(Sys_role,Cnd)
        return 0;
    }

    public boolean getAdminRole(){
        boolean adminFlag = false;
        Subject subject = SecurityUtils.getSubject();
        Sys_user curUser = (Sys_user) subject.getPrincipal();
        if(curUser!=null) {
            List<Sys_role> roles = curUser.getRoles();
            //获取超级管理员权限标识，超级管理员权限标识为sysadmin可以查看所有数据
            for (int i = 0; i < roles.size(); i++) {
                if (roles.get(i).getCode().toString().equals("sysadmin")) {
                    adminFlag = true;
                    break;
                }
            }
        }
        return adminFlag;
    }

    public String getRolesFlag(){
        String roleP="self";
        Subject subject = SecurityUtils.getSubject();
        Sys_user curUser = (Sys_user) subject.getPrincipal();
        List<Sys_role> roles = curUser.getRoles();
        for(int i=0;i<roles.size();i++){
            if(roles.get(i).getIsUnit()==0){
                if(roles.get(i).getIsDept()==1){
                    roleP="dept";
                }
            }else{
                    roleP="unit";
                    break;
            }
        }
        return roleP;
    }


    /**
     *    //数据权限
        //查询单位

     * @param cnd  必须不为null
     * @param unitid 必须，为sql中的单位的字段名
     * @param deptid  暂无用
     * @param userid  必须，为sql中创建人的字段名
     * @param isNeed
     * @return
     */
    public Cnd getPermission(Cnd cnd,String unitid,String deptid,String userid,boolean isNeed){
        Subject subject = SecurityUtils.getSubject();
        Sys_user curUser = (Sys_user) subject.getPrincipal();
        boolean adminFlag = getAdminRole();
        String roleP="self";
        if(isNeed){
            roleP=getRolesFlag();
        }
        if(adminFlag==false){
            if(isNeed&&(roleP=="dept"||roleP=="self")){
                if(roleP=="self"){
                    cnd.and(userid,"=",curUser.getId());
                }
                else{

                }
            }else{
//                cnd = baseCnctobjService.unitDataPermission(cnd,curUser.getId(),unitid);
                //20180614 update by kdp 根据用户分配的组织范围来过滤条件
                Sql sql = Sqls.create("select unitId from sys_user_unit where userId='"+curUser.getId()+"' ");
                List<Record> recordList = this.list(sql);
                Set unitIds = new HashSet<String>();
                for(Record record:recordList){
                    unitIds.add(record.getString("unitId"));
                }
                if(unitIds.size()>0) {
                    cnd.and(unitid, "in", unitIds);
                }
            }
        }

        return cnd;
    }


    @Override
    public Map<String,List<String>> getUsersByRole(String unitid, String roleCode, String roleName) {
        Map<String,List<String>> retMap = new HashMap<String,List<String>>();
        Cnd rcnd=Cnd.NEW();
        if(Strings.isNotBlank(unitid)){
            rcnd.and("unitid","=",unitid);
        }
        if(Strings.isNotBlank(roleCode)){
            rcnd.and("code","=",roleCode);
        }
        if(Strings.isNotBlank(roleName)){
            rcnd.and("name","=",roleName);
        }

        List<Sys_role> roleList=this.query(rcnd);
        for(Sys_role role : roleList){
            this.fetchLinks(role, "users");
            List<Sys_user> userList = role.getUsers();
            List<String>userIdList = new ArrayList<String>();
            for(Sys_user user:userList){
                if(!userIdList.contains(user.getId())) {
                    userIdList.add(user.getId());
                }
            }
            retMap.put(role.getId(),userIdList);
        }
        return retMap;
    }

}
