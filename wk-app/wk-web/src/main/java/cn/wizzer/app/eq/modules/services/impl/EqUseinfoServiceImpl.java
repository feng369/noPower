package cn.wizzer.app.eq.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.eq.modules.models.eq_useinfo;
import cn.wizzer.app.eq.modules.services.EqUseinfoService;
import cn.wizzer.framework.page.OffsetPager;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.QueryResult;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.Param;

import java.util.List;

@IocBean(args = {"refer:dao"})
public class EqUseinfoServiceImpl extends BaseServiceImpl<eq_useinfo> implements EqUseinfoService {
    public EqUseinfoServiceImpl(Dao dao) {
        super(dao);
    }

    public NutMap getDataList(String unitid,String starttime,String endtime,int pstatus,int start,int length,int draw){
        Cnd cnd=Cnd.NEW();
        NutMap re = new NutMap();
        Sql sql = Sqls.queryRecord("select t.equsenum,t2.username,t3.eqnum,t3.eqname ,t4.name as eqtype,t.starttime,t.endtime,\n" +
                "time_format(timediff(t.endtime,t.starttime),'%H小时%i分%s秒') as optime,TIMESTAMPDIFF(HOUR,t.starttime,t.endtime)*10 as sf\n" +
                "from eq_useinfo t INNER JOIN sys_unit t1 on t.personunitid = t1.id \n" +
                "INNER JOIN sys_user t2 on t.personid=t2.id\n" +
                "INNER JOIN eq_materiel t3  on t.eqid= t3.id\n" +
                "INNER JOIN  sys_dict t4 on t3.typeid = t4.id " +
                " $condition");
        cnd.and("pstatus","=",pstatus);
        if(!Strings.isBlank(unitid)){
            cnd.and("t.personunitid","=",unitid);
        }
        if(!Strings.isBlank(starttime)&&!Strings.isBlank(endtime)){
            String year=endtime.split("-")[0];
            String month=endtime.split("-")[1];
            String newendtime="";
            String newstarttime=starttime+"-01";
            try{
                int m = Integer.parseInt(month);
                if(m>9){
                    m++;
                    newendtime = year+"-"+Integer.toString(m)+"-01";
                }else{
                    m++;
                    newendtime = year+"-0"+Integer.toString(m)+"-01";
                }
                cnd.and("t.starttime",">=",newstarttime);
                cnd.and("t.starttime","<",newendtime);
            }catch(NumberFormatException e){
                throw e;
            }
        }
        cnd.desc("endtime");
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


    public NutMap geteqTotal(String units,String starttime,String endtime, int start,int length,int draw,List<DataTableOrder> orders,List<DataTableColumn> columns){
        Cnd cnd=Cnd.NEW();
        NutMap re = new NutMap();
        starttime=starttime+"-01'";
        endtime=endtime+"-31'";
        Sql sql = Sqls.queryRecord("select a.id,a.name,'start' as start,'end' as end,ifnull( number,0) number from sys_unit a " +
                " left join (select count(*) number,personunitid from " +
                " eq_useinfo where " +
                " starttime BETWEEN "+starttime+" AND "+endtime+" " +
                " group by personunitid) b on a.id=b.personunitid " +
                " where a.id in ("+units+") "+
                " $condition");

        if (orders != null && orders.size() > 0) {
            for (DataTableOrder order : orders) {
                DataTableColumn col = columns.get(order.getColumn());
                cnd.orderBy(Sqls.escapeSqlFieldValue(col.getData()).toString(), order.getDir());
            }
        }

        Pager pager = new OffsetPager(start, length);
        sql.setCondition(cnd.getOrderBy());
        sql.setPager(pager);
        re.put("recordsFiltered", Daos.queryCount(dao(),sql));
        dao().execute(sql);
        List list = sql.getList(Record.class);
        re.put("data", list);
        re.put("draw", draw);
        re.put("recordsTotal", length);
        return  re;
    }

    public NutMap geteqtypeTotal(String types,String syear, String aircorp, int start,int length,int draw){
        Cnd cnd=Cnd.NEW();
        NutMap re = new NutMap();
        Sql sql = Sqls.queryRecord("select d.id,d.`name` unit, count(*) number,b.typeid,c.`name`,YEAR(a.starttime) `year`, MONTH(a.starttime) `month` from eq_useinfo a left join eq_materiel b on a.eqid=b.id " +
                " left join sys_dict c on b.typeid=c.id "+
                " left join sys_unit d on a.personunitid=d.id "+
                " where b.typeid in ("+types+") and YEAR(a.starttime)="+syear+" "+
                " and d.id='"+aircorp+"' "+
                " group by b.typeid,c.`name`,MONTH(a.starttime),YEAR(a.starttime) " +
                " order by `year`,`month` "
        );

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

    public NutMap geteqtypeList(String types,String syear, String aircorp, int start,int length,int draw,List<DataTableOrder> orders,List<DataTableColumn> columns){
        Cnd cnd=Cnd.NEW();
        NutMap re = new NutMap();
        Sql sql = Sqls.queryRecord(
                "select a.*,b.username,c.eqname,c.eqnum from eq_useinfo a " +
                        "left join sys_user b on a.personid=b.id " +
                        "left join eq_materiel c on a.eqid=c.id " +
                        "where a.personunitid='"+aircorp+"' "+
                        "and Year(a.starttime)="+syear+" "+
                        "and c.typeid in ("+types+") $condition"
        );
        if (orders != null && orders.size() > 0) {
            for (DataTableOrder order : orders) {
                DataTableColumn col = columns.get(order.getColumn());
                cnd.orderBy(Sqls.escapeSqlFieldValue(col.getData()).toString(), order.getDir());
            }
        }
        Pager pager = new OffsetPager(start, length);
        sql.setCondition(cnd.getOrderBy());
        sql.setPager(pager);
        re.put("recordsFiltered", Daos.queryCount(dao(),sql));
        dao().execute(sql);
        List list = sql.getList(Record.class);
        re.put("data", list);
        re.put("draw", draw);
        re.put("recordsTotal", length);
        return  re;
    }

    @Override
    public String insertInfo(String equsenum, String eqid, String personid, String personunitid, String starttime, String endtime,String sstartime,String sendtime, String pstatus, String equnitid,String lid)
    {
        try{
            if(!Strings.isBlank(eqid)){
                eq_useinfo useinfo=new eq_useinfo();
                useinfo.setEqid(eqid);
                if(!Strings.isBlank(equsenum))
                    useinfo.setEqusenum(equsenum);
                if(!Strings.isBlank(personid))
                    useinfo.setPersonid(personid);
                if(!Strings.isBlank(personunitid))
                    useinfo.setPersonunitid(personunitid);
                if(!Strings.isBlank(starttime))
                    useinfo.setStarttime(starttime);
                if(!Strings.isBlank(endtime))
                    useinfo.setEndtime(endtime);
                if(!Strings.isBlank(sstartime))
                    useinfo.setSstarttime(sstartime);
                if(!Strings.isBlank(sendtime))
                    useinfo.setSendtime(sendtime);
                if(!Strings.isBlank(pstatus))
                    useinfo.setPstatus(pstatus);
                if(!Strings.isBlank(equnitid))
                    useinfo.setEqunitid(equnitid);
                useinfo.setLid(lid);
                eq_useinfo ui = this.insert(useinfo);
                return ui.getId();
            }
            return null;
        }catch(Exception e){
            return null;
        }
    }
}
