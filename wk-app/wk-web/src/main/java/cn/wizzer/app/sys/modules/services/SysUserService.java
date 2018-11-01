package cn.wizzer.app.sys.modules.services;

import cn.wizzer.app.sys.modules.models.Sys_menu;
import cn.wizzer.app.sys.modules.models.Sys_role;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.framework.base.service.BaseService;

import java.util.List;
import java.util.Map;

/**
 * Created by wizzer on 2016/12/22.
 */
public interface SysUserService extends BaseService<Sys_user> {
    List<String> getRoleCodeList(Sys_user user);

    List<Sys_role> getRolesList(Sys_user user);

    List<Sys_menu> getMenusAndButtons(String userId);

    List<Sys_menu> getDatas(String userId);

    void fillMenu(Sys_user user);

    void deleteById(String userId);

    void deleteByIds(String[] userIds);

    Map addUserByMobile(String airportid, String username, String loginname, String password, String unitid, String cardid, int sex, String pictureads, String tel, String deptid, String jobs);

    Sys_user addUser(String airportid, String username, String loginname, String password, String unitid, String cardid, boolean needAuditFlag, boolean leader);

    Map getUserInfo(Sys_user curUser);
}
