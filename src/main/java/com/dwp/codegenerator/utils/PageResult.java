package com.dwp.codegenerator.utils;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应实体
 * @author Deng.wp
 *
 * @param <T>
 */
@Data
public class PageResult<T> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	//列数据
	private List<T> content;

	//总条数
	private long totalElements;

}