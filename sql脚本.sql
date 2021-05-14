create table data_connection
(
    id          varchar(36)   null,
    name        varchar(100)  null,
    type        varchar(100)  null,
    description varchar(100)  null,
    property    varchar(1000) null
)
    comment '数据连接表';

create table database_column
(
    id             varchar(36)  not null
        primary key,
    column_name    varchar(100) null,
    column_type    varchar(100) null,
    column_comment varchar(200) null,
    is_primary     varchar(1)   null,
    table_id       varchar(36)  not null
);

create table database_table
(
    id                 varchar(36)  not null
        primary key,
    table_name         varchar(100) null,
    table_comment      varchar(100) null,
    data_connection_id varchar(36)  not null
);

create table driver_path
(
    id     varchar(36)  null,
    driver varchar(100) null,
    path   varchar(100) null,
    type   varchar(100) null
);

INSERT INTO driver_path (id, driver, path, type) VALUES ('1', 'com.mysql.jdbc.Driver', 'D:\\driver\\mysql-connector-java-5.1.34.jar', 'MySQL');
INSERT INTO driver_path (id, driver, path, type) VALUES ('2', 'com.mysql.cj.jdbc.Driver', 'D:\\driver\\mysql-connector-java-8.0.15.jar', 'MySQL');
INSERT INTO driver_path (id, driver, path, type) VALUES ('3', 'oracle.jdbc.driver.OracleDriver', 'D:\\driver\\Oracle_10g_10.2.0.4_JDBC_ojdbc14.jar', 'Oracle');
INSERT INTO driver_path (id, driver, path, type) VALUES ('4', 'org.postgresql.Driver', 'D:\\driver\\postgresql-9.2-1004-jdbc41.jar', 'PostgreSQL');
INSERT INTO driver_path (id, driver, path, type) VALUES ('5', 'com.microsoft.sqlserver.jdbc.SQLServerDriver', 'D:\\driver\\sqljdbc4-4.0.jar', 'SQLServer');
INSERT INTO driver_path (id, driver, path, type) VALUES ('6', 'org.postgresql.Driver', 'D:\\driver\\postgresql-42.2.5.jar', 'TBase');
