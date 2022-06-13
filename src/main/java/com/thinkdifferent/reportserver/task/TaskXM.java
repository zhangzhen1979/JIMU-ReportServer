package com.thinkdifferent.reportserver.task;

import com.thinkdifferent.reportserver.service.Data2XMReportService;
import com.thinkdifferent.reportserver.util.WriteBackUtil;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class TaskXM implements RabbitTemplate.ConfirmCallback {
    // 日志对象，用于输出执行过程中的日志信息
    private static final Logger log = LoggerFactory.getLogger(TaskXM.class);

    /**
     * 处理接收列表中的数据，异步多线程任务
     * @param data2XMReportService 创建报表文件的Service对象
     * @param jsonInput 队列中待处理的JSON数据
     * @throws Exception
     */
    @Async("taskExecutor")
    public void doTask(Data2XMReportService data2XMReportService, JSONObject jsonInput) {

        log.info("开始处理-将数据转换为报表文件");
        long longStart = System.currentTimeMillis();

        log.info("MQ中存储的数据（包含数据）:" + jsonInput.toString());

        JSONObject jsonReturn = new JSONObject();
        jsonReturn.put("flag", "error" );
        jsonReturn.put("message", "Create Report file Error" );

        try{
            File file = data2XMReportService.getReportFileByID(jsonInput, null);

            JSONObject jsonFile = new JSONObject();
            if(file.exists()){
                jsonFile.put("flag", "success");
                jsonFile.put("file", file.getCanonicalPath());
            }

            boolean blnSuccess = WriteBackUtil.writeBack(jsonInput, jsonFile);
            if(blnSuccess){
                log.info("报表文件生成完成");
                log.info(jsonReturn.toString());
            }else{
                log.info("报表文件生成错误");
                log.info(jsonReturn.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.info("创建报表文件异常");
            log.error(e.getMessage());
        }
        long longEnd = System.currentTimeMillis();
        log.info("完成-将数据转换为报表文件，耗时：" + (longEnd - longStart) + "毫秒");
    }


    /**
     * 回调反馈消费者消费信息
     * @param correlationData
     * @param b
     * @param msg
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String msg)
    {
        log.info(" 回调id:" + correlationData);
        if (b) {
            log.info("消息成功消费");
        } else {
            log.info("消息消费失败:" + msg);
        }
    }


}
