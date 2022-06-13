package com.thinkdifferent.reportserver.controller;

import com.thinkdifferent.reportserver.entity.ReportParamEntity;
import com.thinkdifferent.reportserver.service.Data2ReportService;
import com.thinkdifferent.reportserver.service.RabbitMQService;
import com.thinkdifferent.reportserver.util.CreateReportUtil;
import com.thinkdifferent.reportserver.util.WriteBackUtil;
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
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Api(tags="根据传入的JSON生成PDF文件")
@RestController
@RequestMapping(value = "/jasper")
public class CreateReportController {

    @Autowired
    private Data2ReportService data2ReportService;

    @Autowired
    private RabbitMQService rabbitMQService;

    @Autowired
    private DataSource dataSource;

    /**
     * 【stream】将JSON数据/数据库查询，生成报表文件，返回Response。此接口只能处理一个PDF文件！
     * @param jsonInput 传入的JSON参数
     * @param response HTTP响应对象
     *================================================
     *  1、传入JSON数据示例
     *{
     * 	"reportFile":"dah/jb-4cm",
     * 	"fileName":"dahjb",
     * 	"docType": "PDF",
     * 	"dataSource": "json",
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
     *================================================
     * 2、传入数据库查询示例
     *{
     *	"reportFile":"dbReport/arclist",
     *	"fileName":"arclist",
     * 	"docType": "PDF",
     * 	"dataSource": "db",
     *	"data":[
     *      {
     *		    "table": "arclist",
     *		    "where": "1=1",
     *		    "orderBy": "id",
     *       }
     *	]
     * }
     *
     * @return
     */
    @RequestMapping(value = "/getStream", method = RequestMethod.POST)
    public void getStream(@RequestBody JSONObject jsonInput, HttpServletResponse response) {

        try {
            CreateReportUtil createReportUtil = new CreateReportUtil();

            ReportParamEntity createReportParamEntity = new ReportParamEntity();

            createReportParamEntity.setDataSource(createReportUtil.getDataSource(jsonInput));
            createReportParamEntity.setOutputType("stream");
            createReportParamEntity.setData2ReportService(data2ReportService);
            createReportParamEntity.setJoInput(jsonInput);
            createReportParamEntity.setResponse(response);

            Connection conn = null;
            if("db".equalsIgnoreCase(createReportUtil.getDataSource(jsonInput))){
                conn = dataSource.getConnection();
                createReportParamEntity.setConn(conn);
            }

            createReportUtil.createReportFromData(createReportParamEntity);

            if(conn != null){
                conn.close();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 将数据生成报表文件，按照要求回写到指定位置。可以生成单个文件，也可以生成多个文件
     * @param jsonInput 传入的JSON参数
     *===================================================================================
     * 1、【multiWriteBack】生成多个报表文件并回写，JSON示例（DB模式只是data中的参数不同）
     *{
     * 	"reportFile":"jzpz/jzpz",
     * 	"fileNameKey":"voucher_code",
     * 	"docType": "PDF",
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
     * 	"dataSource": "json",
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
     *=====================================================================================
     * 2、【singleWriteBack】生成单个报表文件并回写，JSON示例（DB模式只是data中的参数不同）
     *{
     * 	"reportFile":"dah/jb-4cm",
     * 	"fileName":"dahjb",
     * 	"docType": "PDF",
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
     * 	"dataSource": "json",
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
    @RequestMapping(value = "/getFile", method = RequestMethod.POST)
    public JSONObject getFile(@RequestBody JSONObject jsonInput) {
        JSONObject jsonReturn = new JSONObject();

        try{
            CreateReportUtil createReportUtil = new CreateReportUtil();
            ReportParamEntity createReportParamEntity = new ReportParamEntity();

            createReportParamEntity.setDataSource(createReportUtil.getDataSource(jsonInput));

            String strOutputType;
            if(jsonInput.has("fileName") && jsonInput.getString("fileName")!=null){
                strOutputType = "singleWriteBack";
            }else {
                strOutputType = "multiWriteBack";
            }
            createReportParamEntity.setOutputType(strOutputType);

            createReportParamEntity.setData2ReportService(data2ReportService);
            createReportParamEntity.setJoInput(jsonInput);

            Connection conn = null;
            if("db".equalsIgnoreCase(createReportUtil.getDataSource(jsonInput))){
                conn = dataSource.getConnection();
                createReportParamEntity.setConn(conn);
            }

            jsonReturn = createReportUtil.createReportFromData(createReportParamEntity);

            if(conn != null){
                conn.close();
            }

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
     * 【singleBase64】将数据生成PDF报表文件，返回base64之后的文件内容，可供页面直接显示。此接口只能处理一个PDF文件！
     * @param jsonInput 传入的JSON参数
     *{
     * 	"reportFile":"dah/jb-4cm",
     * 	"fileName":"dahjb",
     * 	"docType": "PDF",
     * 	"dataSource": "json",
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
    @RequestMapping(value = "/getBase64", method = RequestMethod.POST)
    public String getBase64(@RequestBody JSONObject jsonInput) {

        try{
            CreateReportUtil createReportUtil = new CreateReportUtil();

            ReportParamEntity createReportParamEntity = new ReportParamEntity();
            createReportParamEntity.setDataSource(createReportUtil.getDataSource(jsonInput));
            createReportParamEntity.setOutputType("singleBase64");
            createReportParamEntity.setData2ReportService(data2ReportService);
            createReportParamEntity.setJoInput(jsonInput);

            Connection conn = null;
            if("db".equalsIgnoreCase(createReportUtil.getDataSource(jsonInput))){
                conn = dataSource.getConnection();
                createReportParamEntity.setConn(conn);
            }

            JSONObject jsonReturn = createReportUtil.createReportFromData(createReportParamEntity);

            if(conn != null){
                conn.close();
            }

            if("success".equalsIgnoreCase(jsonReturn.getString("flag"))){
                JSONArray jaBase64 = jsonReturn.getJSONArray("base64");
                if(jaBase64 != null){
                    return jaBase64.getJSONObject(0).getString("value");
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        // 返回处理完毕消息
        return "error";
    }

    /**
     * 【multiBase64】将数据生成PDF报表文件，返回Base64之后的文件内容，可供页面直接显示。
     * 此接口支持返回多个PDF文件的Base64值。
     * @param jsonInput 传入的JSON参数。内容与“getFile”接口相同，data中可以有多个JSON对象。
     * @return
     */
    @RequestMapping(value = "/getBase64s", method = RequestMethod.POST)
    public JSONObject getBase64s(@RequestBody JSONObject jsonInput) {
        JSONObject jsonReturn = new JSONObject();

        try{
            CreateReportUtil createReportUtil = new CreateReportUtil();

            ReportParamEntity createReportParamEntity = new ReportParamEntity();
            createReportParamEntity.setDataSource(createReportUtil.getDataSource(jsonInput));
            createReportParamEntity.setOutputType("multiBase64");
            createReportParamEntity.setData2ReportService(data2ReportService);
            createReportParamEntity.setJoInput(jsonInput);

            Connection conn = null;
            if("db".equalsIgnoreCase(createReportUtil.getDataSource(jsonInput))){
                conn = dataSource.getConnection();
                createReportParamEntity.setConn(conn);
            }

            jsonReturn = createReportUtil.createReportFromData(createReportParamEntity);

            if(conn != null){
                conn.close();
            }

            if("success".equalsIgnoreCase(jsonReturn.getString("flag"))){
                JSONArray jsonArrayPDF = new JSONArray();

                String strPdfFilePathName = jsonReturn.getString("file");

                if(strPdfFilePathName != null){
                    String[] strFiles = strPdfFilePathName.split(";");
                    for(int i=0; i<strFiles.length; i++){
                        JSONObject jsonObjectPDF = new JSONObject();
                        jsonObjectPDF.put("filename", strFiles[i]);

                        JSONArray jaBase64 = jsonReturn.getJSONArray("base64");
                        if(jaBase64 != null){
                            jsonObjectPDF.put("base64",
                                    jaBase64.getJSONObject(0).getString("value"));
                        }
                        jsonArrayPDF.add(jsonObjectPDF);
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
     * @param jsonInput 传入的JSON参数。
     *                  与接口“reportserver”传入的内容基本相同。
     *                  如果报表是读取数据库记录，则需要加入“dataSource”参数，值为“db”。
     *                  不加此参数，则认为是通过JSON数据生成报表。
     *                  例如：
     *{
     * 	"reportFile":"dbReport/arclist",
     * 	"fileName":"arclist",
     * 	"docType":"pdf",
     * 	"dataSource":"db",
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
     * 		    "table": "arclist",
     * 		    "where": "1=1",
     * 		    "orderBy": "id"
     * 		}
     *   ]
     * }

     * @return
     */
    @ApiOperation("接收传入的JSON数据，加入到RabbitMQ队列中，队列异步处理，在指定目录中生成PDF文件")
    @RequestMapping(value = "/put2Mq", method = RequestMethod.POST)
    public Map<String, String> put2Mq(@RequestBody JSONObject jsonInput) {
        Map<String, String> mapReturn = new HashMap<>();
        mapReturn.put("flag", "success" );
        mapReturn.put("message", "Set JSON Data to MQ Success" );

        rabbitMQService.setData2MQ(jsonInput);

        return mapReturn;
    }



}
