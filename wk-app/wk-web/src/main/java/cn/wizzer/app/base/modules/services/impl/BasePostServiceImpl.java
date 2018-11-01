package cn.wizzer.app.base.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.base.modules.models.base_post;
import cn.wizzer.app.base.modules.services.BasePostService;
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
public class BasePostServiceImpl extends BaseServiceImpl<base_post> implements BasePostService {
    public BasePostServiceImpl(Dao dao) {
        super(dao);
    }
    /**
     * 新增单位
     *
     * @param post
     * @param pid
     */
    @Aop(TransAop.READ_COMMITTED)
    public void save(base_post post, String pid) {
        String path = "";
        if (!Strings.isEmpty(pid)) {
            base_post pp = this.fetch(pid);
            path = pp.getPath();
        }
        post.setPath(getSubPath("base_post", "path", path));
        post.setParentId(pid);
        dao().insert(post);
        if (!Strings.isEmpty(pid)) {
            this.update(Chain.make("hasChildren", true), Cnd.where("id", "=", pid));
        }
    }

    /**
     * 级联删除单位
     *
     * @param post
     */
    @Aop(TransAop.READ_COMMITTED)
    public void deleteAndChild(base_post post) {
        dao().execute(Sqls.create("delete from base_post where path like @path").setParam("path", post.getPath() + "%"));
        if (!Strings.isEmpty(post.getParentId())) {
            int count = count(Cnd.where("parentId", "=", post.getParentId()));
            if (count < 1) {
                dao().execute(Sqls.create("update base_post set hasChildren=0 where id=@pid").setParam("pid", post.getParentId()));
            }
        }
    }

    /**
     * 更新部门数据
     *
     * @param post
     */
    @Aop(TransAop.READ_COMMITTED)
    public void update(base_post post) {
        //更新前先查询原值对象
        base_post old = null;
        if(StringUtils.isNotBlank(post.getId())){
            old = this.fetch(post.getId());
        }

        dao().updateIgnoreNull(post);
        if (!Strings.isEmpty(post.getParentId())) {
            this.update(Chain.make("hasChildren", true), Cnd.where("id", "=", post.getParentId()));
        }

        //判断原上级岗位是否还有下级岗位，没有了需要修改原上级岗位的的hasChildren属性 20180308 koudepei
        if(old!=null && StringUtils.isNotBlank(old.getParentId())){
            if( (StringUtils.isNotBlank(post.getParentId()) && !post.getParentId().equals(old.getParentId()))
                    || StringUtils.isBlank(post.getParentId())){
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
