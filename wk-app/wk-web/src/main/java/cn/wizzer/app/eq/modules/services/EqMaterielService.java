package cn.wizzer.app.eq.modules.services;

import cn.wizzer.framework.base.service.BaseService;
import cn.wizzer.app.eq.modules.models.eq_materiel;
import org.nutz.lang.util.NutMap;

public interface EqMaterielService extends BaseService<eq_materiel>{

    void bindStake(String[] eqid, String stakeid);

    void bindLock(String eqid, String lockid);

    void unBindLock(String eqid);

    void relieveStake(String id);
}
