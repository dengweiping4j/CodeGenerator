package com.dwp.codegenerator.controller;

import com.dwp.codegenerator.domain.dto.GeneratorParamsDTO;
import com.dwp.codegenerator.service.GeneratorService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 代码生成控制器
 *
 * @author dengweiping
 * @date 2021/1/8 15:04
 */
@RestController
@RequestMapping("/api/generator")
public class GenerateCodeController {

    @Autowired
    private GeneratorService generatorService;

    /**
     * 生成代码
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "生成代码", notes = "需要表名、包名、作者等信息", produces = "application/json")
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功")})
    public void code(@RequestBody GeneratorParamsDTO params, HttpServletResponse response) throws IOException {
        byte[] data = generatorService.generatorCode(params);

        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"generator-code.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");

        IOUtils.write(data, response.getOutputStream());
    }

    /**
     * 生成代码
     */
    @RequestMapping("/code")
    public void code(String dataConnectionId, String tables, String moduleName, String packageName, String author, String generatorType, HttpServletResponse response) throws IOException {
        byte[] data = generatorService.generatorCode(dataConnectionId, tables.split(","), moduleName, packageName, author, generatorType);

        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"generator-code.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");

        IOUtils.write(data, response.getOutputStream());
    }

}
