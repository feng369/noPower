package cn.wizzer.app.logistics.modules.services;

import cn.wizzer.app.logistics.modules.models.logistics_orderdelivery;
import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.logistics.modules.models.logistics_Deliveryorderentry;
import org.nutz.dao.Cnd;

public interface LogisticsDeliveryorderentryService extends BaseService<logistics_Deliveryorderentry>{
    int upDventry(int pstatus, String content,String toreason, String optime, Cnd cnd);
}
