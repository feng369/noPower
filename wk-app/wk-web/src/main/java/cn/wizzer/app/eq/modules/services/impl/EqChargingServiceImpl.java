package cn.wizzer.app.eq.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.eq.modules.models.eq_charging;
import cn.wizzer.app.eq.modules.services.EqChargingService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class EqChargingServiceImpl extends BaseServiceImpl<eq_charging> implements EqChargingService {
    public EqChargingServiceImpl(Dao dao) {
        super(dao);
    }
}
