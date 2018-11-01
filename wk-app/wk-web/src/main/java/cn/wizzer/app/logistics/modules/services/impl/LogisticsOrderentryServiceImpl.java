package cn.wizzer.app.logistics.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.logistics.modules.models.logistics_orderentry;
import cn.wizzer.app.logistics.modules.services.LogisticsOrderentryService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class LogisticsOrderentryServiceImpl extends BaseServiceImpl<logistics_orderentry> implements LogisticsOrderentryService {
    public LogisticsOrderentryServiceImpl(Dao dao) {
        super(dao);
    }
}
