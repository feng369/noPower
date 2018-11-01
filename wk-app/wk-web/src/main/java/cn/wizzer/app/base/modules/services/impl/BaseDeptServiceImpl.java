package cn.wizzer.app.base.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.base.modules.models.base_dept;
import cn.wizzer.app.base.modules.services.BaseDeptService;
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
public class BaseDeptServiceImpl extends BaseServiceImpl<base_dept> implements BaseDeptService {
    public BaseDeptServiceImpl(Dao dao) {
        super(dao);
    }

    /**
     * 新增单位
     *
     * @param dept
     * @param pid
     */
    @Aop(TransAop.READ_COMMITTED)
    public void save(base_dept dept, String pid) {
        String path = "";
        if (!Strings.isEmpty(pid)) {
            base_dept pp = this.fetch(pid);
            path = pp.getPath();
        }
        dept.setPath(getSubPath("base_dept", "path", path));
        dept.setParentId(pid);
        dao().insert(dept);
        if (!Strings.isEmpty(pid)) {
            this.update(Chain.make("hasChildren", true), Cnd.where("id", "=", pid));
        }
    }

    /**
     * 级联删除单位
     *
     * @param dept
     */
    @Aop(TransAop.READ_COMMITTED)
    public void deleteAndChild(base_dept dept) {
        dao().execute(Sqls.create("delete from base_dept where path like @path").setParam("path", dept.getPath() + "%"));
       if (!Strings.isEmpty(dept.getParentId())) {
            int count = count(Cnd.where("parentId", "=", dept.getParentId()));
            if (count < 1) {
                dao().execute(Sqls.create("update base_dept set hasChildren=0 where id=@pid").setParam("pid", dept.getParentId()));
            }
        }
    }

    /**
     * 更新部门数据
     *
     * @param dept
     */
    @Aop(TransAop.READ_COMMITTED)
    public void update(base_dept dept) {
        //更新前先查询原值对象
        base_dept old = null;
        if(StringUtils.isNotBlank(dept.getId())){
            old = this.fetch(dept.getId());
        }

        dao().updateIgnoreNull(dept);
        if (!Strings.isEmpty(dept.getParentId())) {
            this.update(Chain.make("hasChildren", true), Cnd.where("id", "=", dept.getParentId()));
        }

        //判断原上级部门是否还有下级部门，没有了需要修改原上级部门的的hasChildren属性 20180307 koudepei
        if(old!=null && StringUtils.isNotBlank(old.getParentId())){
            if( (StringUtils.isNotBlank(dept.getParentId()) && !dept.getParentId().equals(old.getParentId()))
                    || StringUtils.isBlank(dept.getParentId())){
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
