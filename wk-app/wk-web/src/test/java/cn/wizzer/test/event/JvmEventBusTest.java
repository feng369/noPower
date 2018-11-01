package cn.wizzer.test.event;


import cn.wizzer.app.TestBase;
import org.junit.Test;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.combo.ComboIocLoader;
import org.nutz.lang.Lang;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.plugins.event.Event;
import org.nutz.plugins.event.EventBus;
import org.nutz.plugins.event.EventCallback;

/**
 * @author gongqin@dhgate.com
 * @varsion 2017-5-15
 */
public class JvmEventBusTest  {
    private Log log = Logs.get();

//        @Inject
//    private EventBus eventBus;

    // 同步事件处理
    @Test
    public void testSyncBase() throws Exception {
        //        ioc.get(Dao.class).insert(u);
        log.info("testSyncBase thread-name:"+Thread.currentThread().getName());
        Ioc ioc = new NutIoc(new ComboIocLoader("*anno", "cn.wizzer.test.event", "*org.nutz.plugins.event.EventIocLoader"));
        EventBus eventBus = ioc.get(EventBus.class, "eventBus");
        for (int i = 0; i < 4; i++) {
            Event e1 = new Event("log-event", i);;
            eventBus.fireEvent(e1);
        }
        ioc.depose();
    }

    // 异步事件处理
    @Test
    public void testAsyncBase() throws Exception {
        log.info("testAsyncBase thread-name:"+Thread.currentThread().getName());
        //2为异步线程池的大小
        /* 采用redis实现分布式事件，依赖nutz-integration-jedis。为提高了系统的稳定性，可参考EventBus接口扩展出基于MQ等方式的事件中心
            @IocBy(args={
                "*js", "ioc/",
                "*anno", "com.xxx",
                "*jedis", // 引用jedis插件
                "*org.nutz.plugins.event.EventIocLoader", "redis" // 增加参数启用redis事件引擎
            })

         */
        Ioc ioc = new NutIoc(new ComboIocLoader("*anno", "cn.wizzer.test.event", "*async", "2", "*org.nutz.plugins.event.EventIocLoader"));
        EventBus eventBus = ioc.get(EventBus.class, "eventBus");
        for (int i = 0; i < 4; i++) {
            Event e1 = new Event("log-event", i);
            e1.setCallback(new EventCallback() {
                @Override
                public void onEventFinished(Object result) {
                    System.out.println("finished:"+ e1.getParam()+" "+result);
                }
            });
            eventBus.fireEvent(e1);
        }
        Lang.sleep(15 * 1000);
        ioc.depose();
    }

}
