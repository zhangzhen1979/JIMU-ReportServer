package com.thinkdifferent.data2pdf.consumer;

import com.thinkdifferent.data2pdf.config.RabbitMQConfig;
import com.thinkdifferent.data2pdf.service.Data2PdfService;
import com.thinkdifferent.data2pdf.task.Task;
import net.sf.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreatePDFConsumer {

    @Autowired
    private Task task;
    @Autowired
    private Data2PdfService data2PdfService;


    /**
     * 队列消费者-创建PDF文件。启动多线程任务，处理队列中的消息
     *
     * @param strData 队列中放入的JSON字符串
     */
    @RabbitListener(queues  = RabbitMQConfig.QUEUE_RECEIVE)
    public void receiveTodoRequestByMap(String strData){
        try{
            JSONObject jsonData = JSONObject.fromObject(strData);
            task.doTask(data2PdfService, jsonData);
            //	      Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
