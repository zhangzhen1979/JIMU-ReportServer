package com.thinkdifferent.reportserver.util;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpConfig;
import cn.hutool.extra.ftp.FtpMode;
import cn.hutool.http.HttpUtil;
import net.sf.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class WriteBackUtil {
    /**
     * 通用回写、回调处理
     * @param jsonInput 接口传入的JSON参数
     * @param jsonReturn 生成PDF报表后，工具类返回的结果
     * @return 回写成功或失败标志
     */
    public static boolean writeBack(JSONObject jsonInput, JSONObject jsonReturn){
        /**
         * {
         *      "flag": "success",
         *      "message": "Create Pdf Report file Success",
         *      "file": "d:/pdf/001.pdf"
         * }
         */

        try{
            // 如果报表成功生成，则进行后续的回写、回调操作
            if("success".equalsIgnoreCase(jsonReturn.getString("flag"))){
                // 获取转换后的PDF文件的路径和文件名
                String strPdfFilePathName = jsonReturn.getString("file");
                String[] strPdfs = strPdfFilePathName.split(";");

                for(int i=0; i<strPdfs.length; i++){
                    File filePDF = new File(strPdfs[i]);
                    String strPdfFileName = filePDF.getName();
                    strPdfFileName = strPdfFileName.substring(0, strPdfFileName.lastIndexOf("."));

                    // 文件回写方式（回写路径[path]/回写接口[api]/ftp回写[ftp]）
                    String strWriteBackType = "path";
                    JSONObject jsonWriteBack = new JSONObject();
                    if(jsonInput.get("writeBackType")!=null){
                        strWriteBackType = String.valueOf(jsonInput.get("writeBackType"));
                        // 回写接口或回写路径
                        jsonWriteBack = JSONObject.fromObject(jsonInput.get("writeBack"));
                    }

                    if(!"path".equalsIgnoreCase(strWriteBackType)){
                        boolean blnFlag = false;

                        // 回写文件
                        Map mapWriteBackHeaders = new HashMap<>();
                        if(jsonInput.get("writeBackHeaders") != null){
                            mapWriteBackHeaders = (Map)jsonInput.get("writeBackHeaders");
                        }

                        if("url".equalsIgnoreCase(strWriteBackType)){
                            // 调用REST API上传文件回写
                            String strWriteBackURL = jsonWriteBack.getString("url");
                            jsonReturn = writeBack2Api(strPdfs[i], strWriteBackURL, mapWriteBackHeaders);
                        }else if("ftp".equalsIgnoreCase(strWriteBackType)){
                            // ftp回写
                            boolean blnPassive = jsonWriteBack.getBoolean("passive");
                            String strFtpHost = jsonWriteBack.getString("host");
                            int intFtpPort = jsonWriteBack.getInt("port");
                            String strFtpUserName = jsonWriteBack.getString("username");
                            String strFtpPassWord = jsonWriteBack.getString("password");
                            String strFtpFilePath = jsonWriteBack.getString("filepath");

                            boolean blnFptSuccess = false;
                            FileInputStream in=new FileInputStream(filePDF);

                            Ftp ftp = null;
                            try {
                                if(blnPassive){
                                    // 服务器需要代理访问，才能对外访问
                                    FtpConfig ftpConfig = new FtpConfig(strFtpHost, intFtpPort,
                                            strFtpUserName, strFtpPassWord,
                                            CharsetUtil.CHARSET_UTF_8);
                                    ftp = new Ftp(ftpConfig, FtpMode.Passive);
                                }else{
                                    // 服务器不需要代理访问
                                    ftp = new Ftp(strFtpHost, intFtpPort,
                                            strFtpUserName, strFtpPassWord);
                                }

                                blnFptSuccess =  ftp.upload(strFtpFilePath, filePDF.getName(), in);

                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (ftp != null) {
                                        ftp.close();
                                    }

                                    if(in != null){
                                        in.close();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }


                            if(blnFptSuccess){
                                blnFlag = true;
                            }

                        }

                        // 不是直接写入路径（path），则存储在本地文件夹中的pdf文件都是临时文件，需要在传输后删除
                        if(blnFlag){
                            if(filePDF.exists()){
                                filePDF.delete();
                            }
                        }

                        // 回调对方系统提供的CallBack方法。
                        if(jsonInput.get("callBackURL")!=null){
                            String strCallBackURL = String.valueOf(jsonInput.get("callBackURL"));

                            Map mapCallBackHeaders = new HashMap<>();
                            if (jsonInput.get("callBackHeaders") != null) {
                                mapCallBackHeaders = (Map) jsonInput.get("callBackHeaders");
                            }

                            Map mapParams = new HashMap<>();
                            mapParams.put("file", strPdfFileName);
                            if(blnFlag){
                                mapParams.put("flag", "success");
                            }else{
                                mapParams.put("flag", "error");
                            }

                            callBack(strCallBackURL, mapCallBackHeaders, mapParams);
                        }

                    }

                }

            }

            return true;

        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }


    /**
     * 调用API接口，将文件上传
     * @param strFilePathName 文件路径和文件名
     * @param strUrl API接口的URL
     * @param mapHeader Header参数
     * @return 接口返回的JSON
     * @throws Exception
     */
    public static JSONObject writeBack2Api(String strFilePathName, String strUrl, Map<String, Object> mapHeader)
            throws Exception {

        JSONObject jsonReturn = new JSONObject();
        jsonReturn.put("flag", "error");
        jsonReturn.put("message", "Call Back Error. URL = " + strUrl);

        // 换行符
        final String strNewLine = "\r\n";
        final String strBoundaryPrefix = "--";
        // 定义数据分隔线
        String strBOUNDARY = "========7d4a6d158c9";
        // 服务器的域名
        URL url = new URL(strUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        // 设置为POST情
        httpURLConnection.setRequestMethod("POST");
        // 发送POST请求必须设置如下两行
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setUseCaches(false);
        // 设置请求头参数
        httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
        httpURLConnection.setRequestProperty("Charset", "UTF-8");
        httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + strBOUNDARY);
        try (
                OutputStream outputStream = httpURLConnection.getOutputStream();
                DataOutputStream out = new DataOutputStream(outputStream);
        ) {
            //传递参数
            if (mapHeader != null) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Map.Entry<String, Object> entry : mapHeader.entrySet()) {
                    stringBuilder.append(strBoundaryPrefix)
                            .append(strBOUNDARY)
                            .append(strNewLine)
                            .append("Content-Disposition: form-data; name=\"")
                            .append(entry.getKey())
                            .append("\"").append(strNewLine).append(strNewLine)
                            .append(entry.getValue())
                            .append(strNewLine);
                }
                out.write(stringBuilder.toString().getBytes(Charset.forName("UTF-8")));
            }

            // 上传文件
            {
                File file = new File(strFilePathName);
                StringBuilder sb = new StringBuilder();
                sb.append(strBoundaryPrefix);
                sb.append(strBOUNDARY);
                sb.append(strNewLine);
                sb.append("Content-Disposition: form-data;name=\"file\";filename=\"").append(strFilePathName)
                        .append("\"").append(strNewLine);
                sb.append("Content-Type:application/octet-stream");
                sb.append(strNewLine);
                sb.append(strNewLine);
                out.write(sb.toString().getBytes());

                try (
                        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
                ) {
                    byte[] byteBufferOut = new byte[1024];
                    int intBytes = 0;
                    while ((intBytes = dataInputStream.read(byteBufferOut)) != -1) {
                        out.write(byteBufferOut, 0, intBytes);
                    }
                    out.write(strNewLine.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 定义最后数据分隔线，即--加上BOUNDARY再加上--。
            byte[] byteEndData = (strNewLine + strBoundaryPrefix + strBOUNDARY + strBoundaryPrefix + strNewLine)
                    .getBytes();
            // 写上结尾标识
            out.write(byteEndData);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //定义BufferedReader输入流来读取URL的响应
        try (
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
        ) {
            String strLine;
            StringBuffer sb = null;
            while ((strLine = reader.readLine()) != null) {
                sb.append(strLine);
            }

            jsonReturn = JSONObject.fromObject(sb);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonReturn;
    }


    /**
     * 回调业务系统提供的接口
     * @param strWriteBackURL 回调接口URL
     * @param mapWriteBackHeaders 请求头参数
     * @param mapParams 参数
     * @return JSON格式的返回结果
     */
    private static JSONObject callBack(String strWriteBackURL, Map<String,String> mapWriteBackHeaders, Map<String, Object> mapParams){
        //发送get请求并接收响应数据
        String strResponse = HttpUtil.createGet(strWriteBackURL).
                addHeaders(mapWriteBackHeaders).form(mapParams)
                .execute().body();

        JSONObject jsonReturn = new JSONObject();
        if(strResponse != null){
            jsonReturn.put("flag", "success");
            jsonReturn.put("message", "Convert Office File Callback Success.\n" +
                    "Message is :\n" +
                    strResponse);
        }

        return jsonReturn;
    }


}
