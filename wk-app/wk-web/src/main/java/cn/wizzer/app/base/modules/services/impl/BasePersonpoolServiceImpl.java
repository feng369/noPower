package cn.wizzer.app.base.modules.services.impl;

import cn.wizzer.framework.base.Result;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.base.modules.models.base_personpool;
import cn.wizzer.app.base.modules.services.BasePersonpoolService;
import cn.wizzer.framework.page.OffsetPager;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

import java.util.List;

@IocBean(args = {"refer:dao"})
public class BasePersonpoolServiceImpl extends BaseServiceImpl<base_personpool> implements BasePersonpoolService {
    public BasePersonpoolServiceImpl(Dao dao) {
        super(dao);
    }


    public NutMap getPersonRow(int length,int start,int draw,Cnd cnd,String linkName) {
        Daos.CHECK_COLUMN_NAME_KEYWORD  = true;
        Sql sqlcount =Sqls.queryRecord("SELECT distinct(rowofficeid),startdata,enddata FROM base_personpool order by startdata asc");
        dao().execute(sqlcount);
        List listcount = sqlcount.getList(Record.class);

        Pager pager = new OffsetPager(start, length);
        Sql sql = Sqls.queryRecord("SELECT distinct(t.rowofficeid),t1.officename,t.startdata,t.enddata,t.pstatus FROM base_personpool t  INNER JOIN base_rowoffice t1 on t.rowofficeid=t1.id order by t.startdata desc");
        sql.setPager(pager);
        dao().execute(sql);
        List list = sql.getList(Record.class);
        NutMap re = new NutMap();
        if (!Strings.isBlank(linkName)) {
            this.dao().fetchLinks(list, linkName);
        }
        re.put("recordsFiltered", listcount.size());
        re.put("data", list);
        re.put("draw", draw);
        re.put("recordsTotal", length);
        return re;
    }
    //检查班次
    public boolean checkWork(String id ,String startdata){
        Cnd cnd = Cnd.NEW();
        cnd.and("rowofficeid", "=", id);
        cnd.and("enddata", ">=", startdata);
        cnd.and("pstatus","=","1");
        List<base_personpool> basepersonpools = this.dao().query(base_personpool.class,cnd);
        if (basepersonpools.size() > 0) {
            return false;
        }else{
            return true;
        }
    }
}
