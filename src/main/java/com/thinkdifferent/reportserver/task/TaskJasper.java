package com.thinkdifferent.reportserver.task;

import com.thinkdifferent.reportserver.entity.ReportParamEntity;
import com.thinkdifferent.reportserver.service.Data2ReportService;
import com.thinkdifferent.reportserver.util.CreateReportUtil;
import com.thinkdifferent.reportserver.util.WriteBackUtil;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class TaskJasper implements RabbitTemplate.ConfirmCallback {
    // 日志对象，用于输出执行过程中的日志信息
    private static final Logger log = LoggerFactory.getLogger(TaskJasper.class);

    @Autowired
    private DataSource dataSource;

    /**
     * 处理接收列表中的数据，异步多线程任务
     * @param data2ReportService 创建PDF文件的Service对象
     * @param jsonInput 队列中待处理的JSON数据
     * @throws Exception
     */
    @Async("taskExecutor")
    public void doTask(Data2ReportService data2ReportService, JSONObject jsonInput) {

        log.info("开始处理-将数据转换为报表文件");
        long longStart = System.currentTimeMillis();

        log.info("MQ中存储的数据（包含数据）:" + jsonInput.toString());

        JSONObject jsonReturn = new JSONObject();
        jsonReturn.put("flag", "error" );
        jsonReturn.put("message", "Create Report file Error" );

        try{
            // 读取数据类型
            String strDataSource = "json";
            if(jsonInput.has("dataSource")){
                strDataSource = jsonInput.getString("dataSource");
            }
            String strDocType = "pdf";
            if(jsonInput.has("docType")){
                strDocType = jsonInput.getString("docType");
            }

            // 判断文件生成方式
            String strOutputType;
            if(jsonInput.has("fileName") && jsonInput.getString("fileName")!=null){
                // 单文件回写
                strOutputType = "singleWriteBack";
            }else {
                // 多文件回写
                strOutputType = "multiWriteBack";
            }

            CreateReportUtil createReportUtil = new CreateReportUtil();

            ReportParamEntity createReportParamEntity = new ReportParamEntity();
            createReportParamEntity.setDataSource(strDataSource);
            createReportParamEntity.setDocType(strDocType);
            createReportParamEntity.setOutputType(strOutputType);
            createReportParamEntity.setData2ReportService(data2ReportService);
            createReportParamEntity.setJoInput(jsonInput);

            Connection conn = null;
            if("db".equalsIgnoreCase(strDataSource)){
                conn = dataSource.getConnection();
                createReportParamEntity.setConn(conn);
            }

            jsonReturn = createReportUtil.createReportFromData(createReportParamEntity);

            if("db".equalsIgnoreCase(strDataSource)) {
                if(conn != null){
                    conn.close();
                }
            }

            boolean blnSuccess = WriteBackUtil.writeBack(jsonInput, jsonReturn);
            if(blnSuccess){
                log.info("创建报表文件完成");
                log.info(jsonReturn.toString());
            }else{
                log.info("创建报表文件错误");
                log.info(jsonReturn.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.info("创建报表异常");
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
