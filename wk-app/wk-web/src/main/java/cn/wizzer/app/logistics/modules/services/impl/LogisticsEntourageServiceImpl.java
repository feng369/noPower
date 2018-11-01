package cn.wizzer.app.logistics.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.logistics.modules.models.logistics_entourage;
import cn.wizzer.app.logistics.modules.services.LogisticsEntourageService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class LogisticsEntourageServiceImpl extends BaseServiceImpl<logistics_entourage> implements LogisticsEntourageService {
    public LogisticsEntourageServiceImpl(Dao dao) {
        super(dao);
    }
}
