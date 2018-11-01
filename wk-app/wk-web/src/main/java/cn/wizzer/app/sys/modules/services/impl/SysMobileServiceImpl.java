package cn.wizzer.app.sys.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.sys.modules.models.Sys_mobile;
import cn.wizzer.app.sys.modules.services.SysMobileService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class SysMobileServiceImpl extends BaseServiceImpl<Sys_mobile> implements SysMobileService {
    public SysMobileServiceImpl(Dao dao) {
        super(dao);
    }
}
