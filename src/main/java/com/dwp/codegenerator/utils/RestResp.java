package com.dwp.codegenerator.utils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 公共调用响应
 */
@ApiModel
public class RestResp<T> implements Serializable {

    /**
     * 请求成功
     */
    public static final String CODE_SUCCESS = "200";
    /**
     * 失败
     */
    public static final String CODE_FAIL_B = "9999";
    /**
     * 没有访问权限
     */
    public static final String CODE_FAIL_A = "10000";
    /**
     * token认证失败
     */
    public static final String CODE_FAIL_T = "10001";
    /**
     * 参数格式不正确
     */
    public static final String CODE_FAIL_P = "10002";
    /**
     * 请求频次超过限额
     */
    public static final String CODE_FAIL_O = "10003";
    /**
     * 用户未注册
     */
    public static final String CODE_FAIL_N = "10004";
    /**
     * 业务数据
     */
    @ApiModelProperty(value="业务数据,json对象")
    @Getter
    @Setter
    private T data;

    /**
     * 状态码
     */
    @ApiModelProperty(value="状态码：200:成功；9999:服务调用失败；10001:token认证失败；10002：参数错误；10004:用户未注册 ")
    @Getter
    @Setter
    private String code;

    /**
     * 消息
     */
    @ApiModelProperty(value="执行结果")
    @Getter
    @Setter
    private String msg;

    /**
     * 详细信息
     */
    @ApiModelProperty(value="详细信息")
    @Getter
    @Setter
    private String details;


    /**
     * 通用服务调用成功
     * @param data
     * @return
     */
    public static RestResp successRestResp(Object data){
        RestResp restResp=new RestResp();
        restResp.setData(data);
        restResp.setCode(CODE_SUCCESS);
        restResp.setMsg("服务调用成功");
        restResp.setDetails("");
        return restResp;
    }

    /**
     * 通用服务调用失败
     * @param details
     * @return
     */
    public static RestResp failureRestResp(String details){
        RestResp restResp=new RestResp();
        restResp.setData("");
        restResp.setCode(CODE_FAIL_B);
        restResp.setMsg("服务调用失败");
        restResp.setDetails(details);
        return restResp;
    }

    /**
     * 自定义服务调用返回结果
     * @param code  200:成功；9999:服务调用失败；10001:token认证失败；10002：参数错误；10004:用户未注册
     * @param msg 执行结果信息
     * @param details 详细信息
     * @return
     */
    public static RestResp customRestResp(String code,String msg,String details){
        RestResp restResp=new RestResp();
        restResp.setCode(code);
        restResp.setMsg(msg);
        restResp.setDetails(details);
        return restResp;
    }
}
