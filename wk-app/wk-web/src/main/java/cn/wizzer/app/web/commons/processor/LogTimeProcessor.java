package cn.wizzer.app.web.commons.processor;

import org.json.JSONObject;
import org.nutz.lang.Stopwatch;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.impl.processor.AbstractProcessor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by Wizzer.cn on 2015/7/2.
 */
public class LogTimeProcessor extends AbstractProcessor {

    private static final Log log = Logs.get();

    public void process(ActionContext ac) throws Throwable {
        Stopwatch sw = Stopwatch.begin();
        try {
            doNext(ac);
        } finally {
            sw.stop();
            if (log.isDebugEnabled()) {
                HttpServletRequest req = ac.getRequest();
                String uri = req.getRequestURI();
                if("/platform/eq/Map/getinfo".equals(uri)||         //硬件调用接口监控
                        "/platform/eq/use/openLock".equals(uri)||   //app开关锁接口关闭
                        "/platform/eq/use/returnLock".equals(uri)){
                    Map pmap = req.getParameterMap();
                    JSONObject json=new JSONObject(pmap);
                    uri=uri+"?"+json;
                }
                log.debugf("[%-4s]URI=%s %sms", req.getMethod(), uri, sw.getDuration());
            }
        }
    }
}
