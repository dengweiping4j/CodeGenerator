package com.dwp.codegenerator.domain;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * 数据库表实体
 *
 * @author dengweiping
 * @date 2021/1/12 10:58
 */
@Entity
@Data
@Table(name = "database_table")
public class DatabaseTable {

    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(generator = "uuid2")
    @Column(name = "id")
    private String id;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "table_comment")
    private String tableComment;

    @Column(name = "data_connection_id")
    private String dataConnectionId;
}
