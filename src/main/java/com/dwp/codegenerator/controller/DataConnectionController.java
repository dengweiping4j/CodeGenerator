package com.dwp.codegenerator.controller;

import com.dwp.codegenerator.domain.Pagination;
import com.dwp.codegenerator.domain.dto.DataConnectionDTO;
import com.dwp.codegenerator.domain.dto.PaginationSuccessDTO;
import com.dwp.codegenerator.domain.mapper.DataConnectionMapper;
import com.dwp.codegenerator.domain.vo.DataConnectionVO;
import com.dwp.codegenerator.service.DataConnectionService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description: 数据连接Controller类
 * @author: DengWeiPing
 * @time: 2020/6/10 9:00
 */
@RestController
@RequestMapping("/api/dataConnection")
public class DataConnectionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataConnectionController.class);

    @Autowired
    private DataConnectionService dataConnectionService;

    /**
     * 分页查询
     *
     * @param queryBuilder
     * @param pagination
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询", produces = "application/json")
    @ApiResponses({@ApiResponse(code = 200, message = "查询成功")})
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public ResponseEntity<PaginationSuccessDTO<DataConnectionDTO>> query(@RequestBody DataConnectionDTO queryBuilder, Pagination pagination) {
        Pageable pageable = PageRequest.of(pagination.getPage() - 1, pagination.getPageSize());
        Page<DataConnectionDTO> data = dataConnectionService.query(queryBuilder, pageable);
        return ResponseEntity.ok(new PaginationSuccessDTO<>(data));
    }

    /**
     * 查询所有
     *
     * @return
     */
    @ApiOperation(value = "查询所有", notes = "查询所有", produces = "application/json")
    @ApiResponses({@ApiResponse(code = 200, message = "查询成功!"),
            @ApiResponse(code = 204, message = "没有内容!")})
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<DataConnectionVO>> getAll() {
        return ResponseEntity.ok(dataConnectionService.getAll());
    }

    /**
     * 条件查询
     *
     * @return
     */
    @ApiOperation(value = "条件查询", notes = "条件查询", produces = "application/json")
    @ApiResponses({@ApiResponse(code = 200, message = "查询成功!"),
            @ApiResponse(code = 204, message = "没有内容!")})
    @RequestMapping(value = "/queryWhere", method = RequestMethod.POST)
    public ResponseEntity<List<DataConnectionVO>> queryWhere(@RequestBody DataConnectionDTO queryDTO) {
        return ResponseEntity.ok(dataConnectionService.queryWhere(queryDTO));
    }

    /**
     * 根据Id获取数据库连接信息
     *
     * @param id 数据连接ID
     * @return
     */
    @ApiOperation(value = "根据id获取数据连接信息", notes = "根据id获取数据连接信息", produces = "application/json")
    @ApiImplicitParam(name = "id", value = "数据库连接信息ID", paramType = "path")
    @ApiResponses({@ApiResponse(code = 200, message = "查询成功!"),
            @ApiResponse(code = 204, message = "没有内容!")})
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Object> getDataConnectionById(@PathVariable("id") String id) {
        LOGGER.debug("dataConnection id: {}", id);
        return ResponseEntity.ok(DataConnectionMapper.toVO(dataConnectionService.getDataConnectionById(id)));
    }

    /**
     * 测试数据库连接
     *
     * @param dataConnectionDTO DataConnectionDTO对象
     * @return 响应类Result
     */
    @ApiOperation(value = "测试数据库连接", notes = "测试数据库连接", produces = "application/json")
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功"),
            @ApiResponse(code = 204, message = "没有内容")})
    @RequestMapping(value = "/testConnect", method = RequestMethod.POST)
    public ResponseEntity<Object> testConnect(@RequestBody DataConnectionDTO dataConnectionDTO) {
        LOGGER.debug("REST request to testConnect : {}", dataConnectionDTO);
        return ResponseEntity.ok(dataConnectionService.testConnect(DataConnectionMapper.toEntity(dataConnectionDTO)));
    }

    /**
     * 新增数据连接 DataConnection
     *
     * @param dataConnectionDTO 新增DataConnectionDTO对象
     * @return 响应类Result
     */
    @ApiOperation(value = "新增DataConnection", notes = "新增数据连接", produces = "application/json")
    @ApiResponses({@ApiResponse(code = 200, message = "新增成功"),
            @ApiResponse(code = 204, message = "没有内容")})
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Object> create(@RequestBody DataConnectionDTO dataConnectionDTO) {
        LOGGER.debug("REST request to create : {}", dataConnectionDTO);
        return ResponseEntity.ok(dataConnectionService.create(dataConnectionDTO));
    }

    /**
     * 修改数据连接 DataConnection
     *
     * @param id                数据连接ID
     * @param dataConnectionDTO 修改 DataConnection 对象
     * @return 响应类Result
     */
    @ApiOperation(value = "修改DataConnection", notes = "修改数据连接", produces = "application/json")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "id", paramType = "path"),
            @ApiImplicitParam(name = "dataConnectionDTO", value = "修改DataConnection对象", paramType = "path")})
    @ApiResponses({@ApiResponse(code = 200, message = "修改成功"),
            @ApiResponse(code = 204, message = "没有内容")})
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Object> modify(@PathVariable("id") String id, @RequestBody DataConnectionDTO dataConnectionDTO) {
        LOGGER.debug("REST request to update : {}", dataConnectionDTO);
        return ResponseEntity.ok(dataConnectionService.modify(id, dataConnectionDTO));
    }

    /**
     * 删除 DataConnection
     *
     * @param id 数据连接主键
     * @return 响应类Result
     */
    @ApiOperation(value = "删除DataConnection",
            notes = "删除数据连接", produces = "application/json")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "数据连接主键", paramType = "path")})
    @ApiResponses({@ApiResponse(code = 204, message = "操作成功")})
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(@PathVariable("id") String id) {
        LOGGER.debug("REST request to Delete : {}", id);
        return ResponseEntity.ok(dataConnectionService.delete(id));

    }

    /**
     * 查询数据库表空间
     *
     * @param dataConnectionId 数据连接ID
     * @return
     */
    @ApiOperation(value = "查询数据库表空间", notes = "查询数据库表空间", produces = "application/json")
    @ApiImplicitParam(name = "dataConnectionId", value = "数据库连接信息ID", paramType = "path")
    @ApiResponses({@ApiResponse(code = 200, message = "查询成功!"),
            @ApiResponse(code = 204, message = "没有内容!")})
    @RequestMapping(value = "/getSchemas/{dataConnectionId}", method = RequestMethod.GET)
    public ResponseEntity<Object> getSchemas(@PathVariable("dataConnectionId") String dataConnectionId) {
        LOGGER.debug("dataConnection id: {}", dataConnectionId);
        return ResponseEntity.ok(dataConnectionService.getSchemas(dataConnectionId));
    }

    /**
     * 根据数据源Id查询表列表
     *
     * @param dataConnectionId 数据连接ID
     * @return
     */
    @ApiOperation(value = "根据数据源Id查询表列表", notes = "根据数据源Id查询表列表", produces = "application/json")
    @ApiImplicitParam(name = "dataConnectionId", value = "数据源Id", paramType = "path")
    @ApiResponses({@ApiResponse(code = 200, message = "查询成功!"),
            @ApiResponse(code = 204, message = "没有内容!")})
    @RequestMapping(value = "/findTables/{dataConnectionId}", method = RequestMethod.GET)
    public ResponseEntity<Object> findTables(@PathVariable("dataConnectionId") String dataConnectionId) {
        LOGGER.debug("dataConnection id: {}", dataConnectionId);
        return ResponseEntity.ok(dataConnectionService.findTableAndViews(dataConnectionId));
    }
}
