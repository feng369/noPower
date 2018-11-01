package cn.wizzer.app.base.modules.services.impl;

import cn.wizzer.app.sys.modules.models.Sys_dict;
import cn.wizzer.app.sys.modules.services.SysDictService;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.base.modules.models.base_place;
import cn.wizzer.app.base.modules.services.BasePlaceService;
import cn.wizzer.framework.page.OffsetPager;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.*;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@IocBean(args = {"refer:dao"})
public class BasePlaceServiceImpl extends BaseServiceImpl<base_place> implements BasePlaceService {
    public BasePlaceServiceImpl(Dao dao) {
        super(dao);
    }

//    public String getArea(String areaId)
//    {
//
//    }
    @Inject
    private SysDictService sysDictService;

    public List<base_place> querydata(Condition cnd, String linkName) {
        List<base_place> list = this.dao().query(this.getEntityClass(), cnd);
        Sys_dict dict=sysDictService.fetch(Cnd.where("id","=",list.get(0).getAreaId()));
        list.get(0).setArea(dict);

        dict=sysDictService.fetch(Cnd.where("id","=",list.get(0).getTypeId()));
        list.get(0).setType(dict);


        dict=sysDictService.fetch(Cnd.where("id","=",list.get(0).getCtrlId()));
        list.get(0).setCtrl(dict);

        if (!Strings.isBlank(linkName)) {
            this.dao().fetchLinks(list, linkName);
        }
        return list;
    }

    public NutMap dataCode(int length, int start, int draw, List<DataTableOrder> orders, List<DataTableColumn> columns,
                           Cnd cnd, String linkName, Cnd subCnd) {
        NutMap re = new NutMap();
        if (orders != null && orders.size() > 0) {
            for (DataTableOrder order : orders) {
                DataTableColumn col = columns.get(order.getColumn());
                cnd.orderBy(Sqls.escapeSqlFieldValue(col.getData()).toString(), order.getDir());
            }
        }
        Pager pager = new OffsetPager(start, length);
        re.put("recordsFiltered", this.dao().count(this.getEntityClass(), cnd));
        List<base_place> list = this.dao().query(this.getEntityClass(), cnd, pager);
        if (!Strings.isBlank(linkName)) {
            if (subCnd != null)
                this.dao().fetchLinks(list, linkName, subCnd);
            else
                this.dao().fetchLinks(list, linkName);
        }
        for(int i=0;i<list.size();i++){
            try {
                base_place entity = list.get(i);

                Sys_dict dict=sysDictService.fetch(Cnd.where("id","=",entity.getAreaId()));
                entity.setArea(dict);



                dict=sysDictService.fetch(Cnd.where("id","=",entity.getTypeId()));
                entity.setType(dict);


                dict=sysDictService.fetch(Cnd.where("id","=",entity.getCtrlId()));
                entity.setCtrl(dict);


                list.set(i, entity);

            } catch (SecurityException e) {
                e.printStackTrace();
            }  catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        re.put("data", list);
        re.put("draw", draw);
        re.put("recordsTotal", length);
        return re;
    }


    /**
     * 新增
     *
     * @param place
     * @param pid
     */
    @Aop(TransAop.READ_COMMITTED)
    public void save(base_place place, String pid) {
        String path = "";
        if (!Strings.isEmpty(pid)) {
            base_place pp = this.fetch(pid);
            path = pp.getPath();
        }
        place.setPath(getSubPath("base_place", "path", path));
        place.setParentId(pid);
        dao().insert(place);
        if (!Strings.isEmpty(pid)) {
            this.update(Chain.make("hasChildren", true), Cnd.where("id", "=", pid));
        }
    }

    /**
     * 级联删除地点
     *
     * @param place
     */
    @Aop(TransAop.READ_COMMITTED)
    public void deleteAndChild(base_place place) {
        dao().execute(Sqls.create("delete from base_place where path like @path").setParam("path", place.getPath() + "%"));
        if (!Strings.isEmpty(place.getParentId())) {
            int count = count(Cnd.where("parentId", "=", place.getParentId()));
            if (count < 1) {
                dao().execute(Sqls.create("update base_dept set hasChildren=0 where id=@pid").setParam("pid", place.getParentId()));
            }
        }
    }

    /**
     * 更新地点数据
     *
     * @param place
     */
    @Aop(TransAop.READ_COMMITTED)
    public void update(base_place place) {
        dao().updateIgnoreNull(place);
        if (!Strings.isEmpty(place.getParentId())) {
            this.update(Chain.make("hasChildren", true), Cnd.where("id", "=", place.getParentId()));
        }
    }
}
