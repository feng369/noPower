package cn.wizzer.app.base.modules.services;

import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.base.modules.models.base_airmeterial;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import org.nutz.dao.Cnd;
import org.nutz.lang.util.NutMap;

import java.util.List;

public interface BaseAirmeterialService extends BaseService<base_airmeterial>{
    NutMap dataCode(int length, int start, int draw, List<DataTableOrder> orders, List<DataTableColumn> columns,
                    Cnd cnd, String linkName, Cnd subCnd);
}
