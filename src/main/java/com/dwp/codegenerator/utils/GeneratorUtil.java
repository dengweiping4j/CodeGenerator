package com.dwp.codegenerator.utils;

import com.dwp.codegenerator.domain.ColumnEntity;
import com.dwp.codegenerator.domain.DatabaseColumn;
import com.dwp.codegenerator.domain.GeneratorParams;
import com.dwp.codegenerator.domain.TableEntity;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GeneratorUtil {

    /**
     * 生成代码
     *
     * @param generatorParams
     * @param zip
     */
    public static void generatorCode(GeneratorParams generatorParams, ZipOutputStream zip) {
        //参数处理
        TableEntity tableEntity = formatParams(generatorParams);
        //设置velocity资源加载器
        initVelocity();
        //封装模板数据
        VelocityContext context = getVelocityContext(generatorParams, tableEntity);
        //渲染模板
        apply(context, zip, tableEntity, generatorParams);
    }

    private static void apply(VelocityContext context, ZipOutputStream zip, TableEntity tableEntity, GeneratorParams generatorParams) {
        List<String> templates = getTemplates(generatorParams.getGeneratorType());
        templates.forEach(template -> {
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);
            try {
                String fileName = getFileName(template, tableEntity.getUpperClassName(), generatorParams);
                //添加到zip
                zip.putNextEntry(new ZipEntry(fileName));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new RuntimeException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
            }
        });
    }

    /**
     * 使用自定义模板
     *
     * @param generatorType
     * @return
     */
    private static List<String> getTemplates(String generatorType) {
        List<String> templates = new ArrayList<>();
        switch (generatorType) {
            case "jpa":
                templates.add("template/jpa/Repository.java.vm");
                templates.add("template/jpa/Specifications.java.vm");
                templates.add("template/jpa/Service.java.vm");
                templates.add("template/jpa/Controller.java.vm");
                templates.add("template/jpa/Domain.java.vm");
                break;
            case "mybatis":
                templates.add("template/mybatis/Mapper.java.vm");
                templates.add("template/mybatis/Mapper.xml.vm");
                templates.add("template/mybatis/Service.java.vm");
                templates.add("template/mybatis/ServiceImpl.java.vm");
                templates.add("template/mybatis/Controller.java.vm");
                templates.add("template/mybatis/Entity.java.vm");
                templates.add("template/mybatis/EntityParam.java.vm");
                templates.add("template/mybatis/PageResult.java.vm");
                templates.add("template/mybatis/RestResp.java.vm");
                break;
            case "mybatis-plus":
                templates.add("template/mybatis-plus/Mapper.java.vm");
                templates.add("template/mybatis-plus/Mapper.xml.vm");
                templates.add("template/mybatis-plus/Service.java.vm");
                templates.add("template/mybatis-plus/ServiceImpl.java.vm");
                templates.add("template/mybatis-plus/Controller.java.vm");
                templates.add("template/mybatis-plus/Entity.java.vm");
                templates.add("template/mybatis-plus/EntityParam.java.vm");
                templates.add("template/mybatis-plus/PageResult.java.vm");
                templates.add("template/mybatis-plus/RestResp.java.vm");
                break;
        }
        return templates;
    }


    private static String getPackagePath(GeneratorParams generatorParams) {
        //配置信息
        Configuration config = getConfig();
        String packageName = StringUtils.isNotBlank(generatorParams.getPackageName())
                ? generatorParams.getPackageName()
                : config.getString("package");
        String moduleName = StringUtils.isNotBlank(generatorParams.getModuleName())
                ? generatorParams.getModuleName()
                : config.getString("moduleName");
        String packagePath = "main" + File.separator + "java" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator + moduleName + File.separator;
        }
        return packagePath;
    }

    private static VelocityContext getVelocityContext(GeneratorParams generatorParams, TableEntity tableEntity) {
        Configuration config = getConfig();
        Map<String, Object> map = new HashMap<>();
        map.put("generatorType", generatorParams.getGeneratorType());
        map.put("tableName", tableEntity.getTableName());
        map.put("comments", tableEntity.getComments());
        map.put("pk", tableEntity.getPk());
        map.put("className", tableEntity.getUpperClassName());
        map.put("classname", tableEntity.getLowerClassName());
        map.put("pathName", tableEntity.getLowerClassName().toLowerCase());
        map.put("columns", tableEntity.getColumns());
        map.put("mainPath", StringUtils.isBlank(config.getString("mainPath")) ? "com.dwp" : config.getString("mainPath"));
        map.put("package", StringUtils.isNotBlank(generatorParams.getPackageName()) ? generatorParams.getPackageName() : config.getString("package"));
        map.put("moduleName", StringUtils.isNotBlank(generatorParams.getModuleName()) ? generatorParams.getModuleName() : config.getString("moduleName"));
        map.put("author", StringUtils.isNotBlank(generatorParams.getAuthor()) ? generatorParams.getAuthor() : config.getString("author"));
        map.put("email", config.getString("email"));
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        VelocityContext context = new VelocityContext(map);
        return context;
    }

    private static void initVelocity() {
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);
    }

    /**
     * 表、字段参数处理
     *
     * @param generatorParams
     * @return
     */
    private static TableEntity formatParams(GeneratorParams generatorParams) {
        TableEntity tableEntity = new TableEntity();
        //表信息
        setTableEntity(tableEntity, generatorParams);
        //设置列信息
        setColumns(tableEntity, generatorParams);
        //没主键，则第一个字段为主键
        if (tableEntity.getPk() == null) {
            tableEntity.setPk(tableEntity.getColumns().get(0));
        }
        return tableEntity;
    }

    private static void setColumns(TableEntity tableEntity, GeneratorParams generatorParams) {
        List<ColumnEntity> columnsList = new ArrayList<>();
        for (DatabaseColumn column : generatorParams.getColumns()) {
            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setColumnName(column.getColumnName());
            //列名转换成Java属性名
            String attrName = columnToJava(column.getColumnName());
            columnEntity.setUpperAttrName(attrName);
            columnEntity.setLowerAttrName(StringUtils.uncapitalize(attrName));
            columnEntity.setComments(column.getColumnComment());

            //列的数据类型，转换成Java类型
            Configuration config = getConfig();
            String attrType = config.getString(column.getColumnType(), "unknowType");
            columnEntity.setAttrType(attrType);
            //是否主键
            if (column.isPrimary()) {
                tableEntity.setPk(columnEntity);
            }
            columnsList.add(columnEntity);
        }
        tableEntity.setColumns(columnsList);
    }

    private static void setTableEntity(TableEntity tableEntity, GeneratorParams generatorParams) {
        tableEntity.setTableName(generatorParams.getTableName());
        tableEntity.setComments(generatorParams.getTableComment());
        //表名转换成Java类名
        Configuration config = getConfig();
        String className = tableToJava(tableEntity.getTableName(), config.getString("tablePrefix"));
        tableEntity.setUpperClassName(className);
        tableEntity.setLowerClassName(StringUtils.uncapitalize(className));
    }

    /**
     * 列名转换成Java属性名
     */
    private static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    /**
     * 表名转换成Java类名
     */
    private static String tableToJava(String tableName, String tablePrefix) {
        if (StringUtils.isNotBlank(tablePrefix)) {
            tableName = tableName.replaceFirst(tablePrefix, "");
        }
        return columnToJava(tableName);
    }

    /**
     * 获取配置信息
     */
    private static Configuration getConfig() {
        try {
            return new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new RuntimeException("获取配置文件失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    private static String getFileName(String templateName, String className, GeneratorParams generatorParams) {
        String packagePath = getPackagePath(generatorParams);
        if (StringUtils.isNotBlank(templateName)) {
            String afterClassName = templateName.substring(templateName.lastIndexOf("/") + 1, templateName.indexOf("."));
            if (templateName.contains("template/jpa/Specifications.java.vm")) {
                return packagePath + "repository" + File.separator + className + "Specifications.java";
            }
            if (templateName.endsWith("Mapper.xml.vm")) {
                return packagePath + afterClassName.toLowerCase() + File.separator + className + afterClassName + ".xml";
            }
            if (templateName.contains("template/jpa/Domain.java.vm")
                    || templateName.endsWith("Entity.java.vm")) {
                return packagePath + afterClassName.toLowerCase() + File.separator + className + ".java";
            }
            if (templateName.endsWith("EntityParam.java.vm")) {
                return packagePath + "entity/param" + File.separator + className + "Param.java";
            }
            if (templateName.endsWith("ServiceImpl.java.vm")) {
                return packagePath + "service/impl" + File.separator + className + afterClassName + ".java";
            }
            if (templateName.endsWith("PageResult.java.vm")) {
                return packagePath + "util" + File.separator + "PageResult.java";
            }
            if (templateName.endsWith("RestResp.java.vm")) {
                return packagePath + "util" + File.separator + "RestResp.java";
            }
            return packagePath + afterClassName.toLowerCase() + File.separator + className + afterClassName + ".java";
        }
        return null;
    }
}
