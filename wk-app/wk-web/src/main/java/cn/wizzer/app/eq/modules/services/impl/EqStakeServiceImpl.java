package cn.wizzer.app.eq.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.eq.modules.models.eq_stake;
import cn.wizzer.app.eq.modules.services.EqStakeService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class EqStakeServiceImpl extends BaseServiceImpl<eq_stake> implements EqStakeService {
    public EqStakeServiceImpl(Dao dao) {
        super(dao);
    }
}
