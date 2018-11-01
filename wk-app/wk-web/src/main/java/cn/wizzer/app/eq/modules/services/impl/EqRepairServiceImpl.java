package cn.wizzer.app.eq.modules.services.impl;

import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.app.base.modules.services.BaseAirportService;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.eq.modules.models.*;
import cn.wizzer.app.eq.modules.services.EqMaterielService;
import cn.wizzer.app.eq.modules.services.EqUseService;
import cn.wizzer.app.msg.modules.models.Msg_option;
import cn.wizzer.app.msg.modules.services.MsgAssignService;
import cn.wizzer.app.msg.modules.services.MsgMessageService;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.app.sys.modules.services.SysRoleService;
import cn.wizzer.app.sys.modules.services.SysWxService;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.modules.controllers.open.api.Datatime.newDataTime;
import cn.wizzer.app.web.modules.controllers.platform.eq.EqMaterielController;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.eq.modules.services.EqRepairService;
import cn.wizzer.framework.page.OffsetPager;
import cn.wizzer.framework.util.PinYinUtil;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.framework.websocket.MyWebsocket;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
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
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import javax.websocket.Session;
import java.text.SimpleDateFormat;
import java.util.*;

@IocBean(args = {"refer:dao"})
public class EqRepairServiceImpl extends BaseServiceImpl<eq_repair> implements EqRepairService {
    private static final Log log = Logs.get();
    @Inject
    private EqMaterielController eqMaterielController;
    @Inject
    private BaseAirportService baseAirportService;
    @Inject
    private BasePersonService basePersonService;
    @Inject
    private EqMaterielService eqMaterielService;
    @Inject
    private BaseCnctobjService baseCnctobjService;
    @Inject
    private SysRoleService sysRoleService;
    @Inject
    private MsgAssignService msgAssignService;
    @Inject
    private SysWxService sysWxService;
    @Inject
    private MsgMessageService msgMessageService;
    @Inject
    private EqUseService eqUseService;

    @Inject
    protected MyWebsocket myWebsocket;

    public EqRepairServiceImpl(Dao dao) {
        super(dao);
    }


    public NutMap getRepairList(String repnum, String personname, String pstatus,String eqname,String eqnum, int start, int length, int draw){
        Cnd cnd=Cnd.NEW();
        NutMap re = new NutMap();
        Sql sql = Sqls.queryRecord("select a.id, a.repnum,a.pstatus,FROM_UNIXTIME(a.createTime) createtime,b.eqname,b.eqnum,c.username,d.username wxname,f.unitid,f.deptid from eq_repair a " +
                " left join eq_materiel b on a.eqid=b.id " +
                " left join sys_user c on a.personid=c.id " +
                " left join sys_user d on a.wxuserid=d.id " +
                " left join base_cnctobj e on a.personid=e.userId" +
                " left join base_person f on e.personId=f.id"+
                " where 1=1 " +
                (!Strings.isBlank(repnum)?" and repnum like '%"+repnum+"%' ":" ") +
                (!Strings.isBlank(personname)?"and username like '%"+personname+"%' ":" ") +
                (!Strings.isBlank(pstatus)?"and pstatus='"+pstatus+"' ":" ") +
                (!Strings.isBlank(eqname)?"and eqname like '%"+eqname+"%' ":" ") +
                (!Strings.isBlank(eqnum)?"and eqnum like '%"+eqnum+"%' ":" ")
        );

        cnd =  sysRoleService.getPermission(cnd,"b.equnitid","deptid","a.personid",true);

        Pager pager = new OffsetPager(start, length);
//        sql.setCondition(cnd.orderBy("createTime","desc"));
        sql.setSourceSql(sql.getSourceSql()+cnd.toString().replace("WHERE","and"));
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
    public void editDo(eq_repair eqRepair) {
        log.debug("修改维修单"+eqRepair.getRepnum()+"="+eqRepair);
        eq_repair old = this.fetch(eqRepair.getId());
        eqRepair.setOpBy(StringUtil.getUid());
        eqRepair.setOpAt((int) (System.currentTimeMillis() / 1000));
        this.updateIgnoreNull(eqRepair);
        //20180329zhf1806
        addRepairTrack(eqRepair);

        //如果是从报修中改成维修中，或者从维修中改成已完成，那么都需要修改use表里的pstatus状态
        if((!"2".equals(old.getPstatus()) && "2".equals(eqRepair.getPstatus())) ||
                (!"0".equals(old.getPstatus()) && "0".equals(eqRepair.getPstatus())) ){
            Cnd cnd=Cnd.NEW();
            cnd.and("eqid","=",eqRepair.getEqid());
            eq_use use=dao().fetch(eq_use.class,cnd);
            if(use!=null){
                if("2".equals(eqRepair.getPstatus()))//维修中
                {
                    use.setBizstatus("2");//维修锁定
                }else if("0".equals(eqRepair.getPstatus()))//已完成
                {
                    use.setBizstatus("0");//正常
                }
                dao().updateIgnoreNull(use);
            }
        }

    }

    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void auditHandler(NutMap objMap,String type) {
        eq_repair eqRepair  = Json.fromJson(eq_repair.class, Json.toJson(objMap, JsonFormat.compact()));
        log.debug("审批维修单:"+eqRepair.getRepnum()+"="+eqRepair);
        if("2".equals(eqRepair.getPstatus())||"0".equals(eqRepair.getPstatus())){
            String eqid = eqRepair.getEqid();
            Cnd cnd=Cnd.NEW();
            cnd.and("eqid","=",eqid);
            eq_use eqUse = eqUseService.fetch(cnd);
            if(eqUse!=null ){
                if("2".equals(eqUse.getBizstatus())) {
                    throw new ValidatException("设备已锁定，请确认设备状态！");
                }
//                else if(!"0".equals(eqUse.getPstatus())) {
//                    throw new ValidatException("设备未归还，请先归还设备才能进行维修！");
//                }
            }
        }

        if(!"0".equals(type) && !"1".equals(type)){//流程未结束
            eqRepair.setPstatus("1");
        }
        this.editDo(eqRepair);
        if(type!=null && ("0".equals(type) || "1".equals(type))){//流程结束
            if("2".equals(eqRepair.getPstatus()))//维修中
            { //添加提醒通知消息
                log.debug("审批维修单发送通知消息 ");
                msgMessageService.addMessage("2","2","0","1","您收到设备维修单["+eqRepair.getRepnum()+"],请进行维修！",null,eqRepair.getWxuserid(),eqRepair.getId(),"/platform/eq/repair/detail/"+eqRepair.getId());
            }
            //发送企业微信消息给设备管理员角色的用户
            if("1".equals(Globals.WxCorpStart)) {
                log.debug("审批维修单发送企业微信消息");
                StringBuffer content = new StringBuffer("");
                content.append("您收到设备维修单["+eqRepair.getRepnum()+"],请进行维修！");
                Set userIds = new HashSet();
                userIds.add(eqRepair.getWxuserid());
                sysWxService.sendWxMessageAsy(userIds, content.toString());
            }

            //webSocket 发送消息到首页地图刷新
            String eqid = eqRepair.getEqid();
            Record mat = this.dao().fetch("eq_materiel",Cnd.where("id","=",eqid),"stakeid");
            String stakeid = mat.getString("stakeid");
            if(Strings.isNotBlank(stakeid)){
                Record record = this.dao().fetch("eq_stake",Cnd.where("id", "=", stakeid),"airportId|seatid");  // ;
                String airportId = record.getString("airportId");
                String seatid = record.getString("seatid") ;
                if(StringUtils.isNotBlank(airportId)&&StringUtils.isNotBlank(seatid)){
                    List<String>seatList = new ArrayList<>();
                    seatList.add(seatid);
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

    //维修完成
    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void complete(String[] ids) {
        if(ids != null && ids.length>0){
            List<String> eqIdList = new ArrayList<>();
            List<String> errorList = new ArrayList<>();
            List<eq_repair> repairList = dao().query(this.getEntityClass(),Cnd.where("id","in",ids));
            for(eq_repair repair:repairList){
                if(!"2".equals(repair.getPstatus())){
                    errorList.add(repair.getRepnum());
                }
                repair.setPstatus("0");
                eqIdList.add(repair.getEqid());
            }
            if(errorList.size()>0){
                throw new ValidatException("维修单"+errorList.toString()+"不是维修中状态，不能此操作！");
            }

            this.update(Chain.make("pstatus", "0"),Cnd.where("id","in",ids));
            this.update("eq_use",Chain.make("bizstatus", "0"),Cnd.where("eqid","in",eqIdList));

            //webSocket 发送消息到首页地图刷新
            List<eq_materiel> matList = this.dao().query(eq_materiel.class,Cnd.where("id","in",eqIdList));
            List<String> stakeIdList = new ArrayList<>();
            for(eq_materiel eqMateriel:matList){
                stakeIdList.add(eqMateriel.getStakeid());
            }
            List<eq_stake> stakeList = this.dao().query(eq_stake.class,Cnd.where("id","in",stakeIdList));
            List<String>seatList = new ArrayList<>();
            String airportId = "";
            for(eq_stake eqStake:stakeList){
                airportId = eqStake.getAirportId();
                if(!seatList.contains(eqStake.getSeatid())) {
                    seatList.add(eqStake.getSeatid());
                }
            }
            if(StringUtils.isNotBlank(airportId)&&seatList.size()>0){
                myWebsocket.each("home-"+airportId, new Each<Session>() {
                    public void invoke(int index, Session ele, int length) {
                        // 逐个会话发送消息
                        myWebsocket.sendJson(ele.getId(), new NutMap("action", "notify").setv("msg", seatList ));
                    }
                });
            }
        }
    }

    @Override
    @Aop(TransAop.READ_COMMITTED)
    public eq_repair insertRepair(String reptype, String eqid, String reptext, String personid, String pstatus) {
        eq_repair repair=new eq_repair();
        eq_materiel eqMateriel = null;
        if(!Strings.isBlank(pstatus)) {
            repair.setPstatus(pstatus);
        }else{
            throw new ValidatException("参数值pstatus不能为空!");
        }
        if(!Strings.isBlank(eqid)) {
            repair.setEqid(eqid);
            eqMateriel = eqMaterielService.fetch(eqid);
        }
        if(eqMateriel==null){
            throw new ValidatException("参数值设备信息不能为空!");
        }
        if(!Strings.isBlank(personid)){
            repair.setPersonid(personid);
            Record record1 = dao().fetch("base_cnctobj",Cnd.where("userId", "=", personid),"personId");
            String personId1 = record1.getString("personId");
            if(StringUtils.isNotBlank(personId1)){
                Record record2 = dao().fetch("base_person",Cnd.where("id", "=", personId1),"airportid");
                String airportid = record2.getString("airportid");
                if(!Strings.isBlank(airportid)){
                    String repnum = getSpecialNum(airportid,"X");
                    repair.setRepnum(repnum);
                }
            }
        }
        if(StringUtils.isBlank(repair.getRepnum())){
            repair.setRepnum("TEMP"+System.currentTimeMillis());
        }
        if(!Strings.isBlank(reptext))
            repair.setReptext(reptext);
        if(!Strings.isBlank(reptype))
            repair.setReptype(reptype);
        eq_repair r = this.insert(repair);
        //改为报修中
        eqUseService.update(Chain.make("bizstatus", "1"), Cnd.where("eqid", "=", eqid));

        //系统内按角色发送，企业微信只能按用户发送
        Map<String,List<String>> roleAndUser = sysRoleService.getUsersByRole(eqMateriel.getEqunitid(),null,"设备管理员");
        Set<String> userIds = new HashSet<String>();
        //发送待办任务20180421
        for(Map.Entry<String,List<String>> entry:roleAndUser.entrySet()){
            String roleId = entry.getKey();
            List<String> userIdList = entry.getValue();
            userIds.addAll(userIdList);
            msgAssignService.addAssignToRole("维修单-"+r.getRepnum(),r.getId(),"您收到维修申报单["+r.getRepnum()+"]，请立即处理!",null,"1",roleId,userIdList,personid
                    ,"/platform/eq/repair/audit/"+r.getId(),"/platform/eq/repair/detail/"+r.getId(),EqRepairServiceImpl.class.getName());
        }
        //发送企业微信消息给设备管理员角色的用户
        if("1".equals(Globals.WxCorpStart)) {
            if (userIds.size() > 0) {
                Cnd cnd = Cnd.NEW();
                cnd.and("userId", "=", personid);
                String npersonid = baseCnctobjService.fetch(cnd).getPersonId();
                base_person basePerson = basePersonService.fetch(npersonid);
                StringBuffer content = new StringBuffer("");
                content.append(basePerson==null?"":basePerson.getPersonname()).append("提交了").append(eqMateriel==null?"":eqMateriel.getEqname()).append("[").append(eqMateriel==null?"":eqMateriel.getEqcode()).append("]").append("的维修单["+repair.getRepnum()+"]!");
                log.debug("报修单发送企业微信号消息,userIds="+userIds);
                sysWxService.sendWxMessageAsy(userIds, content.toString());
            }
        }
        return r;
    }

    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void updateRepair(String repairid, String repnum, String reptype, String eqid, String reptext, String personid, String pstatus) {
        eq_repair repair = this.fetch(repairid);
        eq_materiel eqMateriel = null;
        if(!Strings.isBlank(eqid)) {
            repair.setEqid(eqid);
            eqMateriel = eqMaterielService.fetch(eqid);
        }
        if(eqMateriel==null){
            throw new ValidatException("参数值设备信息不能为空!");
        }
        if(!Strings.isBlank(reptype))
            repair.setReptype(reptype);
        if(!Strings.isBlank(reptext))
            repair.setReptext(reptext);
        if(!Strings.isBlank(repnum))
            repair.setRepnum(repnum);
        if(!Strings.isBlank(personid))
            repair.setPersonid(personid);
        if(!Strings.isBlank(pstatus))
            repair.setPstatus(pstatus);
        int i=this.updateIgnoreNull(repair);

//        //系统内按角色发送，企业微信只能按用户发送  此接口暂时不使用了~~~~~
//        Map<String,List<String>> roleAndUser = sysRoleService.getUsersByRole(eqMateriel.getEqunitid(),null,"设备管理员");
        Set<String> userIds = new HashSet<String>();
//        //发送待办任务20180421
//        for(Map.Entry<String,List<String>> entry:roleAndUser.entrySet()) {
//            String roleId = entry.getKey();
//            List<String> userIdList = entry.getValue();
//            userIds.addAll(userIdList);
//            msgAssignService.addAssignToRole("维修单",repair.getId(),"您收到维修申报单["+repair.getRepnum()+"]，请立即处理!",null,"1",roleId,personid,"/platform/eq/repair/audit/"+repair.getId());
//        }

        //修改该维修单时 发送微信消息给设备管理员20180327 koudepei
        if("1".equals(Globals.WxCorpStart)) {
            if(userIds.size()>0) {
                Cnd cnd = Cnd.NEW();
                cnd.and("userId", "=", personid);
                String npersonid = baseCnctobjService.fetch(cnd).getPersonId();
                base_person basePerson = basePersonService.fetch(npersonid);
                StringBuffer content = new StringBuffer("");
                content.append(basePerson==null?"":basePerson.getPersonname()).append("重新提交了").append(eqMateriel==null?"":eqMateriel.getEqname()).append("[").append(eqMateriel==null?"":eqMateriel.getEqcode()).append("]").append("的维修单!");
                sysWxService.sendWxMessageAsy(userIds, content.toString());
            }
        }

    }


    //20180329zhf1806
    //添加一条eq_repairtrack
    private void addRepairTrack(eq_repair eqRepair){
        eq_repairtrack eqRepairtrack = new eq_repairtrack();
        eqRepairtrack.setEqRepairId(eqRepair.getId());
        eqRepairtrack.setOperaterId(((Sys_user) SecurityUtils.getSubject().getPrincipal()).getId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String operateTime = sdf.format(new Date());
        eqRepairtrack.setOperatetime(operateTime);
        eqRepairtrack.setRepairStatus(eqRepair.getPstatus());
        dao().insert(eqRepairtrack);
    }


    //20180322zhf1545
    //得到制定编码
    public String getSpecialNum(String airportId,String firstWord) {
        StringBuilder sb = new StringBuilder(80);
        if(!Strings.isBlank(airportId)){
            //得到相应的机场对象
            base_airport baseAirport = baseAirportService.fetch(airportId);
            if(!Strings.isBlank(baseAirport.getAirportname())){
                //将机场名的前两个字的首子母提取出来,并大写
                String firstSpell = PinYinUtil.getFirstSpell(baseAirport.getAirportname().substring(0, 2));
                sb.append(firstSpell.toUpperCase()).append(baseAirport.getAirportnum()).append("-").append(firstWord);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String formatDate = sdf.format(new Date());
                sb.append(formatDate);
                //今天的使用明细条数
                int useinfoCountOfToday = this.count("eq_repair",
                        Cnd.where("createTime", ">=", newDataTime.getIntegerByDate(newDataTime.startOfToday())).
                                and("createTime", "<=", newDataTime.getIntegerByDate(newDataTime.endOfToday())));
                sb.append(String.format("%04d",useinfoCountOfToday+1));
            }
        }
        return sb.toString();
    }


}
