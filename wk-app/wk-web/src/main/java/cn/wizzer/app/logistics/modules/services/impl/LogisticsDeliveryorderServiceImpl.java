package cn.wizzer.app.logistics.modules.services.impl;

import cn.wizzer.app.base.modules.models.base_personpool;
import cn.wizzer.app.base.modules.models.base_place;
import cn.wizzer.app.base.modules.services.BaseCustomerService;
import cn.wizzer.app.base.modules.services.BasePersonpoolService;
import cn.wizzer.app.base.modules.services.BasePlaceService;
import cn.wizzer.app.logistics.modules.models.logistics_Deliveryorderentry;
import cn.wizzer.app.logistics.modules.models.logistics_order;
import cn.wizzer.app.logistics.modules.models.logistics_orderdelivery;
import cn.wizzer.app.logistics.modules.services.*;
import cn.wizzer.app.sys.modules.services.SysDictService;
import cn.wizzer.app.sys.modules.services.SysTaskService;
import cn.wizzer.framework.base.service.BaseServiceImpl;
import cn.wizzer.app.logistics.modules.models.logistics_Deliveryorder;
import cn.wizzer.framework.map.MapUtils;
import cn.wizzer.framework.map.ValueComparator;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import java.text.SimpleDateFormat;
import java.util.*;

@IocBean(args = {"refer:dao"})
public class LogisticsDeliveryorderServiceImpl extends BaseServiceImpl<logistics_Deliveryorder> implements LogisticsDeliveryorderService {
    private static final Log log = Logs.get();
    @Inject
    private SysTaskService sysTaskService;

    @Inject
    private LogisticsDeliveryorderentryService logisticsDeliveryorderentryService;

    @Inject
    private LogisticsOrderService logisticsOrderService;

    @Inject
    private LogisticsOrderstepService logisticsOrderstepService;

    @Inject
    private LogisticsOrderdeliveryService logisticsOrderdeliveryService;


    @Inject
    private BaseCustomerService baseCustomerService;

    @Inject
    private BasePlaceService basePlaceService;

    @Inject
    private SysDictService sysDictService;

    @Inject
    private BasePersonpoolService basePersonpoolService;

    public LogisticsDeliveryorderServiceImpl(Dao dao) {
        super(dao);
    }

    public void save(logistics_Deliveryorder logistics_deliveryorder){
        dao().insertWith(logistics_deliveryorder,null);
    }

    public List<logistics_Deliveryorder> getDeliveryOrderByMobile(String pstatus,String personid){
        try{
            SqlExpressionGroup s=null;
            SqlExpressionGroup q=null;
            if (!Strings.isBlank(pstatus))
            {
                String [] stat=pstatus.split(",");
                s= Cnd.exps("pstatus","=",stat[0]);
                for(int i=1;i<stat.length;i++){
                    s.or("pstatus","=",stat[i]);
                }

            }

            if(!Strings.isBlank(personid)){
                q=Cnd.exps("personid","=",personid);
            }
            Cnd cnd=Cnd.where(s).and(q);
            return this.query(cnd);
        }
        catch(Exception e){
            e.printStackTrace();
            return  null;
        }
    }

    public void orderexe(String orderid){
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat formatternow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = formatter.format(new Date());
            String datetime=formatternow.format(new Date());
            Cnd stepcnd=Cnd.NEW();
            Date data=new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateNowStr = sdf.format(data);

            String num="";
            //获取订单信息
            logistics_order order = logisticsOrderService.fetch(orderid);
            logistics_Deliveryorder deliveryorder = new logistics_Deliveryorder();

            stepcnd.and("otype","=",order.getOtype());

            //获取步骤
            List<logistics_Deliveryorderentry> deliveryorderentry= Json.fromJsonAsList(logistics_Deliveryorderentry.class, Json.toJson(logisticsOrderstepService.query(stepcnd)));

            for (int i = 0; i < deliveryorderentry.size(); i++) {
                deliveryorderentry.get(i).setOrderid(orderid);
                //如果步骤为1，将步骤1设置为完成状态，并且设置订单提交时间
                if(deliveryorderentry.get(i).getStep()==1){
                    deliveryorderentry.get(i).setPstatus(1);
                    deliveryorderentry.get(i).setOperatetime(datetime);
                }else{
                    deliveryorderentry.get(i).setPstatus(0);
                }
            }

            //生成配送单编号

            Cnd cnd = Cnd.NEW();
            cnd.and("DATE_FORMAT(datetime, '%Y-%m-%d')","=",date);
            logistics_Deliveryorder delorder=this.fetch(cnd);

            if(delorder == null){
                num="PSD-"+date+"-0001";
            }else{

                List<logistics_Deliveryorder> dorder=this.query("select max(right(deliveryordernum,4)) from logistics_order");
                if(dorder.size()==0) {
                    num = "PSD-" + date + "-0001";

                }else{
                    String ordernum = dorder.get(0).getDeliveryordernum();
                    int _num= (Integer.parseInt(ordernum.substring(ordernum.length()- 4))+1);
                    num=String.format("%04d", _num);
                    num="PSD"+"-"+date+"-"+num;
                }
            }
            //获取订单中的地点经纬度
            base_place basePlace = basePlaceService.fetch(order.getStartstock());
            String positions=basePlace.getPosition();
            List<String> result = Arrays.asList(positions.split(","));
            double lat1=Double.parseDouble(result.get(1));
            double lng1=Double.parseDouble(result.get(0));
            //获取人员的经纬度
            Cnd cp = Cnd.NEW();
            cp.and("pstatus","=",1);
            List<base_personpool> basePersonpools = basePersonpoolService.query(cp);
            HashMap<String, Double> hashmap = new HashMap();
            //取所有空闲人员的地理位置进行比较
            for(int i=0;i<basePersonpools.size();i++){
                String posi = basePersonpools.get(i).getPosition();
                List<String> results = Arrays.asList(posi.split(","));
                double lat2=Double.parseDouble(results.get(1));
                double lng2=Double.parseDouble(results.get(0));
                double distance= MapUtils.GetDistance(lat1,lng1,lat2,lng2);
                hashmap.put(basePersonpools.get(i).getPersonid(),distance);
            }
            //取出距离最近的personid
            ValueComparator bvc = new ValueComparator(hashmap);
            TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);
            sorted_map.putAll(hashmap);
            String persionid = sorted_map.lastKey();
            //生成配送单
            deliveryorder.setDeliveryordernum(num);
            deliveryorder.setDatetime(formatternow.format(new Date()));
            deliveryorder.setPstatus(0);
            //deliveryorder.setPersonid(persionid);
            deliveryorder.setLogistics_deliveryorderentry(deliveryorderentry);

            //生成订单与配送单关联信息
            logistics_orderdelivery lorderdelivery= new logistics_orderdelivery();
            lorderdelivery.setOrderid(orderid);
            List<logistics_orderdelivery> logistics_orderdelivery = new ArrayList();
            logistics_orderdelivery.add(lorderdelivery);
            deliveryorder.setLogistics_orderdelivery(logistics_orderdelivery);

            //检查订单的状态，只有待接订单状态的才能接单
            if(order.getPstatus() == 1){
                this.save(deliveryorder);
                //修改需求订单状态
                order.setPstatus(2);
                //根据订单id获取配送单ID
                Cnd cri = Cnd.NEW();
                cri.and("orderid","=",order.getId());
                logistics_orderdelivery orderdelivery =logisticsOrderdeliveryService.getField("deliveryorderid",cri);
                order.setDeliveryorderid(orderdelivery.getDeliveryorderid());
                logisticsOrderService.update(order);
            }
        } catch (Exception e) {
            log.info("订单接单错误：" + e.getMessage());
        }
    }

    public boolean checkorderre(String orderid){
        Cnd cnd = Cnd.NEW();
        cnd.and("orderid","=",orderid);
        int count =logisticsDeliveryorderentryService.count(cnd);
        if(count == 0){
            return true;
        }else {
            return false;
        }
    }

}
