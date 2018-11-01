package cn.wizzer.app.sys.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.sys.modules.models.Sys_useradd;
import cn.wizzer.app.sys.modules.services.SysUseraddService;
import cn.wizzer.framework.page.OffsetPager;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

import java.util.ArrayList;
import java.util.List;

@IocBean(args = {"refer:dao"})
public class SysUseraddServiceImpl extends BaseServiceImpl<Sys_useradd> implements SysUseraddService {
    public SysUseraddServiceImpl(Dao dao) {
        super(dao);
    }

    //20180325zhf1103
    public NutMap getRegistAuditList(int result, int length, int start, int draw, List<DataTableOrder> order, List<DataTableColumn> columns) {
       Cnd cnd = Cnd.NEW();
        NutMap re = new NutMap();
        StringBuilder sb = new StringBuilder(80);
        String baseSql = "SELECT " +
                "ua.id as uaid,ua.needAudit,ua.tel,ua.jobs,ua.pictureads,ua.cardid,ua.sex," +
                "u.id as uid,u.disabled,u.loginname,u.username," +
                "su.id as suid, su.name," +
                "ba.id as baid,ba.airportname," +
                "sr.id as srid,sr.result,sr.way " +
                "FROM sys_user u " +
                "LEFT JOIN sys_useradd ua ON ua.userid = u.id " +
                "LEFT JOIN sys_unit su ON u.unitid = su.id  " +
                "LEFT JOIN base_airport ba ON su.unitairport = ba.id " +
                "JOIN sys_registaudit sr ON u.id = sr.id ";
        //把筛选条件都装在一个list里
        List<String> conditions = new ArrayList<>();
        conditions.add(" sr.result = "+result+" ");
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
        List list = sql.getList(Record.class);
        re.put("data", list);
        re.put("draw", draw);
        re.put("recordsTotal", length);

        return  re;
    }
}
