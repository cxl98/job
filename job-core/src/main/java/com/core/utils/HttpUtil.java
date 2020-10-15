package com.core.utils;

import com.core.bean.model.ReturnT;
import com.cxl.registry.client.util.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpUtil {
    private static final Logger LOGGER= LoggerFactory.getLogger(HttpUtil.class);
    private static String RPC_ACCESS_TOKEN="RPC_ACCESS_TOKEN";
    /**
     * post
     *
     * @param url
     * @param requestObj
     * @param token
     * @param timeout
     * @return
     */
    public static ReturnT<String> postBody(String url, Object requestObj, String token, int timeout) {
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        try {
            //connection
            URL realUrl = new URL(url);
            connection = (HttpURLConnection) realUrl.openConnection();

            //connection setting
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(timeout * 1000);
            connection.setConnectTimeout(3 * 1000);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");
            if (token != null&& token.length()>0) {
                connection.setRequestProperty(RPC_ACCESS_TOKEN,token);
            }
            // do connection
            connection.connect();

            //write requestBody
            String requestBody= Json.toJson(requestObj);

            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes(requestBody);
            dataOutputStream.flush();
            dataOutputStream.close();

            //valid StatusCode
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                return new ReturnT<String>(ReturnT.FAIL_CODE,"xxl-rpc remoting fail, StatusCode("+ statusCode +") invalid. for url : " + url);
            }

            //result
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            String resultJson=result.toString();

            try {
                Map<String,Object> resultMap=Json.parseMap(resultJson);

                ReturnT<String> returnT=new ReturnT<>();
                if (resultMap == null) {
                    returnT.setCode(ReturnT.FAIL_CODE);
                    returnT.setMsg("Admin Remoting call fail.");
                }else{
                    returnT.setCode(Integer.valueOf(String.valueOf(resultMap.get("code"))));
                    returnT.setMsg(String.valueOf(resultMap.get("msg")));
                    returnT.setContent(String.valueOf(resultMap.get("content")));
                }
                return returnT;
            } catch (NumberFormatException e) {
                LOGGER.error("rpc remoting (url="+url+") response content invalid("+ resultJson +").", e);
                return new ReturnT<>(ReturnT.FAIL_CODE, "rpc remoting (url="+url+") response content invalid("+ resultJson +").");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return null;
    }
}
