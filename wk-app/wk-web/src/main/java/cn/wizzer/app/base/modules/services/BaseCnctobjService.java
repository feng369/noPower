package cn.wizzer.app.base.modules.services;

import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.base.modules.models.base_cnctobj;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import org.nutz.dao.Cnd;
import org.nutz.lang.util.NutMap;

import java.util.List;

public interface BaseCnctobjService extends BaseService<base_cnctobj>{

     base_cnctobj  getbase_cnctobj(String  condition);

     base_cnctobj getcnctobj(String userid);

     Cnd airportDataPermission(Cnd cnd);

     Cnd unitDataPermission(Cnd cnd,String userid,String unitid);

     Cnd airportOrderPermission(Cnd cnd);

     String getPersonIDByUserID();

    NutMap getList(String loginName, String personName, int length, int start, int draw, List<DataTableOrder> order, List<DataTableColumn> columns, Cnd cnd);

}
