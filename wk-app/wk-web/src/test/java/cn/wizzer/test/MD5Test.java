package cn.wizzer.test;

import cn.wizzer.framework.util.DateUtil;
import org.nutz.lang.Lang;

import java.security.MessageDigest;
import java.util.Date;

public class MD5Test {

    public static void main(String args[]){

       String result=MD5Encode("f9e5874baa"+"2d3f219ce878411e" + DateUtil.format(new Date(), "yyyyMMddHH"));
             System.err.println(result);
        String result2=getMD5("f9e5874baa"+"2d3f219ce878411e" + DateUtil.format(new Date(), "yyyyMMddHH"));
             System.out.println(result2);

        System.out.println("java md5:"+ Lang.md5("f9e5874baa"+"2d3f219ce878411e" + DateUtil.format(new Date(), "yyyyMMddHH")));
      }


    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7",
                "8", "9", "a", "b", "c", "d", "e", "f"};

        /**
         * 转换字节数组为16进制字串
         * @param b 字节数组
         * @return 16进制字串
         */
        public static String byteArrayToHexString(byte[] b) {
            StringBuilder resultSb = new StringBuilder();
            for (byte aB : b) {
                resultSb.append(byteToHexString(aB));
            }
            return resultSb.toString();
        }

        /**
         * 转换byte到16进制
         * @param b 要转换的byte
         * @return 16进制格式
         */
        private static String byteToHexString(byte b) {
            int n = b;
            if (n < 0) {
                n = 256 + n;
            }
            int d1 = n / 16;
            int d2 = n % 16;
            return hexDigits[d1] + hexDigits[d2];
        }

        /**
         * MD5编码
         * @param origin 原始字符串
         * @return 经过MD5加密之后的结果
         */
        public static String MD5Encode(String origin) {
            String resultString = null;
            try {
                resultString = origin;
                //创建信息摘要
                MessageDigest md = MessageDigest.getInstance("MD5");
                //用明文字符串计算消息摘要
                md.update(resultString.getBytes("UTF-8"));
                //读取消息摘要,并转换为16进制字符串
                resultString = byteArrayToHexString(md.digest());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultString;
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





}
