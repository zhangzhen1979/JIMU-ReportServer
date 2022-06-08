package com.thinkdifferent.reportserver.task;

import com.thinkdifferent.reportserver.entity.ReportParamEntity;
import com.thinkdifferent.reportserver.service.Data2PdfService;
import com.thinkdifferent.reportserver.util.CreatePdfUtil;
import com.thinkdifferent.reportserver.util.WriteBackUtil;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class Task implements RabbitTemplate.ConfirmCallback {
    // 日志对象，用于输出执行过程中的日志信息
    private static final Logger log = LoggerFactory.getLogger(Task.class);


    /**
     * 处理接收列表中的数据，异步多线程任务
     * @param data2PdfService 创建PDF文件的Service对象
     * @param jsonInput 队列中待处理的JSON数据
     * @throws Exception
     */
    @Async("taskExecutor")
    public void doTask(Data2PdfService data2PdfService, JSONObject jsonInput) {

        log.info("开始处理-将JSON数据转换为PDF文件");
        long longStart = System.currentTimeMillis();

        log.info("MQ中存储的数据（包含数据）:" + jsonInput.toString());

        JSONObject jsonReturn = new JSONObject();
        jsonReturn.put("flag", "error" );
        jsonReturn.put("message", "Create Pdf file Error" );

        try{
            String strOutputType;
            if(jsonInput.has("fileName") && jsonInput.getString("fileName")!=null){
                strOutputType = "singleWriteBack";
            }else {
                strOutputType = "multiWriteBack";
            }

            CreatePdfUtil createPdfUtil = new CreatePdfUtil();

            ReportParamEntity createReportParamEntity = new ReportParamEntity();
            createReportParamEntity.setDataSource("json");
            createReportParamEntity.setOutputType(strOutputType);
            createReportParamEntity.setData2PdfService(data2PdfService);
            createReportParamEntity.setJoData(jsonInput);

            jsonReturn = createPdfUtil.createReportFromData(createReportParamEntity);

            boolean blnSuccess = WriteBackUtil.writeBack(jsonInput, jsonReturn);
            if(blnSuccess){
                log.info("创建PDF完成");
                log.info(jsonReturn.toString());
            }else{
                log.info("创建PDF错误");
                log.info(jsonReturn.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.info("创建PDF异常");
            log.error(e.getMessage());
        }
        long longEnd = System.currentTimeMillis();
        log.info("完成-将JSON数据转换为PDF文件，耗时：" + (longEnd - longStart) + "毫秒");
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
