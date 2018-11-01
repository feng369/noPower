package cn.wizzer.app.web.commons.quartz.job;

import cn.wizzer.app.eq.modules.models.eq_materiel;
import cn.wizzer.app.eq.modules.models.eq_stake;
import cn.wizzer.app.eq.modules.models.eq_use;
import cn.wizzer.app.eq.modules.services.EqMaterielService;
import cn.wizzer.app.eq.modules.services.EqUseService;
import cn.wizzer.app.msg.modules.services.MsgMessageService;
import cn.wizzer.app.sys.modules.models.Sys_task;
import cn.wizzer.app.sys.modules.services.SysRoleService;
import cn.wizzer.app.sys.modules.services.SysWxService;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.framework.util.DateUtil;
import cn.wizzer.framework.websocket.MyWebsocket;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Each;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.websocket.Session;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by koudepei on 2018/9/17.
 * 设备是否失去信号处理
 */
@IocBean
public class EqNoSignalJob implements Job {

    private static final Log log = Logs.get();
    @Inject
    protected Dao dao;

    @Inject
    private EqUseService eqUseService;
    @Inject
    private EqMaterielService eqMaterielService;
    @Inject
    private SysRoleService sysRoleService;
    @Inject
    protected MyWebsocket myWebsocket;
    @Inject
    private MsgMessageService msgMessageService;
    @Inject
    private SysWxService sysWxService;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        String taskId = context.getJobDetail().getKey().getName();
        try{
            Map<String,List<String>> socketMap = new HashMap<String,List<String>>(); //按机场存放更新的机位上的设备监控信息
            List<eq_materiel> noSignalList = new ArrayList<eq_materiel>();//存放信号失联的设备
            //参数中有此参数时，以参数位准，否则取系统参数设置的默认值
            String noSignal = data.getString("NoSignal");
            if(Strings.isBlank(noSignal)){
                noSignal = Globals.NoSignal;
            }
            long warnNoSignal = Long.valueOf(noSignal);
            List<eq_use> useList = eqUseService.query(Cnd.where("errstatus","!=","4"));
//            List<eq_use> useList = eqUseService.query("eqMateriel");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = df.format(new Date());
            for(eq_use eqUse:useList){
                log.info("设备跟踪：："+eqUse.getEqid() );
                if(eqUse.getEqid()!=null) {
                    String select= "select time from eq_trace where eqid='"+eqUse.getEqid()+"' order by time desc limit 1";
                    List<Record> mapList=new ArrayList<Record>();
                    Sql sql = Sqls.queryRecord(select);
                    dao.execute(sql);
                    List<Record> pslist = sql.getList(Record.class);
                    if(pslist.size()>0){
                        Record record = pslist.get(0);
                        String time = record.getString("time");
                        if(Strings.isNotBlank(time)){
                            long ss = DateUtil.getMinuteBetweens(time,now);
                            log.debug("time="+time+";now="+now+";Betweens="+ss+";warnNosignal="+warnNoSignal);
                            if(ss>=warnNoSignal && !"4".equals(eqUse.getErrstatus())){//避免重复处理
                                dao.update(eq_use.class, Chain.make("errstatus", "4"), Cnd.where("id", "=", eqUse.getId()));
                                //处理需要socket发送监控的设备信息
                                List<eq_materiel> eqMaterielList = eqMaterielService.query(Cnd.where("id","=",eqUse.getEqid()),"stake");
                                if(eqMaterielList.size()>0){
                                    eq_materiel eqMateriel = eqMaterielList.get(0);
                                    noSignalList.add(eqMateriel);
                                    if(eqMateriel.getStake()!=null){
                                        String airportid = eqMateriel.getStake().getAirportId();
                                        String seatid = eqMateriel.getStake().getSeatid();
                                        List<String> mlist = socketMap.get(airportid);
                                        if(mlist==null){
                                            mlist = new ArrayList<String>();
                                        }
                                        if(!mlist.contains(seatid)){
                                            mlist.add(seatid);
                                        }
                                        socketMap.put(airportid,mlist);
                                    }

                                }
                            }
                        }
                    }
                }
            }

            //推送消息到监控首页，更新相关设备机位信息
            if(socketMap.size()>0) {
                for (Map.Entry<String, List<String>> entry : socketMap.entrySet()) {
                    myWebsocket.each("home-" + entry.getKey(), new Each<Session>() {
                        public void invoke(int index, Session ele, int length) {
                            // 逐个会话发送消息
                            myWebsocket.sendJson(ele.getId(), new NutMap("action", "notify").setv("msg", entry.getValue()));
                        }
                    });
                }
            }
            //产生异常时，发送系统通知消息和企业微信消息给设备管理员，先按设备所属单位统计要发送的设备管理员和设备信息
            Map<String,List<String>> equnitUserMap = new HashMap<String,List<String>>();//按设备所属单位映射设备管理员用户
            Map<String,List<String>> equnitMaterMap = new HashMap<String,List<String>>();//按设备所属单位映射信号失联的设备
            for(int i=0;i<noSignalList.size();i++) {
                eq_materiel eqMateriel = noSignalList.get(i);
                if(eqMateriel==null||eqMateriel.getStake()==null) continue;
                String key = eqMateriel.getStake().getAirportId()+"||"+eqMateriel.getEqunitid();
                if (!equnitUserMap.containsKey(key)) {
                    Map<String, List<String>> roleAndUser = sysRoleService.getUsersByRole(eqMateriel.getEqunitid(), null, "设备管理员");
                    List<String> userIdList = new ArrayList<>();
                    for (Map.Entry<String, List<String>> entry : roleAndUser.entrySet()) {
                        String roleId = entry.getKey();
                        userIdList.addAll(entry.getValue());
                    }
                    equnitUserMap.put(key, userIdList);
                }

                List<String> eqCodeList;
                if (equnitMaterMap.containsKey(key)) {
                    eqCodeList = equnitMaterMap.get(key);
                } else {
                    eqCodeList = new ArrayList<String>();
                }
                eqCodeList.add(eqMateriel.getStake().getStakename());
                equnitMaterMap.put(key, eqCodeList);
            }
            //发送异常通知消息和企业微信消息 20180917
            String title = "检测到设备信号失联，请尽快核实处理!";
            for (Map.Entry<String, List<String>> entry : equnitUserMap.entrySet()) {
                String key = entry.getKey();
                String[]keyArr = key.split("||");
                List<String> userIdList = entry.getValue();
                List<String> eqcodeList =  equnitMaterMap.get(key);
                Map<String,List<String>> contentMap = spiltList(eqcodeList,40);//list拆分（count根据放入内容调整），以免数据太多（目前存放的时桩位编码，每个10位），字段无法保存
                if(eqcodeList.size()>0) {
                    Set<String> userIds = new HashSet<String>();
                    for (String userId : userIdList) {
                        userIds.add(userId);
                        for(Map.Entry<String,List<String>> entry2:contentMap.entrySet()){
                            String content = String.valueOf(entry2.getValue());
                            msgMessageService.addMessage("1", "3", "0", "2", title, content, userId, null, keyArr[0], "", "");
                        }
                    }
                    //发送企业微信消息给设备管理员角色的用户
                    if ("1".equals(Globals.WxCorpStart)) {
                        if (userIds.size() > 0) {
                            String content = String.valueOf(eqcodeList);
                            sysWxService.sendWxMessageAsy(userIds, title.toString()+"桩位如下："+content);
                        }
                    }
                }
            }

            log.info("设备信号判断任务执行完成!" );
            dao.update(Sys_task.class, Chain.make("exeAt", (int) (System.currentTimeMillis() / 1000)).add("exeResult", "执行完成"), Cnd.where("id", "=", taskId));
        }catch(Exception e){
            log.error("设备信号判断失败:"+e.getMessage());
            dao.update(Sys_task.class, Chain.make("exeAt", (int) (System.currentTimeMillis() / 1000)).add("exeResult", "执行失败"), Cnd.where("id", "=", taskId));
        }

    }

    /**
     * @Desc :  切分list位多个固定长度的list集合（我这是业务需要，直接是1w条数据切分）
     * @Author :
     * @Params: [historyList]
     * @Return: java.util.Map
     * @Date : 2019/09/17 18:30
     */
    public static Map spiltList(List historyList,int count) {
        int listSize = historyList.size();
        int toIndex = count;
        Map map = new HashMap();     //用map存起来新的分组后数据
        int keyToken = 0;
        for (int i = 0; i < historyList.size(); i += toIndex) {
            if (i + toIndex > listSize) {        //作用为toIndex最后没有count条数据则剩余几条newList中就装几条
                toIndex = listSize - i;
            }
            List newList = historyList.subList(i, i + toIndex);
            map.put(keyToken+"", newList);
            keyToken++;
        }
        return map;
    }
}
