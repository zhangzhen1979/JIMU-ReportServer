package com.thinkdifferent.reportserver.consumer;

import com.thinkdifferent.reportserver.config.RabbitMQConfig;
import com.thinkdifferent.reportserver.service.Data2ReportService;
import com.thinkdifferent.reportserver.service.Data2XMReportService;
import com.thinkdifferent.reportserver.task.TaskJasper;
import com.thinkdifferent.reportserver.task.TaskXM;
import net.sf.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class XMReportConsumer {

    @Autowired
    private TaskXM taskXM;
    @Autowired
    private Data2XMReportService data2XMReportService;


    /**
     * 队列消费者-创建报表文件。启动多线程任务，处理队列中的消息
     *
     * @param strData 队列中放入的JSON字符串
     */
    @RabbitListener(queues  = RabbitMQConfig.QUEUE_XM)
    public void receiveTodoRequestByMap(String strData){
        try{
            JSONObject jsonData = JSONObject.fromObject(strData);
            taskXM.doTask(data2XMReportService, jsonData);
            //	      Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
