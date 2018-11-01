package cn.wizzer.app.logistics.modules.services.impl;

import cn.wizzer.app.base.modules.models.base_cnctobj;
import cn.wizzer.app.base.modules.models.base_customer;
import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.app.base.modules.models.base_place;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.base.modules.services.BaseCustomerService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.base.modules.services.BasePlaceService;
import cn.wizzer.app.logistics.modules.services.LogisticsDeliveryorderService;
import cn.wizzer.app.logistics.modules.services.LogisticsOrderdeliveryService;
import cn.wizzer.app.sys.modules.models.Sys_dict;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.app.sys.modules.services.SysDictService;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.logistics.modules.models.logistics_order;
import cn.wizzer.app.logistics.modules.services.LogisticsOrderService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;

import java.util.List;

@IocBean(args = {"refer:dao"})
public class LogisticsOrderServiceImpl extends BaseServiceImpl<logistics_order> implements LogisticsOrderService {

    @Inject
    private BasePersonService basePersonService;
    @Inject
    private BaseCnctobjService baseCnctobjService;
    @Inject
    private BaseCustomerService baseCustomerService;
    @Inject
    private LogisticsOrderdeliveryService logisticsOrderdeliveryService;
    @Inject
    private SysDictService sysDictService;
    @Inject
    private BasePlaceService basePlaceService;
    @Inject
    private LogisticsDeliveryorderService logisticsDeliveryorderService;




    public LogisticsOrderServiceImpl(Dao dao) {
        super(dao);
    }
    public void save(logistics_order logistics_order, String logistics_orderentry){
        dao().insertWith(logistics_order,logistics_orderentry);
    }

    public logistics_order getLogistics_order(String id){

        logistics_order logisticsOrder = dao().fetchLinks(dao().fetch(logistics_order.class, id), "logistics_orderentry");
        if(!Strings.isBlank(logisticsOrder.getCustomerid())){
            base_customer baseCustomer =baseCustomerService.fetch(logisticsOrder.getCustomerid());
            logisticsOrder.setCustomer(baseCustomer);
        }
        if(!Strings.isBlank(logisticsOrder.getBtype())){
            Sys_dict dict_btype =sysDictService.fetch(logisticsOrder.getBtype());
            logisticsOrder.setDict_btype(dict_btype);
        }
        if(!Strings.isBlank(logisticsOrder.getEmergency())){
            Sys_dict dict_emergency =sysDictService.fetch(logisticsOrder.getEmergency());
            logisticsOrder.setDict_emergency(dict_emergency);
        }
        if(!Strings.isBlank(logisticsOrder.getStartstock())){
            base_place place_startstock =basePlaceService.fetch(logisticsOrder.getStartstock());
            logisticsOrder.setPlace_startstock(place_startstock);
        }
        if(!Strings.isBlank(logisticsOrder.getEndstock())){
            base_place place_endstock =basePlaceService.fetch(logisticsOrder.getEndstock());
            logisticsOrder.setPlace_endstock(place_endstock);
        }
        if(!Strings.isBlank(logisticsOrder.getTransporttype())){
            Sys_dict dict_transporttype =sysDictService.fetch(logisticsOrder.getTransporttype());
            logisticsOrder.setDict_transporttype(dict_transporttype);
        }
        if(!Strings.isBlank(logisticsOrder.getHctype())){
            Sys_dict dict_hctype =sysDictService.fetch(logisticsOrder.getHctype());
            logisticsOrder.setDict_hctype(dict_hctype);
        }
        if(!Strings.isBlank(logisticsOrder.getDeliveryorderid())){
           logisticsOrder.setLogisticsDeliveryorder(logisticsDeliveryorderService.fetch(logisticsOrder.getDeliveryorderid()));
        }
        if(!Strings.isBlank(logisticsOrder.getPersonid())){
            logisticsOrder.setPerson(basePersonService.fetch(logisticsOrder.getPersonid()));
        }
        return logisticsOrder;
    }
    public List<logistics_order> getdata(Cnd cnd,String linkname){
        List<logistics_order> list = dao().fetchLinks(dao().query(logistics_order.class, cnd), linkname);
        return list;
    }



}
