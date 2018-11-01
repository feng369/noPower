package cn.wizzer.test;

import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.sys.modules.services.SysMobileService;
import com.mchange.v2.async.ThreadPoolAsynchronousRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(MyNutTestRunner.class)
@IocBean// 必须有
public class SimpleTest extends Assert {
    @Inject("refer:$ioc")
    protected Ioc ioc;

    @Inject
    protected Dao dao;
@Inject
private BasePersonService basePersonService;


    @Test
    public void test_user() throws Exception {
        String baseSql = "SELECT * FROM eq_trace WHERE lockid LIKE '%380' ORDER BY time DESC";
        Set<String> sets = new HashSet<>();
        while (true){
            if(sets.size() > 1){
                break;
            }
            Sql sql = Sqls.queryRecord(baseSql);
            dao.execute(sql);
            List<Record> list = sql.getList(Record.class);
            System.out.println(list.get(0));
            if(list.size() >  0){
                sets.add(list.get(0).getString("position"));
            }
            Thread.sleep(3000);
        }

    }
}