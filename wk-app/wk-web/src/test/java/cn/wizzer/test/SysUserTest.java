package cn.wizzer.test;

import cn.wizzer.app.TestBase;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.app.sys.modules.services.SysWxService;
import cn.wizzer.app.sys.modules.services.impl.SysWxServiceImpl;
import cn.wizzer.app.web.commons.base.Globals;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.junit.Test;
import org.nutz.dao.Dao;

/**
 * Created by wizzer on 2017/5/19.
 */
public class SysUserTest extends TestBase {

    @Override
    protected void _before() throws Exception {
        super._before();
    }

    @Test
    public void sendWxMsg() throws Exception {

        SysWxService sysWxService = ioc.get(SysWxServiceImpl.class);
        String[]user = new String[]{"14923292cea54d92830a8dc977e5f86b","db131168193649f4b9810f69a150eafe","0cda5d04195e4183b9d1984501251c0f"};

        sysWxService.sendMessageToUser(user,"10001","您好！欢迎使用无动力设备管理系统。");

    }


    @Test
    public void testInsert() {
        Sys_user user = new Sys_user();
        user.setLoginname("test");
        user.setUsername("测试帐号");
        user.setOpAt((int) (System.currentTimeMillis() / 1000));
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        String salt = rng.nextBytes().toBase64();
        String hashedPasswordBase64 = new Sha256Hash("1", salt, 1024).toBase64();
        user.setSalt(salt);
        user.setPassword(hashedPasswordBase64);
        user.setLoginIp("127.0.0.1");
        user.setLoginAt(0);
        user.setLoginCount(0);
        user.setEmail("wizzer@qq.com");
        user.setLoginTheme("palette.css");
        user.setLoginBoxed(false);
        user.setLoginScroll(false);
        user.setLoginSidebar(false);
        user.setLoginPjax(true);
        user.setUnitid("");
        Sys_user dbuser = ioc.get(Dao.class).insert(user);
        assertTrue(dbuser != null);
        assertTrue(!dbuser.isDisabled());
    }
}
