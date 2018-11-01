package cn.wizzer.app.logistics.modules.services.impl;

import cn.wizzer.app.logistics.modules.models.logistics_orderdelivery;
import cn.wizzer.app.web.modules.controllers.open.api.Datatime.newDataTime;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.logistics.modules.models.logistics_Deliveryorderentry;
import cn.wizzer.app.logistics.modules.services.LogisticsDeliveryorderentryService;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;

@IocBean(args = {"refer:dao"})
public class LogisticsDeliveryorderentryServiceImpl extends BaseServiceImpl<logistics_Deliveryorderentry> implements LogisticsDeliveryorderentryService {
    public LogisticsDeliveryorderentryServiceImpl(Dao dao) {
        super(dao);
    }

    public int upDventry(int pstatus, String content,String toreason, String optime, Cnd cnd){

            logistics_Deliveryorderentry ld =dao().fetch(logistics_Deliveryorderentry.class,cnd);
            if(ld.getPstatus()<1){
                ld.setOperatetime(optime);
            }
            if(pstatus!=ld.getPstatus()){
                ld.setPstatus(pstatus);
            }
            if(content!=null)
                ld.setContent(content);
            if(toreason!=null)
                ld.setTimeoutreason(toreason);
            int re=dao().updateIgnoreNull(ld);
            return  re;

    }
}
