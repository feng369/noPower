package cn.wizzer.app.wx.modules.services.impl;

import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.wx.modules.models.Wx_config;
import cn.wizzer.app.wx.modules.services.WxConfigService;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.weixin.at.impl.DaoAccessTokenStore;
import org.nutz.weixin.impl.WxApi2Impl;
import org.nutz.weixin.spi.WxApi2;

import java.util.HashMap;
import java.util.Map;

/**
 * 如果是集群部署请使用RedisAccessTokenStore
 */
@IocBean(args = {"refer:dao"})
public class WxConfigServiceImpl extends BaseServiceImpl<Wx_config> implements WxConfigService {
    public WxConfigServiceImpl(Dao dao) {
        super(dao);
    }

    public WxApi2 getWxApi2(String wxid) {
        Wx_config appInfo = this.fetch(Cnd.where("id", "=", wxid));
        DaoAccessTokenStore myDaoAccessTokenStore = new DaoAccessTokenStore(dao());
        Map<String, Object> params = new HashMap<>();
        params.put("id", appInfo.getId());
        myDaoAccessTokenStore.setTableAccessToken("access_token");
        myDaoAccessTokenStore.setTableAccessTokenExpires("access_token_expires");
        myDaoAccessTokenStore.setTableAccessTokenLastat("access_token_lastat");
        myDaoAccessTokenStore.setFetch("select access_token,access_token_expires,access_token_lastat from wx_config where id=@id");
        myDaoAccessTokenStore.setUpdate("update wx_config set access_token=@access_token, access_token_expires=@access_token_expires, access_token_lastat=@access_token_lastat where id=@id");
        myDaoAccessTokenStore.setParams(params);
        WxApi2Impl wxApi2 = new WxApi2Impl();
        wxApi2.setAppid(appInfo.getAppid());
        wxApi2.setAppsecret(appInfo.getAppsecret());
        wxApi2.setEncodingAesKey(appInfo.getEncodingAESKey());
        wxApi2.setToken(appInfo.getToken());
        wxApi2.setAccessTokenStore(myDaoAccessTokenStore);
        return wxApi2;
    }

}
