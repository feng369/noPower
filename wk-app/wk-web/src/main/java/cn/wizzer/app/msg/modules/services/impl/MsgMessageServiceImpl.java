package cn.wizzer.app.msg.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.msg.modules.models.Msg_message;
import cn.wizzer.app.msg.modules.services.MsgMessageService;
import cn.wizzer.framework.util.DateUtil;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.framework.websocket.MyWebsocket;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Each;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

import javax.websocket.Session;

@IocBean(args = {"refer:dao"})
public class MsgMessageServiceImpl extends BaseServiceImpl<Msg_message> implements MsgMessageService {
    @Inject
    protected MyWebsocket myWebsocket;

    public MsgMessageServiceImpl(Dao dao) {
        super(dao);
    }

    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void addMessage(String type, String bizType, String state, String priority, String title, String body, String receiverid, String sourceid, String linkurl) {
        addMessage(type,bizType,state,priority,title,body,receiverid, StringUtil.getUid(),StringUtil.getAirportid(),sourceid,linkurl);
    }

    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void addMessage(String type, String bizType, String state, String priority, String title, String body, String receiverid, String sendid, String airportid, String sourceid, String linkurl) {
        Msg_message msg = new Msg_message();
        msg.setType(type);
        msg.setBizType(bizType);
        msg.setState(state);
        msg.setPriority(priority);
        msg.setTitle(title);
        msg.setBody(body);
        msg.setSendtime(DateUtil.getDateTime());
        msg.setReceivetime(DateUtil.getDateTime());
        msg.setReceiverId(receiverid);
        msg.setSenderId(sendid);
        msg.setAirportId(airportid);
        msg.setSourceid(sourceid);
        msg.setLinkUrl(linkurl);
        this.insert(msg);

        //发送消息中心更新消息数量
        myWebsocket.each("home-msg-"+receiverid, new Each<Session>() {
            public void invoke(int index, Session ele, int length) {
                myWebsocket.sendJson(ele.getId(), new NutMap("action", "message").setv("msg", "ok"));
            }
        });
    }

    @Override
    public int getMessageCount(String userid, int[] type, int[] bizType,String state) {
        Cnd cnd = Cnd.NEW();
        cnd.and("type","in",type);
        cnd.and("bizType","in",bizType);
        cnd.and("receiverId","=",userid);
        if(Strings.isNotBlank(state)){
            cnd.and("state","=",state);
        }
        return this.count(cnd);
    }


}
