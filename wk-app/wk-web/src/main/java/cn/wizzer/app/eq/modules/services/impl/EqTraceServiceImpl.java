package cn.wizzer.app.eq.modules.services.impl;

import cn.wizzer.app.eq.modules.models.eq_trace;
import cn.wizzer.app.eq.modules.services.EqTraceService;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class EqTraceServiceImpl extends BaseServiceImpl<eq_trace> implements EqTraceService {
    public EqTraceServiceImpl(Dao dao) {
        super(dao);
    }
}
