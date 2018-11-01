package cn.wizzer.app.base.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.base.modules.models.base_airport;
import cn.wizzer.app.base.modules.services.BaseAirportService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class BaseAirportServiceImpl extends BaseServiceImpl<base_airport> implements BaseAirportService {
    public BaseAirportServiceImpl(Dao dao) {
        super(dao);
    }
}
