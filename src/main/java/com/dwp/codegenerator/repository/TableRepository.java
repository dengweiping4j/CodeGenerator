package com.dwp.codegenerator.repository;

import com.dwp.codegenerator.domain.DatabaseTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author dengweiping
 * @date 2021/1/9 16:50
 */
@Repository
public interface TableRepository extends JpaRepository<DatabaseTable, String>, JpaSpecificationExecutor<DatabaseTable> {
}
