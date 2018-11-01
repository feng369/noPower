package cn.wizzer.app.eq.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.eq.modules.models.eq_power;
import cn.wizzer.app.eq.modules.services.EqPowerService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class EqPowerServiceImpl extends BaseServiceImpl<eq_power> implements EqPowerService {
    public EqPowerServiceImpl(Dao dao) {
        super(dao);
    }
}
