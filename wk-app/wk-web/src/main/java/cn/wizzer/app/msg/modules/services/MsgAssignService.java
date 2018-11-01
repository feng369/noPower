package cn.wizzer.app.msg.modules.services;

import cn.wizzer.app.msg.modules.models.Msg_option;
import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.msg.modules.models.Msg_assign;
import org.nutz.lang.util.NutMap;

import java.util.List;

public interface MsgAssignService extends BaseService<Msg_assign>{

    void addAssignToUser(String bizObject, String bizObjectId,String title,String body,String state,String userid,String personid,String sendid,String linkurl,String linkViewUrl,String bizObjectServiceImpl);

    void addAssignToRole(String bizObject, String bizObjectId, String title, String body, String state, String roleId, List<String> userIdList, String sendid, String linkurl,String linkViewUrl,String bizObjectServiceImpl);

    void complete(Msg_assign assign, Msg_option option);

    int getTodoCount(String userid,int[]state);

    void handler(String assignid, Msg_option msgOption, NutMap objectMap);
}
