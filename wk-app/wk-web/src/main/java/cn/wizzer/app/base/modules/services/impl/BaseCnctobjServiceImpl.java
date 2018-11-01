package cn.wizzer.app.base.modules.services.impl;

import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.app.sys.modules.models.Sys_role;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.base.modules.models.base_cnctobj;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.framework.page.OffsetPager;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;

import java.util.ArrayList;
import java.util.List;

@IocBean(args = {"refer:dao"})
public class BaseCnctobjServiceImpl extends BaseServiceImpl<base_cnctobj> implements BaseCnctobjService {
    public BaseCnctobjServiceImpl(Dao dao) {
        super(dao);
    }
    @Inject
    private BasePersonService basePersonService;
    public base_cnctobj  getbase_cnctobj(String condition){

        return dao().fetch(base_cnctobj.class,condition);
    }

    public base_cnctobj getcnctobj(String userid){
        Cnd cnd=Cnd.NEW();
        cnd.and("userid","=",userid);
        base_cnctobj baseCnctobj = dao().fetch(base_cnctobj.class,cnd);
        if(baseCnctobj != null){
            String personid = baseCnctobj.getPersonId();
            if(!Strings.isBlank(personid))
                baseCnctobj.setPerson(dao().fetch(base_person.class,personid));
        }


        return baseCnctobj;

    }

    //根据当前登录人信息，过滤单位信息,当前登录人只能查看自己的组织信息
    public  Cnd unitDataPermission(Cnd cnd,String userid,String unitid){
        base_cnctobj baseCnctobj = this.getcnctobj(userid);
        if(baseCnctobj != null){
            base_person basePerson = baseCnctobj.getPerson();
            String Punitid = basePerson.getUnitid();
            if (!Strings.isBlank(Punitid)) {
                cnd.and(unitid, "=", Punitid);
                if("id".equals(unitid))//兼容Sys_unit界面数据查询的操作
                    cnd.or("parentId","=",Punitid);
            }
        }
        return cnd;
    }

    //根据当前登录人信息，过滤机场数据，查看当前机场信息
    public  Cnd airportDataPermission(Cnd cnd){
        Subject subject = SecurityUtils.getSubject();
        Sys_user curUser = (Sys_user) subject.getPrincipal();
        String curUserId = curUser.getId();
        List<Sys_role> role = curUser.getRoles();
        if(!role.get(0).getCode().toString().equals("sysadmin")) {
            base_cnctobj baseCnctobj=this.getcnctobj(curUserId);
            base_person basePerson=baseCnctobj.getPerson();
            String airportid= basePerson.getAirportid();
            if (!Strings.isBlank(airportid)){
                cnd.and("airportid", "=", airportid);
            }else{
                cnd.and("airportid", "=", "");
            }
        }
        return cnd;
    }

    //根据当前登录人信息，过滤机场数据，查看当前机场信息
    public  Cnd airportOrderPermission(Cnd cnd){
        Subject subject = SecurityUtils.getSubject();
        Sys_user curUser = (Sys_user) subject.getPrincipal();
        String curUserId = curUser.getId();
        List<Sys_role> role = curUser.getRoles();
        if(!role.get(0).getCode().toString().equals("sysadmin")) {
            base_cnctobj baseCnctobj = this.getcnctobj(curUserId);
            base_person basePerson = baseCnctobj.getPerson();
            String customerid = "";
            String unitid = "";
            String startstock = "";
            //获取人员类型
            String persontype = basePerson.getEmptypeId();
            if (persontype.equals("empType.customer")) {
                customerid = basePerson.getCustomerId();
                cnd.and("customerid", "=", customerid);
            } else if (persontype.equals("empType.stock")) {
                startstock = basePerson.getPlaceid();
                cnd.and("startstock", "=", startstock);
            }else if (persontype.equals("empType.employee")) {
                startstock = basePerson.getPlaceid();
                if(role.get(0).getIsUnit() ==0)
                    cnd.and("personid", "=", basePerson.getId());
            }

            String airportid = basePerson.getAirportid();
            if (!Strings.isBlank(airportid)) {
                cnd.and("airportid", "=", airportid);
            } else {
                cnd.and("airportid", "=", "");
            }
        }
        return cnd;
    }

    public String getPersonIDByUserID(){
        Subject subject = SecurityUtils.getSubject();
        Sys_user curUser = (Sys_user) subject.getPrincipal();
        String curUserId = curUser.getId();
        Cnd cnd=Cnd.NEW();
        cnd.and("userId","=",curUserId);
        base_cnctobj cnctobj = this.fetch(cnd);
        return cnctobj.getPersonId();
    }

    public NutMap getList(String loginName, String personName, int length, int start, int draw, List<DataTableOrder> order, List<DataTableColumn> columns, Cnd cnd) {
        NutMap re = new NutMap();
        StringBuilder sb = new StringBuilder(80);
        //20180421zhf1735
        String baseSql = "SELECT bc.id,su.loginname,bp.personname,bp.personnum,sd.name FROM base_cnctobj bc LEFT JOIN sys_user su ON bc.userId = su.id LEFT JOIN base_person bp ON bc.personId = bp.id LEFT JOIN sys_dict sd ON  bc.emptypeId = sd.id " ;
        //把筛选条件都装在一个list里
        List<String> conditions = new ArrayList<>();
        if(!Strings.isBlank(loginName)){
            loginName = loginName.replace("'","");
            conditions.add(" su.loginname LIKE '%"+loginName+"%' ");
        }
        if(!Strings.isBlank(personName)){
            personName = personName.replace("'","");
            conditions.add(" bp.personname LIKE '%"+personName+"%'");
        }
        //如果有查询条件才拼接WHERE
        if(conditions.size() > 0){
            sb.append(" WHERE ");
        }
        //遍历conditions列表,判断是否是第一个元素
        //如果不是第一个元素,则在语句前面加AND
        for (int index = 0; index < conditions.size(); index++) {
            if(index == 0){
                sb.append(conditions.get(index));
            }else{
                sb.append(" AND " + conditions.get(index));
            }
        }
        Sql sql = Sqls.queryRecord(baseSql + sb.toString());

        Pager pager = new OffsetPager(start, length);
        sql.setCondition(cnd);
        sql.setPager(pager);
        re.put("recordsFiltered", Daos.queryCount(dao(),sql));
        dao().execute(sql);

        System.out.println(sql);
        List list = sql.getList(Record.class);
        System.out.println(list);
        re.put("data", list);
        re.put("draw", draw);
        re.put("recordsTotal", length);

        return  re;
    }


}
