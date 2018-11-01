package cn.wizzer.framework.websocket;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.plugins.mvc.websocket.AbstractWsEndpoint;
import org.nutz.plugins.mvc.websocket.NutWsConfigurator;
import org.nutz.plugins.mvc.websocket.WsHandler;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/*
ServerEndpoint是websocket的必备注解, value是映射路径, configurator是配置类.
特别提醒: 已知限制, Endpoint类不能使用@Aop或者aop相关的注解(如@Async/@SLog)
 */
@ServerEndpoint(value = "/websocket", configurator=NutWsConfigurator.class)
@IocBean // 使用NutWsConfigurator的必备条件
public class MyWebsocket extends AbstractWsEndpoint {
    // 并不需要你马上实现任何方法,它也马上能工作
    public WsHandler createHandler(Session session, EndpointConfig config) {
        return new MySimpleWsHandler(); // 是的,返回你自己的实现类就可以了,需要每次新建哦
    }
}