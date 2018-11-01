package cn.wizzer.app.web.modules.controllers.platform.logistics;

import cn.wizzer.app.base.modules.models.base_cnctobj;
import cn.wizzer.app.base.modules.models.base_customer;
import cn.wizzer.app.base.modules.models.base_person;
import cn.wizzer.app.base.modules.models.base_personpool;
import cn.wizzer.app.base.modules.models.base_place;
import cn.wizzer.app.base.modules.services.BaseCnctobjService;
import cn.wizzer.app.base.modules.services.BaseCustomerService;
import cn.wizzer.app.base.modules.services.BasePersonService;
import cn.wizzer.app.base.modules.services.BasePersonpoolService;
import cn.wizzer.app.base.modules.services.BasePlaceService;
import cn.wizzer.app.logistics.modules.models.*;
import cn.wizzer.app.logistics.modules.services.*;
import cn.wizzer.app.sys.modules.models.Sys_dict;
import cn.wizzer.app.sys.modules.models.Sys_user;
import cn.wizzer.app.sys.modules.services.SysDictService;
import cn.wizzer.app.web.commons.base.Globals;
import cn.wizzer.app.web.modules.controllers.open.api.Datatime.newDataTime;
import cn.wizzer.framework.base.Result;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import cn.wizzer.framework.util.DateUtil;
import cn.wizzer.framework.util.StringUtil;
import cn.wizzer.app.logistics.modules.models.logistics_order;
import cn.wizzer.app.logistics.modules.services.LogisticsEntourageService;
import net.sourceforge.jtds.jdbc.DateTime;
import org.apache.commons.lang.enums.Enum;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.nutz.dao.Cnd;
import org.nutz.dao.util.cri.Static;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.impl.AdaptorErrorContext;
import org.nutz.mvc.upload.UploadAdaptor;
import sun.util.calendar.BaseCalendar;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@IocBean
@At("/platform/logistics/order")
public class LogisticsOrderController{
    private static final Log log = Logs.get();
    @Inject
    private LogisticsOrderService logisticsOrderService;
    @Inject
    private BaseCustomerService baseCustomerService;
    @Inject
    private BasePersonService basePersonService;
    @Inject
    private BaseCnctobjService baseCnctobjService;
    @Inject
    private LogisticsOrderdeliveryService logisticsOrderdeliveryService;
    @Inject
    private SysDictService sysDictService;
    @Inject
    private BasePlaceService basePlaceService;
    @Inject
    private LogisticsOrderstepService logisticsOrderstepService;
    @Inject
    private LogisticsDeliveryorderentryService logisticsDeliveryorderentryService;
    @Inject
    private LogisticsDeliveryorderService logisticsDeliveryorderService;
    @Inject
    private LogisticsOrderentryService logisticsOrderentryService;
    @Inject
    private BasePersonpoolService basePersonpoolService;
    @Inject
    private LogisticsEntourageService logisticsEntourageService;

    private String linkname ="customer|dict_btype|dict_emergency|place_startstock|place_endstock|dict_transporttype|dict_otype|logisticsDeliveryorder";


    @At("")
    @Ok("beetl:/platform/logistics/order/index.html")
    @RequiresPermissions("platform.logistics.order")
    public void index() {
    }

    @At("/jsindex")
    @Ok("beetl:/platform/logistics/order/jsindex.html")
    @RequiresPermissions("platform.logistics.order")
    public void jsindex() {
    }



    @At("/data")
    @Ok("json")
    @RequiresPermissions("platform.logistics.order")
    public Object data(@Param("otype") String otype,@Param("selectForm") String selectForm,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
		Cnd cnd = Cnd.NEW();
        cnd=baseCnctobjService.airportOrderPermission(cnd);
        logistics_select select =Json.fromJson(logistics_select.class,selectForm);
        if (!Strings.isBlank(select.getOrdernum()))
            cnd.and("ordernum", "like", "%" + select.getOrdernum() + "%");
        if (!Strings.isBlank(select.getStartstock()))
            cnd.and("startstock","=",select.getStartstock());
        if (!Strings.isBlank(select.getEndstock()))
            cnd.and("endstock","=",select.getEndstock());
        if (!Strings.isBlank(select.getCustomerid()))
            cnd.and("customerid","=",select.getCustomerid());
        if (!Strings.isBlank(select.getBtypename()))
            cnd.and("btype","=",select.getBtypename());
        if (!Strings.isBlank(select.getEmergencyname()))
            cnd.and("emergency","=",select.getEmergencyname());
        if(!Strings.isBlank(otype))
            cnd.and("otype","=",otype);
        cnd.desc("intime");
        return logisticsOrderService.data(length, start, draw, order, columns, cnd, "customer|dict_btype|dict_emergency|place_startstock|place_endstock");
    }


    @At("/stock")
    @Ok("beetl:/platform/logistics/order/stockindex.html")
    @RequiresPermissions("platform.logistics.stockorder")
    public void stockindexindex() {

    }

    @At("/delivery")
    @Ok("beetl:/platform/logistics/order/deliveryindex.html")
    @RequiresPermissions("platform.logistics.deliverydata")
    public void delivery(HttpServletRequest req) {
        req.setAttribute("OrderCountRef", Globals.OrderCountRef);
    }
    //
    @At("/deliverydata")
    @Ok("json")
    @RequiresPermissions("platform.logistics.deliverydata")
    public Object deliverydata(@Param("status") String status,@Param("selectForm") String selectForm,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns){
        Cnd cnd = Cnd.NEW();

         return logisticsOrderService.data(length, start, draw, order, columns, cnd, "customer|dict_btype|dict_emergency|place_startstock|place_endstock|dict_hctype");
    }
//仓库接单
    @At("/stockdata")
    @Ok("json")
    @RequiresPermissions("platform.logistics.stockorder")
    public Object stockdata(@Param("selectForm") String selectForm,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        Cnd cnd = Cnd.NEW();
        //获取登录人的地点信息

        cnd=baseCnctobjService.airportOrderPermission(cnd);
        logistics_select select =Json.fromJson(logistics_select.class,selectForm);
        if (!Strings.isBlank(select.getOrdernum()))
            cnd.and("ordernum", "like", "%" + select.getOrdernum() + "%");

            cnd.and("pstatus","in",("3,4"));
        // List<logistics_order> List = logisticsOrderService.getdata(cnd,"customer|dict_btype|dict_emergency|place_startstock|place_endstock");

        return logisticsOrderService.data(length, start, draw, order, columns, cnd, linkname);
    }


    //将派单模式改为抢单 cw20170909
    @At("/mdata")
    @Ok("json")
    public Object mdata(@Param("personid") String personid,@Param("pstatus") String pstatus,@Param("numorder") String numorder,@Param("startstock") String startstock,@Param("endstock") String endstock,@Param("btype") String btype,@Param("deliveryorderid") String deliveryorderid, @Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        Cnd cnd = Cnd.NEW();

        try {

            if (!Strings.isBlank(pstatus))
                cnd.and("pstatus", "=", pstatus);
            if (!Strings.isBlank(numorder))
                cnd.and("ordernum", "like", "%"+numorder+"%");
            if (!Strings.isBlank(startstock))
                cnd.and("startstock", "=", startstock);
            if (!Strings.isBlank(endstock))
                cnd.and("endstock", "=", endstock);
            if(!Strings.isBlank(btype))
                cnd.and("btype","=",btype);
            if(!Strings.isBlank(deliveryorderid))
                cnd.and("deliveryorderid","=",deliveryorderid);


            List<logistics_order> logisticsOrders =logisticsOrderService.query(cnd,linkname);
            if(Strings.isBlank(deliveryorderid)){
                List<logistics_order> ltemp=new ArrayList<>();
                for(logistics_order logisticsOrder:logisticsOrders){
                    if(logisticsOrder.getLogisticsDeliveryorder()!=null){
                        if(logisticsOrder.getLogisticsDeliveryorder().getPstatus()!=0)
                            ltemp.add(logisticsOrder);
                    }
                    else{
                        ltemp.add(logisticsOrder);
                    }
                    List<logistics_orderentry> logisticsOrderentrys = logisticsOrderentryService.query(cnd.where("orderid","=",logisticsOrder.getId()));
                    logisticsOrder.setLogistics_orderentry(logisticsOrderentrys);
                }
                for(logistics_order l :ltemp){
                    logisticsOrders.remove(l);
                }
            }


            return logisticsOrders;
        }
        catch(Exception e){
            e.printStackTrace();
            return  null;
        }
    }

    //将派单模式改为抢单模式 cw20170909
    @At("/getOrderCountByMobile")
    @Ok("jsonp:full")
    public Object getOrderCountByMobile(@Param("personid") String personid,@Param("pstatus") String pstatus){
        try{
        Cnd cnd=Cnd.NEW();
        if(!Strings.isBlank(pstatus)){
            cnd.and("pstatus","=",pstatus);
        }


        List<logistics_order> logisticsOrders =logisticsOrderService.query(cnd,"logisticsDeliveryorder");
        List<logistics_order> ltemp=new ArrayList<>();
        for(logistics_order logisticsOrder:logisticsOrders){
            if(logisticsOrder.getLogisticsDeliveryorder()!=null){
                if(logisticsOrder.getLogisticsDeliveryorder().getPstatus()!=0)
                    ltemp.add(logisticsOrder);
            }
            else
            {
                ltemp.add(logisticsOrder);
            }
        }
        for(logistics_order l :ltemp){
            logisticsOrders.remove(l);
        }
        return logisticsOrders.size();
        }
        catch(Exception e){
            return null;
        }
    }

    @At("/getOrderCountByDateByMobile")
    @Ok("jsonp:full")
    public Object getOrderCountByDateByMobile(@Param("pstatus") String pstatus,@Param("personid") String personid,@Param("curdate") String curdate ){
        try {
            Cnd cnd = Cnd.NEW();
            switch (curdate) {
                case "today":
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String dateNowStr = sdf.format(date);
                    cnd = Cnd.NEW().where(new Static("Date(cptime)='" + dateNowStr+"'"));

                    break;
                case "week":
                    break;
                case "month":
                    Calendar c = Calendar.getInstance();
                    int month = c.get(Calendar.MONTH) + 1;
                    int year = c.get(Calendar.YEAR);

                    cnd = Cnd.NEW().where(new Static("month(cptime)=" + month + " and year(cptime)=" + year));


                    //return logisticsOrderService.count(cnd);

                    break;
                case "quarter":
                    break;
                case "year":
                    break;
                case "overtime":
                    String currentDate=newDataTime.getDateYMDHMS();
                    cnd.and("timerequest","<",currentDate);
                    break;
            }

            if (!Strings.isBlank(pstatus))
                cnd.and("pstatus", "=", pstatus);

            List<logistics_order> logisticsOrders = logisticsOrderService.query(cnd);
            List<logistics_order> ltemp=new ArrayList<>();
            for (logistics_order logisticsOrder : logisticsOrders) {
                if (!Strings.isBlank(logisticsOrder.getDeliveryorderid())) {
                    logisticsOrder.setLogisticsDeliveryorder(logisticsDeliveryorderService.fetch(logisticsOrder.getDeliveryorderid()));
                    if (!Strings.isBlank(personid) && !personid.equals(logisticsOrder.getLogisticsDeliveryorder().getPersonid()))
                        ltemp.add(logisticsOrder);
                }
                else{
                    ltemp.add(logisticsOrder);
                }
            }
            for(logistics_order l :ltemp){
                logisticsOrders.remove(l);
            }

            return logisticsOrders.size();
        }
        catch(Exception e){
            return null;
        }
    }




    @At("/add")
    @Ok("beetl:/platform/logistics/order/add.html")
    @RequiresPermissions("platform.logistics.order")
    public void add() {

    }
    @At("/jshbadd")
    @Ok("beetl:/platform/logistics/order/jshbadd.html")
    @RequiresPermissions("platform.logistics.order")
    public void jshbadd() {

    }

    @At("/addDo")
    @Ok("json")
    @RequiresPermissions("platform.logistics.order.add")
    @SLog(tag = "新增", msg = "新增需求订单")
    public Object addDo( HttpServletRequest req,@Param("logisticsOrderentry")String logisticsOrderentry,@Param("logisticsOrder")String logisticsOrder) {
		try {
            String num="";
           // newDataTime newDataTime= new newDataTime();
            logistics_order logistics_Order=Json.fromJson(logistics_order.class,logisticsOrder);
            String newtimerequest=logistics_Order.getTimerequest();
            String newintime = logistics_Order.getIntime();
            String estimatetime=logistics_Order.getEstimatetime();
            newtimerequest=newtimerequest.replace('+',' ');
            newintime=newintime.replace('+',' ');
            estimatetime=estimatetime.replace('+',' ');
            logistics_Order.setTimerequest(newtimerequest);
            logistics_Order.setIntime(newintime);
            logistics_Order.setEstimatetime(estimatetime);
            List<logistics_orderentry> logistics_orderentry= Json.fromJsonAsList(logistics_orderentry.class, logisticsOrderentry);
            logistics_Order.setLogistics_orderentry(logistics_orderentry);
            //自动生成订单编号
            String customerid = logistics_Order.getCustomerid();
            base_customer base_customer=  baseCustomerService.fetch(customerid);

            String intime = logistics_Order.getIntime();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = formatter.parse(intime);
            intime=formatter.format(date);

            Cnd cnd = Cnd.NEW();
            cnd.and("DATE_FORMAT( intime, '%Y-%m-%d')","=",intime);
            cnd.and("customerid","=",customerid);

            logistics_order order= logisticsOrderService.fetch(cnd);

            List<logistics_order> logisticsOrders=logisticsOrderService.query(cnd);
            ArrayList list = new ArrayList();
            if(logisticsOrders.size() == 0){
                num=base_customer.getCusnum()+"-"+intime+"-0001";
            }else{
                for(logistics_order lo:logisticsOrders){
                    String _num=lo.getOrdernum().substring(lo.getOrdernum().lastIndexOf("-")+1,lo.getOrdernum().length());
                    String newStr = _num.replaceFirst("^0*", "");
                    list.add(newStr);
                }
                Collections.sort(list);
                int n =Integer.parseInt(list.get(list.size()-1).toString());
                num=String.format("%04d", n+1);
                num=base_customer.getCusnum()+"-"+intime+"-"+num;
            }

//            if(order == null){
//                num=base_customer.getCusnum()+"-"+intime+"-0001";
//            }else{
//                List<logistics_order> lorder=logisticsOrderService.query("select max(right(ordernum,4)) from logistics_order");
//                if(lorder.size()==0){
//                    if(base_customer.getCusnum() == null){
//                        num="QTXQ-"+intime+"-0001";
//                    }else{
//                        num=base_customer.getCusnum()+"-"+intime+"-0001";
//                    }
//
//                }else{
//                    String ordernum = lorder.get(0).getOrdernum();
//                    int _num= (Integer.parseInt(ordernum.substring(ordernum.length()- 4))+1);
//                    num=String.format("%04d", _num);
//                    num=base_customer.getCusnum()+"-"+intime+"-"+num;
//
//                }
//            }
            logistics_Order.setOrdernum(num);
            logisticsOrderService.save(logistics_Order,"logistics_orderentry");
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }



    @At("/edit/?")
    @Ok("beetl:/platform/logistics/order/edit.html")
    @RequiresPermissions("platform.logistics.order")
    public void edit(String id,HttpServletRequest req) {
        logistics_order order= logisticsOrderService.getLogistics_order(id);
		req.setAttribute("obj", order);
    }

    @At("/jsedit/?")
    @Ok("beetl:/platform/logistics/order/jsedit.html")
    @RequiresPermissions("platform.logistics.order")
    public void jsedit(String id,HttpServletRequest req) {
        logistics_order order= logisticsOrderService.getLogistics_order(id);
        req.setAttribute("obj", order);
    }

    @At("/editDo")
    @Ok("json")
    @RequiresPermissions("platform.logistics.order.edit")
    @SLog(tag = "配送订单", msg = "修改订单信息")
    public Object editDo(HttpServletRequest req,@Param("logisticsOrderentry")String logisticsOrderentry,@Param("logisticsOrder")String logisticsOrder) {
		try {
            logistics_order logistics_Order=Json.fromJson(logistics_order.class,logisticsOrder);
            List<logistics_orderentry> logistics_orderentry= Json.fromJsonAsList(logistics_orderentry.class, logisticsOrderentry);
            logistics_Order.setLogistics_orderentry(logistics_orderentry);
            String newtimerequest=logistics_Order.getTimerequest();
            String newintime = logistics_Order.getIntime();
            String estimatetime=logistics_Order.getEstimatetime();
            String newhbtime=logistics_Order.getHbtime();
            if(!Strings.isBlank(newtimerequest))
            newtimerequest=newtimerequest.replace('+',' ');
            if(!Strings.isBlank(newintime))
            newintime=newintime.replace('+',' ');
            if(!Strings.isBlank(estimatetime))
            estimatetime=estimatetime.replace('+',' ');
            if(!Strings.isBlank(newhbtime))
            newhbtime=newhbtime.replace('+',' ');
            logistics_Order.setTimerequest(newtimerequest);
            logistics_Order.setIntime(newintime);
            logistics_Order.setEstimatetime(estimatetime);
            logistics_Order.setHbtime(newhbtime);
            logistics_Order.setOpBy(StringUtil.getUid());
            logistics_Order.setOpAt((int) (System.currentTimeMillis() / 1000));

            //首先更新订单主表
            logisticsOrderService.updateIgnoreNull(logistics_Order);
            //再更新订单分录表
            for(logistics_orderentry orderentry:logistics_orderentry){
                if(Strings.isBlank(orderentry.getId())){
                    logisticsOrderentryService.dao().insert(orderentry);
                }else {
                    logisticsOrderService.dao().updateWith(logistics_Order,"logistics_orderentry");
                }
            }
			return Result.success("system.success");
		} catch (Exception e) {
			return Result.error("system.error",e);
		}
    }

    @At("/submitOrderDo")
    @Ok("json")
    @SLog(tag = "配送需求", msg = "提交订单")
    public Object submitOrderDo(@Param("id")String id,@Param("status")Integer status, HttpServletRequest req) {
        try {
            logistics_order logisticsOrder = logisticsOrderService.fetch(id);
            logisticsOrder.setOpBy(StringUtil.getUid());
            logisticsOrder.setOpAt((int) (System.currentTimeMillis() / 1000));
            logisticsOrder.setPstatus(status);
            int i = logisticsOrderService.updateIgnoreNull(logisticsOrder);

            //生成配送单
            if(i==1 && status==1){
                if(logisticsDeliveryorderService.checkorderre(logisticsOrder.getId())){
                    logisticsDeliveryorderService.orderexe(logisticsOrder.getId());
                }
            }

            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At({"/delete/?", "/delete"})
    @Ok("json")
    @RequiresPermissions("platform.logistics.order.delete")
    @SLog(tag = "logistics_order", msg = "${req.getAttribute('id')}")
    public Object delete(String id, @Param("ids")  String[] ids, HttpServletRequest req) {
		try {
			if(ids!=null&&ids.length>0){
				logisticsOrderService.delete(ids);
    			req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
			}else{
				logisticsOrderService.delete(id);
    			req.setAttribute("id", id);
			}
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error",e);
        }
    }

    @At("/detail/?")
    @Ok("beetl:/platform/logistics/order/detail.html")
    @RequiresPermissions("platform.logistics.order")
	public Object detail(String id, HttpServletRequest req) {
		if (!Strings.isBlank(id)) {
		    logistics_order logisticsOrder=logisticsOrderService.getLogistics_order(id);
		    return logisticsOrder;
		}else{
            return null;
        }
    }
    @At("/jsdetail/?")
    @Ok("beetl:/platform/logistics/order/jsdetail.html")
    @RequiresPermissions("platform.logistics.order")
    public Object jsdetail(String id, HttpServletRequest req) {
        if (!Strings.isBlank(id)) {
            logistics_order logisticsOrder=logisticsOrderService.getLogistics_order(id);
            return logisticsOrder;
        }else{
            return null;
        }
    }
    @At({"/getOrderInfo"})
    @Ok("jsonp:full")
    public logistics_order getOrderInfo(@Param("id")String id) {
        try {

            logistics_order logisticsOrder = logisticsOrderService.getLogistics_order(id);
            return logisticsOrder;
            }catch (Exception e) {
            return null;
        }

    }
    @At({"/updvstock"})
    @Ok("json")
    @RequiresPermissions("platform.logistics.order")
    public Object updvstock(@Param("id")String id) {
        try {
            if(!Strings.isBlank(id)) {
                logistics_order logisticsOrder = logisticsOrderService.getLogistics_order(id);
                //变更仓库状态为5
                int pstatus=logisticsOrder.getPstatus();
                if(pstatus <=4){
                    logisticsOrder.setPstatus(5);
                }
                logisticsOrderService.updateIgnoreNull(logisticsOrder);
                //变更更新情况
                String dvid =logisticsOrder.getDeliveryorderid();
                if(!Strings.isBlank(dvid)){
                    Cnd cnd=Cnd.NEW();
                    cnd.and("orderid","=",id);
                    cnd.and("deliveryorderid","=",dvid);
                    cnd.and("step","=","3");
                    String now = newDataTime.getDateYMDHMS();
                    logisticsDeliveryorderentryService.upDventry(1,"","",now,cnd);
                }
            }
            return Result.success("system.success");
        }catch (Exception e) {
            return Result.error("system.error",e);
        }

    }
    @At("/orderprint/?")
    @Ok("beetl:/platform/logistics/order/printinfo.html")
    @RequiresPermissions("platform.logistics.order")
    public void orderprint(String id, HttpServletRequest req) {
        try {
            if(!Strings.isBlank(id)){
                logistics_order logisticsOrder=logisticsOrderService.getLogistics_order(id);
                //变更仓库状态为4
                int pstatus=logisticsOrder.getPstatus();
                if(pstatus <= 3){
                    logisticsOrder.setPstatus(4);
                }
                logisticsOrderService.updateIgnoreNull(logisticsOrder);
                //变更更新情况
                String dvid =logisticsOrder.getDeliveryorderid();
                if(!Strings.isBlank(dvid)){
                    Cnd cnd=Cnd.NEW();
                    cnd.and("orderid","=",id);
                    cnd.and("deliveryorderid","=",dvid);
                    cnd.and("step","=","2");
                    String now = newDataTime.getDateYMDHMS();
                    logisticsDeliveryorderentryService.upDventry(1,"","",now,cnd);
                }

                req.setAttribute("orderprint",logisticsOrder);
            }else{
                req.setAttribute("orderprint",null);
            }
        } catch (Exception e) {
            req.setAttribute("orderprint",null);
        }
    }

    private int getStep(String otype,String stepnum){
        Cnd c=Cnd.NEW();
        c.and("otype","=",otype);
        c.and("stepnum","=",stepnum);
        logistics_orderstep orderstep=logisticsOrderstepService.fetch(c);
        int step=0;
        if(orderstep != null){

            step=orderstep.getStep();
        }
        return step;
    }


    @At("/updateDvSteps")
    @Ok("jsonp:full")
    public Object updateDvSteps(@Param("orderid") String orderid,@Param("content") String content,@Param("stepnum") String stepnum,@Param("toreason") String toreason,@Param("pid") String personid) {
        /************
         * 1. 通过订单ID orderid 查询到订单类型otype
         * 2. 通过otype和stepnum从orderstep表中得到
         * 3. logisticsDeliveryorderentryService.upDventry修改配送状态，核料、送达、完成
         * 4. 如果是在时限内的送达，则直接完成。
         * 5. 如果是送达和完成状态，则反过来再需要修改订单、配送单的状态
         * 6. 通过订单ID找到订单，修改订单状态
         * 7. 通过订单ID找到对应的配送单ID
         * 8. 通过配送单ID找到配送单下所有的订单
         * 9. 循环订单得到最小的pstatus值
         * 10. 将pstatus值给配送单
         * 11. 如果是完成状态，则查找当前配送员下是否还存在未完成的配送单
         * 12. 如果配送单都已经完成，则修改当前配送员状态为空闲
         ************/
        int rslt = 0;
        String deliveryid=null;
        Cnd cnd = Cnd.NEW();
        cnd.and("id", "=", orderid);
        logistics_order order = logisticsOrderService.fetch(cnd);
        String otype = order.getOtype();
//        if(order.isOtype()==false){
//            otype=0;
//        }

//        Cnd cnd1 = Cnd.NEW();
//        cnd1.and("stepname", "=", step);
//        cnd1.and("otype", "=", otype);
//        logistics_orderstep orderstep = logisticsOrderstepService.fetch(cnd1);

        //通过otypeid找到logistics_orderstep步骤
        //前端订单类型修改完毕后，传入step值再修改

        int step=getStep(otype,stepnum);

        if (order != null) {
            Cnd cnd3 = Cnd.NEW();
            cnd3.and("orderid", "=", orderid);
            deliveryid = order.getDeliveryorderid();
            cnd3.and("deliveryorderid", "=", deliveryid);


            String now = newDataTime.getDateYMDHMS();
            if(stepnum.equals("SD")&&!Strings.isBlank(order.getTimerequest()))//送达
            {


                Date timeRequest = new Date();
                Date nowDate= new Date();
                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    timeRequest = sdf.parse(order.getTimerequest());

                }catch(Exception e){
                    String request=order.getTimerequest()+":00";
                    try{
                        timeRequest=sdf.parse(request);
                    }catch (Exception ef){
                        ef.printStackTrace();
                    }

                }
                try{
                    nowDate=sdf.parse(now);
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                if(nowDate.getTime()<timeRequest.getTime()){//在超时之前
                    stepnum="WC";
                    step=getStep(otype,stepnum);
                }
            }
            cnd3.and("step", "=", step);//0908cw修改，如果是按时送达，则直接完成
            rslt = logisticsDeliveryorderentryService.upDventry(1, content, toreason, now, cnd3);
            //如果是送达状态，则需要修改订单状态为送达

                //1修改订单状态为送达
                //2检查订单所在配送单的所有单据状态
                //3将配送单状态改为最小状态，依次为配送中-送达未完成-已完成
                if(stepnum.equals("HL")){
                    order.setPstatus(6);
                }
                else if (stepnum.equals("SD")){
                    order.setPstatus(7);}
                else if(stepnum.equals("WC")){
                    order.setPstatus(8);
                    order.setCptime(newDataTime.getDateYMDHMS());
                }
                rslt += logisticsOrderService.updateIgnoreNull(order);
                Cnd cnd2 = Cnd.NEW();
                cnd2.and("deliveryorderid", "=", deliveryid);
                List<logistics_orderdelivery> orderdeliveryList = logisticsOrderdeliveryService.query(cnd2);
                if (orderdeliveryList.size() > 1) {
                    //说明存在并单情况
                    int status = 0;
                    for (int i = 0; i < orderdeliveryList.size(); i++) {
                        logistics_order logisticsOrder = logisticsOrderService.fetch(orderdeliveryList.get(i).getOrderid());
                        if (status > logisticsOrder.getPstatus()) {
                            status = logisticsOrder.getPstatus();
                        }
                    }
                    logistics_Deliveryorder deliveryorder = logisticsDeliveryorderService.fetch(deliveryid);
                    if (status > 3 && status < 7) {
                        deliveryorder.setPstatus(1);
                        //配送中
                    } else if (status == 7) {
                        //送达
                        deliveryorder.setPstatus(2);
                    } else if (status == 8) {
                        //完成
                        deliveryorder.setPstatus(3);
                        deliveryorder.setCptime(newDataTime.getDateYMDHMS());
                    } else {
                        //错误
                    }
                    rslt += logisticsDeliveryorderService.updateIgnoreNull(deliveryorder);
                }else{
                    logistics_Deliveryorder deliveryorder = logisticsDeliveryorderService.fetch(deliveryid);
                    if(stepnum.equals("SD")){
                        //送达
                        deliveryorder.setPstatus(2);
                    }
                    else if(stepnum.equals("WC")){
                        //完成
                        deliveryorder.setPstatus(3);
                        deliveryorder.setCptime(newDataTime.getDateYMDHMS());
                    }else if(stepnum.equals("HL")){
                        //配送中
                        deliveryorder.setPstatus(1);
//                        deliveryorder.setCptime(newDataTime.getDateYMDHMS());
                    }else{
                        //错误
                    }
                    rslt += logisticsDeliveryorderService.updateIgnoreNull(deliveryorder);
                }

        }

        if(stepnum.equals("WC")){

            if(!Strings.isBlank(personid)){
                Cnd cnd4=Cnd.NEW();
                cnd4.and("personid","=",personid);
                cnd4.and("pstatus","<>","3");
                List<logistics_Deliveryorder> deliveryorderList=logisticsDeliveryorderService.query(cnd4);
                if(deliveryorderList.size()==0){//说明该配送员所有运单都已经完成，则该配送员空闲
                    Cnd cnd5=Cnd.NEW();
                    cnd5.and("personid","=",personid);
                    base_personpool personpool=basePersonpoolService.fetch(cnd5);
                    personpool.setPersonstatus(0);
                    basePersonpoolService.updateIgnoreNull(personpool);
                    //随行人员也要空闲
                    Cnd cnd6=Cnd.NEW();
                    cnd6.and("orderid","=",orderid);
                    List<logistics_entourage> entourageList=logisticsEntourageService.query(cnd);
                    for(int i=0;i<entourageList.size();i++){
                        String enpid=entourageList.get(i).getPersonid();
                        Cnd cnd7=Cnd.NEW();
                        cnd7.and("personid","=",enpid);
                        personpool=basePersonpoolService.fetch(cnd7);
                        personpool.setPersonstatus(0);
                        basePersonpoolService.updateIgnoreNull(personpool);
                    }
                }

            }
        }
        return rslt;
    }

    //获取正在配送的订单信息
    @At("/getdvorderList")
    @Ok("json")
    @RequiresPermissions("platform.logistics.order")
    public List<logistics_order> getdvorderList(@Param("pstatus")String[] pstatus,@Param("deliveryorderid")String deliveryorderid){
        Cnd cnd =Cnd.NEW();
        cnd=baseCnctobjService.airportOrderPermission(cnd);
        if(pstatus != null)
            cnd.and("pstatus","in",pstatus);
        if(!Strings.isBlank(deliveryorderid))
            cnd.and("deliveryorderid","=",deliveryorderid);
        List<logistics_order> logisticsOrders= logisticsOrderService.getdata(cnd,linkname);
        for(logistics_order logisticsOrder:logisticsOrders){
            if(!Strings.isBlank(logisticsOrder.getDeliveryorderid())) {
                logistics_Deliveryorder logisticsDeliveryorder = logisticsDeliveryorderService.fetch(logisticsOrder.getDeliveryorderid());
                if(!Strings.isBlank(logisticsDeliveryorder.getPersonid())) {
                    base_person person = basePersonService.fetch(logisticsDeliveryorder.getPersonid());
                    logisticsDeliveryorder.setPerson(person);
                    logisticsOrder.setLogisticsDeliveryorder(logisticsDeliveryorder);
                }
            }
        }

        return logisticsOrders;
    }

    //获取已完成的订单信息
    @At("/getdvcomp")
    @Ok("json")
    @RequiresPermissions("platform.logistics.order")
    public Object getdvcomp(@Param("selectForm") String selectForm,@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns){
        Cnd cnd = Cnd.NEW();
        logistics_select select =Json.fromJson(logistics_select.class,selectForm);
        cnd.and("pstatus","in","7,8");
        if (!Strings.isBlank(select.getOrdernum()))
            cnd.and("ordernum", "like", "%" + select.getOrdernum() + "%");


        // List<logistics_order> List = logisticsOrderService.getdata(cnd,"customer|dict_btype|dict_emergency|place_startstock|place_endstock");

        return logisticsOrderService.data(length, start, draw, order, columns, cnd, linkname);

    }
    @At("/getordercount")
    @Ok("json")
    public int getordercount(@Param("pstatus") String[] pstatus){
        Cnd cnd =Cnd.NEW();
        cnd=baseCnctobjService.airportOrderPermission(cnd);
        cnd.and("pstatus","in",pstatus);
        int count = logisticsOrderService.count(cnd);
        return count;
    }

    //改派
    @At("/reassignment")
    @Ok("json")
    @RequiresPermissions("platform.logistics.order")
    public Object reassignment(@Param("orderid") String orderid,@Param("personid") String personid){
        try{
            logistics_order logisticsOrder =logisticsOrderService.fetch(orderid);

            //修改配送单人员
            logistics_Deliveryorder logisticsDeliveryorder=logisticsDeliveryorderService.fetch(logisticsOrder.getDeliveryorderid());
            logisticsDeliveryorder.setPersonid(personid);
            logisticsDeliveryorderService.updateIgnoreNull(logisticsDeliveryorder);

            //修改订单状态
            logisticsOrder.setPstatus(2);
            logisticsOrderService.updateIgnoreNull(logisticsOrder);

            return Result.success("system.success");
        }catch(Exception e){
            return Result.error("system.error",e);
        }



    }




    @At("/img")
    @Ok("jsonp:full")
    //AdaptorErrorContext必须是最后一个参数
    public Object img(@Param("filename") String filename, @Param("base64") String base64,@Param("orderid") String orderid,@Param("step") String step,HttpServletRequest req, AdaptorErrorContext err) {
        byte[] buffer;
        try {
            if (err != null && err.getAdaptorErr() != null) {
                return NutMap.NEW().addv("code", 1).addv("msg", "文件不合法");
            } else if (base64 == null) {
                return Result.error("空文件");
            } else {
                String p = Globals.AppRoot;
                String fn=R.UU32()+filename.substring(filename.lastIndexOf("."));
                String path=Globals.AppUploadPath + "/image/" + DateUtil.format(new Date(), "yyyyMMdd") + "/";
                String f = Globals.AppUploadPath + "/image/" + DateUtil.format(new Date(), "yyyyMMdd") + "/" + fn ;
                File file=new File(p+Globals.AppUploadPath + "/image/" + DateUtil.format(new Date(), "yyyyMMdd"));
                if(!file.exists()){
                    file.mkdirs();
                }
                buffer = Base64.getDecoder().decode(base64.split(",")[1]);
                FileOutputStream out = new FileOutputStream(p + f);
                out.write(buffer);
                out.close();
                /********************
                 * 将上传的文件路径修改到数据库中
                 * 1.先将数据库中的此行得到
                 * 2.得到picpath及picname、oldpicname
                 * 3.将新的合并到这些字段
                 * 4.修改
                 *******************/
                Cnd cnd=Cnd.NEW();
                cnd.and("orderid","=",orderid);
                cnd.and("step","=",step);
                logistics_Deliveryorderentry deliveryorderentry=logisticsDeliveryorderentryService.fetch(cnd);
                if(deliveryorderentry!=null){
//                    String entryID=deliveryorderentry.getId();
                    String picpath=deliveryorderentry.getPicpath();
                    String picname=deliveryorderentry.getPicname();
                    String oldpicname=deliveryorderentry.getOldpicname();
                    if(!Strings.isBlank(picpath)){
                        picpath+=","+path+fn;
                    }else{
                        picpath=path+fn;
                    }
                    if(!Strings.isBlank(picname)){
                        picname+=","+fn;
                    }else{
                        picname=fn;
                    }
                    if(!Strings.isBlank(oldpicname)){
                        oldpicname+=","+filename;
                    }else{
                        oldpicname=filename;
                    }
                    deliveryorderentry.setPicpath(picpath);
                    deliveryorderentry.setPicname(picname);
                    deliveryorderentry.setOldpicname(oldpicname);
                    logisticsDeliveryorderentryService.updateIgnoreNull(deliveryorderentry);

                }

                return Result.success("上传成功", Globals.AppBase+f);
            }
        } catch (Exception e) {
            return Result.error("system.error",e);
        } catch (Throwable e) {
            return Result.error("图片格式错误");
        }
    }

    @At("/delUpload")
    @Ok("jsonp:full")
    public Object delUpload(@Param("filename") String filename,@Param("orderid") String orderid,@Param("step") String step){
        if(!Strings.isBlank(filename)){
            Cnd cnd= Cnd.NEW();
            cnd.and("orderid","=",orderid);
            cnd.and("step","=",step);
            logistics_Deliveryorderentry deliveryorderentry= logisticsDeliveryorderentryService.fetch(cnd);
            if(deliveryorderentry!=null){
                String oldpicname=deliveryorderentry.getOldpicname();
                String picname=deliveryorderentry.getPicname();
                String picpath=deliveryorderentry.getPicpath();
                if(oldpicname.indexOf(",")>-1){
                    String [] op=oldpicname.split(",");
                    List<String> oplist=java.util.Arrays.asList(op);
                    ArrayList opArray = new ArrayList<>(oplist);
                    String [] pn=picname.split(",");
                    List<String> pnlist=java.util.Arrays.asList(pn);
                    ArrayList pnArray = new ArrayList<>(pnlist);
                    String [] pp=picpath.split(",");
                    List<String> pplist=java.util.Arrays.asList(pp);
                    ArrayList ppArray = new ArrayList<>(pplist);
                    int number=-1;
                    for(int i=0;i<opArray.size();i++){
                        if(ppArray.get(i).toString().equals(filename)){
                            number=i;
                            File file=new File(Globals.AppRoot+ppArray.get(i));
                            if(file.exists()){
                                file.delete();
                                break;
                            }
                        }
                    }

                    /**************
                     * 修改字段
                     * picpath,picname,oldpicname
                     */
                    if(number>-1){
                        String newoldpn="";
                        String newpn="";
                        String newpp="";
                        opArray.remove(number);
                        pnArray.remove(number);
                        ppArray.remove(number);
                        newoldpn=org.apache.commons.lang.StringUtils.join(opArray.toArray(),",");
                        newpn=org.apache.commons.lang.StringUtils.join(pnArray.toArray(),",");
                        newpp=org.apache.commons.lang.StringUtils.join(ppArray.toArray(),",");

                        deliveryorderentry.setOldpicname(newoldpn);
                        deliveryorderentry.setPicname(newpn);
                        deliveryorderentry.setPicpath(newpp);
                        logisticsDeliveryorderentryService.updateIgnoreNull(deliveryorderentry);
                        return Result.success("已删除");
                    }
                }
                else{
                    if((picpath).equals(filename)){
                        File file=new File(Globals.AppRoot+picpath);
                        if(file.exists()){
                            file.delete();
                            /**************
                             * 修改字段
                             * picpath,picname,oldpicname
                             */
                            deliveryorderentry.setOldpicname("");
                            deliveryorderentry.setPicname("");
                            deliveryorderentry.setPicpath("");
                            logisticsDeliveryorderentryService.updateIgnoreNull(deliveryorderentry);
                            return Result.success("已删除");
                        }
                    }
                }



            }
        }
        return Result.error("没有此文件");
    }

    @At("/updateVehicle")
    @Ok("jsonp:full")
    public Object updateVehicle(@Param("vehicleid") String vehicleid,@Param("orderid") String orderid){
        try{
            if(!Strings.isBlank(vehicleid)&&!Strings.isBlank(orderid)){
                logistics_order order=logisticsOrderService.fetch(orderid);
                order.setVehicleid(vehicleid);
                logisticsOrderService.updateIgnoreNull(order);
                return Result.success("更新成功");
            }
            return Result.error("更新失败");
        }
        catch (Exception e){
            return Result.error("更新失败");
        }
    }

    @At("/updateOrderReject")
    @Ok("jsonp:full")
    public Object updateOrderReject(@Param("id") String id){
        try{
            if(!Strings.isBlank(id)){
                logistics_order order=logisticsOrderService.fetch(id);
                order.setPstatus(99);
                logisticsOrderService.updateIgnoreNull(order);
                return Result.success("更新成功");
            }
            return Result.error("更新失败");
        }
        catch (Exception e){
            return Result.error("更新失败");
        }
    }
}
