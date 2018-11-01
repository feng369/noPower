package cn.wizzer.app.base.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.base.modules.models.base_job;
import cn.wizzer.app.base.modules.services.BaseJobService;
import org.apache.commons.lang3.StringUtils;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;

import java.util.List;

@IocBean(args = {"refer:dao"})
public class BaseJobServiceImpl extends BaseServiceImpl<base_job> implements BaseJobService {
    public BaseJobServiceImpl(Dao dao) {
        super(dao);
    }

    /**
     * 新增单位
     *
     * @param job
     * @param pid
     */
    @Aop(TransAop.READ_COMMITTED)
    public void save(base_job job, String pid) {
        String path = "";
        if (!Strings.isEmpty(pid)) {
            base_job pp = this.fetch(pid);
            path = pp.getPath();
        }
        job.setPath(getSubPath("base_job", "path", path));
        job.setParentId(pid);
        dao().insert(job);
        if (!Strings.isEmpty(pid)) {
            this.update(Chain.make("hasChildren", true), Cnd.where("id", "=", pid));
        }
    }

    /**
     * 级联删除单位
     *
     * @param job
     */
    @Aop(TransAop.READ_COMMITTED)
    public void deleteAndChild(base_job job) {
        dao().execute(Sqls.create("delete from base_job where path like @path").setParam("path", job.getPath() + "%"));
        if (!Strings.isEmpty(job.getParentId())) {
            int count = count(Cnd.where("parentId", "=", job.getParentId()));
            if (count < 1) {
                dao().execute(Sqls.create("update base_job set hasChildren=0 where id=@pid").setParam("pid", job.getParentId()));
            }
        }
    }

    /**
     * 更新职务数据
     *
     * @param job
     */
    @Aop(TransAop.READ_COMMITTED)
    public void update(base_job job) {
        //更新前先查询原值对象
        base_job old = null;
        if(StringUtils.isNotBlank(job.getId())){
            old = this.fetch(job.getId());
        }

        dao().updateIgnoreNull(job);
        if (!Strings.isEmpty(job.getParentId())) {
            this.update(Chain.make("hasChildren", true), Cnd.where("id", "=", job.getParentId()));
        }

        //判断原上级岗位是否还有下级岗位，没有了需要修改原上级岗位的的hasChildren属性 20180308 koudepei
        if(old!=null && StringUtils.isNotBlank(old.getParentId())){
            if( (StringUtils.isNotBlank(job.getParentId()) && !job.getParentId().equals(old.getParentId()))
                    || StringUtils.isBlank(job.getParentId())){
                Cnd cnd=Cnd.NEW();
                cnd.and("parentId","=",old.getParentId());
                List list = this.query(cnd);
                if(list.size()==0){
                    this.update(Chain.make("hasChildren", false), Cnd.where("id", "=", old.getParentId()));
                }
            }
        }
    }
}
