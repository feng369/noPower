package cn.wizzer.app.eq.modules.services;

import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.eq.modules.models.eq_lock;

public interface EqLockService extends BaseService<eq_lock>{

    int unLock(String id, String type);
}
