package cn.wizzer.app.base.modules.services;

import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.base.modules.models.base_place;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.lang.util.NutMap;

import java.util.List;

public interface BasePlaceService extends BaseService<base_place>{

    NutMap dataCode(int length, int start, int draw, List<DataTableOrder> orders, List<DataTableColumn> columns,
                           Cnd cnd, String linkName, Cnd subCnd);

    void save(base_place place, String pid);

    void deleteAndChild(base_place place);

    void update(base_place place);

    List<base_place> querydata(Condition cnd, String linkname);
}
