package cn.wizzer.app.eq.modules.services.impl;

import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.app.base.modules.services.BaseAirportService;
import cn.wizzer.app.eq.modules.models.*;
import cn.wizzer.app.eq.modules.services.*;
import cn.wizzer.app.msg.modules.services.MsgMessageService;
import cn.wizzer.app.sys.modules.models.Sys_unit;
import cn.wizzer.app.sys.modules.services.SysRoleService;
import cn.wizzer.app.sys.modules.services.SysUnitService;
import cn.wizzer.app.sys.modules.services.SysWxService;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.base.Transform;
import cn.wizzer.app.web.commons.plugin.PostRun;
import cn.wizzer.app.web.modules.controllers.open.api.Datatime.newDataTime;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.framework.page.OffsetPager;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.ClassReflection;
import cn.wizzer.framework.util.PinYinUtil;
import cn.wizzer.framework.websocket.MyWebsocket;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.Each;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import javax.websocket.Session;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@IocBean(args = {"refer:dao"})
public class EqUseServiceImpl extends BaseServiceImpl<eq_use> implements EqUseService {
    //专门针对硬件通讯接口getinfo方法定制日志文件
    private static Logger luckyLog = Logger.getLogger("LuckyLog");

    private Log log = Logs.get();
    public EqUseServiceImpl(Dao dao) {
        super(dao);
    }

    @Inject
    protected EqUseinfoService eqUseinfoService;
    @Inject
    private SysUnitService sysUnitService;
    @Inject
    private BaseAirportService baseAirportService;
    @Inject
    private EqMaterielService eqMaterielService;
    @Inject
    private EqTraceService eqTraceService;
    @Inject
    private SysRoleService sysRoleService;
    @Inject
    private MsgMessageService msgMessageService;
    @Inject
    private SysWxService sysWxService;
    @Inject
    protected MyWebsocket myWebsocket;
    @Inject
    private EqPlaneseatService eqPlaneseatService;
    @Inject
    private EqLockService eqLockService;

    //记录最近发送开锁指令的用户信息：记录开锁操作人
    private ConcurrentHashMap<String,String> openLockMap = new ConcurrentHashMap<String,String> ();

    //记录最近发送开锁指令的用户信息：控制频繁发送关锁指令
    private ConcurrentHashMap<String,String> reqOpenLockMap = new ConcurrentHashMap<String,String> ();
    //记录最近发送关锁指令的用户信息：控制频繁发送关锁指令
    private ConcurrentHashMap<String,String> reqReturnLockMap = new ConcurrentHashMap<String,String> ();
    /**
     * 下线
     * @param eqid
     */
    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void useOffline(String[] eqid) throws Exception {
        if(eqid != null && eqid.length>0){
            eq_use eqUse=null;
            eq_materiel eqMateriel = null;
            List<eq_use> eulist =new ArrayList<eq_use>();
            List<eq_materiel> list2 =new ArrayList<eq_materiel>();
            List<String> stakeList = new ArrayList<String>();
            for(String id:eqid) {
                eqUse=null;
                eqMateriel = null;
                Cnd cnd=Cnd.NEW();
                cnd.and("eqid","=",id);
                List<eq_use> list = dao().query(eq_use.class,cnd);
                eqMateriel = dao().fetch(eq_materiel.class,id);
                if(list.size()==1){
                    eqUse = list.get(0);
                }
                if(eqUse==null || eqMateriel==null){
                    throw new ValidatException("未找到设备["+eqMateriel.getEqnum()+"]的使用数据!");
                }
                if(!"1".equals(eqMateriel.getEqusestatus())){
                    throw new ValidatException("设备["+eqMateriel.getEqnum()+"]未上线，不能进行此操作!");
                }
                if("1".equals(eqUse.getPstatus())){
                    throw new ValidatException("设备["+eqMateriel.getEqnum()+"]正在借用中，不能进行此操作!");
                }
                eulist.add(eqUse);
                list2.add(eqMateriel);
                stakeList.add(eqMateriel.getStakeid());
            }
            for(eq_use equse:eulist){
                eq_use_his eqUseHis=new eq_use_his();
                ClassReflection.reflectionAttr(eqUse,eqUseHis);
                eqUseHis.setOffTime(new Date());
                dao().insert(eqUseHis);
                dao().delete(eq_use.class,eqUse.getId());
            }
            this.dao().clear(eq_use.class, Cnd.where("id", "in", eqid));
            for (eq_materiel em:list2){
                eqMateriel.setEqusestatus("0");
                dao().updateIgnoreNull(eqMateriel);
//            dao().execute(Sqls.create("update sys_menu set hasChildren=0 where id=@pid").setParam("pid", unit.getParentId()));
            }

            //发送消息到客户端更新地图
            List<eq_stake> stakes = this.dao().query(eq_stake.class, Cnd.where("id", "in", stakeList));
            List<String> seatList = new ArrayList<String>();
            for(eq_stake eq:stakes){
                if(!seatList.contains(eq.getSeatid())){
                    seatList.add(eq.getSeatid());
                }
            }
            if(seatList.size()>0){
                //获取机场id，一次操作所有设备只会是同一个机场的设备
                Record record = this.dao().fetch("eq_planeseat",Cnd.where("id", "=", seatList.get(0)),"airportId");  // ;
                String airportId = record.getString("airportId");
                if(StringUtils.isNotBlank(airportId)){
                    myWebsocket.each("home-"+airportId, new Each<Session>() {
                        public void invoke(int index, Session ele, int length) {
                            // 逐个会话发送消息
                            myWebsocket.sendJson(ele.getId(), new NutMap("action", "notify").setv("msg", seatList ));
                        }
                    });
                }
            }

        }
    }



    /**
     * 上线
     * @param eqid
     */
    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void useOnline(String[] eqid) throws Exception{
        if(eqid != null && eqid.length>0){
            eq_use eqUse=null;
            List<eq_use> list =new ArrayList<eq_use>();
            List<eq_materiel> list2 =new ArrayList<eq_materiel>();
            List<String> stakeList = new ArrayList<String>();
            for(String id:eqid) {
                eqUse=new eq_use();
                eqUse.setEqid(id);
                eq_materiel eqMateriel = dao().fetch(eq_materiel.class,id);
                if(StringUtils.isBlank(eqMateriel.getStakeid())){
                    throw new ValidatException("设备["+eqMateriel.getEqnum()+"]未分配桩位，不能进行此操作!");
                }
                if(StringUtils.isBlank(eqMateriel.getLockid())||eqMateriel.getLid()==null){
                    throw new ValidatException("设备["+eqMateriel.getEqnum()+"]未分配锁号，不能进行此操作!");
                }
                //判断是否 已存在此设备的数据
                Cnd cnd=Cnd.NEW();
                cnd.and("eqid","=",id);
                eq_use use= dao().fetch(eq_use.class,cnd);
                if(use!=null){
                    throw new ValidatException("设备["+eqMateriel.getEqnum()+"]已上线使用!");
                }
                eqUse.setEqunitid(eqMateriel.getEqunitid());
                eqUse.setStakeid(eqMateriel.getStakeid());
                eqUse.setLid(eqMateriel.getLid());
                eqUse.setPstatus("0");
                eqUse.setErrstatus("0");
                eqUse.setBizstatus("0");
                list.add(eqUse);
                eqMateriel.setEqusestatus("1");
                list2.add(eqMateriel);
                stakeList.add(eqMateriel.getStakeid());
            }
            for(eq_use equse:list){
                dao().insert(equse);
            }
            for (eq_materiel em:list2){
                dao().updateIgnoreNull(em);
            }

            //发送消息到客户端更新地图
            List<eq_stake> stakes = this.dao().query(eq_stake.class, Cnd.where("id", "in", stakeList));
            List<String> seatList = new ArrayList<String>();
            for(eq_stake eq:stakes){
                if(!seatList.contains(eq.getSeatid())){
                    seatList.add(eq.getSeatid());
                }
            }
            if(seatList.size()>0){
                //获取机场id，一次操作所有设备只会是同一个机场的设备
                Record record = this.dao().fetch("eq_planeseat",Cnd.where("id", "=", seatList.get(0)),"airportId");  // ;
                String airportId = record.getString("airportId");
                    for (String sid : seatList) {
                        eq_planeseat eqPlaneseat = eqPlaneseatService.fetch(sid);
                        if(eqPlaneseat != null){
                            eqPlaneseat.setOpAt(newDataTime.getIntegerByDate(new Date()));
                            eqPlaneseatService.updateIgnoreNull(eqPlaneseat);
                        }
                    }
                if(StringUtils.isNotBlank(airportId)){
                    myWebsocket.each("home-"+airportId, new Each<Session>() {
                        public void invoke(int index, Session ele, int length) {
                            // 逐个会话发送消息
                            myWebsocket.sendJson(ele.getId(), new NutMap("action", "notify").setv("msg", seatList ));
                        }
                    });
                }
            }
        }
    }
    //20180412zhf1615
    //得到当前机位设备信息
    public NutMap getEqUseListForDataTable(String seatId,int length, int start, int draw, List<DataTableOrder> orders, List<DataTableColumn> columns) {
        NutMap re = new NutMap();
        Cnd cnd = Cnd.NEW();
        String baseSql = " SELECT es.stakenum,em.eqnum,em.eqname,eu.pstatus,eu.errstatus,eu.bizstatus FROM eq_use eu LEFT JOIN eq_materiel em ON eu.eqid = em.id LEFT JOIN eq_stake es ON eu.stakeid = es.id WHERE es.seatid =  '" + seatId + "' ";
        if (orders != null && orders.size() > 0) {
            for (DataTableOrder order : orders) {
                DataTableColumn col = columns.get(order.getColumn());
                baseSql  = baseSql +" order by " + Sqls.escapeSqlFieldValue(col.getData()).toString() + " " + order.getDir();
            }
        }
        Sql sql = Sqls.queryRecord(baseSql);
        Pager pager = new OffsetPager(start, length);
//        sql.setCondition(cnd);
        sql.setPager(pager);
        re.put("recordsFiltered", Daos.queryCount(dao(),sql));
        dao().execute(sql);
        List list = sql.getList(Record.class);
        re.put("data", list);
        re.put("draw", draw);
        re.put("recordsTotal", length);
        return  re;
    }

    public List<Record> getEqUseList(String [] seatList,String airportid) {
        StringBuilder sb = new StringBuilder(80);
        if(seatList.length>0){
            for (String s : seatList) {
                sb.append("'"+s+"'").append(",");
            }
            sb = sb.deleteCharAt(sb.length()-1);
        }
        String tsql=" SELECT em.eqnum,em.eqname,u.errstatus,se.id seatid,se.seatname,se.seatnum," +
                "(case when (u.bizstatus=1 or u.bizstatus=2 or u.errstatus = 3 or u.errstatus = 4) then 'err'  when (u.errstatus =1 OR u.errstatus =2) then 'warn' else '' end ) as msg  " +
                "FROM " +
                " eq_use u " +
                " join eq_stake st on u.stakeid=st.id   " +
                "join eq_materiel em on u.eqid = em.id   " +
                "left join eq_planeseat se on st.seatid=se.id "+
                "WHERE (u.bizstatus!=0 or u.errstatus!=0) and se.airportid = '"+airportid+"'";
        if(seatList.length>0){
            tsql+=" and se.id in ("+sb.toString()+") ";
        }


        Sql sql = Sqls.queryRecord(tsql);


        dao().execute(sql);
        return  sql.getList(Record.class);
    }
/*
    //20180412zhf1615
    //得到当前机位设备信息
    public NutMap getEqUseList(String status, String seatId, int length, int start, int draw, List<DataTableOrder> order, List<DataTableColumn> columns) {

        Cnd cnd=Cnd.NEW();
        NutMap re = new NutMap();
        Sql sql = Sqls.queryRecord(" SELECT eu.id,em.eqnum,em.eqname,eu.pstatus,eu.errstatus FROM eq_use eu LEFT JOIN eq_materiel em ON eu.eqid = em.id LEFT JOIN eq_stake es ON eu.stakeid = es.id WHERE es.seatid =  '" + seatId + "'" + ("3".equals(status)?" AND eu.errstatus  != '0'  ":"") + (("2".equals(status)?" AND  (  eu.errstatus  = '0' AND eu.pstatus != '0') ":"")) +("1".equals(status)?" AND (   eu.errstatus  = '0' AND eu.pstatus = '0') ":"") );
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
*/
    @Override
    public NutMap getList(int length, int start, int draw, List<DataTableOrder> orders, List<DataTableColumn> columns, Cnd cnd) {
        NutMap re = new NutMap();
        Sql sql = Sqls.queryRecord(" SELECT " +
                "em.eqnum,em.eqname," +
                "es.stakename,"  +
                "eu.id,eu.pstatus,eu.errstatus, " +
                "su.name as suname, " +
                "sus.username," +
                "eui.pstatus as ustatus," +
                "sui.name as suiname, " +
                "eui.starttime as starttime " +
                "FROM eq_use eu " +
                "LEFT JOIN eq_materiel em ON eu.eqid = em.id " +
                "LEFT JOIN eq_stake es ON eu.stakeid = es.id " +
                "LEFT JOIN sys_unit su ON eu.equnitid = su.id " +
                "LEFT JOIN eq_useinfo eui ON eu.equseinfoid = eui.id " +
                " LEFT JOIN  sys_user sus ON eui.personid = sus.id " +
                " LEFT JOIN  sys_unit sui ON eui.personunitid = sui.id "
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
    @Aop(TransAop.READ_COMMITTED)
    @Async  //线程池异步方式，提高设备访问的处理能力
    public int getInfo(Map map) {
        String type = "",bikeid="",lockstate="",lon="",lat="",time="",power="";
        Object otype = map.get("type");
        Object obikeid = map.get("bikeid");
        Object olockstate = map.get("lockstate");
        Object olon = map.get("lon");
        Object olat = map.get("lat");
        Object otime = map.get("time");
        Object opower = map.get("power");
        if(otype!=null){
            type = (String)otype;
        }
        if(obikeid!=null){
            bikeid = (String)obikeid;
        }
        if(olockstate!=null){
            lockstate = (String)olockstate;
        }
        if(olon!=null){
            lon = (String)olon;
        }
        if(olat!=null){
            lat = (String)olat;
        }
        if(otime!=null){
            time = (String)otime;
        }
        if(opower!=null){
            power = (String)opower;
        }

        if(!Strings.isBlank(type)){
            if(type.equals("1")||type.equals("0")){
              return  this.lockinfo(type,bikeid,lockstate,time);
            }else if(type.equals("2")){
              return   this.mapinfo(bikeid,lockstate,lon,lat,time,power);
            }
        }
        return 0;
    }

    //开关锁，修改eq_use
    private int lockinfo(String type,String bikeid,String lockstate,String time){
            if(!Strings.isBlank(bikeid)){
                Cnd cnd= Cnd.NEW();
                cnd.and("lockid","=",bikeid);
                eq_materiel eqMateriel = eqMaterielService.fetch(cnd);
                if(eqMateriel==null){
                    luckyLog.info("-->"+"未找到锁["+bikeid+"]对应的设备资料！");
                    return 0;
                }
                String eqid=eqMateriel.getId();
                eq_lock eqLock = eqLockService.fetch(eqMateriel.getLid());
                if(eqLock==null){
                    luckyLog.info("-->"+"未找到["+bikeid+"]对应的智能锁资料！");
                    return 0;
                }
                Cnd e=Cnd.NEW();
                e.and("eqid","=",eqid);
                eq_use eqUse=this.fetch(e);
                if(eqUse==null){
                    luckyLog.info("-->"+"未找到锁["+bikeid+"]对应的设备的上线资料信息！");
                    return 0;
                }
                String equseinfoid = null;
                String pstatus = null;
                boolean isSendSocket= false;//是否需要推送消息到首页

                //处理由于网络延迟等造成的收到的用户重复操作的命令 add by koudepei 20180823
                String loType = eqLock.getLastopType();
                String loTime = eqLock.getLastopTime();
                if(Strings.isNotBlank(loTime)&& Strings.isNotBlank(time) && time.compareTo(loTime)<=0){//收到过期的命令不进行任何处理
                    luckyLog.warn("抛弃收到的过期getinfo消息：type="+type+",bikeid="+bikeid+",lockstate="+lockstate+",time="+time);
                    return 0;
                }
                if(Strings.isNotBlank(loType)&& (type+""+lockstate).compareTo(loType)==0){//收到了相同的命令
                    // 注：目前关锁失败和开锁成功一致，开锁失败和关锁成功一致，因此这里会阻止失败命令继续向下执行，后期版本type会区分成功和失败的类型!
                    if((lockstate.equals("0")&& "1".equals(eqUse.getPstatus()))|| (lockstate.equals("1")&& "0".equals(eqUse.getPstatus()))){
                        //开关锁失败，需要记录操作失败标识，供app端操作时动态识别处理
                        String failStatus="";
                        if(lockstate.equals("0")&& "1".equals(eqUse.getPstatus())){
                            failStatus = "2";
                        }
                        if(lockstate.equals("1")&& "0".equals(eqUse.getPstatus())){
                            failStatus = "1";
                        }
                        Chain chain = Chain.make("failStatus", failStatus);
                        this.update(chain, Cnd.where("id", "=", eqUse.getId()));
                    }
                    luckyLog.warn("抛弃连续收到的相同的getinfo消息：type="+type+",bikeid="+bikeid+",lockstate="+lockstate+",time="+time+",pstatus="+eqUse.getPstatus());
                    return 1;
                }

                if(lockstate.equals("0")){//开锁中 或  关锁失败？
                    luckyLog.info("开锁:"+bikeid+"--> openLockMap中锁的数量:"+openLockMap.keySet().size()+":openLockMap.get(bikeid)="+openLockMap.get(bikeid));
//                    if("1".equals(eqUse.getPstatus())){//临时表示关锁失败信息 或者收到重复的开锁延迟的反馈
//                        return 1;
//                    }
//                    if(Strings.isNotBlank(eqUse.getEquseinfoid())){//重复开锁或关锁失败时，造成原关联的eq_userinfo数据被刷新
//                        return 0;
//                    }
                    if("0".equals(eqUse.getPstatus())) {
                        isSendSocket = true;
                    }
                    pstatus = "1";
                    //防止移动端调用getUnlockStatus接口失败,没有往eq_useinfo表中写入数据 add by koudepei 20180505
                    //先添加一条数据到eq_useinfo表，移动端调用getUnLockStatus时更新使用人员信息即可
                    String equsenum = getEqUseNum(eqUse);
                    String sstarttime = newDataTime.getDateYMDHMS();
                    String personid = "";
                    String personunitid = "";
                    String pp = openLockMap.get(bikeid);
                    if(Strings.isNotBlank(pp)){
                       String[]ppArr = pp.split("::");
                        personid = ppArr[0];
                        if(ppArr.length>1)
                            personunitid = ppArr[1];
                        openLockMap.remove(bikeid);
                    }
                    equseinfoid = eqUseinfoService.insertInfo(equsenum, eqMateriel.getId(), personid, personunitid, sstarttime, "", sstarttime, "", "0", eqMateriel.getEqunitid(),eqMateriel.getLid());
                    luckyLog.info("开锁完成："+bikeid+";开锁人："+personid);
                }else if(lockstate.equals("1")){//关锁中  或者 开锁失败？
                    luckyLog.info("关锁:"+bikeid+"--> openLockMap中锁的数量:"+openLockMap.keySet().size()+":openLockMap.get(bikeid)="+openLockMap.get(bikeid));
//                    if("0".equals(eqUse.getPstatus())){//临时表示开锁失败信息?
//                        return 1;
//                    }
                    pstatus = "0";
                    String useinfoid = eqUse.getEquseinfoid();
                    if(!Strings.isBlank(useinfoid)){
                        eq_useinfo eqUseinfo = eqUseinfoService.fetch(useinfoid);
                        eqUseinfo.setSendtime(newDataTime.getDateYMDHMS());
                        eqUseinfo.setEndtime(newDataTime.getDateYMDHMS());
                        eqUseinfo.setPstatus("1");
//                        eqUseinfo.setRemark("归还处理！");
                        eqUseinfoService.updateIgnoreNull(eqUseinfo);
                    }
                    equseinfoid="";
                    if("1".equals(eqUse.getPstatus())) {
                        isSendSocket = true;
                    }
                    luckyLog.info("关锁完成："+bikeid);
                }
                Chain chain = Chain.make("equseinfoid", equseinfoid).add("pstatus",pstatus );
                if("3".equals(eqUse.getErrstatus()) && ("0".equals(lockstate) &&  "0".equals(eqUse.getPstatus()))||("1".equals(lockstate) &&  "1".equals(eqUse.getPstatus()))){//解除异常
                    chain.add("errstatus","0");
                    isSendSocket = true;
                }
                chain.add("failStatus","0");//失败状态归0 add by koudepei 20180823
                this.update(chain, Cnd.where("id", "=", eqUse.getId()));
                //记录本次操作命令和时间戳   add by koudepei 20180823
                Chain chainLock = Chain.make("lastopType", type+""+lockstate).add("lastopTime",time );
                eqLockService.update(chainLock, Cnd.where("id", "=", eqLock.getId()));

                if(isSendSocket){//开关锁推送消息到首页监控中
                    String airportId = null;
                    String seatId = null;
                    eq_stake eqStake = dao().fetch(eq_stake.class, eqMateriel.getStakeid());
                    if (eqStake != null) {
                        airportId = eqStake.getAirportId();
                        seatId = eqStake.getSeatid();
                        if (Strings.isNotBlank(airportId) && Strings.isNotBlank(seatId)) {
                            List<String> seatList = new ArrayList<>();
                            seatList.add(eqStake.getSeatid());
                            myWebsocket.each("home-" + airportId, new Each<Session>() {
                                public void invoke(int index, Session ele, int length) {
                                    // 逐个会话发送消息
                                    myWebsocket.sendJson(ele.getId(), new NutMap("action", "notify").setv("msg", seatList));
                                }
                            });
                        }
                    }
                }
            }
            return 1;
    }

    //查询地理信息，修改eq_use
    private int mapinfo(String bikeid,String lockstate,String lon,String lat,String time,String power){
            if(!Strings.isBlank(bikeid)){
                Cnd cnd= Cnd.NEW();
                cnd.and("lockid","=",bikeid);
                eq_materiel eqMateriel = eqMaterielService.fetch(cnd);
                if(eqMateriel==null){
                    luckyLog.info("-->"+"未找到锁["+bikeid+"]对应的设备资料！");
                    return 0;
                }
                eqMaterielService.fetchLinks(eqMateriel,"stake");
                String eqid=eqMateriel.getId();
                Cnd e=Cnd.NEW();
                e.and("eqid","=",eqid);
                eq_use eqUse=this.fetch(e);
                if(eqUse==null){
                    luckyLog.info("-->"+"未找到锁["+bikeid+"]对应的设备的上线资料信息！");
                    return 0;
                }
                Transform transform=new Transform();
                double mlon = Double.parseDouble(lon);
                double mlat = Double.parseDouble(lat);
                String position=null;
                if(mlon!=0.0 && mlat != 0.0){
                    position=transform.wgs84togcj02(mlon,mlat);
                }
                Chain chain = Chain.make("power", power);
                if(Strings.isNotBlank(position)){//未获取到新的地理位置时保留上次的地理位置信息 add by koudepei 20180823
                    chain.add("position", position);
                }
                boolean isSendSocket= false;//是否需要推送消息到首页
                boolean isUpdateErrStatus = false;//标识：是否要修改errstatus字段
                String bizType = null;//发送通知类型
                if(Strings.isNotBlank(power) && Strings.isNotBlank(Globals.PowerWarn)){
                    try {
                        int pw = Integer.valueOf(Globals.PowerWarn);
                        int oldp = Integer.valueOf(eqUse.getPower());
                        int newp = Integer.valueOf(power);
                        if(!"3".equals(eqUse.getErrstatus()) && ("0".equals(lockstate) &&  "0".equals(eqUse.getPstatus()))){//当锁状态和数据库中数据状态不一致时，发送异常信息
                           //只处理锁实际为开，数据库为关的异常标识情况；对于实际为关，数据库为开需要修正处理
                            chain.add("errstatus","3");
                            isUpdateErrStatus = true;
                            isSendSocket = true;
                            bizType = "3";//异常
                        }else if("3".equals(eqUse.getErrstatus()) && (("0".equals(lockstate) &&  "1".equals(eqUse.getPstatus()))||("1".equals(lockstate) &&  "0".equals(eqUse.getPstatus())))){//解除异常
                            chain.add("errstatus","0");
                            isUpdateErrStatus = true;
                            isSendSocket = true;
                        }else if(newp<=pw && oldp>pw && "0".equals(eqUse.getErrstatus())){//电量降到预警电量，需要发送系统通知，注意防止重复发送预警消息
                            chain.add("errstatus","1");
                            isUpdateErrStatus = true;
                            isSendSocket = true;
                            bizType = "1";//预警
                        }else if("1".equals(eqUse.getErrstatus()) && newp>pw && Math.abs(newp-oldp)>10){//恢复电量后,注意获取电量值不稳定，会在1个单位内跳动，
                            chain.add("errstatus","0");
                            isUpdateErrStatus = true;
                            isSendSocket = true;
                        }
                    }catch (Exception e1){
                        luckyLog.error("-->"+e1.getMessage());
                    }
                }
                //对于实际为关，数据库为开，此时需要修正处理，解除异常改变数据库中的状态为关，方便别个借用
                if("1".equals(lockstate) &&  "1".equals(eqUse.getPstatus())){
                    if(!isUpdateErrStatus &&("2".equals(eqUse.getErrstatus()) ||"3".equals(eqUse.getErrstatus()) || "4".equals(eqUse.getErrstatus()))) {
                        chain.add("errstatus", "0");
                        isUpdateErrStatus = true;
                    }
                    //关锁处理
                    chain.add("pstatus","0");
                    chain.add("equseinfoid","");
                    String useinfoid = eqUse.getEquseinfoid();
                    if(!Strings.isBlank(useinfoid)){
                        eq_useinfo eqUseinfo = eqUseinfoService.fetch(useinfoid);
                        eqUseinfo.setSendtime(newDataTime.getDateYMDHMS());
                        eqUseinfo.setEndtime(newDataTime.getDateYMDHMS());
                        eqUseinfo.setPstatus("1");
                        eqUseinfo.setRemark("系统自动归还处理！");
                        eqUseinfoService.updateIgnoreNull(eqUseinfo);
                    }
                    isSendSocket = true;
                }
                //若之前是信号失联，则归0
                if(!isUpdateErrStatus && "4".equals(eqUse.getErrstatus())){
                    chain.add("errstatus","0");
                    isSendSocket = true;
                }
                this.update(chain, Cnd.where("id", "=", eqUse.getId()));
                //增加记录到锁跟踪记录表 add by koudepei 20180311
                eq_trace eqTrace = new eq_trace();
                eqTrace.setEqid(eqMateriel.getId());
                eqTrace.setLockid(eqMateriel.getLockid());
                eqTrace.setEquseid(eqUse.getId());
                eqTrace.setEquseinfoid(eqUse.getEquseinfoid());
                eqTrace.setPower(power);
                eqTrace.setPosition(position);
                eqTrace.setLockstate(lockstate);
                eqTrace.setTime(time);
                eqTraceService.insert(eqTrace);

                if(isSendSocket) {//推送消息到首页，更新相关设备机位信息
                    String airportId = null;
                    String seatId = null;
                    eq_stake eqStake = dao().fetch(eq_stake.class, eqMateriel.getStakeid());
                    if (eqStake != null) {
                        airportId = eqStake.getAirportId();
                        seatId = eqStake.getSeatid();
                        if (Strings.isNotBlank(airportId) && Strings.isNotBlank(seatId)) {
                            List<String> seatList = new ArrayList<>();
                            seatList.add(eqStake.getSeatid());
                            myWebsocket.each("home-" + airportId, new Each<Session>() {
                                public void invoke(int index, Session ele, int length) {
                                    // 逐个会话发送消息
                                    myWebsocket.sendJson(ele.getId(), new NutMap("action", "notify").setv("msg", seatList));
                                }
                            });
                        }
                    }
                    if (Strings.isNotBlank(bizType)) {//产生预警或者异常时，发送系统通知消息和企业微信消息给设备管理员
                        Map<String, List<String>> roleAndUser = sysRoleService.getUsersByRole(eqMateriel.getEqunitid(), null, "设备管理员");
                        Set<String> userIds = new HashSet<String>();
                        //发送预警通知消息20180421
                        String content = (eqMateriel.getStake()!=null?eqMateriel.getStake().getStakename():"")+"设备[" + eqMateriel.getEqcode() + "]出现" + (bizType.equals("3") ? "异常，实际锁状态与系统不一致!" : "电量不足") + "，请尽快核实处理!";
                        for (Map.Entry<String, List<String>> entry : roleAndUser.entrySet()) {
                            String roleId = entry.getKey();
                            List<String> userIdList = entry.getValue();
                            userIds.addAll(userIdList);
                            for (String userId : userIdList) {
                                msgMessageService.addMessage("2", bizType, "0", "2", content, null, userId, null, airportId, eqMateriel.getId(), "/platform/eq/materiel/detail/" + eqMateriel.getId());
                            }
                        }
                        //发送企业微信消息给设备管理员角色的用户
                        if ("1".equals(Globals.WxCorpStart)) {
                            if (userIds.size() > 0) {
                                luckyLog.debug("-->"+"设备产生预警或异常发送企业微信号消息,userIds=" + userIds);
                                sysWxService.sendWxMessageAsy(userIds, content.toString());
                            }
                        }
                    }
                }
            }
            return 1;
    }

    //发送开锁指令
    @Override
    public int unLockProcess(String lockID, eq_use use, eq_materiel materiel, String personid, String personunitid, String starttime) {
        luckyLog.info("APP用户："+personid+",准备发送开锁指令，锁号："+lockID);
        int ret = -1;
        if(use==null||materiel==null||Strings.isBlank(lockID))return ret;
        if(reqOpenLockMap.get(lockID)!=null && Strings.isNotBlank(personid)){//防止重复发送
            String pp = reqOpenLockMap.get(lockID);
            String[]ppArr = pp.split("::");
            String oldPersonid = ppArr[0];
            long old = Long.valueOf(ppArr[1]);
            long now = (new Date()).getTime();
            if(personid.equals(oldPersonid) && now-old<Long.valueOf(Globals.opInterval).longValue()){//控制同一用户连续重复对同一个锁发送开锁指令
                long s = Long.valueOf(Globals.opInterval).longValue()/1000;
                throw new ValidatException("操作频繁，请在"+s+"秒后重试!");
            }else{
                reqOpenLockMap.put(lockID,personid+"::"+(new Date()).getTime());
            }
        }else if(reqOpenLockMap.get(lockID)==null && Strings.isNotBlank(personid)){
            reqOpenLockMap.put(lockID,personid+"::"+(new Date()).getTime());
        }
        //第二步 解锁
        //获取地点标记
        String url = Globals.luckyUrl + Globals.unLock;
        PostRun pr = new PostRun();
        String Json = pr.post(url, lockID, "1");
        if(Json==null){
            throw new ValidatException("操作失败，无法连接设备通讯服务，请联系系统管理员！");
        }
        Object o = org.nutz.json.Json.fromJson(Json);
        if (o instanceof HashMap) {
            String  status= ((HashMap) o).get("status").toString();
            //第三步 修改use表pstatus为1
            if (("1").equals(status)) {//解锁成功
                // 注意：这里仅仅是指令发送成功，如下逻辑在判断解锁成功后处理 此处不需要更新数据库201804225
                luckyLog.info("APP用户："+personid+",成功发送了开锁指令，锁号："+lockID);
                if(!"0".equals(use.getFailStatus())) {
                    //failStatus归0，防止之前开锁失败数据造成影响
                    Chain chain = Chain.make("failStatus", "0");
                    this.update(chain, Cnd.where("id", "=", use.getId()));
                }
                ret = 1;
            }else if (("0").equals(status)) {
                luckyLog.info("APP用户："+personid+",发送开锁指令，lucky返回失败，锁号："+lockID);
                ret =0;
            }
            openLockMap.put(lockID,personid+"::"+personunitid+"::"+(new Date()).getTime());
        }
        luckyLog.info("APP用户："+personid+",完成发送开锁指令，锁号："+lockID);

        return ret;
    }

    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void unlockSuccess(eq_use use, eq_materiel materiel, String personid, String personunitid) {
        log.debug("personid:"+personid+"|use:"+use+"|materiel:"+materiel);
        if(use==null||materiel==null)return;
        //正常情况下use.getEquseinfoid()不应该为空，因为在getinfo接口的开锁指令会先新增一条数据到eq_useinfo表，所以此处只需要更新使用人员信息即可 update by koudepei 20180505
        if(Strings.isBlank(use.getEquseinfoid())){
            String equsenum = getEqUseNum(use);
            String sstarttime = newDataTime.getDateYMDHMS();
            String pstatus = "0";
            String equseinfoid = eqUseinfoService.insertInfo(equsenum, materiel.getId(), personid, personunitid, sstarttime, "", sstarttime, "", pstatus, materiel.getEqunitid(),materiel.getLid());
            if (equseinfoid!=null) {
                //第五步 修改eq_use表中的useinfoid
//                use.setEquseinfoid(equseinfoid);
//                this.updateIgnoreNull(use);
                this.update(Chain.make("equseinfoid", equseinfoid), Cnd.where("id", "=", use.getId()));
            }
        }else{//存在就更新人员信息
//            eq_useinfo eqUseinfo = eqUseinfoService.fetch(use.getEquseinfoid());
//            eqUseinfo.setPersonid(personid);
//            eqUseinfo.setPersonunitid(personunitid);
//            eqUseinfoService.updateIgnoreNull(eqUseinfo);
            eqUseinfoService.update(Chain.make("personid", personid).add("personunitid",personunitid ), Cnd.where("id", "=", use.getEquseinfoid()));
        }
    }

    private String getEqUseNum(eq_use use) {
        //20180319zhf1214
        //eq-useinfo编码规则:KM001-J20180001
        StringBuilder sb = new StringBuilder(80);
        if (!Strings.isBlank(use.getId())) {
            //根据提交的use得到上线设备使用对象
            eq_use eqUse = this.fetch(use.getId());
            if (!Strings.isBlank(eqUse.getEqunitid())) {
                //得到相应的单位对象
                Sys_unit sysUnit = sysUnitService.fetch(eqUse.getEqunitid());
                String unitairport = sysUnit.getUnitairport();
                if (!Strings.isBlank(unitairport)) {
                    //得到相应的机场对象
                    base_airport baseAirport = baseAirportService.fetch(unitairport);
                    if (!Strings.isBlank(baseAirport.getAirportname())) {
                        //将机场名的前两个字的首子母提取出来,并大写
                        String firstSpell = PinYinUtil.getFirstSpell(baseAirport.getAirportname().substring(0, 2));
                        sb.append(firstSpell.toUpperCase()).append(baseAirport.getAirportnum()).append("-J");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                        String formatDate = sdf.format(new Date());
                        sb.append(formatDate);
                        //今天的使用明细条数
                        int useinfoCountOfToday = eqUseinfoService.count("eq_useinfo",
                                Cnd.where("createTime", ">=", newDataTime.getIntegerByDate(newDataTime.startOfToday())).
                                        and("createTime", "<=", newDataTime.getIntegerByDate(newDataTime.endOfToday())));
                        sb.append(String.format("%04d", useinfoCountOfToday + 1));
                    }
                }
            }
        }
        return sb.toString();
    }

    //发送关锁指令
    @Override
    public int lockProcess(String lockID, String personid, eq_materiel materiel, eq_use use) {
        luckyLog.info("APP用户："+personid+",准备发送关锁指令，锁号："+lockID);
        int ret=-1;
        if(use==null||materiel==null||Strings.isBlank(lockID))return ret;
        if(reqReturnLockMap.get(lockID)!=null && Strings.isNotBlank(personid)){//防止重复发送
            String pp = reqReturnLockMap.get(lockID);
            String[]ppArr = pp.split("::");
            String oldPersonid = ppArr[0];
            long old = Long.valueOf(ppArr[1]);
            long now = (new Date()).getTime();
            if(personid.equals(oldPersonid) && now-old<Long.valueOf(Globals.opInterval).longValue()){//控制同一用户连续重复对同一个锁发送关锁指令
                long s = Long.valueOf(Globals.opInterval).longValue()/1000;
                throw new ValidatException("操作频繁，请在"+s+"秒后重试!");
            }else{
                reqReturnLockMap.put(lockID,personid+"::"+(new Date()).getTime());
            }
        }else if(reqReturnLockMap.get(lockID)==null && Strings.isNotBlank(personid)){
            reqReturnLockMap.put(lockID,personid+"::"+(new Date()).getTime());
        }

        String url = Globals.luckyUrl + Globals.unLock;
        PostRun pr = new PostRun();
        String Json = pr.post(url, lockID, "2");
        if(Json==null){
            throw new ValidatException("操作失败，无法连接设备通讯服务，请联系系统管理员！");
        }
        Object o = org.nutz.json.Json.fromJson(Json);
        if (o instanceof HashMap) {
            String  status= ((HashMap) o).get("status").toString();
            if (("1").equals(status)) {//归还成功
                //此处仅仅是调用归还指令成功，还需要结合getLockStatus接口判断是否锁已归还，此处不需要更新数据库201804225
                luckyLog.info("APP用户："+personid+",成功发送归还设备指令，锁号:"+lockID);
                if(!"0".equals(use.getFailStatus())) {
                    //failStatus归0，防止之前操作失败记录造成影响，直接抛出此提示
                    Chain chain = Chain.make("failStatus", "0");
                    this.update(chain, Cnd.where("id", "=", use.getId()));
                }
                ret = 1;
            }else if("0".equals(status)){
                luckyLog.info("APP用户："+personid+",发送归还设备指令,lucky返回失败，锁号:"+lockID);
                ret = 0;
            }
        }
        luckyLog.info("APP用户："+personid+",完成发送关锁指令，锁号："+lockID);
        return ret;
    }


    public NutMap getEqUsePowerData(String stakenum, String locknum, int length, int start, int draw, List<DataTableOrder> orders, List<DataTableColumn> columns, Cnd cnd) {
        NutMap re = new NutMap();
        String sqlStr = "select m.id eqid,m.eqname,s.stakenum,l.locknum,m.eqnum,u.power,u.pstatus,u.errstatus,u.bizstatus,u.failStatus, " +
                "t1.position,t1.time,u.equseinfoid,ui.personid,ui.personunitid,ui.starttime,ui.endtime " +
                " from eq_use u " +
                "left join eq_materiel m on m.id=u.eqid " +
                "left join eq_stake s on s.id=u.stakeid " +
                "left join eq_useinfo ui on ui.id=u.equseinfoid " +
                "left join eq_lock l on l.id=m.lid " +
                "left join (select t.* from (select eqid,lockid,position,power,time from eq_trace order by lockid, time desc ) t group by t.lockid )t1 on t1.eqid=m.id " +
                "WHERE 1 = 1 " +
                (!Strings.isBlank(stakenum) ? " AND s.stakenum LIKE '%"+stakenum+"%' ":" ")+
                (!Strings.isBlank(locknum) ? " AND l.locknum LIKE '%"+locknum+"%' ":" ");
        Sql sql = Sqls.queryRecord(sqlStr);
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
}
