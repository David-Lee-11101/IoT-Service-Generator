package com.applet.mpinterface.Request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class CodeRequest {
    private String javaCode;
    private String appletName;
    @JsonAlias("genAddress")
    private String projectName;
    private Integer portNum;
}
