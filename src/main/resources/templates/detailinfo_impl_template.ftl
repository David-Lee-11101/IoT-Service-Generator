
package com.${appletName}.mpinterface.service.impl;

import com.applet.mpinterface.domain.pojo.Rundetails;
import com.applet.mpinterface.mapper.RundetailsMapper;

import com.${appletName}.mpinterface.service.ISpecificService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;

/**
* <p>
    *  服务实现类
    * </p>
*
* @author nana
* @since 2024-01-25
*/

@Lazy
@Service
public class SpecificServiceImpl extends ServiceImpl<RundetailsMapper, Rundetails> implements ISpecificService {
@Autowired
private RundetailsMapper rundetailsMapper;
// 选择具体的select值
<#list fields as field>
    public String select${field.name?cap_first}(String id,Integer actionTime)
    {
    QueryWrapper<Rundetails> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("id", id).eq("action_time",actionTime);
    //如果只要最新的一条，用下面这个就行
    //queryWrapper.eq("id", id).orderByDesc("action_time").last("LIMIT 1");
    Rundetails rundetails = rundetailsMapper.selectOne(queryWrapper);
    String ${field.type}Detail=rundetails.get${field.type}();//需要根据实际detail的类型修改模板这里，需要填入值
    // 使用 Jackson 的 ObjectMapper 来处理 JSON
    ObjectMapper objectMapper = new ObjectMapper();
    try {
    JsonNode jsonNode = objectMapper.readTree(${field.type}Detail.replaceAll("'", "\"").replaceAll("None", "null").replaceAll("\"\\{\"","\"").replaceAll("\"}\"","\"").replaceAll("t\":\"5",":").replaceAll("l\":\"H","l:H").replaceAll("3\",\"d","3,d"));
    // 如果是数组，你可以使用下面的方式遍历，后面的遍历需要根据类型改变
    if (jsonNode.isArray()) {
    for (JsonNode element : jsonNode) {
    <#if field.type == "Trigger" || field.type == "Filter" || field.type == "Delay">
        // 获取 ingredients 数组
        JsonNode ingredientsArray = element.get("ingredients");
        if (ingredientsArray != null && ingredientsArray.isArray()) {
        // 遍历数组，查找 label 为 "${field.name}" 的对象
        for (JsonNode ingredient : ingredientsArray)
          {
             if (${field.literal}.equals(ingredient.get("label").asText()))
                 {
                    // 找到了匹配的对象，获取对应的值
                   String relatedValue = ingredient.get("value").asText();
                   System.out.println(${field.literal} + ": " + relatedValue);
                    return  relatedValue;// 如果找到了就退出循环
                 }
          }
        }

    <#elseif field.type=="Query">
        // 获取 fields 数组
        JsonNode ingredientsArray = element.get("fields");
        if (ingredientsArray != null && ingredientsArray.isArray()) {
        // 遍历数组，查找 label 为 "${field.name}" 的对象
        for (JsonNode ingredient : ingredientsArray)
        {
        if (${field.literal}.equals(ingredient.get("label").asText()))
        {
        // 找到了匹配的对象，获取对应的值
        String relatedValue = ingredient.get("value").asText();
        System.out.println(${field.literal} + ": " + relatedValue);
        return  relatedValue;// 如果找到了就退出循环
        }
        }
        }

        // 获取 query_ingredients 数组
        JsonNode query_ingredientsArray = element.get("query_ingredients");
        if (query_ingredientsArray != null && query_ingredientsArray.isArray()) {
        // 遍历数组，查找 label 为 "${field.name}" 的对象
        for (JsonNode outer_ingredient : query_ingredientsArray)
        for (JsonNode ingredient : outer_ingredient)
        {
        if (${field.literal}.equals(ingredient.get("label").asText()))
        {
        // 找到了匹配的对象，获取对应的值
        String relatedValue = ingredient.get("value").asText();
        System.out.println(${field.literal} + ": " + relatedValue);
        return  relatedValue;// 如果找到了就退出循环
        }
        }
        }
     <#else >
        // 获取 action_fields 数组
        JsonNode ingredientsArray = element.get("action_fields");
        if (ingredientsArray != null && ingredientsArray.isArray()) {
        // 遍历数组，查找 label 为 "${field.name}" 的对象
        for (JsonNode ingredient : ingredientsArray)
        {
        if (${field.literal}.equals(ingredient.get("label").asText()))
        {
        // 找到了匹配的对象，获取对应的值
        String relatedValue = ingredient.get("value").asText();
        System.out.println(${field.literal} + ": " + relatedValue);
        return  relatedValue;// 如果找到了就退出循环
        }
        }
        }
     </#if>
        }return null;


        }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
        }
</#list>
}
