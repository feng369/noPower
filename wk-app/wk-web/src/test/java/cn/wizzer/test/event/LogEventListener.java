package cn.wizzer.test.event;

import org.nutz.aop.interceptor.async.Async;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.plugins.event.Event;
import org.nutz.plugins.event.EventListener;

/**
 * @author gongqin@dhgate.com
 * @varsion 2017-5-15
 */
@IocBean
public class LogEventListener implements EventListener {

    private Log log = Logs.get();

    /** 本监听器关注的事件主题 */
    @Override
    public String subscribeTopic() {
        return "log-event";
    }

    @Async  //加此注解可实现事件异步处理，必须在iocby中启用异步参数*async
    @Override
    public void onEvent(Event e) {
        log.info("thread-name:"+Thread.currentThread().getName());
        log.debugf("->into log event: %s", e.getParam());
        Lang.sleep((Integer)e.getParam() * 1000);
        log.debugf("-> out log event: %s", e.getParam());
    }

}
