package com.dwp.codegenerator.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 数据连接VO实体对象
 * @author: DengWeiPing
 * @time: 2020/6/10 9:33
 */
@Data
public class DataConnectionVO implements Serializable {

    private static final long serialVersionUID = -7578401966328599779L;

    private String id;

    private String name; // 数据连接名称

    private String type; // 数据库类型

    private String version;//数据库版本

    private String description; // 数据连接描述

    private String ip; //ip地址

    private String port;//端口号

    private String database;//数据库名称

    private String schema;//数据库模式

    private String username;//用户名

    private String password;//密码

    private String url;//数据库连接串

}
