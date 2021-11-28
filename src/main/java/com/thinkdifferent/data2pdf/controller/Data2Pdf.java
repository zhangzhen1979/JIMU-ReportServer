package com.thinkdifferent.data2pdf.controller;

import com.thinkdifferent.data2pdf.service.Data2PdfService;
import com.thinkdifferent.data2pdf.service.RabbitMQService;
import com.thinkdifferent.data2pdf.util.CreatePdfUtil;
import com.thinkdifferent.data2pdf.util.WriteBackUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api(tags="根据传入的JSON生成PDF文件")
@RestController
@RequestMapping(value = "/api")
public class Data2Pdf {

    @Autowired
    private Data2PdfService data2PdfService;

    @Autowired
    private RabbitMQService rabbitMQService;

    /**
     * 将数据生成PDF报表文件
     * @param jsonInput
     *{
     * 	"reportFile":"jzpz/jzpz",
     * 	"fileNameKey":"voucher_code",
     * 	"writeBackType": "path",
     *  "writeBack":
     *  {
     *   	"path":"D:/cvtest/"
     *  },
     *  "writeBackHeaders":
     *  {
     *      "Authorization":"Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0"
     *  },
     *  "callBackURL": "http://10.11.12.13/callback",
     * 	"data":[
     *       {
     * 			"id": "1",
     * 		    "voucher_code": "20210507SJFBX1234567",
     * 		    "voucher_company_name": "3000XXXX有限公司",
     * 		    "create_date": "2021年08月31日",
     * 		    "voucher_number": "6012345234",
     * 		    "ac_doc_typ_name": "EMS凭证",
     * 		    "total_chn": "壹佰元整",
     * 		    "debit_sum": "100.00",
     * 		    "credit_sum": "100.00",
     * 		    "post_name": "PI_USER",
     * 		    "pages":1,
     * 		    "detail":[
     *                {
     * 				    "abstract": "XXXXX店报销手机费",
     * 				    "subject_name": "6601000000手机费",
     * 				    "debit_amount_lc": "100.00",
     * 				    "credit_amount_lc": ""
     *                }
     * 		    ]
     *        }
     * 	]
     * }
     *
     *
     * @return
     */
    @RequestMapping(value = "/data2pdf", method = RequestMethod.POST)
    public JSONObject data2PDF(@RequestBody JSONObject jsonInput) {

        CreatePdfUtil createPdfUtil = new CreatePdfUtil();

        JSONObject jsonReturn = createPdfUtil.data2PDF(data2PdfService, jsonInput);

        boolean blnSuccess = WriteBackUtil.writeBack(jsonInput, jsonReturn);
        if(blnSuccess){
            jsonReturn.put("flag", "success" );
            jsonReturn.put("message", "PDF reoprt create success. PDF file write back success. API call back success." );
        }else{
            jsonReturn.put("flag", "error" );
            jsonReturn.put("message", "PDF reoprt create error ,OR PDF file write back error. OR API call back error." );
        }

        // 返回处理完毕消息
        return jsonReturn;
    }


    @ApiOperation("接收传入的JSON数据，加入到RabbitMQ队列中，队列异步处理，在指定目录中生成PDF文件")
    @RequestMapping(value = "/data2mq", method = RequestMethod.POST)
    public Map<String, String> data2MQ(@RequestBody JSONObject jsonInput) {
        Map<String, String> mapReturn = new HashMap<>();
        mapReturn.put("flag", "success" );
        mapReturn.put("message", "Set Data to MQ Success" );

        rabbitMQService.setData2MQ(jsonInput);

        return mapReturn;
    }


}
