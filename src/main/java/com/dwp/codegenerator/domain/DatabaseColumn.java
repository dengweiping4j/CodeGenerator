package com.dwp.codegenerator.domain;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * 字段实体
 *
 * @author dengweiping
 * @date 2021/1/9 17:26
 */
@Entity
@Data
@Table(name = "database_column")
public class DatabaseColumn {

    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(generator = "uuid2")
    @Column(name = "id")
    private String id;

    @Column(name = "table_id")
    private String tableId;

    //字段名
    @Column(name = "column_name")
    private String columnName;

    //字段类型
    @Column(name = "column_type")
    private String columnType;

    //备注
    @Column(name = "column_comment")
    private String columnComment;

    //是否主键
    @Column(name = "is_primary")
    private boolean primary;
}
