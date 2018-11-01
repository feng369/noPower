package cn.wizzer.app.eq.modules.services;

import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.eq.modules.models.eq_repair;
import org.nutz.lang.util.NutMap;

public interface EqRepairService extends BaseService<eq_repair>{
    public NutMap getRepairList(String repnum,String personname,String pstatus,String eqname,String eqnum, int start, int length,int draw);

    void editDo(eq_repair eqRepair);

    void complete(String[] ids);

    eq_repair insertRepair(String reptype, String eqid, String reptext, String personid, String pstatus);

    void updateRepair(String repairid, String repnum, String reptype, String eqid, String reptext, String personid, String pstatus);
}
