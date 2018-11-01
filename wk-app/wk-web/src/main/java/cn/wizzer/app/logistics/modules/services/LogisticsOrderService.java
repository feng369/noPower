package cn.wizzer.app.logistics.modules.services;

import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.logistics.modules.models.logistics_order;
import org.nutz.dao.Cnd;

import java.util.List;

public interface LogisticsOrderService extends BaseService<logistics_order>{
    void save(logistics_order logistics_order, String logisticsOrder);

    logistics_order getLogistics_order(String id);

    List<logistics_order>  getdata(Cnd cnd,String linkname);


}
