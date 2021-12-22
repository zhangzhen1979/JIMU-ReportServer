package com.thinkdifferent.data2pdf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class Data2PDFConfig {

    public static String outPutPath;
    @Value("${convert.data2pdf.outPutPath}")
    public void setOutPutPath(String strOutPutPath) {
        Data2PDFConfig.outPutPath = strOutPutPath;
    }

}
