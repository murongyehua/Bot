package com.bot.base.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.util.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CmmsUtil {

    private static final String URL = "https://cmms.hrtn.net:19002/payweb/query/query!queryPayInfo";
    private static final String CLIENTCODE = "1066";
    private static final String REFUND_KEY = "c4M9mS02";

    public static String refund(String orderNo)
            throws Exception
    {
        String respStr = "";
        HttpPost httpPost = new HttpPost("https://cmms.hrtn.net:19002/payweb/query/query!queryPayInfo");
        try
        {
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectionRequestTimeout(20000).build();

            httpPost.setConfig(requestConfig);
            httpPost.setEntity(createEntity(orderNo));

            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200)
            {
                respStr = EntityUtils.toString(response.getEntity(), "utf-8");
                System.out.println(respStr);
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            httpPost.releaseConnection();
        }
        return respStr;
    }

    public static UrlEncodedFormEntity createEntity(String orderNo)
    {
        UrlEncodedFormEntity formEntity = null;
        List<BasicNameValuePair> pairList = new ArrayList();
        pairList.add(new BasicNameValuePair("servicecode", "REFUND"));
        pairList.add(new BasicNameValuePair("version", "1"));
        pairList.add(new BasicNameValuePair("citycode", "WH"));
        pairList.add(new BasicNameValuePair("clientcode", "1066"));
        pairList.add(new BasicNameValuePair("clientpwd", "484f723ccdbe15ffbb9f4313bed45a55"));

        String requestid = createRequestid();
        pairList.add(new BasicNameValuePair("requestid", requestid));

        String requestContent = createRequestContent(requestid, orderNo);
        pairList.add(new BasicNameValuePair("requestContent", requestContent));
        try
        {
            formEntity = new UrlEncodedFormEntity(pairList, "utf-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return formEntity;
    }

    public static String createRequestContent(String requestid, String orderNo)
    {
        String requestContent = null;

        JSONObject jsonObject = new JSONObject();
        jsonObject.set("dataSign", createDataSign(requestid));
        jsonObject.set("orderNo", "1066-" + orderNo);
        requestContent = JSONUtil.toJsonStr(jsonObject);

        return requestContent;
    }

    private static String createDataSign(String requestid)
    {
        String dataSign = null;
        requestid = requestid + "c4M9mS02";
        try
        {
            dataSign = DigestUtils.md5DigestAsHex(requestid.getBytes("utf-8"));
        }
        catch (Exception localException) {}
        return dataSign.toUpperCase();
    }

    public static String createRequestid()
    {
        String requestid = "";
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = dateFormat.format(date);
        requestid = "1066" + dateStr + buildRandom(2);

        return requestid;
    }

    public static int buildRandom(int length)
    {
        int num = 1;
        double random = Math.random();
        if (random < 0.1D) {
            random += 0.1D;
        }
        for (int i = 0; i < length; i++) {
            num *= 10;
        }
        return (int)(random * num);
    }

    public static void main(String[] args)
    {
        try
        {
            refund("99678672");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
