package cn.wizzer.app.sys.modules.services.impl;

import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.app.base.modules.models.base_cnctobj;
import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.app.base.modules.services.BaseAirportService;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.msg.modules.models.Msg_option;
import cn.wizzer.app.msg.modules.services.MsgAssignService;
import cn.wizzer.app.msg.modules.services.MsgMessageService;
import cn.wizzer.app.sys.modules.models.*;
import cn.wizzer.app.sys.modules.services.*;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@IocBean(args = {"refer:dao"})
public class SysRegistauditServiceImpl extends BaseServiceImpl<Sys_registaudit> implements SysRegistauditService {
    private static final Log log = Logs.get();
    @Inject
    private MsgMessageService msgMessageService;
    @Inject
    private MsgAssignService msgAssignService;
    @Inject
    SysUserService sysUserService;
    @Inject
    SysUseraddService sysUseraddService;
    @Inject
    BaseCnctobjService baseCnctobjService;
    @Inject
    private SysRoleService roleService;
    @Inject
    private BasePersonService basePersonService;
    @Inject
    private BaseAirportService baseAirportService;
    @Inject
    private SysUnitService unitService;

    public SysRegistauditServiceImpl(Dao dao) {
        super(dao);
    }

    @Aop(TransAop.READ_COMMITTED)
    public boolean completeAudit(String id, int result, String roleCode, String personId, int way) {
        Sys_user currentUser = (Sys_user) SecurityUtils.getSubject().getPrincipal();
        String cid = null;
        if (currentUser != null && !Strings.isBlank(currentUser.getId())) {
            cid = currentUser.getId();
        }
        Sys_user sysUser = sysUserService.fetch(id);
        if (sysUser != null) {
            if (result == 0) {
                //通过


                    sysUser.setDisabled(false);
                    sysUser.setCreater(cid);
                    sysUserService.updateIgnoreNull(sysUser);
                    if (!Strings.isBlank(roleCode)) {
                        Sys_role role = roleService.fetch(Cnd.where("code", "=", roleCode));
                        if (role != null) {
                            roleService.insert("sys_user_role", org.nutz.dao.Chain.make("roleId", role.getId()).add("userId", sysUser.getId()));
                        }
                    }
                    Sys_useradd sysUseradd = sysUseraddService.fetch(Cnd.where("userid", "=", id));
                    if (sysUseradd != null) {
                        sysUseradd.setNeedAudit(false);
                        sysUser.setCreater(cid);
                        sysUseraddService.updateIgnoreNull(sysUseradd);
                        if (way == 0) {

                            base_person basePerson = basePersonService.fetch(Cnd.where("cardid", "=", sysUseradd.getCardid()).and("airportid", "=", sysUseradd.getAirportid()));
                            if (basePerson != null) {
                                //person已经存在
                                basePerson.setSex(sysUseradd.getSex());
                                basePerson.setPicture(sysUseradd.getPictureads());
                                basePerson.setTel(sysUseradd.getTel());
                                basePerson.setPersonnum(getPersonNum(sysUseradd.getAirportid(), sysUseradd.getUnitid()));
                                basePersonService.updateIgnoreNull(basePerson);
                            } else {
                                //person不存在
                                basePerson = new base_person();
                                basePerson.setSex(sysUseradd.getSex());
                                basePerson.setPicture(sysUseradd.getPictureads());
                                basePerson.setTel(sysUseradd.getTel());
                                basePerson.setAirportid(sysUseradd.getAirportid());
                                basePerson.setPersonname(sysUser.getUsername());
//                                basePerson.setPersonnum(getPersonNum(sysUseradd.getAirportid(), sysUseradd.getUnitid()));
                                basePerson.setEmptypeId("empType.employee");
                                basePerson.setUnitid(sysUseradd.getUnitid());

                                //20180323zhf1156
                                if (!Strings.isBlank(sysUseradd.getCardid())) {
                                    basePerson.setCardid(sysUseradd.getCardid());
                                }
                                basePerson.setPersonnum(basePerson.getCardid());//让员工编号为证件号
                                basePerson = basePersonService.insert(basePerson);

                            }

                            //创建关联对象
                            base_cnctobj cnctobj = new base_cnctobj();
                            cnctobj.setUserId(sysUser.getId());
                            cnctobj.setPersonId(basePerson.getId());
                            baseCnctobjService.insert(cnctobj);
                        }
                    }
                    //处理base_person和base_cncobj
                    if (way == 1) {
                        //web注册
                        base_person basePerson = basePersonService.fetch(personId);
                        if (basePerson != null) {
                            //完善人员表的信息
                            basePerson.setPicture(sysUseradd.getPictureads());
                            basePersonService.updateIgnoreNull(basePerson);
                            base_cnctobj cnctobj = new base_cnctobj();
                            cnctobj.setUserId(sysUser.getId());
                            cnctobj.setPersonId(basePerson.getId());
                            baseCnctobjService.insert(cnctobj);
                        }
                    }
                return true;
            }else if(result == 1){
                //没通过,伪删除sys_user
                sysUserService.vDelete(sysUser.getId());
            }

        }
        return false;
    }

    public String getPersonNum(String airportid, String unitid) {
        base_airport airport = baseAirportService.fetch(airportid);
        Sys_unit unit = unitService.fetch(unitid);
        Cnd cnd1 = Cnd.NEW();
        cnd1.and("airportid", "=", airportid);
        cnd1.and("unitid", "=", unitid);
        cnd1.desc("createTime");
        List<base_person> personList = basePersonService.query(cnd1);

        String pnumber = "";
        System.out.println(personList.get(0).getPersonnum());
        System.out.println(personList.get(0));

        if (personList.size() > 0 && personList.get(0).getPersonnum().split("-").length > 1) {
            pnumber = personList.get(0).getPersonnum().split("-")[2];
            int num = Integer.parseInt(pnumber) + 1;
            switch (String.valueOf(num).length()) {
                case 1:
                    pnumber = airport.getAirportnum() + "-" + unit.getUnitcode() + "-000" + String.valueOf(num);
                    break;
                case 2:
                    pnumber = airport.getAirportnum() + "-" + unit.getUnitcode() + "-00" + String.valueOf(num);
                    break;
                case 3:
                    pnumber = airport.getAirportnum() + "-" + unit.getUnitcode() + "-0" + String.valueOf(num);
                    break;
                case 4:
                    pnumber = airport.getAirportnum() + "-" + unit.getUnitcode() + "-" + String.valueOf(num);
                    break;
            }
        } else {
            pnumber = airport.getAirportnum() + "-" + unit.getUnitcode() + "-0001";
        }

        return pnumber;
    }


    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void auditHandler(NutMap objMap,String type) {
        String id = (String)objMap.get("id");
        int result  = -1;
        if(type!=null && ("0".equals(type)||"1".equals(type))){
            result = Integer.parseInt((String)objMap.get("option.type"));
        }
        String roleCode =  (String) objMap.get("roleCode");
        String personId = null;
        if(objMap.get("personId") != null){
            personId = (String) objMap.get("personId");
        }
        int way = Integer.parseInt((String)objMap.get("way"));
        String suggestion = null;
        if(objMap.get("option.content") != null){
            suggestion = (String)objMap.get("option.content");

        }
        Sys_registaudit sysRegistaudit = this.fetch(Cnd.where("id","=",id));
        sysRegistaudit.setCreater(((Sys_user) SecurityUtils.getSubject().getPrincipal()).getId());

        //处理结果
        sysRegistaudit.setResult(result);
        sysRegistaudit.setWay(way);
        sysRegistaudit.setPersonId(personId);
        sysRegistaudit.setRoleCode(roleCode);
        sysRegistaudit.setSuggestion(suggestion);
        if(result == 1){
            sysRegistaudit.setRoleCode(null);
        }
        //更新进数据库
        this.updateIgnoreNull(sysRegistaudit);
        if(type!=null && ("0".equals(type)||"1".equals(type))){

            boolean success =  this.completeAudit(id,result,roleCode,personId,way);

//        String assignid = (String) objMap.get("assignid");
            if (success) {
                //发送审核通知
//            log.debug("注册审批发送通知消息 assignid=" + assignid);
                msgMessageService.addMessage("0", "0", "0", "1", "恭喜您注册成功！", null, sysRegistaudit.getId(), null, null);
//            if(option instanceof Msg_option){//待办任务处理完成
            }
        }

    }
}
