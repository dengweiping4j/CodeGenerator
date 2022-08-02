package com.dwp.codegenerator.service;

import com.dwp.codegenerator.domain.DatabaseColumn;
import com.dwp.codegenerator.domain.GeneratorParams;
import com.dwp.codegenerator.domain.Result;
import com.dwp.codegenerator.domain.dto.GeneratorParamsDTO;
import com.dwp.codegenerator.utils.GeneratorUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器
 *
 * @author dengweiping
 */
@Service
public class GeneratorService {
    @Autowired
    private DataConnectionService dataConnectionService;

    /**
     * 代码生成
     *
     * @param params
     * @return
     */
    public byte[] generatorCode(GeneratorParamsDTO params) {
        List<Map<String, Object>> tables = params.getTables();
        Validate.notEmpty(tables, "请选择需要生成的数据库表");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        tables.forEach(table -> GeneratorUtil.generatorCode(getGeneratorParams(params, table), zip));
        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }

    private GeneratorParams getGeneratorParams(GeneratorParamsDTO params, Map<String, Object> table) {
        String tableName = table.get("tableName").toString();
        //查询列信息
        Result result = dataConnectionService.getTableColumns(params.getDataConnectionId(), tableName);
        Validate.isTrue(result.succeed(), "查询表字段出错");
        List<DatabaseColumn> columns = (List<DatabaseColumn>) result.getData();
        //生成代码
        GeneratorParams generatorParams = new GeneratorParams();
        BeanUtils.copyProperties(params, generatorParams);
        generatorParams.setTableName(tableName)
                .setTableComment(table.get("tableComment") + "")
                .setColumns(columns);
        return generatorParams;
    }

}
