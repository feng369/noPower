package cn.wizzer.app.eq.modules.services.impl;

import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.plugin.PostRun;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.eq.modules.models.eq_lock;
import cn.wizzer.app.eq.modules.services.EqLockService;
import cn.wizzer.framework.util.StringUtil;
import org.apache.log4j.Logger;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import java.util.Date;
import java.util.HashMap;

@IocBean(args = {"refer:dao"})
public class EqLockServiceImpl extends BaseServiceImpl<eq_lock> implements EqLockService {
    //专门针对硬件通讯接口getinfo方法定制日志文件
    private static Logger luckyLog = Logger.getLogger("LuckyLog");

    private Log log = Logs.get();

    public EqLockServiceImpl(Dao dao) {
        super(dao);
    }

    @Override
    public int unLock(String id, String type) {
        eq_lock lock = this.fetch(id);
        if(lock==null || Strings.isBlank(lock.getLocknum())){
            throw new ValidatException("未找到锁编号!");
        }
        String cmd="";
        if("1".equals(type)){
            cmd = "开锁";
        }else if("2".equals(type)) {
            cmd = "关锁";
        }else{
            throw new ValidatException("参数type不正确!");
        }
        String personid = StringUtil.getUid();
        String lockID = lock.getLocknum();
        luckyLog.info("WEB用户："+personid+",准备发送"+cmd+"指令，锁号："+lockID);
        int ret=-1;
        String url = Globals.luckyUrl + Globals.unLock;
        PostRun pr = new PostRun();
        String Json = pr.post(url, lockID, type);
        if(Json==null){
            throw new ValidatException("操作失败，无法连接设备通讯服务，请联系系统管理员！");
        }
        Object o = org.nutz.json.Json.fromJson(Json);
        if (o instanceof HashMap) {
            String  status= ((HashMap) o).get("status").toString();
            //第三步 修改use表pstatus为1
            if (("1").equals(status)) {//解锁成功
                // 注意：这里仅仅是指令发送成功，如下逻辑在判断解锁成功后处理 此处不需要更新数据库201804225
                luckyLog.info("WEB用户："+personid+",成功发送了"+cmd+"指令，锁号："+lockID);
                ret = 1;
            }else if (("0").equals(status)) {
                luckyLog.info("WEB用户："+personid+",发送"+cmd+"指令，lucky返回失败，锁号："+lockID);
                ret =0;
            }
        }
        return ret;
    }
}
