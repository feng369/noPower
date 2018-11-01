package cn.wizzer.app.eq.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.eq.modules.models.eq_planeseat;
import cn.wizzer.app.eq.modules.services.EqPlaneseatService;
import org.apache.commons.lang3.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

import java.util.*;

@IocBean(args = {"refer:dao"})
public class EqPlaneseatServiceImpl extends BaseServiceImpl<eq_planeseat> implements EqPlaneseatService {
    public EqPlaneseatServiceImpl(Dao dao) {
        super(dao);
    }

    public List<Record> getPlaneSeatByAirport(String airportid,String borrow,String seatId) {

        String select ="select s.id,s.seatnum,s.seatname,s.position,u.pstatus,u.errstatus,u.bizstatus from eq_planeseat s "+
                " left join eq_stake st on st.seatid=s.id "+
                " left join eq_use u on u.stakeid=st.id "+
                " where s.airportid='"+airportid+"' "+
                //20180502zhf1524
                //传进来seatId
                (!Strings.isBlank(seatId) ? " AND s.id =  '" + seatId + "' ":" order by s.seatnum ");
        List<Record> mapList=new ArrayList<Record>();
        Sql sql = Sqls.queryRecord(select);
        dao().execute(sql);
        //左右桩位和设备
        List<Record> pslist = sql.getList(Record.class);
        return getMaplist(mapList,pslist,borrow,false);
    }



    public List<Record> getMarkerBySeatList(String[] seatList ,String airportid, String borrow) {
        StringBuilder sb = new StringBuilder(80);
        for (String s : seatList) {
            sb.append("'"+s+"'").append(",");
        }
        sb = sb.deleteCharAt(sb.length()-1);
        String select ="select s.id,s.seatnum,s.seatname,s.position,u.pstatus,u.errstatus,u.bizstatus from eq_planeseat s "+
                " left join eq_stake st on st.seatid=s.id "+
                " left join eq_use u on u.stakeid=st.id "+
                " where  s.id in (" + sb.toString() +
                ") " +
                "and s.airportid='"+airportid+"' "+
                "order by u.opAt ";

        List<Record> mapList=new ArrayList<Record>();

        Sql sql = Sqls.queryRecord(select);
        dao().execute(sql);
        //左右桩位和设备
        List<Record> pslist = sql.getList(Record.class);
        return getMaplist(mapList,pslist,borrow,false);
    }


    //首页右边的marker机位查询
    public List<Record> getSeatsOfEqUse(String airportid, String borrow, String seatId) {
        String select ="select s.id,s.seatnum,s.seatname,s.position,u.pstatus,u.errstatus,u.bizstatus,em.eqnum,em.eqname from eq_planeseat s "+
                " left join eq_stake st on st.seatid=s.id "+
                " join eq_use u on u.stakeid=st.id "+
                " LEFT join eq_materiel em on u.eqid = em.id "+
                " where s.airportid='"+airportid+"' "+
                //传进来seatId
                (!Strings.isBlank(seatId) ? " AND s.id =  '" + seatId + "' ":" order by s.opAt DESC  ");
        List<Record> mapList=new ArrayList<>();
        Sql sql = Sqls.queryRecord(select);
        dao().execute(sql);
        //左右桩位和设备
        List<Record> pslist = sql.getList(Record.class);
        boolean rightInfo = true;
       return getMaplist(mapList,pslist,borrow,rightInfo);
    }
    /*
    * public List<List<Record>> getSeatsOfEqUse(String airportid, String borrow, String seatId) {
        String select ="select s.id,s.seatnum,s.seatname,s.position,u.id as uid,u.pstatus,u.errstatus,em.eqnum,em.eqname from eq_planeseat s "+
                " left join eq_stake st on st.seatid=s.id "+
                " join eq_use u on u.stakeid=st.id "+
                " LEFT join eq_materiel em on u.eqid = em.id "+
                " where s.airportid='"+airportid+"' "+
                (!Strings.isBlank(borrow) && "3".equals(borrow)?" AND u.errstatus != '0'":" ")+
                //传进来seatId
                (!Strings.isBlank(seatId) ? " AND s.id =  '" + seatId + "' ":" order by s.opAt ");
        List<List<Record>> mapList=new ArrayList<>();
        Sql sql = Sqls.queryRecord(select);
        dao().execute(sql);
        //左右桩位和设备
        List<Record> pslist = sql.getList(Record.class);
        //机位有设备异常
        List<Record> errorList = new ArrayList<>();
        Map<String ,List<Record>> errorMap = new HashMap<> ();
        //机位有设备借用
        List<Record> usedList = new ArrayList<>();
        Map<String ,List<Record>>  usedMap = new HashMap<> ();
        //机位无设备借用
        List<Record> onList = new ArrayList<>();
        Map<String ,List<Record>>  onMap = new HashMap<> ();
        for (Record record : pslist) {
            String id = record.getString("id");
            if(errorMap.containsKey(id)){
                //有当前id
                continue;
            }
            String pstatus = record.getString("pstatus");
            String errstatus = record.getString("errstatus");
    //            record.remove("pstatus");
    //            record.remove("errstatus");

            if(StringUtils.isNotBlank(errstatus) && !"0".equals(errstatus)){
                record.put("status","3");
                errorList.add(record);
                errorMap.put(id,errorList);
            }else if("0".equals(errstatus)  && !"0".equals(pstatus) ){
                record.put("status","2");
                usedList.add(record);
                usedMap.put(id,usedList);
            }else{
                record.put("status","1");
                onList.add(record);
                onMap.put(id,onList);
            }
        }
        if (Strings.isBlank(borrow)) {
            for (Map.Entry<String, List<Record>> entry : errorMap.entrySet()) {
                mapList.add(entry.getValue());
            }
            for (Map.Entry<String, List<Record>> entry : usedMap.entrySet()) {
                mapList.add(entry.getValue());
            }
            for (Map.Entry<String, List<Record>> entry : onMap.entrySet()) {
                if (!usedMap.containsKey(entry.getKey())) {
                    mapList.add(entry.getValue());
                }
            }
        }else  if("3".equals(borrow)){
            for (Map.Entry<String, List<Record>> entry : errorMap.entrySet()) {
                mapList.add(entry.getValue());
            }
        }else  if("2".equals(borrow)){
            for(Map.Entry<String ,List<Record>> entry:usedMap.entrySet()){
                if(!errorMap.containsKey(entry.getKey()))
                    mapList.add(entry.getValue());
            }
        }else if("1".equals(borrow)){
            for(Map.Entry<String ,List<Record>> entry:onMap.entrySet()){
                if(!errorMap.containsKey(entry.getKey())&&!usedMap.containsKey(entry.getKey())){
                    mapList.add(entry.getValue());
                }
            }
        }
        return mapList;
    }*/
    private List<Record> getMaplist(List<Record> mapList, List<Record> pslist,String borrow,boolean rightInfo) {
        //机位有设备异常
        Map<String ,Record> errorMap = new HashMap<String ,Record> ();
        //机位有设备预警
        Map<String ,Record> warnMap = new HashMap<> ();
        //机位有设备借用
        Map<String ,Record>  usedMap = new HashMap<String ,Record> ();
        //机位无设备借用
        Map<String ,Record>  onMap = new HashMap<String ,Record> ();

        for(Record record : pslist){
            String id = record.getString("id");
            if(errorMap.containsKey(id)){
                //有当前id
                continue;
            }
            String pstatus = record.getString("pstatus");
            String errstatus = record.getString("errstatus");
            String bizstatus = record.getString("bizstatus");
            record.remove("pstatus");
            record.remove("errstatus");
            record.remove("bizstatus");
            if((StringUtils.isNotBlank(bizstatus) &&("1".equals(bizstatus)||"2".equals(bizstatus)))   ||
                    ( StringUtils.isNotBlank(errstatus) && ( "3".equals(errstatus)|| "4".equals(errstatus)))){
                //异常
                record.put("status","3");
                errorMap.put(id,record);
            }else if(StringUtils.isNotBlank(errstatus) && ("1".equals(errstatus) ||"2".equals(errstatus))){
                //警告
                record.put("status","4");
                warnMap.put(id,record);
            }else if("1".equals(pstatus) ){
                //借用中
                record.put("status","2");
                usedMap.put(id,record);
            }else{//去掉判断  if("0".equals(pstatus)) 可显示所有机位
                //未使用
                record.put("status","1");
                onMap.put(id,record);
            }
        }

        if(Strings.isBlank(borrow)) {
            for (Map.Entry<String, Record> entry : errorMap.entrySet()) {
                mapList.add(entry.getValue());
            }
            for (Map.Entry<String, Record> entry : warnMap.entrySet()) {
                if(!errorMap.containsKey(entry.getKey()))
                    mapList.add(entry.getValue());
            }
            for (Map.Entry<String, Record> entry : usedMap.entrySet()) {
                if(!errorMap.containsKey(entry.getKey()) && !warnMap.containsKey(entry.getKey()))
                    mapList.add(entry.getValue());
            }

            for (Map.Entry<String, Record> entry : onMap.entrySet()) {
                if (!usedMap.containsKey(entry.getKey()) && !errorMap.containsKey(entry.getKey()) && !warnMap.containsKey(entry.getKey())) {
                    mapList.add(entry.getValue());
                }
            }
        }else  if("3".equals(borrow)){
            for (Map.Entry<String, Record> entry : errorMap.entrySet()) {
                mapList.add(entry.getValue());
            }
        }else if("4".equals(borrow)){
            for (Map.Entry<String, Record> entry : warnMap.entrySet()) {
                if(!errorMap.containsKey(entry.getKey()))
                    mapList.add(entry.getValue());
            }
        }
        else  if("2".equals(borrow)){
            for (Map.Entry<String, Record> entry : usedMap.entrySet()) {
                if(!errorMap.containsKey(entry.getKey()) && !warnMap.containsKey(entry.getKey()))
                    mapList.add(entry.getValue());
            }
        }else if("1".equals(borrow)){
            for(Map.Entry<String ,Record> entry:onMap.entrySet()){
                if(!errorMap.containsKey(entry.getKey()) && !warnMap.containsKey(entry.getKey()) && !usedMap.containsKey(entry.getKey())){
                    mapList.add(entry.getValue());
                }
            }
        }
        if(rightInfo && mapList.size() > 20){
            //右边只显示20个机位信息
            return mapList.subList(0,20);
        }
        return mapList;
    }
}
