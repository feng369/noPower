package cn.wizzer.app.web.commons.quartz.job;

import cn.wizzer.app.eq.modules.models.eq_use;
import cn.wizzer.app.eq.modules.services.EqUseService;
import cn.wizzer.app.sys.modules.models.Sys_task;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.plugin.PostRun;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashMap;
import java.util.List;

/**
 * Created by koudepei on 2018/3/11.
 */
@IocBean
public class EqTraceJob implements Job {

    private static final Log log = Logs.get();
    @Inject
    protected Dao dao;

    @Inject
    private EqUseService eqUseService;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        String taskId = context.getJobDetail().getKey().getName();
        try{
            //获取位置
            String url= Globals.luckyUrl + Globals.QueryLock;;
            PostRun pr=new PostRun();
            String pstatus = data.getString("pstatus");
            Cnd cnd = Cnd.NEW();
            if(Strings.isNotBlank(pstatus)) {
                cnd.and("pstatus", "=", pstatus);
            }
            List<eq_use> useList = eqUseService.query(cnd,"eqMateriel");
            for(eq_use eqUse:useList){
                log.info("设备跟踪：："+eqUse.getEqid() );
                if(eqUse.getEqMateriel()!=null) {
                    String Json = pr.post(url, eqUse.getEqMateriel().getLockid(), null);
                    log.info(eqUse.getEqMateriel().getLockid()+"返回结果：："+Json );
                    Object o = org.nutz.json.Json.fromJson(Json);
                    if(o instanceof HashMap) {
                        if("ok".equalsIgnoreCase(((HashMap) o).get("msg").toString())){
                            log.info("设备接口调用：：OK"+eqUse.getEqMateriel().getLockid());
                            continue;
                        }
                    }
                }
                log.error("设备跟踪FAIL：："+eqUse.getEqid() );
            }
            log.info("设备跟踪任务执行完成!" );
            dao.update(Sys_task.class, Chain.make("exeAt", (int) (System.currentTimeMillis() / 1000)).add("exeResult", "执行完成"), Cnd.where("id", "=", taskId));
        }catch(Exception e){
            log.error("设备跟踪失败:"+e.getMessage());
            dao.update(Sys_task.class, Chain.make("exeAt", (int) (System.currentTimeMillis() / 1000)).add("exeResult", "执行失败"), Cnd.where("id", "=", taskId));
        }

    }
}
