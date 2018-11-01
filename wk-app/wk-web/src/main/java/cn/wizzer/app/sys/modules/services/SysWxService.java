package cn.wizzer.app.sys.modules.services;

import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.sys.modules.models.Sys_wx;
import org.nutz.dao.Cnd;

import java.util.Set;

public interface SysWxService extends BaseService<Sys_wx>{

    String getTokenOrNew(Cnd cnd) throws Exception;

    String sendMessageToUser(String[]userIds,String appnum, String content);

    public void sendWxMessageAsy(Set<String> userIdList, String content);

}
