package com.dwp.codegenerator.domain.dto;

import com.dwp.codegenerator.domain.Pagination;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页响应类
 *
 * @author dengweiping
 * @date 2021/1/11 15:11
 */
public class PaginationSuccessDTO<T> {
    private static final int START_PAGE_NUMBER = 1;
    private Pagination pagination;
    private List<T> data;

    public PaginationSuccessDTO(List<T> data) {
        this.pagination = new Pagination(1, data.size(), (long) data.size());
        this.data = data;
    }

    public PaginationSuccessDTO(Page<T> pageData) {
        this.pagination = new Pagination(pageData.getNumber() + 1, pageData.getSize(), pageData.getTotalElements());
        this.data = pageData.getContent();
    }

    public Pagination getPagination() {
        return this.pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public List<T> getData() {
        return this.data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
