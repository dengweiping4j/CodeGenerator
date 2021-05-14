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
     * 使用自定义模板
     *
     * @param generatorType
     * @return
     */
    public static List<String> getTemplates(String generatorType) {
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
                templates.add("template/mybatis/Controller.java.vm");
                templates.add("template/mybatis/Entity.java.vm");
                break;
            case "mybatis-plus":
                templates.add("template/mybatis-plus/Mapper.java.vm");
                templates.add("template/mybatis-plus/Mapper.xml.vm");
                templates.add("template/mybatis-plus/Service.java.vm");
                templates.add("template/mybatis-plus/Controller.java.vm");
                templates.add("template/mybatis-plus/Entity.java.vm");
                break;
        }
        return templates;
    }

    /**
     * 生成代码
     */
    public static void generatorCode(GeneratorParams generatorParams, ZipOutputStream zip) {
        //配置信息
        Configuration config = getConfig();
        boolean hasBigDecimal = false;
        //表信息
        TableEntity tableEntity = new TableEntity();
        tableEntity.setTableName(generatorParams.getTableName());
        tableEntity.setComments(generatorParams.getTableComment());

        //表名转换成Java类名
        String className = tableToJava(tableEntity.getTableName(), config.getString("tablePrefix"));
        tableEntity.setUpperClassName(className);
        tableEntity.setLowerClassName(StringUtils.uncapitalize(className));

        //列信息
        List<ColumnEntity> columnsList = new ArrayList<>();
        for (DatabaseColumn column : generatorParams.getColumns()) {
            ColumnEntity columnEntity = new ColumnEntity();

            //列名转换成Java属性名
            String attrName = columnToJava(column.getColumnName());
            columnEntity.setUpperAttrName(attrName);
            columnEntity.setLowerAttrName(StringUtils.uncapitalize(attrName));

            //列的数据类型，转换成Java类型
            String attrType = config.getString(column.getColumnType(), "unknowType");
            columnEntity.setAttrType(attrType);
            if (!hasBigDecimal && attrType.equals("BigDecimal")) {
                hasBigDecimal = true;
            }
            //是否主键
            if (column.isPrimary()) {
                tableEntity.setPk(columnEntity);
            }

            columnsList.add(columnEntity);
        }
        tableEntity.setColumns(columnsList);

        //没主键，则第一个字段为主键
        if (tableEntity.getPk() == null) {
            tableEntity.setPk(tableEntity.getColumns().get(0));
        }

        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);
        String mainPath = config.getString("mainPath");
        mainPath = StringUtils.isBlank(mainPath) ? "com.dwp" : mainPath;
        //封装模板数据
        Map<String, Object> map = new HashMap<>();

        map.put("generatorType", generatorParams.getGeneratorType());

        map.put("tableName", tableEntity.getTableName());
        map.put("comments", tableEntity.getComments());
        map.put("pk", tableEntity.getPk());
        map.put("className", tableEntity.getUpperClassName());
        map.put("classname", tableEntity.getLowerClassName());
        map.put("pathName", tableEntity.getLowerClassName().toLowerCase());
        map.put("columns", tableEntity.getColumns());
        map.put("hasBigDecimal", hasBigDecimal);
        map.put("mainPath", mainPath);
        map.put("package", StringUtils.isNotBlank(generatorParams.getPackageName()) ? generatorParams.getPackageName() : config.getString("package"));
        map.put("moduleName", StringUtils.isNotBlank(generatorParams.getModuleName()) ? generatorParams.getModuleName() : config.getString("moduleName"));
        map.put("author", StringUtils.isNotBlank(generatorParams.getAuthor()) ? generatorParams.getAuthor() : config.getString("author"));
        map.put("email", config.getString("email"));
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        VelocityContext context = new VelocityContext(map);

        //获取模板列表
        List<String> templates = getTemplates(generatorParams.getGeneratorType());
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);

            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, tableEntity.getUpperClassName(), StringUtils.isNotBlank(generatorParams.getPackageName()) ? generatorParams.getPackageName() : config.getString("package"), StringUtils.isNotBlank(generatorParams.getModuleName()) ? generatorParams.getModuleName() : config.getString("moduleName"))));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new RuntimeException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
            }
        }
    }


    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String tablePrefix) {
        if (StringUtils.isNotBlank(tablePrefix)) {
            tableName = tableName.replaceFirst(tablePrefix, "");
        }
        return columnToJava(tableName);
    }

    /**
     * 获取配置信息
     */
    public static Configuration getConfig() {
        try {
            return new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new RuntimeException("获取配置文件失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String templateName, String className, String packageName, String moduleName) {
        String packagePath = "main" + File.separator + "java" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator + moduleName + File.separator;
        }

        if (StringUtils.isNotBlank(templateName)) {
            String afterClassName = templateName.substring(templateName.lastIndexOf("/") + 1, templateName.indexOf("."));

            if (templateName.contains("template/jpa/Specifications.java.vm")) {
                return packagePath + "repository" + File.separator + className + "Specifications.java";
            } else if (templateName.contains("template/mybatis/Mapper.xml.vm") || templateName.contains("template/mybatis-plus/Mapper.xml.vm")) {
                return packagePath + afterClassName.toLowerCase() + File.separator + className + afterClassName + ".xml";
            } else if (templateName.contains("template/jpa/Domain.java.vm")
                    || templateName.contains("template/mybatis/Entity.java.vm")
                    || templateName.contains("template/mybatis-plus/Entity.java.vm")) {
                return packagePath + afterClassName.toLowerCase() + File.separator + className + ".java";
            } else {
                return packagePath + afterClassName.toLowerCase() + File.separator + className + afterClassName + ".java";
            }
        }

        return null;
    }
}
