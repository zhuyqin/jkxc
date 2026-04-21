package org.jeecg.modules.customer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 第三方API响应DTO
 */
@Data
public class ApiResponseDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("result")
    private List<EnterpriseInfoDTO> result;
    
    @JsonProperty("cause")
    private String cause;
}

