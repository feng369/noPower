package cn.wizzer.app.logistics.modules.services;

import cn.wizzer.app.logistics.modules.models.logistics_orderdelivery;
import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.logistics.modules.models.logistics_Deliveryorder;

import java.util.List;

public interface LogisticsDeliveryorderService extends BaseService<logistics_Deliveryorder>{

    void save(logistics_Deliveryorder logistics_deliveryorder);

    List<logistics_Deliveryorder> getDeliveryOrderByMobile(String pstatus,String personid);

    void orderexe(String orderid);

    boolean checkorderre(String orderid);

}
