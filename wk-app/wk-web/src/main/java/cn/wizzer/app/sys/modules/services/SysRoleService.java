package cn.wizzer.app.sys.modules.services;

import cn.wizzer.app.sys.modules.models.Sys_menu;
import cn.wizzer.app.sys.modules.models.Sys_role;
import cn.wizzer.framework.base.service.BaseService;
import org.nutz.dao.Cnd;

import java.util.List;
import java.util.Map;

/**
 * Created by wizzer on 2016/12/22.
 */
public interface SysRoleService extends BaseService<Sys_role> {
    List<Sys_menu> getMenusAndButtons(String roleId);

    List<Sys_menu> getDatas(String roleId);

    List<Sys_menu> getDatas();

    List<String> getPermissionNameList(Sys_role role);

    boolean getAdminRole();

    String getRolesFlag();

    Cnd getPermission(Cnd cnd,String unitid,String deptid,String userid,boolean isNeed);

    void del(String roleid);

    void del(String[] roleids);

    int updateDataRight(String roleid,String isdept,String isunit);

    Map<String,List<String>> getUsersByRole(String unitid, String roleCode, String roleName);

}
