package cn.wizzer.app.logistics.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.logistics.modules.models.logistics_orderreject;
import cn.wizzer.app.logistics.modules.services.LogisticsOrderrejectService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class LogisticsOrderrejectServiceImpl extends BaseServiceImpl<logistics_orderreject> implements LogisticsOrderrejectService {
    public LogisticsOrderrejectServiceImpl(Dao dao) {
        super(dao);
    }
}
