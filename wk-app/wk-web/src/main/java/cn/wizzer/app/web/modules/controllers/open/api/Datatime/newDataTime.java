package cn.wizzer.app.web.modules.controllers.open.api.Datatime;

/**
 * Created by xl on 2017/7/15.
 */
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

public class newDataTime {
    //转化毫秒
    //20180309zhf1435
    public static long nd = 24 * 60 * 60 *1000; //一天
    public static long nh = 60 * 60 * 1000;     //一小时
    public static long nm = 60 * 1000;          //一分钟
    public static long ns = 1000;               //一秒

    public static String getDateYMDHMS() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//可以方便地修改日期格式
        return dateFormat.format( now );
    }

    public static String getDateYMDHMS2() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");//可以方便地修改日期格式
        return dateFormat.format( now );
    }

    public static String getDateYMD() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");//可以方便地修改日期格式
        return dateFormat.format( now );
    }

    public static String getDateFormat(String fmt){
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(fmt);//可以方便地修改日期格式
        return dateFormat.format( now );
    }

    public static String formatDatatime(Date data){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//可以方便地修改日期格式
        return dateFormat.format( data );
    }

    //
    /**
     *20180309zhf1409
     * 两个时间之差(传入String类型的时间)
     * @param oldDate 旧时间
     * @param newDate   最近时间
     * @param format  时间格式
     * @throws ParseException
     */
    public static String getTimeDistanceOfString(String oldDate,String newDate,String format) throws ParseException {
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        Date d1 = sdf.parse(oldDate);
        Date d2 = sdf.parse(newDate);
        return   newDataTime.getTimeDistanceOfDate(d1,d2,format);
    }

    /**
     * 20180309zhf1409
     *两个时间之差(传入Date类型的时间)
     * @param oldDate 旧时间
     * @param newDate   最近时间
     * @param format  时间格式
     * @throws ParseException
     */
    public static String getTimeDistanceOfDate(Date oldDate,Date newDate,String format) throws ParseException {
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        oldDate=sdf.parse(sdf.format(oldDate));
        newDate=sdf.parse(sdf.format(newDate));
        long diff = newDate.getTime() - oldDate.getTime();

        String min = String.format("%02d",(diff % nh) / nm);
        String sec = String.format("%02d",((diff % nh )%nm) /ns);
        return diff / nh + ":" + min + ":" +  sec;

    }


    /**
     * 当天的开始时间 00:00:00
     * @return
     */
    //20180228zhf1030
    public static Date startOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date date = calendar.getTime();
        return date;
    }


    /**
     * 当天的结束时间 23:59:59
     */
    //20180228zhf1030
    public static Date endOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date date = calendar.getTime();
        return date;
    }

    //将Date类型时间转化为Integer类型
    //20180228zhf1030
    public static Integer getIntegerByDate(Date date) {
        return Integer.parseInt(String.valueOf(date.getTime()).substring(0, 10));
    }



    public static void main(String[] args) throws ParseException {
        //String format = "yyyy-MM-dd HH:mm:ss";
        /*String oldDate = "2018-03-8 12:28:10";
        String newDate = "2018-03-9 13:58:10";
        System.out.println(newDataTime.getTimeDistanceOfString(oldDate,newDate,format));*/

      /* SimpleDateFormat sdf=new SimpleDateFormat(format);
        Date d1 =sdf.parse("2018-03-9 13:28:10");
        System.out.println(newDataTime.getTimeDistanceOfDate(d1,new Date(),format));*/
        //System.out.println(new Date().getTime());
        System.out.println(newDataTime.startOfToday().toLocaleString());
        System.out.println(newDataTime.getIntegerByDate(newDataTime.startOfToday()));
        System.out.println(newDataTime.endOfToday().toLocaleString());
        System.out.println(newDataTime.getIntegerByDate(newDataTime.endOfToday()));

    }


}
