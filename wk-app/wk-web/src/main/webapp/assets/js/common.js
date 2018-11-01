/**
 * Created by xl on 2017/6/20.
 */
// 下拉框选择
function bindVehicleDDL(name,code,opname,hidname){
    //$.ajaxSetup({ async:false });
    var url="/platform/sys/dict/bindVehicleDDL";
    var param={"name": name,"code":code};
    $.post(url,param,function(d){
        $("#"+opname).append("<option value='' selected = 'selected'>请选择</option>");
        for(var i=0;i<d.length;i++)
        {
            $("#"+opname).append("<option value="+d[i].id+">"+d[i].name+"</option>");
        }
        $("#"+hidname).val($("#"+opname+" option:selected").val());
    });
}
// 下拉框选择
function bindVehicleDDLREL(parentId,opname,hidname){
    //$.ajaxSetup({ async:false });
    var url="/platform/sys/dict/bindVehicleDDLREL";
    var param={"parentid":parentId};
    $.post(url,param,function(d){
        $("#"+opname ).empty();
        $("#"+opname).append("<option value='' selected = 'selected'>请选择</option>");
        for(var i=0;i<d.length;i++)
        {
            $("#"+opname).append("<option value="+d[i].id+">"+d[i].name+"</option>");
        }
        $("#"+hidname).val($("#"+opname+" option:selected").val());
    });
}
// 下拉框选择
function bindVehicleDDL(name,code,opname,hidname,id) {
    //$.ajaxSetup({ async:false });
    var url = "/platform/sys/dict/bindVehicleDDL";
    var param = {"name": name, "code": code};
    $.post(url, param, function (d) {
        $("#" + opname).append("<option value='' selected = 'selected'>请选择</option>");
        for (var i = 0; i < d.length; i++) {
            if (id == d[i].id) {
                $("#" + opname).append("<option value=" + d[i].id + " selected>" + d[i].name + "</option>");
            } else {
                $("#" + opname).append("<option value=" + d[i].id + " >" + d[i].name + "</option>");
            }
        }
        $("#" + hidname).val($("#" + opname + " option:selected").val());
    });
}

$.ajaxSetup({
    contentType: "application/x-www-form-urlencoded; charset=utf-8"
});
var DataDeal = {
//将从form中通过$('#form').serialize()获取的值转成json
    formToJson: function (data) {
        data=data.replace(/&/g,"\",\"");
        data=data.replace(/=/g,"\":\"");
        data="{\""+data+"\"}";
        return data;
    },
};
$.fn.tableinputtoJson=function(){
    var dataJson=[];
    $(this).find('tr:not(:first)').each(function () {
        var o = {};
        $(this).find("input").each(function(){
            o[this.name] = this.value;
        });
        if(!$.isEmptyObject(o)){
            dataJson.push(o);
        }
    });
    return dataJson;
};

function getDisStstus(data) {
    var status="";
    if(data=="0"){
        status ="保存";
    }else if(data=="1"){
        status ="订单提交，等待派单";
    }else if(data=="2"){
        status ="已派单，待配送员接单";
    }else if(data=="3"){
        status ="配送员已接单";
    }else if(data=="4"){
        status ="仓库备料中";
    }else if(data=="5"){
        status ="仓库备料完成";
    }else if(data=="6"){
        status ="配送中";
    }else if(data=="7"){
        status ="送达";
    }else if(data=="8"){
        status ="订单完成";
    }
    return status;
}
function geteqStatus(data) {
    var status="";
    if(data=="0"){
        status ="关锁";
    }else if(data=="1"){
        status ="开锁";
    }
    return status;
}

function getuseStatus(data) {
    var status="";
    if(data=="0"){
        status ="正常";
    }else if(data=="1"){
        status ="即将超时";
    }else if(data=="2"){
        status ="已超时";
    }else if(data=="3"){
        status ="故障";
    }
    return status;
}

function getDeliveryStstus(data) {
    var status="";
    if(data=="0"){
        status ="待接单";
    }else if(data=="1"){
        status ="配送中";
    }else if(data=="2"){
        status ="送达未完成";
    }else if(data=="3"){
        status ="已完成";
    }
    return status;
}
function getUseStstus(data){
    if(data == 0){
        return "未使用";
    }else if(data == 1){
        return "使用中";
    }else{
        return "";
    }
}
function getUseErrorStatus(data){
    if(data == 0){
        return "正常";
    }else if(data == 1){
        return "告警";
    }else if (data == 2) {
        return "超时借用";
    }else if(data == 3){
        return "错误异常";
    }else if(data == 4){
        return "设备信号失联";
    }else{
        return "";
    }
}
function getBizzStatus(data){
    if(data == 0){
        return "正常使用中";
    }else if(data == 1){
        return "报修中";
    }else if (data == 2){
        return "维修中";
    }else{
        return "";
    }
}
function getFailStatus(data){
    if(data == 0){
        return "无";
    }else if(data == 1){
        return "开锁失败";
    }else if (data == 2){
        return "关锁失败";
    }else{
        return "";
    }
}
$('body').on('hidden.bs.modal', '.modal', function () {
    $(this).removeData('bs.modal');
});
