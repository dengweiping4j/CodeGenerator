package com.dwp.codegenerator.repository;

import com.dwp.codegenerator.domain.DataConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description:
 * @author: DengWeiPing
 * @time: 2020/6/10 10:22
 */
@Repository
public interface DataConnectionRepository extends JpaRepository<DataConnection, String>, JpaSpecificationExecutor<DataConnection> {

    /**
     * 查询除指定id外的全部结果
     *
     * @param id
     * @return
     */
    List<DataConnection> findAllByIdNotIn(List<String> id);

    @Query(value = "select * from data_connection ORDER BY CONVERT(name USING gbk)", nativeQuery = true)
    List<DataConnection> findAllByOrderByName();
}
