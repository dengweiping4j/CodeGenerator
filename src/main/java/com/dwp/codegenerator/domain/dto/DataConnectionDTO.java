package com.dwp.codegenerator.domain.dto;

import com.dwp.codegenerator.domain.DataConnection;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: DengWeiPing
 * @time: 2020/6/10 9:28
 */
@ApiModel(value = "DataConnectionDTO", description = "DataConnectionDTO")
@Data
public class DataConnectionDTO {

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "数据连接名称")
    private String name;

    @ApiModelProperty(value = "数据库类型")
    private String type;

    @ApiModelProperty(value = "数据库版本")
    private String version;

    @ApiModelProperty(value = "数据连接描述")
    private String description;

    @ApiModelProperty(value = "数据连接IP")
    private String ip;

    @ApiModelProperty(value = "数据连接端口号")
    private String port;

    @ApiModelProperty(value = "数据库名称")
    private String database;

    @ApiModelProperty(value = "数据库模式")
    private String schema;

    @ApiModelProperty(value = "数据连接用户名")
    private String username;

    @ApiModelProperty(value = "数据连接密码")
    private String password;

    @ApiModelProperty(value = "数据库驱动")
    private String driver;

    @ApiModelProperty(value = "数据库连接串")
    private String url;

    public String getDriverByType() {
        if (this.type == null) {
            return null;
        }
        switch (this.type) {
            case DataConnection.MYSQL:
                if ("8.0".equals(this.version)) {
                    return "com.mysql.cj.jdbc.Driver";
                }
                return "com.mysql.jdbc.Driver";
            case DataConnection.POSTGRESQL:
                return "org.postgresql.Driver";
            case DataConnection.ORACLE:
                return "oracle.jdbc.driver.OracleDriver";
            case DataConnection.SQL_SERVER:
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case DataConnection.T_BASE:
                return "org.postgresql.Driver";
        }
        return null;
    }

}
