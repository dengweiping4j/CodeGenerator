package com.dwp.codegenerator.repository;

import com.dwp.codegenerator.domain.DataConnection;
import com.dwp.codegenerator.domain.dto.DataConnectionDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: MetadataETL查询类
 * @author: DengWeiPing
 * @time: 2020/7/2 15:59
 */
public class DataConnectionSpecifications {

    /**
     * queryMetadataETLList
     *
     * @return Specification查询对象信息
     */
    public static Specification<DataConnection> queryList(DataConnectionDTO queryDTO) {
        return (Specification<DataConnection>) (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(queryDTO.getName())) {
                predicates.add(builder.like(root.get("name").as(String.class), "%" + queryDTO.getName() + "%"));
            }
            if (StringUtils.isNotBlank(queryDTO.getType())) {
                predicates.add(builder.like(root.get("type").as(String.class), "%" + queryDTO.getType() + "%"));
            }
            Predicate[] p = new Predicate[predicates.size()];
            query.orderBy(builder.asc(root.get("name")));
            return query.where(builder.and(predicates.toArray(p))).getRestriction();
        };
    }
}
