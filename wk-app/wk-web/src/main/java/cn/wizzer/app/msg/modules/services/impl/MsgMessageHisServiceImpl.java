package cn.wizzer.app.msg.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.msg.modules.models.Msg_message_his;
import cn.wizzer.app.msg.modules.services.MsgMessageHisService;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(args = {"refer:dao"})
public class MsgMessageHisServiceImpl extends BaseServiceImpl<Msg_message_his> implements MsgMessageHisService {
    public MsgMessageHisServiceImpl(Dao dao) {
        super(dao);
    }
}
