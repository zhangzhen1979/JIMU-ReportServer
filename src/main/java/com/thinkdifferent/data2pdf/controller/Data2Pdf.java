package com.thinkdifferent.data2pdf.controller;

import com.thinkdifferent.data2pdf.service.Data2PdfService;
import com.thinkdifferent.data2pdf.service.RabbitMQService;
import com.thinkdifferent.data2pdf.util.CreatePdfUtil;
import com.thinkdifferent.data2pdf.util.WriteBackUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
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
     * @param jsonInput 传入的JSON参数
     *  生成多个PDF报表文件JSON示例
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
     *  生成单个PDF报表文件JSON示例
     *{
     * 	"reportFile":"dah/jb-4cm",
     * 	"fileName":"dahjb",
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
     * 		{
     * 			"year": "2022",
     * 			"fonds": "维森集团",
     * 		    "retention":"30年",
     * 		    "box_no":"0001",
     * 		    "barcode":"1234567891"
     * 		},
     * 		{
     * 		    "year": "2022",
     * 		    "fonds": "维森集团",
     * 		    "retention":"10年",
     * 		    "box_no":"0002",
     * 		    "barcode":"1234567892"
     * 		},
     * 		{
     * 		    "year": "2022",
     * 		    "fonds": "维森集团",
     * 		    "retention":"10年",
     * 		    "box_no":"0003",
     * 		    "barcode":"1234567893"
     * 		},
     * 		{
     * 		    "year": "2022",
     * 		    "fonds": "维森集团",
     * 		    "retention":"30年",
     * 		    "box_no":"0004",
     * 		    "barcode":"1234567894"
     * 		}
     *   ]
     * }
     *
     * @return
     */
    @RequestMapping(value = "/json2pdf", method = RequestMethod.POST)
    public JSONObject json2PDF(@RequestBody JSONObject jsonInput) {
        JSONObject jsonReturn = new JSONObject();

        try{
            CreatePdfUtil createPdfUtil = new CreatePdfUtil();
            jsonReturn = createPdfUtil.data2PDF(data2PdfService, jsonInput, null);

            boolean blnSuccess = WriteBackUtil.writeBack(jsonInput, jsonReturn);
            if(blnSuccess){
                jsonReturn.put("flag", "success" );
                jsonReturn.put("message", jsonReturn.getString("message") + " PDF file write back success. API call back success." );
            }else{
                jsonReturn.put("flag", "error" );
                jsonReturn.put("message", jsonReturn.getString("message") + " ,OR PDF file write back error. OR API call back error." );
            }

        }catch (Exception e) {
            e.printStackTrace();

            jsonReturn.put("flag", "exception" );
            jsonReturn.put("message", e.getMessage() );
        }

        // 返回处理完毕消息
        return jsonReturn;
    }

    /**
     * 将数据生成PDF报表文件，返回Response。此接口只能处理一个PDF文件！
     * @param jsonInput 传入的JSON参数
     *  生成单个PDF报表文件JSON示例
     *{
     * 	"reportFile":"dah/jb-4cm",
     * 	"fileName":"dahjb",
     * 	"data":[
     * 		{
     * 			"year": "2022",
     * 			"fonds": "维森集团",
     * 		    "retention":"30年",
     * 		    "box_no":"0001",
     * 		    "barcode":"1234567891"
     * 		},
     * 		{
     * 		    "year": "2022",
     * 		    "fonds": "维森集团",
     * 		    "retention":"10年",
     * 		    "box_no":"0002",
     * 		    "barcode":"1234567892"
     * 		},
     * 		{
     * 		    "year": "2022",
     * 		    "fonds": "维森集团",
     * 		    "retention":"10年",
     * 		    "box_no":"0003",
     * 		    "barcode":"1234567893"
     * 		},
     * 		{
     * 		    "year": "2022",
     * 		    "fonds": "维森集团",
     * 		    "retention":"30年",
     * 		    "box_no":"0004",
     * 		    "barcode":"1234567894"
     * 		}
     *   ]
     * }
     *
     * @return
     */
    @RequestMapping(value = "/getPdf", method = RequestMethod.POST)
    public void get2PDF(@RequestBody JSONObject jsonInput, HttpServletResponse response) {

        try {
            CreatePdfUtil createPdfUtil = new CreatePdfUtil();
            createPdfUtil.data2PDF(data2PdfService, jsonInput, response);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 将数据生成PDF报表文件，返回base64之后的文件内容，可供页面直接显示。此接口只能处理一个PDF文件！
     * @param jsonInput 传入的JSON参数
     *{
     * 	"reportFile":"dah/jb-4cm",
     * 	"fileName":"dahjb",
     * 	"data":[
     * 		{
     * 			"year": "2022",
     * 			"fonds": "维森集团",
     * 		    "retention":"30年",
     * 		    "box_no":"0001",
     * 		    "barcode":"1234567891"
     * 		},
     * 		{
     * 		    "year": "2022",
     * 		    "fonds": "维森集团",
     * 		    "retention":"10年",
     * 		    "box_no":"0002",
     * 		    "barcode":"1234567892"
     * 		},
     * 		{
     * 		    "year": "2022",
     * 		    "fonds": "维森集团",
     * 		    "retention":"10年",
     * 		    "box_no":"0003",
     * 		    "barcode":"1234567893"
     * 		},
     * 		{
     * 		    "year": "2022",
     * 		    "fonds": "维森集团",
     * 		    "retention":"30年",
     * 		    "box_no":"0004",
     * 		    "barcode":"1234567894"
     * 		}
     *   ]
     * }
     *
     * @return
     */
    @RequestMapping(value = "/json2pdf2base64", method = RequestMethod.POST)
    public String json2PDF2Base64(@RequestBody JSONObject jsonInput) {

        try{
            CreatePdfUtil createPdfUtil = new CreatePdfUtil();
            JSONObject jsonReturn = createPdfUtil.data2PDF(data2PdfService, jsonInput, null);

            if("success".equalsIgnoreCase(jsonReturn.getString("flag"))){
                String strPdfFilePathName = jsonReturn.getString("file");
                if(strPdfFilePathName != null){
                    String[] strFiles = strPdfFilePathName.split(";");
                    strPdfFilePathName = strFiles[0];
                }

                File filePDF = new File(strPdfFilePathName);
                if(filePDF.exists()){
                    try {
                        byte[] b = Files.readAllBytes(Paths.get(strPdfFilePathName));
                        // 转换为byte后，PDF文件即可删除
                        filePDF.delete();
                        return Base64.getEncoder().encodeToString(b);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        // 返回处理完毕消息
        return null;
    }

    /**
     * 将数据生成PDF报表文件，返回Base64之后的文件内容，可供页面直接显示。
     * 此接口支持返回多个PDF文件的Base64值。
     * @param jsonInput 传入的JSON参数。内容与“data2pdf2base64”接口相同，data中可以有多个JSON对象。
     * @return
     */
    @RequestMapping(value = "/json2pdfs2base64", method = RequestMethod.POST)
    public JSONObject json2PDFs2Base64(@RequestBody JSONObject jsonInput) {
        JSONObject jsonReturn = new JSONObject();

        try{
            CreatePdfUtil createPdfUtil = new CreatePdfUtil();
            jsonReturn = createPdfUtil.data2PDF(data2PdfService, jsonInput, null);

            if("success".equalsIgnoreCase(jsonReturn.getString("flag"))){
                JSONArray jsonArrayPDF = new JSONArray();

                String strPdfFilePathName = jsonReturn.getString("file");

                if(strPdfFilePathName != null){
                    String[] strFiles = strPdfFilePathName.split(";");
                    for(int i=0; i<strFiles.length; i++){
                        File filePDF = new File(strFiles[i]);
                        if(filePDF.exists()){
                            try {
                                byte[] b = Files.readAllBytes(Paths.get(strFiles[i]));
                                JSONObject jsonObjectPDF = new JSONObject();
                                jsonObjectPDF.put("filename", filePDF.getName());
                                jsonObjectPDF.put("base64", Base64.getEncoder().encodeToString(b));
                                jsonArrayPDF.add(jsonObjectPDF);
                                // 转换为byte后，PDF文件即可删除
                                filePDF.delete();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    jsonReturn.put("base64", jsonArrayPDF);
                }


            }
        }catch (Exception e) {
            e.printStackTrace();

            jsonReturn.put("flag", "exception" );
            jsonReturn.put("message", e.getMessage() );
        }

        // 返回处理完毕消息
        return jsonReturn;
    }

    /**
     * 接收传入的JSON数据，加入到RabbitMQ队列中，队列异步处理，在指定目录中生成PDF文件
     * @param jsonInput 传入的JSON参数。与接口“data2pdf”传入的内容相同
     * @return
     */
    @ApiOperation("接收传入的JSON数据，加入到RabbitMQ队列中，队列异步处理，在指定目录中生成PDF文件")
    @RequestMapping(value = "/json2mq", method = RequestMethod.POST)
    public Map<String, String> data2MQ(@RequestBody JSONObject jsonInput) {
        Map<String, String> mapReturn = new HashMap<>();
        mapReturn.put("flag", "success" );
        mapReturn.put("message", "Set JSON Data to MQ Success" );

        rabbitMQService.setData2MQ(jsonInput);

        return mapReturn;
    }


}
