package com.dwp.codegenerator.service;

import com.dwp.codegenerator.domain.DatabaseColumn;
import com.dwp.codegenerator.domain.GeneratorParams;
import com.dwp.codegenerator.domain.Result;
import com.dwp.codegenerator.domain.dto.GeneratorParamsDTO;
import com.dwp.codegenerator.utils.GeneratorUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        if (params.getTables() == null || params.getTables().size() == 0) {
            return null;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);

        for (Map<String, Object> paramsMap : params.getTables()) {
            String tableName = paramsMap.get("tableName").toString();
            //查询列信息
            Result result = dataConnectionService.getTableColumns(params.getDataConnectionId(), tableName);
            if (result.errored()) {
                return null;
            }

            List<DatabaseColumn> columns = (List<DatabaseColumn>) result.getData();

            //生成代码
            GeneratorParams generatorParams = new GeneratorParams();
            generatorParams.setTableName(tableName);
            generatorParams.setTableComment(paramsMap.get("tableComment") + "");
            generatorParams.setAuthor(params.getAuthor());
            generatorParams.setModuleName(params.getModuleName());
            generatorParams.setPackageName(params.getPackageName());
            generatorParams.setColumns(columns);
            generatorParams.setGeneratorType(params.getGeneratorType());
            GeneratorUtil.generatorCode(generatorParams, zip);
        }

        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }

    public byte[] generatorCode(String dataConnectionId, String[] tableNames, String moduleName, String packageName, String author, String generatorType) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);

        for (String tableName : tableNames) {
            //查询列信息
            Result result = dataConnectionService.getTableColumns(dataConnectionId, tableName);
            if (result.errored()) {
                return null;
            }

            List<DatabaseColumn> columns = (List<DatabaseColumn>) result.getData();

            //生成代码
            GeneratorParams generatorParams = new GeneratorParams();
            generatorParams.setTableName(tableName);
            generatorParams.setAuthor(author);
            generatorParams.setModuleName(moduleName);
            generatorParams.setPackageName(packageName);
            generatorParams.setColumns(columns);
            generatorParams.setGeneratorType(generatorType);

            GeneratorUtil.generatorCode(generatorParams, zip);
        }

        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }
}
