package com.thinkdifferent.reportserver.service;

import net.sf.json.JSONObject;

public interface RabbitMQService {
    /**
     * 将传入的JSON对象，转换为PDF文件，输出到指定的目录中。
     * @param jsonInput 输入的JSON数据对象
     */
    void  setData2MQ(JSONObject jsonInput);

    /**
     * 将传入的JSON对象，转换为PDF文件，输出到指定的目录中。
     * @param jsonInput 输入的JSON数据对象
     */
    void  setXMData2MQ(JSONObject jsonInput);

}
