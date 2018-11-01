package cn.wizzer.app.sys.modules.services;

import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.sys.modules.models.Sys_wx_user;

public interface SysWxUserService extends BaseService<Sys_wx_user>{

    void download() throws Exception;
}
