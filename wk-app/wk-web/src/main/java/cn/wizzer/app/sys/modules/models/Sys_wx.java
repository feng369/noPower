package cn.wizzer.app.sys.modules.models;

import cn.wizzer.framework.base.model.BaseModel;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

/**
 * Created by cw on 2018/1/4.
 */
@Comment("企业微信Token表")
@Table("sys_wx")
public class Sys_wx extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column
    @Name
    @ColDefine(type = ColType.VARCHAR, width = 32)
    @Prev(els = {@EL("uuid()")})
    private String id;

    @Column
    @Comment("应用编号")
    @ColDefine(type = ColType.VARCHAR, width = 16)
    private String number;

    @Column
    @Comment("应用名称")
    @ColDefine(type = ColType.VARCHAR, width = 64)
    private String name;

    @Column
    @Comment("企业id")
    @ColDefine(type = ColType.VARCHAR, width = 64)
    private String corpid;

    //目前secret有两种(企业微信管理员可重新生成)：1. 通讯录管理secret； 2. 应用secret(每个应用不一样)。
    @Column
    @Comment("安全密钥")
    @ColDefine(type = ColType.VARCHAR, width = 128)
    private String secret;

    //对应通讯录的secret时，该值为：memo
    @Column
    @Comment("应用id")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String agentid;

    @Column
    @Comment("token凭证")
    @ColDefine(type = ColType.VARCHAR, width = 512)
    private String token;

    @Column
    @Comment("token有效时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String expire;

    @Column
    @Comment("开始时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String starttime;

    @Column
    @Comment("结束时间")
    @ColDefine(type = ColType.VARCHAR, width = 32)
    private String endtime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpire() {
        return expire;
    }

    public void setExpire(String expire) {
        this.expire = expire;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public String getCorpid() {
        return corpid;
    }

    public void setCorpid(String corpid) {
        this.corpid = corpid;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getAgentid() {
        return agentid;
    }

    public void setAgentid(String agentid) {
        this.agentid = agentid;
    }
}
