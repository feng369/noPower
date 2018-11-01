package cn.wizzer.app.msg.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.msg.modules.models.Msg_option;
import cn.wizzer.app.msg.modules.services.MsgOptionService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class MsgOptionServiceImpl extends BaseServiceImpl<Msg_option> implements MsgOptionService {
    public MsgOptionServiceImpl(Dao dao) {
        super(dao);
    }
}
