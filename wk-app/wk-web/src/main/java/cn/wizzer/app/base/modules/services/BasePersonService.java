package cn.wizzer.app.base.modules.services;

import cn.wizzer.app.sys.modules.models.Sys_menu;
import cn.wizzer.app.sys.modules.models.Sys_unit;
import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.base.modules.models.base_person;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.upload.TempFile;

import java.util.List;
import java.util.Map;

public interface BasePersonService extends BaseService<base_person>{

    List<Sys_unit> getDatas(String personid);

    base_person getPersonInfo();

    void bindWxuser(String id, String wxuserid);

    void autoBindWxuser();

    /**
     * 20180322zhf1216
     * 通过真实姓名和证件号判断是否是领导
     * @param personname
     * @param cardid
     * @param unitid
     * @return
     */
    NutMap judgeLeaderByPersonNameAndCardId(String personname, String cardid, String unitid);

    /**
     * 20180417zhf1415
     * 导入人员表
     * @param tf
     * @return
     */
    Object uploadFile(TempFile tf);

    String getPersonNum(String airportid, String unitid);

    //根据人员批量创建用户
    Map createuser(String loginType, String roleId, String[] ids);
}
