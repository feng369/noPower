package cn.wizzer.app.msg.modules.services;

import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.msg.modules.models.Msg_message;

public interface MsgMessageService extends BaseService<Msg_message>{

    void addMessage(String type,String bizType,String state,String priority,String title,String body,String receiverid,String sourceid,String linkurl);

    void addMessage(String type,String bizType,String state,String priority,String title,String body,String receiverid,String sendid,String airportid,String sourceid,String linkurl);


    int getMessageCount(String userid, int[] type, int[] bizType,String state);
}
