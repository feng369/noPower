package cn.wizzer.app.eq.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.eq.modules.models.eq_chockuse;
import cn.wizzer.app.eq.modules.services.EqChockuseService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class EqChockuseServiceImpl extends BaseServiceImpl<eq_chockuse> implements EqChockuseService {
    public EqChockuseServiceImpl(Dao dao) {
        super(dao);
    }
}
