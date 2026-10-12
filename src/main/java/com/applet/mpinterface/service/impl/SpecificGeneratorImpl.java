package com.applet.mpinterface.service.impl;

import com.applet.mpinterface.domain.pojo.Rundetails;
import com.applet.mpinterface.mapper.RundetailsMapper;
import com.applet.mpinterface.service.ISpecificGenerator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.SourceVersion;

@Service
public class SpecificGeneratorImpl extends ServiceImpl<RundetailsMapper, Rundetails> implements ISpecificGenerator {
    @Autowired
    private RundetailsMapper rundetailsMapper;

    @Override
    public void generateAllDetailgetterCode(String id, Integer actionTime, String path, String appletName) {
        requireIdentifier(appletName, "appletName");

        //获取到对应条的执行记录，从而获得各个json中的数据
        QueryWrapper<Rundetails> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).eq("action_time", actionTime);
        // 仅获取最新的一条时queryWrapper换成下面的这个就行
//      queryWrapper.eq("id", id).orderByDesc("action_time").last("LIMIT 1");

        Rundetails rundetails = rundetailsMapper.selectOne(queryWrapper);
        if (rundetails == null) {
            throw new IllegalArgumentException("No execution detail matches the requested id and action time");
        }
        // 使用 Jackson 的 ObjectMapper 来处理 JSON
        ObjectMapper objectMapper = new ObjectMapper();

        //Freemarker生成代码文件的相关
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setClassForTemplateLoading(RundetailsServiceImpl.class, "/templates");

        // 创建数据模型,数据模型put的list需要五个类别的if都运行完才完
        Map<String, Object> dataModel = new HashMap<>();
        // 创建一个可变的 ArrayList
        List<Map<String, String>> fieldsList = new ArrayList<>();
        try {
            //实际用的应该是下面这个模板
            // 获取模板
            Template impl_template = cfg.getTemplate("detailinfo_impl_template.ftl");
            Template service_template = cfg.getTemplate("detailinfo_service_template.ftl");
            Template rimpl_template = cfg.getTemplate("registry_impl_template.ftl");
            Template rservice_template = cfg.getTemplate("registry_service_template.ftl");
            Template controller_template = cfg.getTemplate("controller_template.ftl");
            if (rundetails.getTriggerNum() != 0) {
                String triggerDetail = rundetails.getTrigger();
                JsonNode jsonNode = objectMapper.readTree(triggerDetail.replaceAll("'", "\"").replaceAll("None", "null"));
                // 如果是数组，你可以使用下面的方式遍历
                if (jsonNode.isArray()) {
                    for (JsonNode element : jsonNode) {
                        // 获取 ingredients 数组
                        JsonNode ingredientsArray = element.get("ingredients");
                        // 遍历数组，提取每个对象的 label 值
                        for (JsonNode ingredient : ingredientsArray) {
                            String label = labelOf(ingredient);
                            Map<String, String> field = new HashMap<>();
                            field.put("name", label);
                            field.put("type", "Trigger");
                            fieldsList.add(field);
                            System.out.println("Label: " + label);
                        }

                    }
                }
            }
            if (rundetails.getFilterNum() != 0) {
                String filterDetail = rundetails.getFilter();
                JsonNode jsonNode = objectMapper.readTree(filterDetail.replaceAll("'", "\"").replaceAll("None", "null"));

                // 如果是数组，你可以使用下面的方式遍历
                if (jsonNode.isArray()) {
                    for (JsonNode element : jsonNode) {
                        // 获取 ingredients 数组
                        JsonNode ingredientsArray = element.get("ingredients");
                        // 遍历数组，提取每个对象的 label 值
                        for (JsonNode ingredient : ingredientsArray) {
                            String label = labelOf(ingredient);
                            Map<String, String> field = new HashMap<>();
                            field.put("name", label);
                            field.put("type", "Filter");
                            fieldsList.add(field);
                            System.out.println("Label: " + label);
                        }

                    }
                }
            }
            if (rundetails.getDelayNum() != 0) {
                String delayDetail = rundetails.getDelay();
                JsonNode jsonNode = objectMapper.readTree(delayDetail.replaceAll("'", "\"").replaceAll("None", "null"));
                // 如果是数组，你可以使用下面的方式遍历
                if (jsonNode.isArray()) {
                    for (JsonNode element : jsonNode) {
                        // 获取 ingredients 数组
                        JsonNode ingredientsArray = element.get("ingredients");
                        // 遍历数组，提取每个对象的 label 值
                        for (JsonNode ingredient : ingredientsArray) {
                            String label = labelOf(ingredient);
                            Map<String, String> field = new HashMap<>();
                            field.put("name", label);
                            field.put("type", "Delay");
                            fieldsList.add(field);
                            System.out.println("Label: " + label);
                        }

                    }
                }
            }
            if (rundetails.getQueryNum() != 0) {
                String queryDetail = rundetails.getQuery();
                JsonNode jsonNode = objectMapper.readTree(queryDetail.replaceAll("'", "\"").replaceAll("None", "null"));

                // 如果是数组，你可以使用下面的方式遍历
                if (jsonNode.isArray()) {
                    for (JsonNode element : jsonNode) {
                        // 获取 fields 数组
                        JsonNode ingredientsArray = element.get("fields");
                        // 遍历数组，提取每个对象的 label 值
                        if (ingredientsArray != null) {
                            for (JsonNode ingredient : ingredientsArray) {
                                String label = labelOf(ingredient);
                                Map<String, String> field = new HashMap<>();
                                field.put("name", label);
                                field.put("type", "Query");
                                fieldsList.add(field);
                                System.out.println("Label: " + label);
                            }
                        }
                        // 获取 query_ingredients 数组
                        JsonNode query_ingredientsArray = element.get("query_ingredients");
                        // 遍历数组，提取每个对象的 label 值
                        if (query_ingredientsArray != null) {
                            for (JsonNode outer_ingredient : query_ingredientsArray) {
                                for (JsonNode ingredient : outer_ingredient) {
                                    String label = labelOf(ingredient);
                                    Map<String, String> field = new HashMap<>();
                                    field.put("name", label);
                                    field.put("type", "Query");
                                    fieldsList.add(field);

                                    System.out.println("Label: " + label);
                                }
                            }

                        }
                    }
                }
            }
            //action中的数据格式和其他的不一样
            if (rundetails.getActionNum() != 0) {
                String actionDetail = rundetails.getAction();
                JsonNode jsonNode = objectMapper.readTree(actionDetail.replaceAll("'", "\"").replaceAll("None", "null").replaceAll("\"\\{\"", "\"").replaceAll("\"}\"", "\"").replaceAll("t\":\"5", ":").replaceAll("l\":\"H", "l:H").replaceAll("3\",\"d", "3,d"));

                // 如果是数组，你可以使用下面的方式遍历
                if (jsonNode.isArray()) {
                    for (JsonNode element : jsonNode) {
                        // 获取 action_fields 数组
                        JsonNode ingredientsArray = element.get("action_fields");
                        // 遍历数组，提取每个对象的 label 值
                        for (JsonNode ingredient : ingredientsArray) {
                            String label = labelOf(ingredient);
                            Map<String, String> field = new HashMap<>();
                            field.put("name", label);
                            field.put("type", "Action");
                            fieldsList.add(field);
                            System.out.println("Label: " + label);
                        }
//                        // 获取 unfiltered_action_fields 数组,好像和上面是重复的
//                        JsonNode unfiltered_action_fields = element.get("action_fields");
//                        // 遍历数组，提取每个对象的 label 值
//                        for (JsonNode ingredient : unfiltered_action_fields) {
//                            String label = labelOf(ingredient);
//                            fieldsList.add(Map.of("name", label,"type","Action"));
//                            System.out.println("Label: " + label);
//                        }

                    }
                }
            }

            validateFields(fieldsList);
            dataModel.put("fields", fieldsList);
            dataModel.put("appletName", appletName);
            System.out.println(fieldsList);
            Path outputRoot = Paths.get(path).toAbsolutePath().normalize();
            Files.createDirectories(outputRoot);
            if (Files.isSymbolicLink(outputRoot)) {
                throw new IllegalArgumentException("output root cannot be a symbolic link");
            }
            outputRoot = outputRoot.toRealPath(LinkOption.NOFOLLOW_LINKS);
            Map<Path, String> rendered = new HashMap<>();
            rendered.put(confined(outputRoot, "service/impl/SpecificServiceImpl.java"), render(impl_template, dataModel));
            rendered.put(confined(outputRoot, "service/ISpecificService.java"), render(service_template, dataModel));
            rendered.put(confined(outputRoot, "service/impl/" + appletName + "ServiceImpl.java"), render(rimpl_template, dataModel));
            rendered.put(confined(outputRoot, "iotruleservice/I" + appletName + "Service.java"), render(rservice_template, dataModel));
            rendered.put(confined(outputRoot, "controller/SpecificController.java"), render(controller_template, dataModel));
            for (Map.Entry<Path, String> file : rendered.entrySet()) {
                writeAtomic(outputRoot, file.getKey(), file.getValue());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Provider source generation failed", e);
        }
    }

    private static String labelOf(JsonNode ingredient) {
        JsonNode label = ingredient == null ? null : ingredient.get("label");
        if (label == null || !label.isTextual()) throw new IllegalArgumentException("label must be a string");
        String value = label.asText();
        if (!value.matches("[A-Za-z_$][A-Za-z0-9_$]{0,63}")
                || !SourceVersion.isIdentifier(value) || SourceVersion.isKeyword(value)) {
            throw new IllegalArgumentException("label must be a bounded Java method identifier");
        }
        return value;
    }

    private static void validateFields(List<Map<String, String>> fields) {
        Set<String> methods = new HashSet<>();
        Set<String> getters = new HashSet<>();
        for (Map<String, String> field : fields) {
            String name = field.get("name");
            String getter = "select" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            if (!methods.add(name) || !getters.add(getter)) {
                throw new IllegalArgumentException("labels produce duplicate Java methods");
            }
            field.put("literal", javaLiteral(name));
        }
    }

    private static String javaLiteral(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t") + "\"";
    }

    private static void requireIdentifier(String value, String field) {
        if (value == null || !value.matches("[A-Z][A-Za-z0-9]{0,63}")) {
            throw new IllegalArgumentException(field + " must be a Java class identifier");
        }
    }

    private static Path confined(Path root, String relative) {
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root)) throw new IllegalArgumentException("generated path escapes output root");
        return target;
    }

    private static String render(Template template, Map<String, Object> dataModel) throws Exception {
        StringWriter writer = new StringWriter();
        template.process(dataModel, writer);
        return writer.toString();
    }

    interface AtomicMover {
        void move(Path source, Path destination) throws IOException;
    }

    private static void writeAtomic(Path root, Path target, String value) throws IOException {
        writeAtomic(root, target, value, (source, destination) -> Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE));
    }

    static void writeAtomic(Path root, Path target, String value, AtomicMover mover) throws IOException {
        Files.createDirectories(target.getParent());
        Path current = root;
        for (Path segment : root.relativize(target.getParent())) {
            current = current.resolve(segment);
            if (Files.isSymbolicLink(current)) throw new IllegalArgumentException("symbolic link path segment rejected");
        }
        Path temporary = Files.createTempFile(target.getParent(), ".generated-", ".tmp");
        try {
            Files.write(temporary, value.getBytes(StandardCharsets.UTF_8));
            mover.move(temporary, target);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
