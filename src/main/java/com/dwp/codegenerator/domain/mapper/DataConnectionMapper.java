package com.dwp.codegenerator.domain.mapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dwp.codegenerator.domain.DataConnection;
import com.dwp.codegenerator.domain.dto.DataConnectionDTO;
import com.dwp.codegenerator.domain.vo.DataConnectionVO;
import com.dwp.codegenerator.utils.CodecUtil;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: DengWeiPing
 * @time: 2020/6/10 9:41
 */
public final class DataConnectionMapper {

    /**
     * 静态类方法,不支持new对象
     */
    private DataConnectionMapper() {
    }

    /**
     * DataConnectionDTO 转化为 DataConnection
     *
     * @param dataConnectionDTO 转换前对象
     * @return 转换后对象
     */
    public static DataConnection toEntity(DataConnectionDTO dataConnectionDTO) {
        if (dataConnectionDTO == null) {
            return null;
        }
        DataConnection dataConnection = new DataConnection();
        dataConnection.setId(dataConnectionDTO.getId());
        dataConnection.setName(dataConnectionDTO.getName());
        dataConnection.setDescription(dataConnectionDTO.getDescription());
        dataConnection.setType(dataConnectionDTO.getType());

        // 将数据连接属性转化为JSON字符串
        Map<String, Object> map = new HashMap<>(16);
        map.put("ip", dataConnectionDTO.getIp());
        map.put("port", dataConnectionDTO.getPort());
        map.put("database", dataConnectionDTO.getDatabase());
        if (DataConnection.POSTGRESQL.equals(dataConnectionDTO.getType())) {
            map.put("schema", dataConnectionDTO.getSchema());
        } else if (DataConnection.T_BASE.equals(dataConnectionDTO.getType())) {
            map.put("schema", dataConnectionDTO.getSchema());
        }
        map.put("version", dataConnectionDTO.getVersion());
        map.put("username", dataConnectionDTO.getUsername());
        // 转换时加密
        map.put("password", CodecUtil.Encrypt(dataConnectionDTO.getPassword()));
        map.put("url", dataConnectionDTO.getUrl());
        if (dataConnectionDTO.getDriver() == null) {
            map.put("driver", dataConnectionDTO.getDriverByType());
        } else {
            map.put("driver", dataConnectionDTO.getDriver());
        }
        String property = JSONObject.toJSONString(map);
        dataConnection.setProperty(property);

        return dataConnection;
    }

    /**
     * DataConnection 转化为 DataConnectionDTO
     *
     * @param dataConnection 转换前对象
     * @return 转换后对象
     */
    public static DataConnectionDTO toDTO(DataConnection dataConnection) {
        if (dataConnection == null) {
            return null;
        }
        DataConnectionDTO dataConnectionDTO = new DataConnectionDTO();
        dataConnectionDTO.setId(dataConnection.getId());
        dataConnectionDTO.setName(dataConnection.getName());
        dataConnectionDTO.setDescription(dataConnection.getDescription());
        dataConnectionDTO.setType(dataConnection.getType());
        // 将数据连接属性JSON字符串转为DTO类属性
        String property = dataConnection.getProperty();
        DataConnectionDTO propertyObj = JSON.parseObject(property, DataConnectionDTO.class);
        dataConnectionDTO.setIp(propertyObj.getIp());
        dataConnectionDTO.setPort(propertyObj.getPort());
        dataConnectionDTO.setDatabase(propertyObj.getDatabase());
        if (DataConnection.POSTGRESQL.equals(dataConnection.getType())) {
            dataConnectionDTO.setSchema(propertyObj.getSchema());
        } else if (DataConnection.T_BASE.equals(dataConnection.getType())) {
            dataConnectionDTO.setSchema(propertyObj.getSchema());
        }
        dataConnectionDTO.setVersion(propertyObj.getVersion());
        dataConnectionDTO.setUsername(propertyObj.getUsername());
        // 转换时解密
        dataConnectionDTO.setPassword(CodecUtil.Decrypt(propertyObj.getPassword()));
        dataConnectionDTO.setUrl(propertyObj.getUrl());
        if (StringUtils.isBlank(propertyObj.getDriver())) {
            dataConnectionDTO.setDriver(dataConnectionDTO.getDriverByType());
        } else {
            dataConnectionDTO.setDriver(propertyObj.getDriver());
        }

        return dataConnectionDTO;
    }

    /**
     * Object[] 转化为 DataConnectionDTO
     *
     * @param objs 转换前对象
     * @return 转换后对象
     */
    public static DataConnectionDTO toDTO(Object[] objs) {
        if (objs == null) {
            return null;
        }
        DataConnectionDTO dataConnectionDTO = new DataConnectionDTO();
        dataConnectionDTO.setId(objs[0] + "");
        dataConnectionDTO.setName(objs[1] + "");
        dataConnectionDTO.setType(objs[3] + "");
        dataConnectionDTO.setDescription(objs[4] + "");
        // 将数据连接属性JSON字符串转为DTO类属性
        String property = objs[5] + "";
        DataConnectionDTO propertyObj = JSON.parseObject(property, DataConnectionDTO.class);
        dataConnectionDTO.setIp(propertyObj.getIp());
        dataConnectionDTO.setPort(propertyObj.getPort());
        dataConnectionDTO.setDatabase(propertyObj.getDatabase());
        if (DataConnection.POSTGRESQL.equals(objs[3] + "")) {
            dataConnectionDTO.setSchema(propertyObj.getSchema());
        } else if (DataConnection.T_BASE.equals(objs[3] + "")) {
            dataConnectionDTO.setSchema(propertyObj.getSchema());
        }
        dataConnectionDTO.setVersion(propertyObj.getVersion());
        dataConnectionDTO.setUsername(propertyObj.getUsername());
        // 转换时解密
        dataConnectionDTO.setPassword(CodecUtil.Decrypt(propertyObj.getPassword()));
        dataConnectionDTO.setUrl(propertyObj.getUrl());
        if (StringUtils.isBlank(propertyObj.getDriver())) {
            dataConnectionDTO.setDriver(dataConnectionDTO.getDriverByType());
        } else {
            dataConnectionDTO.setDriver(propertyObj.getDriver());
        }
        return dataConnectionDTO;
    }

    /**
     * dataConnection 转化为 DataConnectionVO
     *
     * @param dataConnection 转换前对象
     * @return 转换后对象
     */
    public static DataConnectionVO toVO(DataConnection dataConnection) {
        if (dataConnection == null) {
            return null;
        }
        DataConnectionVO dataConnectionVO = new DataConnectionVO();
        dataConnectionVO.setId(dataConnection.getId());
        dataConnectionVO.setName(dataConnection.getName());
        dataConnectionVO.setDescription(dataConnection.getDescription());
        dataConnectionVO.setType(dataConnection.getType());
        // 将数据连接属性JSON字符串转为DTO类属性
        String property = dataConnection.getProperty();
        DataConnectionDTO propertyObj = JSON.parseObject(property, DataConnectionDTO.class);
        dataConnectionVO.setIp(propertyObj.getIp());
        dataConnectionVO.setPort(propertyObj.getPort());
        dataConnectionVO.setDatabase(propertyObj.getDatabase());
        if (DataConnection.POSTGRESQL.equals(dataConnection.getType()) || DataConnection.T_BASE.equals(dataConnection.getType())) {
            dataConnectionVO.setSchema(propertyObj.getSchema());
        }
        dataConnectionVO.setVersion(propertyObj.getVersion());
        dataConnectionVO.setUsername(propertyObj.getUsername());
        dataConnectionVO.setUrl(propertyObj.getUrl());
        return dataConnectionVO;
    }
}
