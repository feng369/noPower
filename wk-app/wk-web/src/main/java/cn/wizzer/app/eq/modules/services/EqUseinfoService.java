package cn.wizzer.app.eq.modules.services;

import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.eq.modules.models.eq_useinfo;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import org.nutz.dao.QueryResult;
import org.nutz.lang.util.NutMap;

import java.util.List;

public interface EqUseinfoService extends BaseService<eq_useinfo>{
    public NutMap getDataList(String unitid,String starttime,String endtime,int pstatus, int start, int length,int draw);

    public NutMap geteqTotal(String units,String starttime,String endtime, int start, int length,int draw, List<DataTableOrder> order, List<DataTableColumn> column);

    public NutMap geteqtypeTotal(String types,String syear,String aircorp, int start, int length,int draw);

    public NutMap geteqtypeList(String types, String syear, String aircorp, int start, int length, int draw, List<DataTableOrder> order, List<DataTableColumn> column);

    public String insertInfo(String equsenum, String eqid, String personid, String personunitid, String starttime, String endtime,String sstartime,String sendtime, String pstatus, String equnitid,String lid);
}
