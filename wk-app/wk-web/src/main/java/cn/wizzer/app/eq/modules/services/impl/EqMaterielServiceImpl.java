package cn.wizzer.app.eq.modules.services.impl;

import cn.wizzer.app.eq.modules.models.eq_lock;
import cn.wizzer.app.eq.modules.models.eq_stake;
import cn.wizzer.app.eq.modules.models.eq_use;
import cn.wizzer.app.eq.modules.services.EqStakeService;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.framework.base.Result;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.eq.modules.models.eq_materiel;
import cn.wizzer.app.eq.modules.services.EqMaterielService;
import org.apache.commons.lang3.StringUtils;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Transaction;

import java.util.ArrayList;
import java.util.List;

@IocBean(args = {"refer:dao"})
public class EqMaterielServiceImpl extends BaseServiceImpl<eq_materiel> implements EqMaterielService {
    public EqMaterielServiceImpl(Dao dao) {
        super(dao);
    }

    @Inject
    private EqStakeService eqStakeService;
    /**
     * 绑定桩位和设备
     *
     * @param eqid
     * @param stakeid
     */
    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void bindStake(String[] eqid, String stakeid) {

        if (eqid != null && eqid.length > 1 && "1".equals(Globals.StakeMaterielRatio)) {
            throw new ValidatException("只能选择一个设备进行桩位绑定！");
        }
        if (StringUtils.isBlank(stakeid)) {
            throw new ValidatException("请选择桩位信息！");
        }
        if (eqid.length > 0) {
            List<eq_materiel> list = new ArrayList<eq_materiel>();
            for (String id : eqid) {
                eq_materiel eqMateriel = dao().fetch(eq_materiel.class, id);
                if (eqMateriel == null) {
                    throw new ValidatException("没有找到选中行的设备信息!");
                }
                //判读是否已上线使用
                Cnd cnd = Cnd.NEW();
                cnd.and("eqid", "=", id);
                eq_use use = dao().fetch(eq_use.class, cnd);
                if (use != null) {
                    throw new ValidatException("设备[" + eqMateriel.getEqnum() + "]已上线使用!");
                }
                //eqMateriel.setEqusestatus("1");
                eqMateriel.setStakeid(stakeid);

                //20180422zhf1424
                //如果可以绑定桩位
                if(!Strings.isBlank(stakeid)){
                    eq_stake eqStake = eqStakeService.fetch(stakeid);
                    if(eqStake != null){
                        eqStake.setLinkEqMaterial(true);
                        eqStakeService.updateIgnoreNull(eqStake);
                    }
                }

                list.add(eqMateriel);
            }
            for (eq_materiel em : list) {
                dao().updateIgnoreNull(em);
            }
        }
    }

    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void bindLock(String eqid, String lockid) {
        eq_materiel materiel=this.fetch(eqid);
        if(materiel!=null) {
            if (!"0".equals(materiel.getEqusestatus())) {
                throw new ValidatException("只能对未上线的设备才能进行锁的分配和解绑!");
            }
            if (!Strings.isBlank(lockid)) {
                eq_lock eqLock = dao().fetch(eq_lock.class, lockid);
                materiel.setLockid(eqLock.getLocknum());
                materiel.setLid(lockid);

                eqLock.setLockstatus("1");
                dao().update(eqLock);
            }
            this.update(materiel);
        }

    }

    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void unBindLock(String eqid) {
        if(StringUtils.isNotBlank(eqid)){
            eq_materiel materiel = this.fetch(eqid);
            if(materiel!=null){
                if(!"0".equals(materiel.getEqusestatus())){
                    throw new ValidatException("只能对未上线的设备才能进行锁的分配和解绑!");
                }
                //修改锁的状态
                String lockid = materiel.getLid();
                if(!Strings.isBlank(lockid)){
                    eq_lock eqLock = dao().fetch(eq_lock.class,lockid);
                    eqLock.setLockstatus("0");
                    dao().updateIgnoreNull(eqLock);
                }
                materiel.setLid("");
                materiel.setLockid("");
                this.updateIgnoreNull(materiel);
            }
        }

    }

    public void relieveStake(String id){
        eq_materiel eqMateriel = this.fetch(id);
        if(Strings.isBlank(eqMateriel.getStakeid())){
            throw new ValidatException("设备未绑定桩位,故不需要解除");
        }
        if("1".equals(eqMateriel.getEqusestatus())){
            throw new ValidatException("设备已上线,不能解除桩位");
        }
        eq_stake eqStake = eqStakeService.fetch(eqMateriel.getStakeid());
        if(eqStake != null){
            //重新可以绑定设备
            eqStake.setLinkEqMaterial(false);
            eqStakeService.updateIgnoreNull(eqStake);
        }
        eqMateriel.setStakeid(null);
        this.update(eqMateriel);
    }
}
