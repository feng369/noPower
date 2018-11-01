package cn.wizzer.test;

import cn.wizzer.framework.util.DateUtil;
import com.alibaba.fastjson.JSONObject;

public class JsonTest
{

    public static void main(String[] args) {
        String s=",ghbsafsfasa";
        System.out.println(s.substring(s.lastIndexOf(",")));
        System.out.println(s.split(",")[1]);
        String s1="safsfasa";
        System.out.println(s1.substring(-1));
        String sstarttime="2018-3-29 12:20:29";
        long diseq = DateUtil.getMinuteBetweens(sstarttime,DateUtil.getDateTime());
        long h = diseq/60;
        long m = diseq % 60;
        System.out.println((h<10?("0"+h):h)+":"+(m<10?("0"+m):m));

        //parseJson();
    }


    private static void parseJson(){

        String tokenJson="{\"errcode\":0,\"errmsg\":\"\",\"access_token\": \"accesstoken000001\",\"expires_in\": 7200}";
//        JSONObject jsonobject = net.sf.json.JSONObject.fromObject(tokenJson);
//        int errcode = jsonobject.getInt("errcode");

        JSONObject jsonobject = JSONObject.parseObject(tokenJson);
        int errcode = jsonobject.getIntValue("errcode");
        String errmsg = jsonobject.getString("errmsg");
        String access_token = jsonobject.getString("access_token");
        String expires_in = jsonobject.getString("expires_in");
        System.out.println("JSON="+errcode+":"+ errmsg+":"+ access_token+":"+ expires_in);


//        String joStr = "{name:\"张三\",age:\"20\"}";
//        //将json字符串转化为JSONObject
//        JSONObject jsonObject = net.sf.json.JSONObject.fromObject(joStr);
//        //通过getString("")分别取出里面的信息
//        String name = jsonObject.getString("name");
//        String age = jsonObject.getString("age");
//        //输出  张三 20
//        System.out.println(name+" "+age);
//
//        String jaStr = "[{user:{name:\"张三\",age:\"20\"}},{score:{yuwen:\"80\",shuxue:\"90\"}}]";
//        //将jsonArray字符串转化为JSONArray
//        JSONArray jsonArray = JSONArray.fromObject(jaStr);
//        //取出数组第一个元素
//        JSONObject jUser = jsonArray.getJSONObject(0).getJSONObject("user");
//        //取出第一个元素的信息，并且转化为JSONObject
//        String name2 = jUser.getString("name");
//        String age2 = jUser.getString("age");
//        //输出 张三 20
//        System.out.println(name2+" "+age2);
//        //取出数组第二个元素，并且转化为JSONObject
//        JSONObject jScore = jsonArray.getJSONObject(1).getJSONObject("score");
//        //取出第二个元素的信息
//        String yuwen = jScore.getString("yuwen");
//        String shuxue = jScore.getString("shuxue");
//        //输出  80 90
//        System.out.println(yuwen+"  "+shuxue);
    }

}
