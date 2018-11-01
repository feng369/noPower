package cn.wizzer.app.sys.modules.models;

import cn.wizzer.app.base.modules.models.base_dept;
import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.framework.base.dao.entity.annotation.Ref;
import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

/**
 * 20180323zhf1511
 * 注册审批
 */
@Table("sys_registaudit")
public class Sys_registaudit extends BaseModel{

    @Name
    @Comment("ID")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String id;

    @Comment("审批人")
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String auditorId;
    @One(field = "auditorId")
    private Sys_user auditor;

    @Comment("审批时间")
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String  auditTime;

    @Comment("审批意见")
    @Column
    @ColDefine(type=ColType.VARCHAR,width = 300)
    private String suggestion;


    @Comment("审批结果:-1;未审核  1.退回 0.通过")
    @Column
    @ColDefine(type = ColType.INT,width = 10)
    private int result = -1;

    @Column
    @ColDefine(type = ColType.INT,width = 10)
    @Comment("注册方式 0:手机 1:web")
    private int way;

    //
    @Column
    @Comment("web端审核通过，选择人员的id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String personId;

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    @Column
    @ColDefine(type = ColType.VARCHAR,width = 40)
    @Comment("占位字段a")
    private String a;

    @Column
    @ColDefine(type = ColType.VARCHAR,width = 40)
    @Comment("占位字段b")
    private String b;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 255)
    private String roleCode;

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public int getWay() {
        return way;
    }
    public void setWay(int way) {
        this.way = way;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuditorId() {
        return auditorId;
    }

    public void setAuditorId(String auditorId) {
        this.auditorId = auditorId;
    }

    public Sys_user getAuditor() {
        return auditor;
    }

    public void setAuditor(Sys_user auditor) {
        this.auditor = auditor;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(String auditTime) {
        this.auditTime = auditTime;
    }
}