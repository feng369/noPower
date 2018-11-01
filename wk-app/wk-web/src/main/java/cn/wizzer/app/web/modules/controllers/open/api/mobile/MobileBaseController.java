package cn.wizzer.app.web.modules.controllers.open.api.mobile;

import cn.wizzer.app.eq.modules.models.eq_planeseat;
import cn.wizzer.app.eq.modules.services.EqPlaneseatService;
import cn.wizzer.app.sys.modules.models.Sys_mobile;
import cn.wizzer.app.sys.modules.models.Sys_role;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.app.sys.modules.models.Sys_version;
import cn.wizzer.app.sys.modules.services.*;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.commons.filter.TokenFilter;
import cn.wizzer.app.web.modules.controllers.open.api.Datatime.newDataTime;
import cn.wizzer.framework.base.Result;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.util.PasswordStrengthCheck;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.filter.CrossOriginFilter;
import org.nutz.mvc.impl.AdaptorErrorContext;
import org.nutz.plugins.apidoc.annotation.Api;
import org.nutz.plugins.apidoc.annotation.ApiMatchMode;
import org.nutz.plugins.apidoc.annotation.ApiParam;
import org.nutz.plugins.apidoc.annotation.ReturnKey;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@IocBean
@At("/open/mobile/base")
@Filters({@By(type = TokenFilter.class)})
@Api(name = "基础数据API", match = ApiMatchMode.ALL, description = "基础资料api接口")
public class MobileBaseController {
    @Inject
    private SysUserService sysUserService;
    @Inject
    private SysVersionService sysVersionService;
    @Inject
    private SysMobileService sysMobileService;
    @Inject
    private EqPlaneseatService eqPlaneseatService;
    @Inject
    private Dao dao;

    @At("/getUserImage")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name = "获取用户头像"
            , params = { @ApiParam(name = "userid", type = "String", description = "用户id")}
            , ok = {@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)")
            ,@ReturnKey(key = "data.path", description = "头像图片地址")}
    )
    public Object getUserImage(@Param("userid") String userid,HttpServletRequest req) {
        byte[] buffer=null;
        try {
            if (StringUtils.isBlank(userid)) {
                return Result.error("传入用户信息不能为空!");
            } else {
                Sys_user sysUser = sysUserService.fetch(userid);
                if(sysUser==null){
                    return Result.error("系统未找到相关用户信息!");
                }
                String path = Globals.AppUploadPath+"/userImage/";
                Map map = new HashMap();
                map.put("userid",userid);
                map.put("path",path+sysUser.getUserImage());
                return Result.success("成功",map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getUserInfo")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name = "得到用户信息"
            , params = {@ApiParam(name = "userid", type = "String", description = "用户ID")}
            , ok = {@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
            @ReturnKey(key = "data.userId", description = "用户ID"),
            @ReturnKey(key = "data.personId", description = "人员ID"),
            @ReturnKey(key = "data.username", description = "人员姓名"),
            @ReturnKey(key = "data.airportid", description = "机场ID"),
            @ReturnKey(key = "data.airportname", description = "机场名称"),
            @ReturnKey(key = "data.airportposition", description = "机场地理坐标"),
            @ReturnKey(key = "data.unitid", description = "单位ID"),
            @ReturnKey(key = "data.customerid", description = "客户id") ,
            @ReturnKey(key = "data.roleName", description = "角色名称"),
            @ReturnKey(key = "data.path", description = "用户头像路径"),
    }
    )
    public Object getUserInfo(@Param("userid") String userid){
        try{
            Sys_user curUser = sysUserService.fetch(userid);
            if(curUser == null){
                return Result.error(2,"user is null");
            }else{
                return Result.success("system.success",sysUserService.getUserInfo(curUser));
            }
        }catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        }

    }

    @At("/getRoleByUserId")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name = "获取当前登陆人角色"
            , params = {@ApiParam(name = "userid", type = "String", description = "用户ID")}
            , ok = {@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                @ReturnKey(key = "data.role", description = "用户角色名称")
            }
    )
    public Object getRoleByUserId(@Param("userid")String userid){
        try{
            if(!Strings.isBlank(userid)){
                Sys_user sysUser = dao.fetchLinks(dao.fetch(Sys_user.class, userid), "roles");
                List<Sys_role> roles = sysUser.getRoles();
                if(roles.size() > 0 ){
                    HashMap map=new HashMap();
                    map.put("role",roles.get(0).getName());
                    return  Result.success("system.success",map);
                }
                return Result.error(2,"roles is null");
            }
            return Result.error(2,"userid is null");
        }
        catch (Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getShowCircle")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name = "是否显示定位周边圆圈"
            , params = {}
            , ok = {@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
            @ReturnKey(key = "data.circle", description = "系统参数ShowCircle：0 不显示 1 显示")
    }
    )
    public Object getShowCircle(){
        try{
            HashMap map =new HashMap();
            map.put("circle",Globals.ShowCircle);
            return Result.success("system.success",map);
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }


    @At("/getAppZoom")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name = "App地图缩放比例"
            , params = {}
            , ok = {@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                @ReturnKey(key = "data.zoom", description = "系统参数AppZoom：地图缩放比例")}
    )
    public Object getAppZoom(){
        try{
            HashMap map =new HashMap();
            map.put("zoom",Globals.AppZoom);
            return Result.success("system.success",map);
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getCtrlDistance")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name = "获取地图是否控制归还距离"
            , params = {}
            , ok = {@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
            @ReturnKey(key = "data.ctrldis", description = "系统参数ctrldis：是否控制归还距离")}
    )
    public Object getCtrlDistance(){
        try{
            HashMap map =new HashMap();
            map.put("ctrldis",Globals.CtrlDis);
            return Result.success("system.success",map);
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }


    @At("/getQueryDistance")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name = "获取地图显示标记的半径距离"
            , params = {}
            , ok = {@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
            @ReturnKey(key = "data.dis", description = "系统参数QueryDis：显示多少距离内的标记")}
    )
    public Object getQueryDistance(){
        try{
            HashMap map =new HashMap();
            map.put("dis",Globals.QueryDis);
            return Result.success("system.success",map);
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/getBackDistance")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name = "设备归还有效距离"
            , params = {}
            , ok = {@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
            @ReturnKey(key = "data.backdis", description = "系统参数BackDis：显示多少距离内的标记")}
    )
    public Object getBackDistance(){
        try{
            HashMap map=new HashMap();
            map.put("backdis",Globals.BackDis);
            return Result.success("system.success",map);
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/uploadUserImage")
    @Ok("json")
    //AdaptorErrorContext必须是最后一个参数
    @Filters({@By(type = TokenFilter.class)})
    @Api(name = "修改用户头像" , params = {
            @ApiParam(name = "filename", type = "String", description = "图片名称"),
            @ApiParam(name = "base64", type = "String", description = "图片base64"),
            @ApiParam(name = "userid", type = "String", description = "用户ID"),
    }, ok = {@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
            @ReturnKey(key = "data.id",  description = "用户id"),
            @ReturnKey(key = "data.path",  description = "用户头像路径")
    })
    public Object uploadUserImage(@Param("filename") String filename, @Param("base64") String base64,@Param("userid") String userid,HttpServletRequest req, AdaptorErrorContext err) {
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
                String path = Globals.AppUploadPath+"/userImage/";
                String pathfile = Globals.AppUploadPath+"/userImage/" + fn ;
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
                if(StringUtils.isNotBlank(sysUser.getUserImage())){
                    String oldfile = Globals.AppRoot+path+sysUser.getUserImage();
                    File file0=new File(oldfile);
                    if(file0.exists()){
                        file0.delete();
                    }
                }
                sysUser.setUserImage(fn);
//                userService.updateIgnoreNull(sysUser);
                sysUserService.update(org.nutz.dao.Chain.make("userImage", fn), Cnd.where("id", "=", sysUser.getId()));
                return Result.success("上传成功","{\"id\":\""+userid+"\",\"path\":\""+(Globals.AppBase+pathfile)+"\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("system.error",e);
        } catch (Throwable e) {
            e.printStackTrace();
            return Result.error("图片错误:"+e.getMessage());
        }
    }

    @At("/changePasswordByMobile")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    @Api(name = "修改用户密码" , params = {
            @ApiParam(name = "userid", type = "String", description = "用户ID"),
            @ApiParam(name = "oldpassword", type = "String", description = "旧密码"),
            @ApiParam(name = "newpassword", type = "String", description = "新密码"),
    }, ok = {@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)")
    })
    public Object changePasswordByMobile(@Param("userid") String userid,@Param("oldpassword") String oldpassword, @Param("newpassword") String newpassword, HttpServletRequest req) {
        try {
//        Subject subject = SecurityUtils.getSubject();
//        Sys_user user = (Sys_user) subject.getPrincipal();
            if (StringUtils.isBlank(userid) || StringUtils.isBlank(oldpassword) || StringUtils.isBlank(newpassword)) {
                return Result.error("输入参数不能为空！");
            }
            Sys_user user = sysUserService.fetch(userid);
            if (user == null) {
                return Result.error("用户不存在！");
            }
            String old = new Sha256Hash(oldpassword, user.getSalt(), 1024).toBase64();
            if (old.equals(user.getPassword())) {
                //校验新密码强弱
//            String pwd = new String((char[])token.getCredentials());//((CaptchaToken)token).getPassword()也可得到密码,本项目采用继承UsernamePasswordToken的自定义类CaptchaToken
                String[] dictionary = new String[]{user.getLoginname(), user.getUsername()};
                int level = PasswordStrengthCheck.showPassstrength(dictionary, newpassword, 8, 20);
                if (level < 1) {
                    throw new ValidatException("新密码太弱，请重新设置新密码！");
                }

                RandomNumberGenerator rng = new SecureRandomNumberGenerator();
                String salt = rng.nextBytes().toBase64();
                String hashedPasswordBase64 = new Sha256Hash(newpassword, salt, 1024).toBase64();
                user.setSalt(salt);
                user.setPassword(hashedPasswordBase64);
                sysUserService.update(Chain.make("salt", salt).add("password", hashedPasswordBase64), Cnd.where("id", "=", user.getId()));
                return Result.success("修改成功");
            } else {
                return Result.error("原密码不正确");
            }
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("system.error", e);
        }
    }


    @At("/getVersion")
    @Ok("json")
    @Filters({@By(type=TokenFilter.class)})
    @Api(name="获取版本号",params ={@ApiParam(name = "name", type = "String", description = "版本名称")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data.name",description="使用名称"),
                    @ReturnKey(key="data.version",description="版本号"),
                    @ReturnKey(key="data.wgt",description="wgt号")} )
    public Object getVersion( @Param("name")String name){
        try{
            Cnd cnd = Cnd.NEW();
            cnd.and("name","=",name);
            List<Sys_version> svList = sysVersionService.query(cnd);
            HashMap map=new HashMap();
            if(svList.size()>0){
                return Result.success("system.success",svList.get(0));

            }
            throw new ValidatException("未找到版本号信息");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }

    @At("/setDeviceLogin")
    @Ok("json")
    @Filters({@By(type = TokenFilter.class)})
    public Object setDeviceLogin(@Param("deviceid") String deviceid,@Param("deviceOS") String deviceOS,@Param("deviceModel") String deviceModel,@Param("userid") String userid,@Param("expire") String expire,@Param("pstatus") String pstatus,@Param("account") String account,@Param("password") String password)
    {
        boolean insert=false;
        try {
            Cnd cnd=Cnd.NEW();
            cnd.and("deviceid","=",deviceid);
            Sys_mobile mobile=sysMobileService.fetch(cnd);
            if(mobile==null){
                mobile=new Sys_mobile();
                insert=true;
            }
            if(!Strings.isBlank(deviceid))
                mobile.setDeviceid(deviceid);
            if(!Strings.isBlank(deviceModel))
                mobile.setDeviceModel(deviceModel);
            if(!Strings.isBlank(deviceOS))
                mobile.setDeviceOS(deviceOS);
            if(!Strings.isBlank(pstatus))
                mobile.setPstatus(pstatus);
            if(!Strings.isBlank(userid))
                mobile.setUserid(userid);
            if(!Strings.isBlank(account))
                mobile.setAccount(account);
            if(!Strings.isBlank(password))
                mobile.setPassword(password);
            if(!Strings.isBlank(expire)){
                mobile.setStartDate(newDataTime.getDateYMDHMS());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DATE, Integer.parseInt(expire));
                String endDate = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(calendar.getTime());
                mobile.setEndDate(endDate);
            }

            if(insert==false){
                sysMobileService.update(mobile);

            }else{
                sysMobileService.insert(mobile);
            }
            return Result.success("设置成功");
        }catch(Exception e){
            return Result.error(-1,"设置失败");
        }
    }

    @At("/getPlaneseat")
    @Ok("json")
    @Filters({@By(type=TokenFilter.class)})
    @Api(name="获取机位列表信息",params ={@ApiParam(name = "airportid", type = "String", description = "机场id")},
            ok={@ReturnKey(key = "code", description = "0 操作成功；非0 操作失败"),@ReturnKey(key = "msg", description = "(提示信息)"),
                    @ReturnKey(key="data[i].position",description="机位坐标位置"),
                    @ReturnKey(key="data[i].seatname",description="机位名称"),
                    @ReturnKey(key="data[i].seatnum",description="机位编号"),
                    @ReturnKey(key="data[i].id",description="机位id")} )
    public Object getPlaneseat(@Param("airportid") String airportid){
        try{
            if(!Strings.isBlank(airportid)){
                Cnd cnd=Cnd.NEW();
                cnd.and("airportid","=",airportid);
                List<eq_planeseat> planeseats = eqPlaneseatService.query(cnd);
                List<HashMap> mapList=new ArrayList<>();
                if(planeseats.size()>0){
                    for(int i=0;i<planeseats.size();i++){
                        HashMap map=new HashMap();
                        map.put("position",planeseats.get(i).getPosition());
                        map.put("seatname",planeseats.get(i).getSeatname());
                        map.put("seatnum",planeseats.get(i).getSeatnum());
                        map.put("id",planeseats.get(i).getId());
                        mapList.add(map);
                    }
                }
                return Result.success("system.success",mapList);
            }
            return Result.error(2,"airportid is null");
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("system.error",e);
        }
    }



}
