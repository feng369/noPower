package cn.wizzer.test;

import cn.wizzer.app.TestBase;
import cn.wizzer.app.web.commons.plugin.HttpClientUtil;
import cn.wizzer.app.web.commons.plugin.PostRun;
import cn.wizzer.framework.map.LngLat;
import cn.wizzer.framework.map.MapUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import sun.misc.BASE64Encoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * 高德地图web API测试
 */
public class AmapTest  extends TestBase {

    private static boolean isPhone(String tel) {
        return Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$").matcher(tel).matches();
    }

    @Test
    public void testPhone(){
        System.out.println(isPhone("18083895210"));
    }


    @Test
    public void testDistance() throws NoSuchAlgorithmException, IOException, URISyntaxException {
        testAPI();

        calcDistance1();

        LngLat start = new LngLat(114.232018,30.586685);
        LngLat end = new LngLat(114.241095,30.553993);
        double disDouble = MapUtils.calculateLineDistance(start, end);
        int disInt = new BigDecimal(disDouble).setScale(0,java.math.BigDecimal.ROUND_HALF_UP).intValue();
        System.err.println(disDouble+":"+disInt);

        System.out.println(MapUtils.GetDistance(30.586685,114.232018,30.553993,114.241095));
    }

    @Test
    public void  calcDistance1() {
        double lonA,latA,lonB,latB;
        lonA = 114.232018;
        latA = 30.586685;
        lonB = 114.241095;
        latB = 30.553993;

        long earthR = 6371393;
        double x = Math.cos(latA * Math.PI / 180.) * Math.cos(latB * Math.PI / 180.) * Math.cos((lonA - lonB) * Math.PI / 180);
        double y = Math.sin(latA * Math.PI / 180.) * Math.sin(latB * Math.PI / 180.);
        double s = x + y;
        if (s > 1) s = 1;
        if (s < -1) s = -1;
        double alpha = Math.acos(s);
        double distance = alpha * earthR;
        System.err.println(distance);
    }


    @Test
    public void testAPI() throws IOException, NoSuchAlgorithmException, URISyntaxException {
        //http://restapi.amap.com/v3/distance?origins=116.481028,39.989643|114.481028,39.989643|115.481028,39.989643&destination=114.465302,40.004717&output=xml&key=<用户的key>
        String url="http://restapi.amap.com/v3/distance?";
        String key = "key=fcb32ddc9807dd68b7d494d9f0581c4e";
        String origins = "origins=114.232018,30.586685";//116.481028,39.989643|115.481028,39.989643%7C114.481028%2C39.989643
        String origins0 = "origins=116.481028,39.989643%7C115.481028,39.989643";
        String dest = "destination=114.241095,30.553993";
        String output = "output=JSON";
        String type = "type=0";
        String privatekey = "d99cc8e5a33d1c04ad080fe78ab902dd";
        String md5 = getMD5Result(new String((dest+"&"+key+"&"+origins+"&"+output+"&"+type+privatekey).getBytes("UTF-8")));
        System.out.println("MD5:"+md5);
        String sign = "sig="+ URLEncoder.encode(md5,"UTF-8");
        url = url + dest+"&"+key +"&" + origins +"&"+output+"&"+sign+"&"+type;
        System.out.println("url:"+url);
//        PostRun pr=new PostRun();
//        String ret=pr.WXTokenget(url);

        String ret = "";

//        HttpClientUtil httpClientUtil = new HttpClientUtil();
//        ret = httpClientUtil.doGet(url,"utf-8");

        URL url2 = new URL(url);
        URI uri = new URI(url2.getProtocol(), url2.getHost(), url2.getPath(), url2.getQuery(), null);
        HttpClient client    = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(uri);
        HttpResponse response = client.execute(httpget);
        if(response != null){
            HttpEntity resEntity = response.getEntity();
            if(resEntity != null){
                ret = EntityUtils.toString(resEntity,"UTF-8");
            }
        }

//        JSONObject jsonobject = net.sf.json.JSONObject.fromObject(getHttpResponse(url));
//        JSONObject jsonobject = JSONObject.parseObject(getHttpResponse(url));
//        JSONArray pathArray = jsonobject.getJSONObject("route").getJSONArray("paths");
//        String distanceString = pathArray.getJSONObject(0).getString("distance");
//        ret = getHttpResponse(url);

        System.out.println("JSON:"+ret);
    }


    public static  String  getMD5Result(String inputStr)
    {
        System.out.println("=======加密前的数据:"+inputStr);
        BigInteger bigInteger=null;
        try {

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] inputData = inputStr.getBytes("utf-8");

            md.update(inputData);

            bigInteger = new BigInteger(md.digest());

        } catch (Exception e) {e.printStackTrace();}

        System.out.println("MD5加密后:" + bigInteger.toString(16));

        return bigInteger.toString(16);

    }

    /**利用MD5进行加密
     　　* @param str  待加密的字符串
     　　* @return  加密后的字符串
     　　* @throws NoSuchAlgorithmException  没有这种产生消息摘要的算法
     　　 * @throws UnsupportedEncodingException
     　　*/
    public static String EncoderByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    //确定计算方法
        MessageDigest md5=MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        //加密后的字符串
        String newstr=base64en.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }

    public static String getHttpResponse(String allConfigUrl) {
        BufferedReader in = null;
        StringBuffer result = null;
        try {
            // url请求中如果有中文，要在接收方用相应字符转码
            URI uri = new URI(allConfigUrl);
            URL url = uri.toURL();
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Content-type", "text/html");
            connection.setRequestProperty("Accept-Charset", "utf-8");
            connection.setRequestProperty("contentType", "utf-8");
            connection.connect();
            result = new StringBuffer();
            // 读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }
}
