package cn.wizzer.app.sys.modules.services;

import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.sys.modules.models.Sys_useradd;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import org.nutz.lang.util.NutMap;

import java.util.List;

public interface SysUseraddService extends BaseService<Sys_useradd>{
    /**
     * 20180326zhf1753
     * 需要审核的列表数据
     * @param length
     * @param start
     * @param draw
     * @param order
     * @param columns
     * @return
     */
    NutMap getRegistAuditList( int result,int length, int start, int draw, List<DataTableOrder> order, List<DataTableColumn> columns);
}
