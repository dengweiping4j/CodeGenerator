package com.dwp.codegenerator.service;

import com.dwp.codegenerator.domain.DataConnection;
import com.dwp.codegenerator.domain.DatabaseColumn;
import com.dwp.codegenerator.domain.DriverPath;
import com.dwp.codegenerator.domain.Result;
import com.dwp.codegenerator.domain.dto.DataConnectionDTO;
import com.dwp.codegenerator.domain.mapper.DataConnectionMapper;
import com.dwp.codegenerator.domain.vo.DataConnectionVO;
import com.dwp.codegenerator.repository.DataConnectionRepository;
import com.dwp.codegenerator.repository.DataConnectionSpecifications;
import com.dwp.codegenerator.repository.DriverPathRepository;
import com.google.common.io.Resources;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author: DengWeiPing
 * @time: 2020/6/10 9:15
 */
@Service
@Transactional(rollbackOn = Exception.class)
public class DataConnectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataConnectionService.class);

    @Autowired
    private DataConnectionRepository dataConnectionRepository;

    @Autowired
    private DriverPathRepository driverPathRepository;

    public DataConnection getDataConnectionById(String id) {
        DataConnection dataConnection = dataConnectionRepository.findById(id).orElse(null);
        return dataConnection;
    }

    public Result create(DataConnectionDTO dataConnectionDTO) {
        DataConnection dataConnection = DataConnectionMapper.toEntity(dataConnectionDTO);
        Result result = testConnect(dataConnection);
        if (result.succeed()) {
            List<DataConnection> dataConnections = dataConnectionRepository.findAll();
            boolean isExisted = isDataConnectionExisted(dataConnections, dataConnection);
            if (isExisted) {
                return Result.error("检查数据库引擎类型、IP地址、端口号、数据库名称、用户名时，发现已存在");
            }
            try {
                DataConnection resultDataConnection = dataConnectionRepository.save(dataConnection);
                if (resultDataConnection != null) {
                    return Result.success();
                }
            } catch (Exception e) {
                LOGGER.error("modify dataConnection error : {}", e);
                return Result.error("保存数据连接时出错", e.getMessage());
            }
        }
        return Result.error("数据库连接异常，请检查数据库连接，配置参数等是否正常", result.getMessage());
    }

    public Result modify(String id, DataConnectionDTO dataConnectionDTO) {
        DataConnection dataConnection = DataConnectionMapper.toEntity(dataConnectionDTO);
        Result result = testConnect(dataConnection);
        if (result.succeed()) {
            List<DataConnection> dataConnections = dataConnectionRepository.findAllByIdNotIn(Collections.singletonList(id));
            boolean isExisted = isDataConnectionExisted(dataConnections, dataConnection);
            if (isExisted) {
                return Result.error("检查数据库引擎类型、IP地址、端口号、数据库名称、用户名时，发现已存在");
            }
            try {
                DataConnection resultDataConnection = dataConnectionRepository.save(dataConnection);
                if (resultDataConnection != null) {
                    return Result.success();
                }
            } catch (Exception e) {
                LOGGER.error("modify dataConnection error : {}", e);
                return Result.error("保存数据连接时出错", e.getMessage());
            }
        }
        return Result.error("数据库连接异常，请检查数据库连接，配置参数等是否正常", result.getMessage());
    }

    public Result delete(String id) {
        DataConnection dataConnection = new DataConnection();
        dataConnection.setId(id);

        // todo...

        return Result.success();
    }

    /**
     * 测试数据库连接
     *
     * @param dataConnection
     * @return
     */
    public Result testConnect(DataConnection dataConnection) {
        Connection conn = null;
        try {
            Result result = getConnection(dataConnection);
            if (result.getData() == null) {
                return result;
            }
            conn = (Connection) result.getData();
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("连接数据库失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } finally {
            disConnect(conn, null, null);
        }
    }

    /**
     * 获取数据库连接
     *
     * @param dataConnection
     * @return
     */
    public Result getConnection(DataConnection dataConnection) {
        DataConnectionDTO dataConnectionDTO = DataConnectionMapper.toDTO(dataConnection);
        String ip = dataConnectionDTO.getIp();
        String port = dataConnectionDTO.getPort();
        String database = dataConnectionDTO.getDatabase();
        String username = dataConnectionDTO.getUsername();
        String password = dataConnectionDTO.getPassword();
        String type = dataConnectionDTO.getType();
        String driver = dataConnectionDTO.getDriver();
        String url = dataConnectionDTO.getUrl();
        if (StringUtils.isBlank(ip)) {
            return Result.error("IP地址不能为空");
        }
        if (StringUtils.isBlank(port)) {
            return Result.error("端口号不能为空");
        }
        if (StringUtils.isBlank(database)) {
            return Result.error("数据库名称不能为空");
        }
        if (StringUtils.isBlank(username)) {
            return Result.error("用户名不能为空");
        }
        if (StringUtils.isBlank(password)) {
            return Result.error("密码不能为空");
        }
        if (StringUtils.isBlank(driver)) {
            return Result.error("未找到数据库驱动");
        }

        if (StringUtils.isBlank(url)) {
            switch (type) {
                case DataConnection.MYSQL:
                    url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useOldAliasMetadataBehavior=true";
                    break;
                case DataConnection.POSTGRESQL:
                case DataConnection.T_BASE:
                    url = "jdbc:postgresql://" + ip + ":" + port + "/" + database;
                    break;
                case DataConnection.ORACLE:
                    url = "jdbc:oracle:thin:@" + ip + ":" + port + "/" + database;
                    break;
                case DataConnection.SQL_SERVER:
                    url = "jdbc:sqlserver://" + ip + ":" + port + ";DatabaseName=" + database;
                    break;
                default:
            }
        }
        final String connUrl = url;

        String schema = dataConnectionDTO.getSchema();
        if (type.equals(DataConnection.POSTGRESQL) || type.equals(DataConnection.T_BASE)) {
            if (StringUtils.isBlank(schema)) {
                schema = "public";
            }
        }
        final String dbSchema = schema;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        FutureTask<Result> future = new FutureTask<>(() -> {
            try {
                //动态加载数据库驱动
                Properties properties = new Properties();
                properties.put("user", username);
                properties.put("password", password);
                Driver driverClass = getDriverLoaderByName(driver, type);
                Connection conn = driverClass.connect(connUrl, properties);
                if (conn == null) {
                    return Result.error("连接失败");
                }

                PreparedStatement preparedStatement;
                if (type.equals(DataConnection.POSTGRESQL) || type.equals(DataConnection.T_BASE)) {
                    preparedStatement = conn.prepareStatement("ALTER USER \"" + username + "\" SET search_path to " + dbSchema);
                    preparedStatement.execute();
                }
                return Result.success(conn);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("connection error: {}", e.getMessage());
                return Result.error("连接失败", e.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
                LOGGER.error("connection error: {}", t.getMessage());
                return Result.error("连接失败，可能是指定的数据库版本不正确", t.getMessage());
            }
        });

        try {
            executor.execute(future);
            // 指定超时时间
            Result result = future.get(5000, TimeUnit.MILLISECONDS);
            return result;
        } catch (TimeoutException e) {
            return Result.error("数据库连接超时");
        } catch (Exception e) {
            return Result.error("系统异常");
        } finally {
            future.cancel(true);
            executor.shutdown();
        }
    }

    public static void main(String[] args) {
        String path = Resources.getResource("drivers/mysql-connector-java-5.1.34.jar").getPath();
        File file = new File(path);
        if (!file.exists()) {
            LOGGER.error("{} 对应的驱动jar不存在.");
        }
    }

    /**
     * 动态加载JDBC驱动
     *
     * @param driverPath 驱动类路径
     * @return
     * @throws Exception
     */
    public Driver getDriverLoaderByName(String driverPath, String type) throws Exception {
        if (StringUtils.isBlank(driverPath)) {
            return null;
        }
        String jarPath = this.getDriverPath(type);
        String path = Resources.getResource(jarPath).getPath();
        File file = new File(path);
        if (!file.exists()) {
            LOGGER.error("{} 对应的驱动jar不存在.", driverPath);
            return null;
        }

        URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()}, null);
        loader.clearAssertionStatus();
        Driver driver = (Driver) Class.forName(driverPath, true, loader).newInstance();
        return driver;
    }

    private String getDriverPath(String type) {
        switch (type) {
            case DataConnection.MYSQL:
                return "drivers/mysql-connector-java-8.0.15.jar";
            case DataConnection.POSTGRESQL:
                return "drivers/postgresql-9.2-1004.jdbc3.jar";
            case DataConnection.ORACLE:
                return "drivers/Oracle_10g_10.2.0.4_JDBC_ojdbc14.jar";
            case DataConnection.SQL_SERVER:
                return "drivers/sqljdbc4.jar";
            default:
                return "drivers/mysql-connector-java-8.0.15.jar";
        }
    }

    /**
     * 关闭数据库连接
     *
     * @param conn
     * @param preparedStatement
     * @param resultSet
     */
    public void disConnect(Connection conn, PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            LOGGER.error("disConnect exception: ", e);
        }
    }

    /**
     * 关闭数据库连接
     *
     * @param conn
     * @param Statement
     * @param resultSet
     */
    public void disConnect(Connection conn, Statement Statement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (Statement != null) {
                Statement.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            LOGGER.error("disConnect exception: ", e);
        }
    }

    /**
     * 获取所有数据连接信息
     *
     * @return
     */
    public List<DataConnectionVO> getAll() {
        List<DataConnection> dataConnections = dataConnectionRepository.findAllByOrderByName();
        List<DataConnectionVO> list = new ArrayList<>();
        for (DataConnection dataConnection : dataConnections) {
            DataConnectionVO dataConnectionVO = DataConnectionMapper.toVO(dataConnection);
            list.add(dataConnectionVO);
        }
        return list;
    }

    /**
     * 条件查询
     *
     * @param queryDTO
     * @return
     */
    public List<DataConnectionVO> queryWhere(DataConnectionDTO queryDTO) {
        List<DataConnection> dataConnectionList = dataConnectionRepository.findAll(DataConnectionSpecifications.queryList(queryDTO));
        List<DataConnectionVO> resultList = new ArrayList<>();
        for (DataConnection dataConnection : dataConnectionList) {
            resultList.add(DataConnectionMapper.toVO(dataConnection));
        }
        return resultList;
    }

    public Page<DataConnectionDTO> query(DataConnectionDTO queryDTO, Pageable pageable) {
        return dataConnectionRepository.findAll(DataConnectionSpecifications.queryList(queryDTO), pageable).map(dataConnection -> DataConnectionMapper.toDTO(dataConnection));
    }

    /**
     * 判断数据连接是否存在
     *
     * @param list
     * @param target
     * @return
     */
    public boolean isDataConnectionExisted(List<DataConnection> list, DataConnection target) {
        for (DataConnection item : list) {
            if (target.equals(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 执行自定义SQL语句
     *
     * @param sql
     * @param dataConnection
     * @param params
     * @param <T>
     * @return
     */
    public <T> Result executeBySql(String sql, DataConnection dataConnection, List<T> params) {
        if (dataConnection == null) {
            return Result.error("未找到指定的数据连接！");
        }
        Result getConnectionResult = getConnection(dataConnection);
        Connection conn = null;
        if (getConnectionResult.succeed()) {
            conn = (Connection) getConnectionResult.getData();
        }
        if (conn == null) {
            return Result.error("数据库连接失败！");
        }
        PreparedStatement preparedStatement = null;
        int result;
        try {
            preparedStatement = conn.prepareStatement(sql);
            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    preparedStatement.setObject(i + 1, params.get(i));
                }
            }
            preparedStatement.executeUpdate();
            return Result.success("执行成功");
        } catch (Exception e) {
            LOGGER.error("execute query sql exception: ", e);
            LOGGER.error("error sql : ", sql);
            e.printStackTrace();
            return Result.error("执行SQL语句时出错 ", e.getMessage());
        } finally {
            disConnect(conn, preparedStatement, null);
        }
    }

    /**
     * 获取数据库类型
     *
     * @param dataConnectionId
     * @return
     */
    public String getDataConnectionTypeById(String dataConnectionId) {
        DataConnection dataConnection = getDataConnectionById(dataConnectionId);
        if (dataConnection != null) {
            return DataConnectionMapper.toVO(dataConnection).getType();
        }
        return null;
    }

    /**
     * 查询数据库表空间
     *
     * @param dataConnectionId
     * @return
     */
    public List<String> getSchemas(String dataConnectionId) {
        String sql = "SELECT username FROM all_users ORDER BY username";
        DataConnection dataConnection = dataConnectionRepository.findById(dataConnectionId).get();
        Result executeQueryResult = executeQueryBySQL(sql, dataConnection, null);
        if (executeQueryResult.errored()) {
            return null;
        }
        List<Map<String, Object>> data = (List<Map<String, Object>>) executeQueryResult.getData();
        List<String> result = new ArrayList<>();
        for (Map<String, Object> map : data) {
            result.add(map.get("USERNAME").toString());
        }
        if (data != null && data.size() > 0) {

        }
        return result;
    }

    public Result findTableAndViews(String dataConnectionId) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //获取表
        Result tableResult = getTableListByDataConnectionId(dataConnectionId);
        if (tableResult.errored()) {
            return tableResult;
        }
        resultList.addAll((List<Map<String, Object>>) tableResult.getData());

        //获取视图
        Result viewResult = getViewListByDataConnectionId(dataConnectionId);
        if (viewResult.errored()) {
            return viewResult;
        }
        resultList.addAll((List<Map<String, Object>>) viewResult.getData());
        return Result.success(resultList);
    }

    /**
     * 根据数据源id查询数据库表列表
     *
     * @param dataConnectionId
     * @return
     */
    public Result getTableListByDataConnectionId(String dataConnectionId) {
        DataConnection dataConnection = dataConnectionRepository.findById(dataConnectionId).orElse(null);
        if (dataConnection == null) {
            return Result.error("未找到指定的数据连接！");
        }
        DataConnectionDTO dataConnectionDTO = DataConnectionMapper.toDTO(dataConnection);
        StringBuffer sql = new StringBuffer();
        List<String> params = new ArrayList<>();
        switch (dataConnectionDTO.getType()) {
            case DataConnection.MYSQL:
                sql.append("select distinct table_name as tableName,table_comment as tableComment from information_schema.tables where Table_type = 'BASE TABLE' and table_schema=? order by table_name");
                params.add(dataConnectionDTO.getDatabase());
                break;
            case DataConnection.POSTGRESQL:
            case DataConnection.T_BASE:
                sql.append("select distinct relname as \"tableName\", cast(obj_description(relfilenode, 'pg_class') as varchar) as \"tableComment\""
                        + " from pg_class c left join pg_namespace p on c.relnamespace = p.oid"
                        + " inner join information_schema.table_privileges i on i.table_name=c.relname"
                        + " where p.nspname = ? and i.table_schema=? and relkind = 'r' and relname not like 'pg_%' and relname not like 'sql_%' order by relname");
                params.add(dataConnectionDTO.getSchema());
                params.add(dataConnectionDTO.getSchema());
                break;
            case DataConnection.ORACLE:
                sql.append("select a.table_name \"tableName\",b.COMMENTS \"tableComment\" from USER_TABLES a,USER_TAB_COMMENTS b WHERE a.TABLE_NAME=b.TABLE_NAME order by table_name");
                break;
            case DataConnection.SQL_SERVER:
                sql.append("select a.name AS \"tableName\",convert(varchar(100), isnull(g.[value], '')) AS \"tableComment\""
                        + " from sys.tables a left join sys.extended_properties g on (a.object_id = g.major_id AND g.minor_id = 0) order by a.name");
                break;
        }
        Result result = executeQueryBySQL(sql.toString(), dataConnection, params);
        if (result.succeed()) {
            List<Map<String, Object>> tableList = (List<Map<String, Object>>) result.getData();
            return Result.success(tableList);
        }
        return result;
    }

    /**
     * 根据数据源id查询数据库视图列表
     *
     * @param dataConnectionId
     * @return
     */
    public Result getViewListByDataConnectionId(String dataConnectionId) {
        DataConnection dataConnection = dataConnectionRepository.findById(dataConnectionId).orElse(null);
        if (dataConnection == null) {
            return Result.error("未找到指定的数据连接！");
        }
        DataConnectionDTO dataConnectionDTO = DataConnectionMapper.toDTO(dataConnection);
        StringBuffer sql = new StringBuffer();
        List<String> params = new ArrayList<>();
        switch (dataConnectionDTO.getType()) {
            case DataConnection.MYSQL:
                sql.append("SELECT distinct TABLE_NAME as tableName FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND  TABLE_TYPE ='VIEW'");
                params.add(dataConnectionDTO.getDatabase());
                break;
            case DataConnection.POSTGRESQL:
            case DataConnection.T_BASE:
                sql.append("select distinct view_name \"tableName\" from (select distinct v.viewname as view_name"
                        + " from pg_views v"
                        + " inner join information_schema.table_privileges i on v.schemaname = i.table_schema"
                        + " WHERE schemaname = ?) t"
                        + " order by view_name");
                params.add(dataConnectionDTO.getSchema());
                break;
            case DataConnection.ORACLE:
                sql.append("select distinct view_name \"tableName\" from USER_VIEWS order by view_name");
                params.add(dataConnectionDTO.getUsername().toUpperCase());
                break;
            case DataConnection.SQL_SERVER:
                sql.append("select [name] as \"tableName\" from sysobjects where xtype='V'");
                break;
        }
        Result result = executeQueryBySQL(sql.toString(), dataConnection, params);
        if (result.succeed()) {
            List<Map<String, Object>> viewList = (List<Map<String, Object>>) result.getData();
            return Result.success(viewList);
        }
        return result;
    }

    /**
     * 执行指定查询语句
     *
     * @param sql            sql语句
     * @param dataConnection 数据库连接信息
     * @param params         查询参数
     * @return
     */
    public <T> Result executeQueryBySQL(String sql, DataConnection dataConnection, List<T> params) {
        if (dataConnection == null) {
            return Result.error("未找到指定的数据连接！");
        }
        Result getConnectionResult = getConnection(dataConnection);
        Connection conn = null;
        if (getConnectionResult.succeed()) {
            conn = (Connection) getConnectionResult.getData();
        }
        if (conn == null) {
            return Result.error("数据库连接失败！");
        }
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = conn.prepareStatement(sql);
            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    preparedStatement.setObject(i + 1, params.get(i));
                }
            }
            resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> resultList = new ArrayList<>();
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rsmd.getColumnName(i);
                    switch (rsmd.getColumnTypeName(i).toUpperCase()) {
                        case "YEAR":
                            String yearStr = resultSet.getString(i);
                            if (yearStr != null) {
                                map.put(columnName, yearStr.substring(0, 4));
                            }
                            break;
                        case "DATETIME":
                            String str = resultSet.getString(i);
                            if (str != null) {
                                Timestamp dateTime = resultSet.getTimestamp(i);
                                if (dateTime != null) {
                                    String dateTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateTime);
                                    map.put(columnName, dateTimeStr);
                                } else {
                                    map.put(columnName, null);
                                }
                            }
                            break;
                        case "TIMESTAMP":
                            String timestampStr = resultSet.getString(i);
                            map.put(columnName, timestampStr);
                            break;
                        case "DATE":
                            String dateStr = resultSet.getString(i);
                            if (dateStr != null) {
                                if (resultSet.getDate(i) != null) {
                                    if (dataConnection.equals(DataConnection.ORACLE)) {
                                        dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(resultSet.getTimestamp(i));
                                    } else {
                                        dateStr = new SimpleDateFormat("yyyy - MM - dd").format(resultSet.getTimestamp(i));
                                    }
                                }
                                map.put(columnName, dateStr);
                            }
                            break;
                        case "TIME":
                            Timestamp time = resultSet.getTimestamp(i);
                            if (time != null) {
                                String timeStr = new SimpleDateFormat("HH:mm:ss").format(time);
                                map.put(columnName, timeStr);
                            } else {
                                map.put(columnName, null);
                            }
                            break;
                        case "BIT":
                        case "VARBIT":
                            map.put(rsmd.getColumnName(i), Long.toBinaryString(resultSet.getLong(i)));
                            break;
                        case "BLOB":
                        case "BINARY":
                        case "VARBINARY":
                        case "TINYBLOB":
                        case "MEDIUMBLOB":
                        case "LONGBLOB":
                        case "BYTEA":
                            InputStream in = null;
                            try {
                                in = resultSet.getBinaryStream(i);
                                if (in == null) {
                                    break;
                                }
                                StringBuilder sb = new StringBuilder();
                                String line;
                                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                                while ((line = br.readLine()) != null) {
                                    sb.append(line);
                                }
                                map.put(columnName, sb.toString());
                            } finally {
                                if (in != null) {
                                    in.close();
                                }
                            }
                            break;
                        case "CIRCLE":
                        case "PATH":
                        case "POINT":
                        case "POLYGON":
                        case "LINE":
                        case "LSEG":
                        case "BOX":
                            map.put(rsmd.getColumnName(i), resultSet.getString(i));
                            break;
                        default:
                            map.put(rsmd.getColumnName(i), resultSet.getObject(i));
                    }
                }
                resultList.add(map);
            }
            return Result.success(resultList);
        } catch (Exception e) {
            LOGGER.error("execute query sql exception: ", e);
            LOGGER.error("error sql : ", sql);
            e.printStackTrace();
            return Result.error("执行SQL查询语句时出错 ", e.getMessage());
        } finally {
            disConnect(conn, preparedStatement, resultSet);
        }
    }


    /**
     * 获取指定表的字段信息
     *
     * @param dataConnectionId
     * @param tableName
     * @return
     */
    public Result getTableColumns(String dataConnectionId, String tableName) {
        if (dataConnectionId == null || tableName == null) {
            return Result.error("未指定数据源或表！");
        }

        DataConnection dataConnection = dataConnectionRepository.findById(dataConnectionId).get();

        //防止sql注入
        if (!tableName.matches("[a-zA-Z0-9_.]*")) {
            //表名不合法
            return Result.error("表名不合法");
        }
        DataConnectionDTO dataConnectionDTO = DataConnectionMapper.toDTO(dataConnection);
        StringBuffer sql = new StringBuffer();
        List<String> params = new ArrayList<>();

        switch (dataConnectionDTO.getType()) {
            case DataConnection.MYSQL:
                sql.append(
                        "select COLUMN_NAME,DATA_TYPE,COLUMN_TYPE,NUMERIC_SCALE,COLUMN_KEY,COLUMN_COMMENT,IS_NULLABLE"
                                + " from information_schema.columns"
                                + " where table_name = '" + tableName + "' and table_schema = '" + dataConnectionDTO.getDatabase() + "'"
                );
                break;
            case DataConnection.POSTGRESQL:
            case DataConnection.T_BASE:
                sql.append(
                        "select a.attnum,"
                                + " a.attname \"columnName\",t.typname \"columnType\",coalesce(d.character_maximum_length, d.numeric_precision) \"columnLength\","
                                + " d.numeric_scale \"numericScaleLength\",(a.attnotnull is false) \"isNullable\",d.column_default \"defaultValue\","
                                + " col_description(a.attrelid, a.attnum) as \"columnComment\",case when length(b.attname) > 0 then 'PRI' end as \"columnKey\""
                                + " from pg_class c left join pg_namespace p on c.relnamespace = p.oid,pg_attribute a,pg_type t,information_schema.columns d"
                                + " left join (select pg_attribute.attname from pg_index,pg_class,pg_attribute"
                                + "  where pg_class.oid = '" + tableName + "' :: regclass and pg_index.indrelid = pg_class.oid and pg_attribute.attrelid = pg_class.oid"
                                + " and pg_attribute.attnum = any (pg_index.indkey) ) b on d.column_name = b.attname"
                                + " where a.attrelid = c.oid and a.atttypid = t.oid and a.attnum > 0 and c.relname = d.table_name and d.column_name = a.attname and c.relname = '" + tableName + "'"
                                + " and d.table_schema = ? and p.nspname = ?"
                );
                params.add(dataConnectionDTO.getSchema());
                params.add(dataConnectionDTO.getSchema());
                break;
            case DataConnection.ORACLE:
                sql.append(
                        "select u1.COLUMN_NAME as \"columnName\",u1.DATA_TYPE as \"columnType\",u1.DATA_LENGTH as \"columnLength\",u1.NULLABLE as \"isNullable\","
                                + " u1.DATA_SCALE as \"dataScale\",u2.comments as \"columnComment\","
                                + " (case when u1.COLUMN_NAME in "
                                + " (select cu.COLUMN_NAME from all_cons_columns cu,all_constraints au where cu.constraint_name = au.constraint_name and au.constraint_type = 'P' and au.table_name = ?"
                                + " )then 'PRI' end) as \"columnKey\""
                                + " from all_tab_columns u1,all_col_comments u2"
                                + " where u1.OWNER=? and u2.OWNER=? and u1.TABLE_NAME = u2.table_name and u1.COLUMN_NAME = u2.column_name and u1.TABLE_NAME = ?"
                );
                if (tableName.indexOf(".") != -1) {
                    String schema = tableName.substring(0, tableName.indexOf("."));
                    String afterTableName = tableName.substring(tableName.indexOf(".") + 1);
                    params.add(afterTableName);
                    params.add(schema);
                    params.add(schema);
                    params.add(afterTableName);
                } else {
                    params.add(tableName);
                    params.add(tableName);
                }
                break;
            case DataConnection.SQL_SERVER:
                sql.append(
                        "SELECT "
                                + " columnName     = a.name,"
                                + " columnKey       = case when exists(SELECT 1 FROM sysobjects where xtype='PK' and parent_obj=a.id and name in ("
                                + " SELECT name FROM sysindexes WHERE indid in( SELECT indid FROM sysindexkeys WHERE id = a.id AND colid=a.colid))) then 'PRI' else '' end,"
                                + " columnType       = b.name,"
                                + " columnLength       = COLUMNPROPERTY(a.id,a.name,'PRECISION'),"
                                + " decimalLength   =CONVERT(nvarchar(50),isnull(COLUMNPROPERTY(a.id, a.name, 'Scale'), 0)),"
                                + " isNullable     = case when a.isnullable=1 then 'true' else 'false' end,"
                                + " columnComment   = CONVERT(nvarchar(50),isnull(g.[value], ''), 0)"
                                + " FROM syscolumns a left join systypes b on a.xusertype=b.xusertype inner join sysobjects d on a.id=d.id  and d.xtype='U' and  d.name<>'dtproperties'"
                                + " left join syscomments e on a.cdefault=e.id left join sys.extended_properties g on a.id=G.major_id and a.colid=g.minor_id"
                                + " left join sys.extended_properties f on d.id=f.major_id and f.minor_id=0"
                                + "  where d.name='" + tableName + "' order by a.id,a.colorder"
                );
                break;
        }
        Result result = executeQueryBySQL(sql.toString(), dataConnection, params);
        List<Map<String, Object>> resultList = (List<Map<String, Object>>) result.getData();
        if (result.succeed()) {
            List<DatabaseColumn> resultColumn = columnMapToEntity(resultList, dataConnectionDTO.getType());
            return Result.success(resultColumn);
        }
        return result;
    }

    /**
     * 字段信息映射到Column实体
     *
     * @param list
     * @param databaseType
     * @return
     */
    private List<DatabaseColumn> columnMapToEntity(List<Map<String, Object>> list, String databaseType) {
        List<DatabaseColumn> resultList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            DatabaseColumn column = new DatabaseColumn();
            if (databaseType.equals(DataConnection.MYSQL)) {
                column.setColumnName(map.get("COLUMN_NAME") == null ? null : map.get("COLUMN_NAME").toString());
                column.setColumnType(map.get("DATA_TYPE") == null ? null : map.get("DATA_TYPE").toString());
                String columnTypeLength = map.get("COLUMN_TYPE").toString();
                column.setColumnComment(map.get("COLUMN_COMMENT") == null ? null : map.get("COLUMN_COMMENT").toString());
                column.setPrimary("PRI".equals(map.get("COLUMN_KEY")));
            } else {
                column.setColumnName(map.get("columnName") == null ? null : map.get("columnName").toString());
                column.setColumnType(map.get("columnType") == null ? null : map.get("columnType").toString());
                column.setColumnComment(map.get("columnComment") == null ? null : map.get("columnComment").toString());
                column.setPrimary("PRI".equals(map.get("columnKey")));
            }
            resultList.add(column);
        }
        return resultList;
    }
}
