package org.jeecg.modules.customer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 企业信息DTO（第三方接口返回）
 */
@Data
public class EnterpriseInfoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("qyid")
    private String qyid;
    
    @JsonProperty("qymc")
    private String qymc;
    
    @JsonProperty("gsid")
    private String gsid;
    
    @JsonProperty("nsrxydj")
    private String nsrxydj;
    
    @JsonProperty("nsrzgdm")
    private String nsrzgdm;
    
    @JsonProperty("nsrzgmc")
    private String nsrzgmc;
    
    @JsonProperty("sffdq")
    private String sffdq;
    
    @JsonProperty("fdqksnf")
    private Integer fdqksnf;
    
    @JsonProperty("fdqksyf")
    private Integer fdqksyf;
    
    @JsonProperty("fdqjsnf")
    private Integer fdqjsnf;
    
    @JsonProperty("fdqjsyf")
    private Integer fdqjsyf;
    
    @JsonProperty("djzclxDm")
    private String djzclxDm;
    
    @JsonProperty("jsfw")
    private String jsfw;
    
    @JsonProperty("zcrq")
    private String zcrq;
    
    @JsonProperty("dqbm")
    private String dqbm;
    
    @JsonProperty("dz")
    private String dz;
    
    @JsonProperty("lrrybm")
    private String lrrybm;
    
    @JsonProperty("fddbrmc")
    private String fddbrmc;
    
    @JsonProperty("frlxfs")
    private String frlxfs;
    
    @JsonProperty("frsfzmc")
    private String frsfzmc;
    
    @JsonProperty("frzjhm")
    private String frzjhm;
    
    @JsonProperty("cwlxr")
    private String cwlxr;
    
    @JsonProperty("cwlxfs")
    private String cwlxfs;
    
    @JsonProperty("cwsfzjmc")
    private String cwsfzjmc;
    
    @JsonProperty("cwzjhm")
    private String cwzjhm;
    
    @JsonProperty("swbm")
    private String swbm;
    
    @JsonProperty("zh")
    private String zh;
    
    @JsonProperty("zfjglx")
    private String zfjglx;
    
    @JsonProperty("qylx")
    private Integer qylx;
    
    @JsonProperty("khbm")
    private String khbm;
    
    @JsonProperty("qylxr")
    private String qylxr;
    
    @JsonProperty("lxrlxfs")
    private String lxrlxfs;
    
    @JsonProperty("tyKjnd")
    private Integer tyKjnd;
    
    @JsonProperty("tyKjqj")
    private Integer tyKjqj;
    
    @JsonProperty("tyZt")
    private String tyZt;
    
    @JsonProperty("hylxid")
    private Integer hylxid;
    
    @JsonProperty("zczb")
    private BigDecimal zczb;
    
    @JsonProperty("sszb")
    private BigDecimal sszb;
    
    @JsonProperty("shtyxydm")
    private String shtyxydm;
    
    @JsonProperty("updateTime")
    private String updateTime;
    
    @JsonProperty("zgrs")
    private String zgrs;
    
    @JsonProperty("qylxmc")
    private String qylxmc;
    
    @JsonProperty("gsnsrsbh")
    private String gsnsrsbh;
    
    @JsonProperty("operator")
    private String operator;
    
    @JsonProperty("kQykjnd")
    private Integer kQykjnd;
    
    @JsonProperty("kQykjqj")
    private Integer kQykjqj;
    
    @JsonProperty("gsnssblj")
    private String gsnssblj;
}

