package com.dwp.codegenerator.domain;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * @description:
 * @author: DengWeiPing
 * @time: 2020/8/21 09:42
 */
@Entity
@Data
@Table(name = "driver_path")
public class DriverPath {

    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(generator = "uuid2")
    @Column(name = "id")
    private String id;

    @Column(name = "driver")
    private String driver;

    @Column(name = "path")
    private String path;

    @Column(name = "type")
    private String type;
}
