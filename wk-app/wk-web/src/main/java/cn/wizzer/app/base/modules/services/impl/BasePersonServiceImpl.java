package cn.wizzer.app.base.modules.services.impl;

import cn.wizzer.app.base.modules.models.*;
import cn.wizzer.app.base.modules.services.*;
import cn.wizzer.app.sys.modules.models.*;
import cn.wizzer.app.sys.modules.services.SysRoleService;
import cn.wizzer.app.sys.modules.services.SysUnitService;
import cn.wizzer.app.sys.modules.services.SysUserService;
import cn.wizzer.app.sys.modules.services.SysUseraddService;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.framework.base.ValidatException;
import cn.wizzer.framework.base.dao.entity.annotation.Ref;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.entity.annotation.*;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.upload.TempFile;

import java.io.*;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@IocBean(args = {"refer:dao"})
public class BasePersonServiceImpl extends BaseServiceImpl<base_person> implements BasePersonService {
    @Inject
    private BaseCnctobjService baseCnctobjService;
    @Inject
    private SysUserService sysUserService;
    @Inject
    private SysRoleService sysRoleService;
    @Inject
    private SysUnitService sysUnitService;
    @Inject
    private BaseDeptService baseDeptService;
    @Inject
    private BaseJobService baseJobService;
    @Inject
    private BasePostService basePostService;
    @Inject
    private BaseAirportService baseAirportService;
    public BasePersonServiceImpl(Dao dao) {
        super(dao);
    }


    public List<Sys_unit> getDatas(String personid) {
        Sql sql = Sqls.create("select a.id,a.name from sys_unit a INNER JOIN base_person b WHERE a.id = b.unitid and b.id = @personid");
        sql.params().set("personid", personid);
        sql.params().set("f", false);
        Entity<Sys_unit> entity = dao().getEntity(Sys_unit.class);
        sql.setEntity(entity);
        sql.setCallback(Sqls.callback.entities());
        dao().execute(sql);
        return sql.getList(Sys_unit.class);
    }

    //根据登录人ID获取人员相关信息
    public base_person getPersonInfo() {
        try {
            Subject subject = SecurityUtils.getSubject();
            Sys_user curUser = (Sys_user) subject.getPrincipal();
            String userid = curUser.getId();
            //通过登录人ID获取人员信息

            base_person person = new base_person();
            Cnd cnd = Cnd.NEW();
            cnd.and("userId", "=", userid);
            base_cnctobj cnctobj = baseCnctobjService.fetch(cnd);

            if (cnctobj != null) {
                person = this.fetch(cnctobj.getPersonId());
            }
            return person;
        } catch (Exception e) {
            return null;
        }
    }


    //绑定系统人员和微信用户账号
    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void bindWxuser(String id, String wxuserid) {
        base_person basePerson = this.fetch(id);
        if (basePerson != null) {
            if (StringUtils.isNotBlank(basePerson.getWxUserid())) {
                dao().execute(Sqls.create("update sys_wx_user set isBindPerson=0 where userid=@wxuserid").setParam("wxuserid", basePerson.getWxUserid()));
            }
//            basePerson.setWxUserid(wxuserid);
//            this.updateIgnoreNull(basePerson);
            dao().execute(Sqls.create("update base_person set wxUserid=@wxuserid where id=@id").setParam("wxuserid", wxuserid).setParam("id", id));
            dao().execute(Sqls.create("update sys_wx_user set isBindPerson=1 where userid=@wxuserid").setParam("wxuserid", wxuserid));
        } else {
            throw new ValidatException("未找到该人员信息！");
        }

    }

    //自动匹配人员和微信账号
    @Override
    @Aop(TransAop.READ_COMMITTED)
    public void autoBindWxuser() {

    }

    //20180322zhf1216
    public NutMap judgeLeaderByPersonNameAndCardId(String personname, String cardid, String unitid) {
        NutMap jsonObj = new NutMap();
        base_person basePerson = null;
        if (!Strings.isBlank(cardid) && !Strings.isBlank(unitid)) {
            //写了证件号
            if (!Strings.isBlank(personname)) {
                basePerson = this.fetch(Cnd.where("cardid", "=", cardid).and("unitid", "=", unitid).and("personname", "=", personname));
                if (basePerson != null) {
                    //防止表单数据重复提交(防止一个人又重新填写用户名和密码,导致多个账号)
                    repeatDataSubmit(personname, cardid, unitid, jsonObj, "该员工已经注册过账号");
                    //通过证件号与通过真实姓名查找出的base_person是一致的
                    if (basePerson.isLeader()) {
                        //是领导
                        jsonObj.put("isLeader", true);
                    } else {
                        //不是领导
                        jsonObj.put("isLeader", false);
                    }
                } else {
                    jsonObj.put("errMsg", "该员工不存在!您的真实姓名、单位、或证件号有误！");
                }
            }
        } else {
            //没写证件号(只能通过personname来判断)
            judgeLeaderByPersonName(personname, unitid, jsonObj);
        }
        return jsonObj;
    }

    //20180322zhf1216
    //通过真实姓名判断是否是领导
    private void judgeLeaderByPersonName(String personname, String unitid, NutMap jsonObj) {
        List<base_person> basePersonList = this.query(Cnd.where("personname", "=", personname).and("unitid", "=", unitid));
        //是否有同名同姓的人且是leader
        boolean flag = false;
        if (basePersonList.size() > 1) {

            //说明有同名同姓的人
            for (base_person basePerson : basePersonList) {
                if (basePerson.isLeader()) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                repeatDataSubmit(personname, null, unitid, jsonObj, "您的账号正在审核中");
                //同名同姓,有leader
                jsonObj.put("needAudit", "需要进入后台审核!");
            } else {
                //同名同姓,不是leader
                jsonObj.put("isLeader", false);
            }
        } else if (basePersonList.size() == 1) {
            //说明找到一个
            if (basePersonList.get(0).isLeader()) {
                //防止表单数据重复提交(防止一个人又重新填写用户名和密码,导致多个账号)
                repeatDataSubmit(personname, null, unitid, jsonObj, "您的账号正在审核中");
                jsonObj.put("isLeader", true);
            } else {
                jsonObj.put("isLeader", false);
            }
        } else {
            jsonObj.put("errMsg", "该员工不存在!");
        }
    }

    //20180322zhf1216
    //防止重复数据反复提交(1.是领导 2.是普通员工,填写了证件号,且通过验证)
    public void repeatDataSubmit(String personname, String cardid, String unitid, NutMap jsonObj, String msg) {
        base_cnctobj baseCnctobj = null;
        int count = 0;
        if (!Strings.isBlank(cardid)) {
            base_person basePerson = this.fetch(Cnd.where("cardid", "=", cardid).and("unitid", "=", unitid).and("personname", "=", personname));
            if(basePerson != null){
                baseCnctobj = baseCnctobjService.fetch(Cnd.where("personId", "=", basePerson.getId()));
            }
        } else {
            count = sysUserService.count("sys_user", Cnd.where("unitid", "=", unitid).and("username", "=", personname).and("delFlag","=",0));
        }
        if (baseCnctobj != null || count > 0) {
            //说明已经注册过
            jsonObj.put("errMsg", msg);
        }
    }

    public Object uploadFile(TempFile tf) {
        Map<String, Object> map = new HashMap<>();
        if (tf == null || "".equals(tf)){
            map.put("info",0);
            map.put("result", "导入失败!请选择5M以内的Excel文件");
            return Json.toJson(map);
        }
        try {
            String[] postfix = tf.getSubmittedFileName().split("\\.");
            String date = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
            String path = Globals.AppRoot+Globals.AppUploadPath+"/uploadExcel";
            File temp = new File(path);
            if (!temp.exists()) {
                temp.mkdirs();
            }
            String fullPath = temp.getPath() + "\\" + postfix[postfix.length - 2] + date + "." + postfix[postfix.length - 1];
            //将目标文件写入该绝对路径中
            tf.write(fullPath);
            //将临时文件删除
            tf.delete();
            List<Map<String, String>> errMaps = excel2db(fullPath);
            if(errMaps.size() > 0 ){
                map.put("info",2);
                map.put("result", "导入数据完毕,一些数据存在问题,确定下载日志文件查看原因吗?");
                map.put("errList",errMaps);
                //将错误信息打印到文档文件中
                try{
                    String filePath = Globals.AppRoot + Globals.AppExcelPath;
                    File doc =  new File(filePath);
                    if(!doc.exists()){
                        doc.mkdirs();
                    }
                    PrintStream ps = new PrintStream(new File(filePath + "/downText.txt"),"utf-8");
                    map.put("path",filePath + "/downText.txt");
                    for (Map<String, String> errMap : errMaps) {
                        for (Map.Entry<String, String> entry : errMap.entrySet()) {
                            ps.println(entry.getValue());
                        }
                    }
                    ps.close();
                }catch (Exception e){
                    e.printStackTrace();
                    map.put("info",0);
                    map.put("result", "导入数据完毕，但出现异常:"+e.getMessage());
                }
            }else{
                map.put("info",1);
                map.put("result", "操作完成，没有导入任何数据！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put("info",0);
            map.put("result", "导入数据失败:"+e.getMessage());
        }
        return Json.toJson(map);
    }
    //
    public String getPersonNum(String airportid,String unitid){
        base_airport airport=baseAirportService.fetch(airportid);
        Sys_unit unit=sysUnitService.fetch(unitid);
        Cnd cnd1=Cnd.NEW();
        cnd1.and("airportid","=",airportid);
        cnd1.and("unitid","=",unitid);
        cnd1.desc("createTime");
        List<base_person> personList=this.query(cnd1);

        String pnumber="";
        System.out.println(personList.get(0).getPersonnum());
        System.out.println(personList.get(0));

        if(personList.size()>0 && personList.get(0).getPersonnum().split("-").length>1){
            pnumber = personList.get(0).getPersonnum().split("-")[2];
            int num=Integer.parseInt(pnumber)+1;
            switch(String.valueOf(num).length()){
                case 1:
                    pnumber=airport.getAirportnum()+"-"+unit.getUnitcode()+"-000"+String.valueOf(num);
                    break;
                case 2:
                    pnumber=airport.getAirportnum()+"-"+unit.getUnitcode()+"-00"+String.valueOf(num);
                    break;
                case 3:
                    pnumber=airport.getAirportnum()+"-"+unit.getUnitcode()+"-0"+String.valueOf(num);
                    break;
                case 4:
                    pnumber=airport.getAirportnum()+"-"+unit.getUnitcode()+"-"+String.valueOf(num);
                    break;
            }
        }else{
            pnumber=airport.getAirportnum()+"-"+unit.getUnitcode()+"-0001";
        }

        return pnumber;
    }

    private List<Map<String,String>> excel2db(String fullPath) throws Exception {
        //获得文件输入流
        InputStream is = new FileInputStream(fullPath);
        Workbook wb = null;
        try{
            wb = new HSSFWorkbook(is);
        }catch (Exception e){
            is = new FileInputStream(fullPath);
            wb = new XSSFWorkbook(is);
        }
        base_person basePerson = null;
        List<Map<String,String>> errList = new ArrayList<>();

        //记录第几行第几列有错误信息
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            //第i张表
            Sheet sheet = wb.getSheetAt(i);
            if (null == sheet) {
                continue;
            }
            Map<String ,String>personNumMap = new HashMap<>();//编号不能重复
            Map<String ,String>personCardMap = new HashMap<>();//证件号不能重复
            Map<String ,String>personPhoneMap = new HashMap<>();//手机号码不能重复
            //遍历表的行(从第二行开始,第一行为属性)
            for (int j = 1; j <= sheet.getLastRowNum(); j++) {
                Map<String,String> errData = new HashMap<>();
                Row row = sheet.getRow(j);
                if (row != null) {
                    basePerson = new base_person();
                    Cell personNum = row.getCell(0);
                    Cell personName = row.getCell(1);
                    Cell sex = row.getCell(2);
                    Cell cardId = row.getCell(3);
                    Cell tel = row.getCell(4);
                    Cell unitName = row.getCell(5);
                    Cell deptName = row.getCell(6);
                    Cell postName = row.getCell(7);
                    Cell jobName = row.getCell(8);
                    Cell airportName = row.getCell(9);
                    //转换成字符串
                    String airpotNameText = getCellValue(airportName);
                    String personNumText = getCellValue(personNum);
                    String personNameText = getCellValue(personName);
                    String sexText = getCellValue(sex);
                    String cardIdText = getCellValue(cardId);
                    String telText = getCellValue(tel);
                    String unitNameText = getCellValue(unitName);
                    String deptNameText = getCellValue(deptName);
                    String postNameText = getCellValue(postName);
                    String jobNameText = getCellValue(jobName);
                    //对附件中本身数据校验,如必录、唯一、格式
                    if(Strings.isBlank(airpotNameText)){
                        errData.put("airport","第"+(j+1) + "行,机场名称不能为空!");
                    }
                    if(Strings.isBlank(personNumText)){
                        errData.put("personName","第" +(j+1)+"行,员工编号不能为空!");;
                    }else{
                        if(personNumMap.containsKey(personNumText)){
                            errData.put("personNum","第" +(j+1)+"行,员工编号重复!");
                        }else {
                            personNumMap.put("personNum",personNumText);
                        }
                    }
                    if(Strings.isBlank(personNameText)){
                        errData.put("personName","第" +(j+1)+"行,姓名不能为空!");
                    }
                    if(Strings.isNotBlank(sexText) && !"男".equals(sexText) && !"女".equals(sexText)){
                        errData.put("sex","第" +(j+1)+"行,性别有误!");
                    }
                    if(Strings.isBlank(cardIdText)){
                        errData.put("cardId","第" + (j+1) + "行,证件号不能为空!");
                    }else{
                        if(personCardMap.containsKey(cardIdText)){
                            errData.put("cardId","第" +(j+1)+"行,证件号重复!");
                        }else {
                            personCardMap.put("cardId",cardIdText);
                        }
                    }
                    if(Strings.isBlank(telText)){
                        errData.put("tel","第"+(j+1) + "行,电话号码不能未空!");
                    }else if(!isPhone(telText)){
                        errData.put("tel","第"+(j+1) + "行,电话号码格式不正确!");
                    }else{
                        if(personPhoneMap.containsKey(telText)){
                            errData.put("tel","第" +(j+1)+"行,电话号码重复!");
                        }else {
                            personPhoneMap.put("personNum",telText);
                        }
                    }
                    if(Strings.isBlank(unitNameText)){
                        errData.put("unit","第"+(j+1) + "行,公司名称不能为空!");
                    }

                    if(errData.size() > 0){
                        errList.add(errData);
                        continue;
                    }

                    //========与后台数据的校验
                    //机场
                    base_airport baseAirport = baseAirportService.fetch(Cnd.where("airportname", "=", airpotNameText));
                    if(baseAirport != null){
                        String airportId = baseAirport.getId();
                        basePerson.setAirportid(airportId);
                    }else{
                        errData.put("airport","第"+(j+1) + "行,机场名称未找到!");
                    }

                    if(Strings.isNotBlank(basePerson.getAirportid())) {
                        //员工编号
                        List<base_person> opList = this.query("id", Cnd.where("airportid", "=", basePerson.getAirportid()).and("personnum", "=", personNumText));
                        if (opList.size() > 0) {
                            errData.put("personNum", "第" + (j + 1) + "行,员工编号在系统中已经存在!");
                        }else{
                            basePerson.setPersonnum(personNumText);
                        }
                        //员工证件号
                        if(Strings.isNotBlank(cardIdText)){
                            int cn = this.count(Cnd.where("airportid", "=", basePerson.getAirportid()).and("cardid","=",cardIdText));
                            if(cn>0){
                                errData.put("cardid","第" + (j + 1) + "行,员工证件号已经存在!");
                            }else{
                                basePerson.setCardid(cardIdText);
                            }
                        }
                        //电话号码
                        if(Strings.isNotBlank(telText)){
                            int c = this.count("base_person",Cnd.where("airportid", "=", basePerson.getAirportid()).and("tel", "=", telText));
                            if(c>0){
                                errData.put("tel","第" + (j + 1) + "行,员工手机号码已经存在!");
                            }else{
                                basePerson.setTel(telText);
                            }
                        }
                        //公司
                        Sys_unit sysUnit = sysUnitService.fetch(Cnd.where("name", "=", unitNameText).and("unitairport", "=", basePerson.getAirportid()));
                        if (sysUnit != null) {
                            String sysUnitId = sysUnit.getId();
                            basePerson.setUnitid(sysUnitId);
                        }else{
                            errData.put("unit","第"+(j+1) + "行,公司名称未找到!");
                        }

                        if(Strings.isNotBlank(basePerson.getUnitid())) {
                           //部门
                            if(Strings.isNotBlank(deptNameText)){
                                base_dept baseDept = baseDeptService.fetch(Cnd.where("deptname", "=",deptNameText).and("unitid","=",basePerson.getUnitid()));
                                if(baseDept != null){
                                    String deptId = baseDept.getId();
                                    basePerson.setDeptid(deptId);
                                }else{
                                    errData.put("baseDept","第"+(j+1) + "行,部门名称未找到!");
                                }
                            }
                           //岗位
                            if(Strings.isNotBlank(postNameText)){
                                base_post basePost = basePostService.fetch(Cnd.where("postname", "=", postNameText).and("unitid","=",basePerson.getUnitid()));
                                if(basePost != null){
                                    String jobId = basePost.getId();
                                    basePerson.setPostid(jobId);
                                }else{
                                    errData.put("job","第"+(j+1) + "行,岗位名称未找到!");
                                }
                            }
                           //职务
                            if(Strings.isNotBlank(jobNameText)){
                                base_job baseJob = baseJobService.fetch(Cnd.where("jobname", "=", jobNameText).and("unitid","=",basePerson.getUnitid()));
                                if(baseJob != null){
                                    String jobId = baseJob.getId();
                                    basePerson.setJobid(jobId);
                                }else{
                                    errData.put("job","第"+(j+1) + "行,职务名称不正确!");
                                }
                            }
                        }
                    }
                    //姓名
                    basePerson.setPersonname(personNameText);
                    //性别
                    if ("男".equals(sexText)) {
                        basePerson.setSex(0);
                    } else if("女".equals(sexText)){
                        basePerson.setSex(1);
                    }else{
                        basePerson.setSex(2);
                    }

                    basePerson.setEmptypeId("empType.employee");//默认人员类型：员工

                    if(errData.size() > 0){
                        errList.add(errData);
                    }else{
                        //人员编号
//                        basePerson.setPersonnum(getPersonNum(basePerson.getAirportid(),basePerson.getUnitid()));
                        this.insert(basePerson);
                    }
                }
                //继续遍历
            }
            is.close();
        }
        return errList;
    }


    //处理Excel导入进来时的数据类型
    private String getCellValue (Cell cell) {
        String cellValue = "";
        if (null != cell) {
                // 以下是判断数据的类型
                switch (cell.getCellType()) {
                    case HSSFCell.CELL_TYPE_NUMERIC: // 数字
                        if (0 == cell.getCellType()) {//判断单元格的类型是否则NUMERIC类型
                            if (HSSFDateUtil.isCellDateFormatted(cell)) {// 判断是否为日期类型
                                Date date = cell.getDateCellValue();
                                DateFormat formater = new SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm");
                                cellValue = formater.format(date).trim();
                            } else {
                                cellValue = ((XSSFCell) cell).getRawValue();//0602简化直接取数值字符串返回即可
//                                //排除特殊符号
//                                if (("" + cell.getNumericCellValue()).trim().indexOf("E") != -1 || ("" + cell.getNumericCellValue()).trim().indexOf("e") != -1 || ("" + cell.getNumericCellValue()).trim().indexOf("+") != -1) {
//                                    cellValue = new BigDecimal((cell.getNumericCellValue() + "").trim()).toString();
//                                } else {
//                                    cellValue = (int) cell.getNumericCellValue() + "";
//                                }
                            }
                        }
                        break;
                    case HSSFCell.CELL_TYPE_STRING: // 字符串
                        cellValue = cell.getStringCellValue().trim();
                        break;
                    case HSSFCell.CELL_TYPE_BOOLEAN: // Boolean
                        cellValue = (cell.getBooleanCellValue() + "").trim();
                        break;
                    case HSSFCell.CELL_TYPE_FORMULA: // 公式
                        cellValue = (cell.getCellFormula() + "").trim();
                        break;
                    case HSSFCell.CELL_TYPE_BLANK: // 空值
                        cellValue = "";
                        break;
                    case HSSFCell.CELL_TYPE_ERROR: // 故障
                        cellValue = "非法字符";
                        break;
                    default:
                        cellValue = "未知类型";
                        break;
                }
        }
        return cellValue;
    }
    //验证电话号码
    private static boolean isPhone(String tel) {
        return Pattern.compile("^[1][3,4,5,6,7,8,9][0-9]{9}$").matcher(tel).matches();
    }


    @Override
    @Aop(TransAop.READ_COMMITTED)
    public Map createuser(String loginType, String roleId, String[] ids) {
        if(ids==null || ids.length<=0)return null;
        Sys_role role = null;
        if(Strings.isNotBlank(roleId)){
            role = sysRoleService.fetch(roleId);
            if(role==null)
                throw new ValidatException("角色参数不正确！");
        }
        if(!"personNum".equals(loginType)&&!"phone".equals(loginType)){
            throw new ValidatException("参数loinType不正确！");
        }
        //去掉已经建立了联系对象的人员
        List<base_cnctobj>objList =  baseCnctobjService.query(Cnd.where("personId","in",ids));
        String []ids2 = null;
        Map objMap = new HashMap();//存放已经在联系对象中存在的人员
        if(objList.size()>0){
            for(base_cnctobj obj:objList){
                objMap.put(obj.getPersonId(),obj.getUserId());
            }
            ids2 = new String[ids.length-objList.size()];
            int j=0;
            for(String id:ids){
                if(!objMap.containsKey(id)){
                    ids2[j]=id;
                    j++;
                }
            }
        }else{
            ids2 = ids;
        }
        //查询未建立联系对象的人员进行处理
        List<base_person> personList = this.query(Cnd.where("id","in",ids2));
        Map<String,base_person> personNumMap = new HashMap<>();//存放即将生成用户的人员
        for(base_person basePerson:personList){
            if("personNum".equals(loginType)){
                personNumMap.put(basePerson.getPersonnum(),basePerson);
            }else if("phone".equals(loginType)){
                personNumMap.put(basePerson.getTel(),basePerson);
            }
        }
        //查询是否已经存在相同名的用户
        List<String>sameNumUser= new ArrayList<String>();//存放已存在账号的人员
        List<Sys_user>euserList = sysUserService.query("loginname",Cnd.where("loginname","in",personNumMap.keySet()).and("delFlag","=","0"));
        if(euserList.size()>0){
            for(Sys_user user:euserList){
                base_person tp = personNumMap.get(user.getLoginname());
                personNumMap.remove(user.getLoginname());

                if(objMap.containsKey(tp.getId())){
                    objMap.remove(tp.getId());
                }
                sameNumUser.add(tp.getPersonnum());
            }
        }
        //生成用户
        for(Map.Entry<String,base_person> entry:personNumMap.entrySet()){
            createToUser(roleId,entry.getKey(),entry.getValue());
        }
        //返回不能生成的人员信息
        Map retMap = new HashMap();
        Set idset = objMap.keySet();
        if(idset.size()>0){
            List<base_person>cncObjList = this.query(Cnd.where("id","in",idset));
            List<String>retList = new ArrayList<>();
            for(base_person person :cncObjList){
                retList.add(person.getPersonnum());
            }
            retMap.put("cnctobj",retList);
        }
        if(sameNumUser.size()>0){
            retMap.put("user",sameNumUser);
        }
        return retMap;
    }

    private void createToUser(String roleId,String loginName, base_person person) {
        Sys_user user=new Sys_user();
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        String salt = rng.nextBytes().toBase64();
        String hashedPasswordBase64 = new Sha256Hash(loginName, salt, 1024).toBase64();
        user.setLoginname(loginName);
        user.setUsername(person.getPersonname());
        user.setUnitid(person.getUnitid());
        user.setSalt(salt);
        user.setPassword(hashedPasswordBase64);
        user.setLoginPjax(true);
        user.setLoginCount(0);
        user.setLoginAt(0);
        user.setDisabled(false);
        Sys_user u = sysUserService.insert(user);

        base_cnctobj cnctobj = new base_cnctobj();
        cnctobj.setUserId(u.getId());
        cnctobj.setPersonId(person.getId());
        baseCnctobjService.insert(cnctobj);

        this.insert("sys_user_unit", Chain.make("userId", u.getId()).add("unitId", person.getUnitid()));

        if(Strings.isNotBlank(roleId)) {
            this.insert("sys_user_role", Chain.make("roleId", roleId).add("userId", u.getId()));
        }

    }

}
