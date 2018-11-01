package cn.wizzer.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.wizzer.app.web.commons.plugin.SSLClient;
import com.alibaba.fastjson.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HttpUtils {

	private static Logger log = Logger.getLogger(HttpUtils.class);

	/**
	 * JSON参数 调用 API
	 *
	 * @param parameters
	 * @param appid
	 * @param token
	 * @return
	 */
	public static String postJson(String apiURL, String parameters, String appid, String token) {
		String body = null;
		log.info("parameters:" + parameters);
		HttpClient httpClient = null;
		HttpPost method = null;
		long startTime = 0L;
		long endTime = 0L;
		if(apiURL!=null) {
			httpClient = new DefaultHttpClient();
			method = new HttpPost(apiURL);
		}
		if (method != null & parameters != null
				&& !"".equals(parameters.trim())) {
			try {
//                com.google.gson.JsonArray arry = new JsonArray();
//                JsonObject j = new JsonObject();
//                j.addProperty("orderId", "中文");
//                j.addProperty("createTimeOrder", "2015-08-11");
//                arry.add(j);
//                String jsonPar = arry.toString();
				//或
//                Map<String,Object> paramMap = new HashMap<String,Object>();
//                paramMap.put("touser",sb.toString());
//                paramMap.put("msgtype","text");
//                paramMap.put("agentid",Integer.valueOf(sysWx.getAgentid()));
//                com.alibaba.fastjson.JSONObject textJsonObj = new JSONObject();
//                textJsonObj.put("content",content);
//                paramMap.put("text",textJsonObj);
//                paramMap.put("safe",0);
//                JSONObject jsonObject = new JSONObject(paramMap);
//                log.info("请求参数："+jsonObject.toJSONString());
				// 建立一个NameValuePair数组，用于存储欲传送的参数
				method.addHeader("appid",appid);
				method.addHeader("token",token);
				method.addHeader("Content-type","application/json; charset=utf-8");
				method.setHeader("Accept", "application/json");
				method.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));
				startTime = System.currentTimeMillis();

				HttpResponse response = httpClient.execute(method);

				endTime = System.currentTimeMillis();
				int statusCode = response.getStatusLine().getStatusCode();

				log.info("statusCode:" + statusCode);
				log.info("调用API 花费时间(单位：毫秒)：" + (endTime - startTime));
				if (statusCode != HttpStatus.SC_OK) {
					log.error("Method failed:" + response.getStatusLine());
				}

				// Read the response body
				body = EntityUtils.toString(response.getEntity());

			} catch (IOException e) {
				// 网络错误
				e.printStackTrace();
			} finally {
				log.info("调用接口完成！" );
			}

		}else{
			log.error("参数体不能为空!");
		}
		return body;
	}

	public static String doPost(String url, Map<String,String> map, String charset){
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try{
			httpClient = new SSLClient();
			httpPost = new HttpPost(url);
			//设置参数
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			Iterator iterator = map.entrySet().iterator();
			while(iterator.hasNext()){
				Map.Entry<String,Object> elem = (Map.Entry<String, Object>) iterator.next();
				list.add(new BasicNameValuePair(elem.getKey(),elem.getValue().toString()));
			}
			if(list.size() > 0){
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);
				httpPost.setEntity(entity);
			}
			HttpResponse response = httpClient.execute(httpPost);
			if(response != null){
				HttpEntity resEntity = response.getEntity();
				if(resEntity != null){
					result = EntityUtils.toString(resEntity,charset);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return result;
	}

	public static String doGet(String url, String charset, String appid, String token){
		HttpClient httpClient = null;
		HttpGet httpget = null;
		String result = null;
		try{
			httpClient = new SSLClient();
			httpget = new HttpGet(url);
			httpget.addHeader("appid",appid);
			httpget.addHeader("token",token);
			HttpResponse response = httpClient.execute(httpget);
			if(response != null){
				HttpEntity resEntity = response.getEntity();
				if(resEntity != null){
					result = EntityUtils.toString(resEntity,charset);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return result;
	}

	public static InputStream getXML(String path)  {
		InputStream inputStream = null;
		try {
			URL url = new URL(path);
			if (url != null) {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(3000); // 设置超时时间为3秒
				connection.setDoInput(true);
				connection.setRequestMethod("GET"); // 设置请求方式
				int code = connection.getResponseCode(); // 接受返回码
				if (200 == code) { // 返回码为200为成功
					inputStream = connection.getInputStream();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return inputStream;
	}

//	public static com.alibaba.fastjson.JSONObject getJSON(String path){
//		com.alibaba.fastjson.JSONObject DataObj = null;
//		org.apache.commons.httpclient.methods.GetMethod getMetohd = new GetMethod(path);
//		org.apache.commons.httpclient.HttpClient httpClient = new HttpClient();
//		int statusCode = 0;
//
//		try {
//			statusCode = httpClient.executeMethod(getMetohd);
//			if (statusCode == 200) {
//
//				String result = getMetohd.getResponseBodyAsString();
//				logger.info(path+"\r\n"+result);
//				DataObj = com.alibaba.fastjson.JSONObject.parseObject(result);
//			} else {
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return DataObj;
//	}
//
//	public static String getRSString(String path){
//		rg.apache.commons.httpclient.methods.GetMethod getMetohd = new GetMethod(path);
//		HttpClient httpClient = new HttpClient();
//		int statusCode = 0;
//
//		try {
//			statusCode = httpClient.executeMethod(getMetohd);
//			if (statusCode == 200) {
//
//				String result = getMetohd.getResponseBodyAsString();
//				logger.info(path+"\r\n"+result);
//				return result;
//			} else {
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}


	public static JSONObject httpGetJSON(String requestUrl) {
		String requestMethod = "GET";
		JSONObject jsonObject = null;
		StringBuffer buffer = new StringBuffer();
		InputStream in = null;
		HttpURLConnection httpUrlConn =null;
		try {

			URL url = new URL(requestUrl);
			// http协议传输
			httpUrlConn = (HttpURLConnection) url.openConnection();

			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);
//            httpUrlConn.setRequestProperty("Charset", "UTF-8");
			httpUrlConn.setRequestProperty("contentType", "application/json");

			if ("GET".equalsIgnoreCase(requestMethod))
				httpUrlConn.connect();
			// 将返回的输入流转换成字符串
			in = httpUrlConn.getInputStream();

//            InputStreamReader inputStreamReader = new InputStreamReader(in, "utf-8");
//            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//
//            String str = null;
//            while ((str = bufferedReader.readLine()) != null) {
//                buffer.append(str);
//            }
//            bufferedReader.close();
//            inputStreamReader.close();

			int count = 0;
			//网络操作时往往出错，因为调用available()方法时，对发发送的数据可能还没有到达，你得到的count是0，如下避免
			while (count == 0) {
				count = in.available();
			}
			byte[] bytes = new byte[count];
//            in.read(bytes);//read()方法可能不保证能读取bytes.length个字节，如下方法可保证读取bytes.length个字节
			int readCount = 0; // 已经成功读取的字节的个数
			while (readCount < count) {
				readCount += in.read(bytes, readCount, count - readCount);
			}
			String res = new String(bytes,"utf-8");
			log.info(requestUrl+"\r\n"+res);
			//jsonObject = net.sf.json.JSONObject.fromObject(res);
			jsonObject = JSONObject.parseObject(res);

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			// 释放资源

			try {
				if(in!=null)
					in.close();
				in = null;
				httpUrlConn.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return jsonObject;
	}

	public static InputStream getPOSTJSON(String path) {
		InputStream inputStream = null;
		try {
			URL url = new URL(path);
			if (url != null) {
				// 建立http连接
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000); // 设置超时时间为3秒
				// 设置允许输出
				conn.setDoOutput(true);
				conn.setDoInput(true);
				// 设置不用缓存
				conn.setUseCaches(false);
				// 设置传递方式
				conn.setRequestMethod("GET");
				// 设置维持长连接
//	            conn.setRequestProperty("Connection", "Keep-Alive");
				// 设置文件字符集:
				conn.setRequestProperty("Charset", "UTF-8");
				// 设置文件类型:
				conn.setRequestProperty("contentType", "application/json");

//	            //转换为字节数组
//	            byte[] data = (obj.toString()).getBytes();
//	            // 设置文件长度
//	            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
				// 开始连接请求
				conn.connect();
//	            OutputStream  out = conn.getOutputStream();
//	            // POST写入请求的字符串
//	            out.write((obj.toString()).getBytes());
//	            out.flush();
//	            out.close();

				int code = conn.getResponseCode(); // 接受返回码
				if (200 == code) { // 返回码为200为成功
					inputStream = conn.getInputStream();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return inputStream;
	}
}
