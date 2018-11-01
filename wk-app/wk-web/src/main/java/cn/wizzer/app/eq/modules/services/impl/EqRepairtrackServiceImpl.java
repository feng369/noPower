package cn.wizzer.app.eq.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.eq.modules.models.eq_repairtrack;
import cn.wizzer.app.eq.modules.services.EqRepairtrackService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class EqRepairtrackServiceImpl extends BaseServiceImpl<eq_repairtrack> implements EqRepairtrackService {
    public EqRepairtrackServiceImpl(Dao dao) {
        super(dao);
    }
}
