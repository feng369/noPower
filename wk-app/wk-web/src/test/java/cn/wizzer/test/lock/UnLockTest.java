package cn.wizzer.test.lock;

import cn.wizzer.app.web.commons.plugin.HttpClientUtil;
import cn.wizzer.app.web.commons.plugin.SSLClient;
import cn.wizzer.framework.util.DateUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.nutz.lang.Lang;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 硬件批量并发测试：开锁
 */
public class UnLockTest {
    private static Log log = Logs.get();
    public static void main(String[] args) {
        String[]lockNumArr = new String[]{
                "358511020000349",
                "358511020000224",
                "358511020000026",
                "358511020000356",
                "358511020000257",
                "358511020000307",
                "358511020000232",
                "358511020000380",
                "358511020000398",
                "358511020000273",
                "358511020000414",
                "358511020000422",
                "358511020000281",
                "358511020000331",
                "358511020000406",
                "358511020000208",
                "358511020000299",
                "358511020000216",
                "358511020000240",
                "358511020000265"
        };

        String url ="http://np.bv-cloud.com/";
        String tokenUrl = url+"/open/api/token/get";
        DateFormat df = new SimpleDateFormat("yyyyMMddHH");
        System.out.println("java md5:"+ Lang.md5("fb4771a975"+"1961117fb1ad4ee1" + DateUtil.format(new Date(), "yyyyMMddHH")));

        String sign = getMD5("fb4771a975"+"1961117fb1ad4ee1"+df.format(new Date()));
        HttpClientUtil httpClientUtil = new HttpClientUtil();
        tokenUrl+= "?appid=fb4771a975&sign="+sign;
        String retToken = httpClientUtil.doPost(tokenUrl,new HashMap<>(),"utf-8");
        System.out.println(retToken);
        JSONObject jsonObject = JSONObject.parseObject(retToken);
        if(jsonObject!=null && "0".equals(jsonObject.getString("code"))){
            JSONObject data = jsonObject.getJSONObject("data");
            String tk = data.getString("token");

            Map lockMap = new LinkedHashMap();
            //发令枪  模拟高并发
            BlockingQueueWN bq = new BlockingQueueWN(lockNumArr.length);
            final CountDownLatch latch = new CountDownLatch(lockNumArr.length);
            for(String lockId : lockNumArr){
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            latch.await();
                            //开锁
//            String openLockUrl = url+"/platform/eq/use/openLock";
//            String pjson="{\"lockID\":\""+lockId+"\"}";
//            String res = postJson(openLockUrl,pjson,"f9e5874baa",tk);
                            String openLockUrl = url + "/platform/eq/use/openLock?lockID=" + lockId;
                            String res = doGet(openLockUrl, "utf-8", "fb4771a975", tk);
                            System.out.println(res);
                            JSONObject jsonRes = JSONObject.parseObject(res);
                            if (jsonRes!=null && "0".equals(jsonRes.getString("code"))) {
                                JSONObject dataRes = jsonRes.getJSONObject("data");
                                String equseid = dataRes.getString("equseid");
                                bq.enqueue(equseid);
                                lockMap.put(equseid,lockId);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }.start();
                latch.countDown();
            }

            //查询开锁结果
            int i=0;
            while(true){
                try {
                    if(i==lockNumArr.length)break;
                    String equseid = (String) bq.dequeue();
                    String getUnLockStatusUrl = url + "/platform/eq/use/getUnLockStatus?equseid=" + equseid+"&personid=14923292cea54d92830a8dc977e5f86b&personunitid=77c088479a714f29bfaecbc744808e7d";
                    String res = doGet(getUnLockStatusUrl, "utf-8", "fb4771a975", tk);
                    System.out.println(res+"+"+lockMap.get(equseid)+"+"+bq.getSize());
                    JSONObject jsonRes = JSONObject.parseObject(res);
                    if (jsonRes!=null && !"0".equals(jsonRes.getString("code"))) {//未打开成功归还到队列中
                        bq.enqueue(equseid);
                    }else if (jsonRes!=null && "0".equals(jsonRes.getString("code"))){
                        i++;
                    }
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }

        }
    }


    /**
     * 生成MD5
     * @return
     */
    public  static  String getMD5(String message){
        String md5Result="";
        try{
            //1.创建一个提供信息摘要算法的对象，初始化为MD5算法对象
            MessageDigest md=MessageDigest.getInstance("MD5");
            //2.将消息变为byte数组
            byte[] input=message.getBytes();
            //3.计算后获得字节数组，128位长度的MD5加密
            byte[] buff=md.digest(input);
            //4.把数组每一个字节（一个字节占8位）换成16进制的md5字符串
            md5Result=bytesHex(buff);

        }catch (Exception e){
            e.printStackTrace();
        }

        return md5Result;
    }

    public static String bytesHex(byte[]bytes){
        StringBuffer md5Result =new StringBuffer();
        //把数组每一字节换成换成16进制连成md5字符串
        int digital;
        for (int i=0;i<bytes.length;i++){


            digital=bytes[i];
            if (digital<0){
                digital+=256;
            }
            if (digital<16){
                md5Result.append("0");
            }
            md5Result.append(Integer.toHexString(digital));
        }
        return md5Result.toString().toUpperCase();
    }

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
}
