package cn.wizzer.app.eq.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.eq.modules.models.eq_order;
import cn.wizzer.app.eq.modules.services.EqOrderService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class EqOrderServiceImpl extends BaseServiceImpl<eq_order> implements EqOrderService {
    public EqOrderServiceImpl(Dao dao) {
        super(dao);
    }
}
