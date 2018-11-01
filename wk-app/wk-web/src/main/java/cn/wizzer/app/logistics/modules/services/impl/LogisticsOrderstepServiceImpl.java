package cn.wizzer.app.logistics.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.logistics.modules.models.logistics_orderstep;
import cn.wizzer.app.logistics.modules.services.LogisticsOrderstepService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class LogisticsOrderstepServiceImpl extends BaseServiceImpl<logistics_orderstep> implements LogisticsOrderstepService {
    public LogisticsOrderstepServiceImpl(Dao dao) {
        super(dao);
    }
}
