package cn.wizzer.app.web.modules.controllers.platform.eq;

import cn.wizzer.app.eq.modules.models.eq_repair;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.eq.modules.models.eq_repairtrack;
import cn.wizzer.app.eq.modules.services.EqRepairtrackService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@IocBean
@At("/platform/eq/repairtrack")
public class EqRepairtrackController{
    private static final Log log = Logs.get();
    @Inject
    private EqRepairtrackService eqRepairtrackService;

    @Inject
    Dao dao;

    @At("/getEqRepairInfo")
    @Ok("json")
    @RequiresPermissions("platform.eq.repair")
    public List<eq_repair> getEqRepairInfo(@Param("rpid") String rpid){
        String sql="SELECT " +

                "rt.id as rtid,rt.operatetime,rt.repairStatus," +
                "er.id as erid,er.repnum,er.reptext,er.imgpath,er.imgname, " +
                "em.id as emid,em.eqcode,em.eqname,em.eqtype,em.starttime,equnitphone,em.eqfac,em.eqcolor,"+
                "sd.id as sdid,sd.name, "+
                "wx.id as wxid,wx.username as wxduty, "+
                "up.id as upid,up.username as upname, "+
                "su.id as suid,su.username as operater, "+
                "un.id as unid,un.name as unname "+
                "FROM eq_repairtrack rt " +

                "LEFT JOIN eq_repair er ON rt.eqRepairId = er.id " +
                "LEFT JOIN sys_user su ON rt.operaterId = su.id " +
                "LEFT JOIN sys_user wx ON er.wxuserid = wx.id " +
                " LEFT JOIN eq_materiel em ON er.eqid = em.id "+
                " LEFT JOIN sys_dict sd ON er.reptype = sd.id " +
                "LEFT JOIN sys_unit as un ON em.equnitid = un.id "+
                "LEFT JOIN sys_user up ON er.personid = up.id "+
                " WHERE rt.eqRepairId = '" + rpid+"' " + " ORDER BY rt.operatetime ASC  ";
        System.out.println(sql+"<-------------------------------------->");
        Sql sqlstr= Sqls.queryRecord(sql);
        dao.execute(sqlstr);
        List<eq_repair> res = sqlstr.getList(eq_repair.class);
        return res;
    }

}
