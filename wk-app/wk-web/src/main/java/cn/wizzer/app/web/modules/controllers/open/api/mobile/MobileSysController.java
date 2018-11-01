package cn.wizzer.app.web.modules.controllers.open.api.mobile;

import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.sys.modules.models.*;
import cn.wizzer.app.sys.modules.services.*;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.app.web.commons.shiro.filter.PlatformAuthenticationFilter;
import cn.wizzer.app.web.commons.slog.SLogService;
import cn.wizzer.framework.base.Result;
import cn.wizzer.framework.util.PasswordStrengthCheck;
import cn.wizzer.framework.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.filter.CrossOriginFilter;
import org.nutz.mvc.impl.AdaptorErrorContext;
import org.nutz.plugins.apidoc.annotation.Api;
import org.nutz.plugins.apidoc.annotation.ApiMatchMode;
import org.nutz.plugins.apidoc.annotation.ApiParam;
import org.nutz.plugins.apidoc.annotation.ReturnKey;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

@IocBean
@At("/open/mobile/sys")
@Filters({@By(type = TokenFilter.class)})
@Api(name = "系统信息API", match = ApiMatchMode.ALL,description="用户登录、系统版本等无需token控制接口")
public class MobileSysController {
    private static final Log log = Logs.get();
    @Inject
    private SysMobileService sysMobileService;
    @Inject
    private SysApiService apiService;
    @Inject
    private SysUserService sysUserService;
    @Inject
    private SLogService sLogService;
    @Inject
    private BaseCnctobjService baseCnctobjService;
    @Inject
    private BasePersonService basePersonService;
    @Inject
    private SysUnitService sysUnitService;
    @Inject
    private SysUseraddService sysUseraddService;
    @Inject
    private Dao dao;

    @At("/getUnitByAirportId")
    @Ok("json")
    @Api(name="获取当前机场下所有公司单位",params ={@ApiParam(name = "airportid", type = "String", description = "机场id")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                @ReturnKey(key="data[i].text",description="公司单位名"),
                @ReturnKey(key="data[i].value",description="公司单位id")} )
    public Object getUnitByAirportId(@Param("airportid") String airportid){
        try{
            if(!Strings.isBlank(airportid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("unitairport","=",airportid);
                cnd.and("unitcode","<>","XTGL");
                List<Sys_unit> units = sysUnitService.query(cnd);
                List<HashMap> maps=new ArrayList<HashMap>();
                for(int i=0;i<units.size();i++){
                    HashMap map =new HashMap();
                    map.put("value",units.get(i).getId());
                    map.put("text",units.get(i).getName());
                    maps.add(map);
                }

                return Result.success("system.success",maps);
            }
            return Result.error(2,"system.error");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/checkLoginname")
    @Ok("json")
    @Api(name = "检查用户名重复"
            , params = {
            @ApiParam(name = "loginname", type = "String", description = "用户名"),
    }
            , ok = {@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key = "data.result", description = "此用户名:OK不重复,Fail存在重复")}
    )
    public Object checkLoginnameByMobile(@Param("loginname") String loginname){
        try{
            Cnd cnd=Cnd.NEW();
            Map map = new HashMap();
            cnd.and("loginname","=",loginname);
            Sys_user user=sysUserService.fetch(cnd);
            if(user==null){
                map.put("result","OK");
            }
            else{
                map.put("result","Fail");
            }
            return Result.success("system.success",map);
        }catch (Exception e){
            return Result.error("system.error",e);
        }
    }


    @At("/uploadCardImage")
    @Ok("json")
    //AdaptorErrorContext必须是最后一个参数
    @Api(name = "注册上传证件照"
            , params = {
            @ApiParam(name = "filename", type = "String", description = "图片名称"),
            @ApiParam(name = "base64", type = "String", description = "base64"),
            @ApiParam(name = "userid", type = "String", description = "用户id")
    }
            , ok = {@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)")}
    )
    public Object uploadCardImage(@Param("filename") String filename, @Param("base64") String base64,@Param("userid") String userid,HttpServletRequest req, AdaptorErrorContext err) {
        byte[] buffer=null;
        try {
            if (StringUtils.isBlank(base64)) {
                return Result.error("传入文件不能为空！");
            }else if (err != null && err.getAdaptorErr() != null) {
                return Result.error("传入文件不能为空！");
            } else if (StringUtils.isBlank(userid)) {
                return Result.error("传入用户信息不能为空!");
            } else {
                Sys_user sysUser = sysUserService.fetch(userid);
                if(sysUser==null){
                    return Result.error("系统未找到相关用户信息!");
                }
                String fn= R.UU32()+filename.substring(filename.lastIndexOf("."));
                String path = Globals.AppUploadPath+"/userCard/";
                String pathfile = Globals.AppUploadPath+"/userCard/" + fn ;
                File file=new File(Globals.AppRoot+path);
                if(!file.exists()){
                    file.mkdirs();
                }
                if(base64.indexOf(",")>=0){//兼容H5
                    buffer = Base64.getDecoder().decode(base64.split(",")[1]);
                }else{
                    buffer = Base64.getDecoder().decode(base64);
                }
                FileOutputStream out = new FileOutputStream(Globals.AppRoot+pathfile);
                out.write(buffer);
                out.close();
                //将上传的文件修改对应用户照片名称
                Cnd c=Cnd.NEW();
                c.and("userid","=",sysUser.getId());
                Sys_useradd useradd = sysUseraddService.fetch(c);
                String picPath=useradd.getPictureads();
                if(!Strings.isBlank(picPath)){
                    picPath+=","+pathfile;
                }else{
                    picPath=pathfile;
                }
                useradd.setPictureads(picPath);
                sysUseraddService.updateIgnoreNull(useradd);
                return Result.success("上传成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        } catch (Throwable e) {
            e.printStackTrace();
            return Result.error("图片错误:"+e.getMessage());
        }
    }

    @At("/addUserByMobile")
    @Ok("json")
    @Api(name = "用户注册"
            , params = {
            @ApiParam(name = "airportid", type = "String", description = "机场id"),
            @ApiParam(name = "username", type = "String", description = "用户姓名"),
            @ApiParam(name = "loginname", type = "String", description = "用户名"),
            @ApiParam(name = "password", type = "String", description = "密码"),
            @ApiParam(name = "unitid", type = "String", description = "单位id"),
            @ApiParam(name = "cardid", type = "String", description = "证件号"),
            @ApiParam(name = "sex", type = "String", description = "性别"),
            @ApiParam(name = "pictureads", type = "String", description = "图片地址"),
            @ApiParam(name = "tel", type = "String", description = "联系电话"),
            @ApiParam(name = "deptid", type = "String", description = "部门id"),
            @ApiParam(name = "jobs", type = "String", description = "职务id")
    }
            , ok = {@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)")
                    ,@ReturnKey(key = "data.userId", description = "用户id")}
    )
    public Object addUserByMobile(@Param("airportid") String airportid,  @Param("username") String username, @Param("loginname") String loginname,@Param("password") String password,@Param("unitid") String unitid,@Param("cardid")String cardid,@Param("sex")int sex,@Param("pictureads") String pictureads,@Param("tel") String tel,@Param("deptid")String  deptid,@Param("jobs") String jobs,HttpServletRequest req) {
        try{
            if(Globals.IsRegister.equals("0")){
                return Result.error("目前不允许进行用户注册！");
            }
            Sys_user sys_user = sysUserService.fetch(Cnd.where("loginname", "=", loginname).and("delFlag","=",0));
            if(sys_user !=null){
                return Result.error(3,"该用户已被使用");
            }
            List<base_person> basePersonList = basePersonService.query(Cnd.where("cardid", "=", cardid).and("airportid", "=", airportid));
            if(basePersonList.size() > 0 ){
                int count = baseCnctobjService.count(Cnd.where("personId", "=", basePersonList.get(0).getId()));
                if(count > 0){
                    //该证件号已经注册过
                    return Result.error(2,"该员工已经注册过");
                }
            }
            return  Result.success("system.success",sysUserService.addUserByMobile(airportid, username, loginname,password,unitid,cardid,sex,pictureads,tel,deptid,jobs));
        } catch (Exception e) {
            e.printStackTrace();

            return Result.error("system.error",e);
        }
    }

    @At("/logoutBymobile")
    @Ok("json")
    @Api(name = "退出系统"
            , params = {}
            , ok = {@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)")}
    )
    public Object logoutBymobile(HttpSession session) {
        try {
            Subject currentUser = SecurityUtils.getSubject();
            Sys_user user = (Sys_user) currentUser.getPrincipal();
            currentUser.logout();
            if (user != null) {
                Sys_log sysLog = new Sys_log();
                sysLog.setType("info");
                sysLog.setTag("用户登出");
                sysLog.setSrc(this.getClass().getName() + "#logout");
                sysLog.setMsg("成功退出系统！");
                sysLog.setIp(StringUtil.getRemoteAddr());
                sysLog.setOpBy(user.getId());
                sysLog.setOpAt((int) (System.currentTimeMillis() / 1000));
                sysLog.setUsername(user.getUsername());
                sLogService.async(sysLog);
                sysUserService.update(Chain.make("isOnline", false), Cnd.where("id", "=", user.getId()));
            }
            return Result.success("system.success");
        } catch (SessionException ise) {
            log.debug("Encountered session exception during logout.  This can generally safely be ignored.", ise);
            ise.printStackTrace();
            return Result.error("system.error",ise);
        } catch (Exception e) {
            log.debug("Logout error", e);
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/doLoginMobile")
    @Ok("json")
    @Filters(@By(type = PlatformAuthenticationFilter.class))
    @Api(name="手机登录",params ={
            @ApiParam(name = "username", type = "String", description = "用户名")
            , @ApiParam(name = "password", type = "String", description = "密码")
    }, ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
            @ReturnKey(key="data.userId",description="登录用户id")
            ,@ReturnKey(key="data.appid",description="appid")
            ,@ReturnKey(key="data.appsecret",description="appsecret")
            ,@ReturnKey(key="data.isNeedResetPwd",description="密码强弱：true or false,是否提示更改密码")
    })
    public Object doLoginMobile(@Attr("loginToken") AuthenticationToken token, HttpServletRequest req, HttpSession session)
    {
        int errCount = 0;
        try {

            Subject subject = SecurityUtils.getSubject();
            ThreadContext.bind(subject);
            subject.login(token);
            Sys_user user = (Sys_user) subject.getPrincipal();
            int count = user.getLoginCount() == null ? 0 : user.getLoginCount();
            sysUserService.update(Chain.make("loginIp", user.getLoginIp()).add("loginAt", (int) (System.currentTimeMillis() / 1000))
                            .add("loginCount", count + 1).add("isOnline", true)
                    , Cnd.where("id", "=", user.getId()));
            Sys_log sysLog = new Sys_log();
            sysLog.setType("info");
            sysLog.setTag("用户登陆");
            sysLog.setSrc(this.getClass().getName() + "#doLogin");
            sysLog.setMsg("成功登录系统！");
            sysLog.setIp(StringUtil.getRemoteAddr());
            sysLog.setOpBy(user.getId());
            sysLog.setOpAt((int) (System.currentTimeMillis() / 1000));
            sysLog.setUsername(user.getUsername());
            sLogService.async(sysLog);
            //添加生成token的参数20180330
            List<Sys_api> apiList = apiService.query();
            Sys_api api = null;
            if(apiList.size()>0){
                api = apiList.get(0);
            }else{
                return Result.error(403, "服务器端未启用移动应用!");//需要往Sys_api表中加一条数据即可使用
            }
            Map data = new HashMap();
            data.put("userid",user.getId());
            data.put("appid",api.getAppId());
            data.put("appsecret",api.getAppSecret());

            String pwd = new String((char[])token.getCredentials());//((CaptchaToken)token).getPassword()也可得到密码,本项目采用继承UsernamePasswordToken的自定义类CaptchaToken
            String[]dictionary=new String[]{user.getLoginname(),user.getUsername()};
            int level = PasswordStrengthCheck.showPassstrength(dictionary,pwd,8,20);
            data.put("isNeedResetPwd",level<=1?true:false);
            return Result.success("login.success",data);

        }  catch (LockedAccountException e) {
            return Result.error(3, "login.error.locked");
        } catch (UnknownAccountException e) {
            errCount++;
            SecurityUtils.getSubject().getSession(true).setAttribute("platformErrCount", errCount);
            return Result.error(4, "login.error.user");
        } catch (AuthenticationException e) {
            errCount++;
            SecurityUtils.getSubject().getSession(true).setAttribute("platformErrCount", errCount);
            return Result.error(5, "login.error.user");
        } catch (Exception e) {
            errCount++;
            SecurityUtils.getSubject().getSession(true).setAttribute("platformErrCount", errCount);
            return Result.error(6, "login.error.system");
        }

    }

    @At("/getDefaultAirport")
    @Ok("json")
    @Api(name="获取当前默认机场信息",params ={},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                @ReturnKey(key="data.airportid",description="系统参数defaultAirport：机场id")
    })
    public Object getDefaultAirport(){
        try{
            HashMap map =new HashMap();
            map.put("airportid",Globals.defaultAirport);
            return Result.success("system.success",map);
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getDeviceInfo")
    @Ok("json")
    public Object getDeviceInfo(@Param("deviceid") String deviceid){
        try{
            Cnd cnd=Cnd.NEW();
            cnd.and("deviceid","=",deviceid).desc("createTime");
//            List<Sys_mobile> mobile=sysMobileService.query(cnd,"user");
            Sys_mobile mobile=sysMobileService.fetch(cnd);
            if(mobile!=null){
                HashMap map=new HashMap();
                map.put("account",mobile.getAccount());
                map.put("password",mobile.getPassword());
                map.put("enddate",mobile.getEndDate());
                return Result.success("system.success",map);
            }
            return Result.error(2,"设备信息为空!");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }
}
