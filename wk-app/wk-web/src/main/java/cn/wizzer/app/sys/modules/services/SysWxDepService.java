package cn.wizzer.app.sys.modules.services;

import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.sys.modules.models.Sys_wx_dep;

import java.text.ParseException;

public interface SysWxDepService extends BaseService<Sys_wx_dep>{

    void download() throws Exception;
}
