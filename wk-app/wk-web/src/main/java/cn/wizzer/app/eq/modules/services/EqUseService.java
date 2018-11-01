package cn.wizzer.app.eq.modules.services;

import cn.wizzer.app.eq.modules.models.eq_materiel;
import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.eq.modules.models.eq_use;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import org.nutz.dao.Cnd;
import org.nutz.dao.entity.Record;
import org.nutz.lang.util.NutMap;

import java.util.List;
import java.util.Map;

public interface EqUseService extends BaseService<eq_use>{
    void useOffline(String[] eqid) throws Exception;
    void useOnline(String[] eqid) throws Exception;
    List<Record> getEqUseList(String [] seatList,String airportid);

    NutMap getList(int length, int start, int draw, List<DataTableOrder> order, List<DataTableColumn> columns, Cnd aNew);

    int getInfo(Map map);

    int unLockProcess(String lockID, eq_use use, eq_materiel materiel, String personid, String personunitid, String starttime);

    void unlockSuccess(eq_use use, eq_materiel materiel, String personid, String personunitid);

    NutMap getEqUseListForDataTable(String seatId, int length, int start, int draw, List<DataTableOrder> order, List<DataTableColumn> columns);

    int lockProcess(String lockID, String personid, eq_materiel materiel, eq_use use);

    NutMap getEqUsePowerData(String stakenum, String locknum, int length, int start, int draw, List<DataTableOrder> order, List<DataTableColumn> columns, Cnd cnd);

}
