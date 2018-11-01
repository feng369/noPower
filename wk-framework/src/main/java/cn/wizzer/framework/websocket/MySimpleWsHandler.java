package cn.wizzer.framework.websocket;

import org.nutz.lang.util.NutMap;
import org.nutz.plugins.mvc.websocket.handler.SimpleWsHandler;


public class MySimpleWsHandler extends SimpleWsHandler {
    public MySimpleWsHandler() {
        super(""); // 覆盖默认前缀
    }
    public void sayhi(NutMap req) { // 对应js端的action名称,方法参数必须是NutMap哦
        String name = req.getString("name");// 可以拿到页面发过来的任意内容
        NutMap resp = new NutMap("action", "notify"); // 响应的内容完全由你决定,推荐用{action:"xxx", ....}
        resp.setv("msg", "hi, " + name);
        endpoint.sendJson(session.getId(), resp); // 通过endpoint可以发生给任何你想发生的对象, session就是当前WebSocket的会话.
    }
}