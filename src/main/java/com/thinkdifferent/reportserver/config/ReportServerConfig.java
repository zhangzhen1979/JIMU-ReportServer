package com.thinkdifferent.reportserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class ReportServerConfig {

    public static String outPutPath;
    @Value("${reportserver.outPutPath}")
    public void setOutPutPath(String strOutPutPath) {
        strOutPutPath = strOutPutPath.replaceAll("\\\\", "/");
        if(!strOutPutPath.endsWith("/")){
            strOutPutPath = strOutPutPath + "/";
        }
        ReportServerConfig.outPutPath = strOutPutPath;
    }
}
