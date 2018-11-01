package cn.wizzer.app.base.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.base.modules.models.base_customer;
import cn.wizzer.app.base.modules.services.BaseCustomerService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class BaseCustomerServiceImpl extends BaseServiceImpl<base_customer> implements BaseCustomerService {
    public BaseCustomerServiceImpl(Dao dao) {
        super(dao);
    }
}
