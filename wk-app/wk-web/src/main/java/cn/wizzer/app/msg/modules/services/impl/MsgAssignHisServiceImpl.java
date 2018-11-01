package cn.wizzer.app.msg.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.msg.modules.models.Msg_assign_his;
import cn.wizzer.app.msg.modules.services.MsgAssignHisService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class MsgAssignHisServiceImpl extends BaseServiceImpl<Msg_assign_his> implements MsgAssignHisService {
    public MsgAssignHisServiceImpl(Dao dao) {
        super(dao);
    }
}
