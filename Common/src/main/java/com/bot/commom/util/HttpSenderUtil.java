package com.bot.commom.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.UnsupportedCharsetException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpSenderUtil {

    private static final String CHARSET = "UTF-8";

    private static RequestConfig defaultConfig = null;

    /**
     * 连接超时时间设置为 10 秒
     * 响应超时时间设置为 20 秒
     * 其他为默认
     * @author lihy  v1.0   2018/6/7
     */
    static {
        defaultConfig = RequestConfig.custom()
                .setConnectTimeout(10 * 1000)
                .setSocketTimeout(20 * 1000)
                .build();
    }

    public static String httpPost(String url, HttpEntity reqEntity) {
    	CloseableHttpClient httpclient = HttpClients.createDefault();
        String result = "";
        HttpPost httppost = new HttpPost(url);
        httppost.setConfig(defaultConfig);
        HttpResponse response = null;
        try {
            httppost.setEntity(reqEntity);
            response = httpclient.execute(httppost);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity entity = null;
        if (response != null) {
            entity = response.getEntity();
        }

        if (entity != null) {
            try {
                String content = EntityUtils.toString(entity, "UTF-8");
                result = content;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return result;
    }

    /**
     * post xmlData
     *
     * @param url
     * @param xmlData
     * @return
     * @throws Exception
     * @author raozj  v1.0   2017年11月20日
     */
    public static String postXmlData(String url, String xmlData) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setConfig(defaultConfig);
        post.addHeader("Content-Type", "text/xml");
        StringEntity stringEntity = new StringEntity(xmlData, CHARSET);
        stringEntity.setContentEncoding(CHARSET);
        post.setEntity(stringEntity);
        CloseableHttpResponse response = httpclient.execute(post);

        if (response != null && response.getEntity() != null) {
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            return result;
        } else {
            log.error("请求失败" + response);
            throw new Exception("请求失败");
        }
    }
    
    /**
     * post xmlData
     *
     * @param url
     * @param xmlData
     * @return
     * @throws Exception
     * @author raozj  v1.0   2017年11月20日
     */
    public static String postXmlDataWithSSLContext(String url, String xmlData) throws Exception {
    	//采用绕过验证的方式处理https请求  
        SSLContext sslcontext = createIgnoreVerifySSL();  
          
		// 设置协议http和https对应的处理socket链接工厂的对象
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslcontext)).build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        HttpClients.custom().setConnectionManager(connManager).build();
        CloseableHttpClient client = HttpClients.createDefault(); 
        HttpPost post = new HttpPost(url);
        post.setConfig(defaultConfig);
        post.addHeader("Content-Type", "text/xml");
        StringEntity stringEntity = new StringEntity(xmlData, CHARSET);
        stringEntity.setContentEncoding(CHARSET);
        post.setEntity(stringEntity);
        CloseableHttpResponse response = client.execute(post);

        if (response != null && response.getEntity() != null) {
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            return result;
        } else {
            log.error("请求失败" + response);
            throw new Exception("请求失败");
        }
    }
    
    /**
     * post jsonData
     *
     * @param url
     * @param jsonData
     * @return
     * @throws Exception
     * @author raozj  v1.0   2017年11月20日
     */
    public static String postJsonData(String url, String jsonData) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setConfig(defaultConfig);
        post.addHeader("Content-Type", "application/json");
        StringEntity stringEntity = new StringEntity(jsonData, CHARSET);
        stringEntity.setContentEncoding(CHARSET);
        post.setEntity(stringEntity);
        CloseableHttpResponse response = httpclient.execute(post);

        if (response != null && response.getEntity() != null) {
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            return result;
        } else {
            log.error("请求失败" + response);
            throw new Exception("请求失败");
        }
    }
    
    /**
     * post xmlData
     *
     * @param url
     * @param byteData
     * @return
     * @throws Exception
     * @author raozj  v1.0   2017年11月20日
     */
    public static String postByteData(String url, byte[] byteData) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setConfig(defaultConfig);
        post.addHeader("Content-Type", "text/xml");
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(byteData);
        post.setEntity(byteArrayEntity);
        CloseableHttpResponse response = httpclient.execute(post);

        if (response != null && response.getEntity() != null) {
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            return result;
        } else {
            log.error("请求失败" + response);
            throw new Exception("请求失败");
        }
    }

    /**
     * @param url
     * @param data
     * @return
     * @throws Exception
     * @author raozj  v1.0   2017年11月29日
     */
    public static String postUrlencodedData(String url, String data) throws Exception {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setConfig(defaultConfig);
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
        StringEntity stringEntity = new StringEntity(data, CHARSET);
        stringEntity.setContentEncoding(CHARSET);
        post.setEntity(stringEntity);
        HttpResponse response = httpclient.execute(post);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            return result;
        } else {
            log.error("请求失败" + response);
            throw new Exception("请求失败");
        }
    }

    /**
     * get
     *
     * @param url    url
     * @param params 参数集合
     * @return 响应结果
     * @author chenlw  v1.0   2017/12/06
     */
    public static String get(String url, List<NameValuePair> params) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClients.createDefault();
            if(params !=null){
                String paramsStr = EntityUtils.toString(new UrlEncodedFormEntity(params, CHARSET));
                url = url + "?" + paramsStr;
            }
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(defaultConfig);
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(entity);
            }
        } catch (UnsupportedCharsetException e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error("请求失败" + response, e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error("关闭响应实体异常", e);
                }
            }
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    log.error("关闭httpClient连接异常", e);
                }
            }
        }
        return null;
    }
    
    /**
     * post key-value对
     * @param url
     * @param params
     * @return
     * @throws Exception
     * @author raozj  v1.0   2018年4月17日
     */
    public static String postNameValuePairs(String url, Map<String, String> params) throws Exception{
    	HttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setConfig(defaultConfig);
    	if(null != params) {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            for (String name : params.keySet()) {
                nvps.add(new BasicNameValuePair(name, params.get(name)));
            }
            log.info(String.format("请求参数：%s",nvps.toString()));
            UrlEncodedFormEntity u = new UrlEncodedFormEntity(nvps, CHARSET);
            u.setContentEncoding(CHARSET);
            post.setEntity(u);
        }
    	HttpResponse response = httpclient.execute(post);
    	if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            return result;
        } else {
            log.error("请求失败" + response);
            throw new Exception("请求失败");
        }
    }
    
    /** 
     * 绕过验证 
     *   
     * @return 
     * @throws NoSuchAlgorithmException  
     * @throws KeyManagementException  
     */  
    public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {  
        SSLContext sc = SSLContext.getInstance("SSLv3");  
      
        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法  
        X509TrustManager trustManager = new X509TrustManager() {  
            @Override  
            public void checkClientTrusted(  
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,  
                    String paramString) throws CertificateException {  
            }  
      
            @Override  
            public void checkServerTrusted(  
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,  
                    String paramString) throws CertificateException {  
            }  
      
            @Override  
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
                return null;  
            }  
        };  
      
        sc.init(null, new TrustManager[] { trustManager }, null);  
        return sc;  
    }

    public static String nativePost(String url, String param) throws IOException {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpsURLConnection conn = (HttpsURLConnection) realUrl.openConnection();
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setReadTimeout(30 * 1000);
            conn.setConnectTimeout(10 * 1000);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            log.error("http post 异常", e);
            throw e;
        }
        finally{
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            }
            catch(IOException ex){
                log.error("http post 关闭流异常", ex);
            }
        }
        return result;
    }
    
    /**
	 * 下载指定地址的文件到本地
	 *
	 * @param downLoadUrl 文件地址
	 * @param expectPath 本地文件路径
	 * @throws IOException
	 */
	public static void download(String downLoadUrl, String expectPath) throws IOException {
		URL url = null;
		HttpURLConnection httpUrlConnection = null;
		InputStream fis = null;
		FileOutputStream fos = null;
		try {
			url = new URL(downLoadUrl);
			httpUrlConnection = (HttpURLConnection) url.openConnection();
			httpUrlConnection.setConnectTimeout(5 * 1000);
			httpUrlConnection.setDoInput(true);
			httpUrlConnection.setDoOutput(true);
			httpUrlConnection.setUseCaches(false);
			httpUrlConnection.setRequestMethod("GET");
			httpUrlConnection.setRequestProperty("Charsert", "UTF-8");
			httpUrlConnection.connect();
			fis = httpUrlConnection.getInputStream();
			byte[] temp = new byte[1024];
			int b;
			fos = new FileOutputStream(new File(expectPath));
			while ((b = fis.read(temp)) != -1) {
				fos.write(temp, 0, b);
				fos.flush();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (fis != null) {fis.close();}
				if (fos != null) {fos.close();}
				if (httpUrlConnection != null){ httpUrlConnection.disconnect();}
			} catch (IOException e) {
				log.error("下载异常：", e);
			}
		}
	}
}
