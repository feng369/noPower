package cn.wizzer.app.web.commons.base;

import cn.wizzer.app.base.modules.models.base_customer;
import cn.wizzer.app.base.modules.services.BaseCustomerService;
import cn.wizzer.app.sys.modules.models.Sys_config;
import cn.wizzer.app.sys.modules.models.Sys_route;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Times;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wizzer on 2016/12/19.
 */
public class Globals {
    //项目路径
    public static String AppRoot = "";
    //项目目录
    public static String AppBase = "";
    //项目名称
    public static String AppName = "航空一体化保障系统";
    //项目短名称
    public static String AppShrotName = "NutzWk";
    //项目域名
    public static String AppDomain = "127.0.0.1";
    //文件上传路径
    public static String AppUploadPath = "/upload";
    //20180419zhf1040
    //上传Excel
    public static String AppExcelPath = "/document";

    // 是否启用了队列
    public static boolean RabbitMQEnabled = false;
    //系统自定义参数
    public static Map<String, String> MyConfig = new HashMap<>();
    //自定义路由
    public static Map<String, Sys_route> RouteMap = new HashMap<>();

    //订单获取数刷新时间（秒）
    public static String OrderCountRef = "20";

    //地图刷新时间（分）
    public static String MapRef = "1";

    //设备归还有效距离（米）
    public static String BackDis="100";

    //是否控制归还距离
    public static String CtrlDis="1";

    //地图上显示多少距离内的标记，默认800米
    public static String QueryDis="800";

    //微信token有效期
    public static String WXExpire="7200";

    //锁通讯平台lucky地址
    public static String luckyUrl = "http://np.bv-cloud.com:8865/";

    //解锁地址
    public static String unLock="api/pro/unLock";

    //查询地址
    public static String QueryLock="api/pro/query";

    //开关锁发送指令间隔时间(即统一用户对同一个锁相同操作的间隔时间(毫秒))
    public static String opInterval ="3000";

    //桩位与设备的绑定关系 0 表示1对多 ； 1 表示1对1
    public static String StakeMaterielRatio="0";

    //移动端自动登录有效期
    public static String MobileExpire="15";

    //API请求token有效期(秒)
    public static String APITokenExpire="7200";

    //企业微信corpid
    public static String WxCorpID = "";

    //是否启用企业微信发送消息
    public static String WxCorpStart = "0";

    //App地图缩放比例
    public static String AppZoom = "16.2";

    //H5AppZoom地图缩放比例
    public static String H5AppZoom = "10";

    //地图背景色，默认标准（normal）具体见：http://lbs.amap.com/api/javascript-api/guide/create-map/mapstye
    public static String MapStyle = "normal";

    //设备电量预警值
    public static String PowerWarn="10";

    //设备失联预警时间(分钟)
    public static String NoSignal="30";

    //是否显示圆圈
    public static String ShowCircle="0";

    //是否允许用户注册
    public static String IsRegister="1";

    //默认机场,昆明长水国际机场ID
    public static String defaultAirport="91e6b25e7c9f4e69ac739225f86f8048";

    public static void initSysConfig(Dao dao) {
        Globals.MyConfig.clear();
        List<Sys_config> configList = dao.query(Sys_config.class, Cnd.NEW());
        for (Sys_config sysConfig : configList) {
            switch (sysConfig.getConfigKey()) {
                case "AppName":
                    Globals.AppName = sysConfig.getConfigValue();
                    break;
                case "AppShrotName":
                    Globals.AppShrotName = sysConfig.getConfigValue();
                    break;
                case "AppDomain":
                    Globals.AppDomain = sysConfig.getConfigValue();
                    break;
                case "AppUploadPath":
                    Globals.AppUploadPath = sysConfig.getConfigValue();
                    break;
                case "OrderCountRef":
                    Globals.OrderCountRef = sysConfig.getConfigValue();
                    break;
                case "MapRef":
                    Globals.MapRef = sysConfig.getConfigValue();
                    break;
                case "BackDis":
                    Globals.BackDis=sysConfig.getConfigValue();
                    break;
                case "CtrlDis":
                    Globals.CtrlDis=sysConfig.getConfigValue();
                    break;
                case "QueryDis":
                    Globals.QueryDis=sysConfig.getConfigValue();
                    break;
                case "WxExpire":
                    Globals.WXExpire=sysConfig.getConfigValue();
                    break;
                case "luckyUrl":
                    Globals.luckyUrl=sysConfig.getConfigValue();
                    break;
                case "unLock":
                    Globals.unLock=sysConfig.getConfigValue();
                    break;
                case "QueryLock":
                    Globals.QueryLock=sysConfig.getConfigValue();
                    break;
                case "opInterval":
                    Globals.opInterval=sysConfig.getConfigValue();
                    break;
                case "StakeMaterielRatio":
                    Globals.StakeMaterielRatio=sysConfig.getConfigValue();
                    break;
                case "MobileExpire":
                    Globals.MobileExpire=sysConfig.getConfigValue();
                    break;
                case "APITokenExpire":
                    Globals.APITokenExpire=sysConfig.getConfigValue();
                    break;
                case "WxCorpID":
                    Globals.WxCorpID=sysConfig.getConfigValue();
                    break;
                case "WxCorpStart":
                    Globals.WxCorpStart=sysConfig.getConfigValue();
                    break;
                case "AppZoom":
                    Globals.AppZoom=sysConfig.getConfigValue();
                    break;
                case "H5AppZoom":
                    Globals.H5AppZoom=sysConfig.getConfigValue();
                    break;
                case "MapStyle":
                    Globals.MapStyle=sysConfig.getConfigValue();
                    break;
                case "ShowCircle":
                    Globals.ShowCircle=sysConfig.getConfigValue();
                    break;
                case "IsRegister":
                    Globals.IsRegister=sysConfig.getConfigValue();
                    break;
                case "PowerWarn":
                    Globals.PowerWarn=sysConfig.getConfigValue();
                    break;
                case "NoSignal":
                    Globals.NoSignal=sysConfig.getConfigValue();
                    break;
                case "defaultAirport":
                    Globals.defaultAirport=sysConfig.getConfigValue();
                    break;
                default:
                    Globals.MyConfig.put(sysConfig.getConfigKey(), sysConfig.getConfigValue());
                    break;
            }
        }
    }

    public static void initRoute(Dao dao) {
        Globals.RouteMap.clear();
        List<Sys_route> routeList = dao.query(Sys_route.class, Cnd.where("disabled", "=", false));
        for (Sys_route route : routeList) {
            Globals.RouteMap.put(route.getUrl(), route);
        }
    }


}
