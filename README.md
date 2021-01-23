# k-jte
    jsp 模板引擎。无需启动 tomcat，直接根据 jsp 源文件渲染得到 html 页面。

## 简介
本项目是一个基于 jsp 的模板引擎。虽然说 jsp 本质是 servlet，但是这个项目达到了不需要 tomcat，
而直接将 jsp 源文件渲染为 html 的功能。因此从某种意义上，可以说它是由 servlet 实现的模板引擎。

## 示例

1、在项目中添加如下依赖；
```xml
<dependency>
    <groupId>com.kfyty</groupId>
    <artifactId>k-jte</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

2、在 resources 资源文件夹下创建 template 文件夹；

3、在 template 文件夹内创建 jsp 文件，支持 jstl 标签，暂时不支持 jsp 指令；
```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>jsp 模板引擎</title>
</head>
<body>
    <h1>${title}</h1>
    <c:forEach begin="1" end="5" step="1" var="index">
        <span>index: ${index}</span>
    </c:forEach>
</body>
</html>
```

4、运行一下实例代码；
```java
package com.kfyty.demo;

import com.kfyty.kjte.JstlRenderEngine;
import com.kfyty.kjte.JstlTemplateEngine;
import com.kfyty.kjte.config.JstlTemplateEngineConfig;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        String savePath = "C:\\Users\\fyty\\Desktop";
        JstlTemplateEngineConfig config = new JstlTemplateEngineConfig(savePath, "/template");

        // 模板变量
        config.putVar("title", "test");

        // 模板引擎
        JstlTemplateEngine engine = new JstlTemplateEngine(config);
        List<String> compiler = engine.compile();

        // 渲染引擎
        JstlRenderEngine renderEngine = new JstlRenderEngine(compiler, config);
        renderEngine.doRenderHtml();
    }
}
```

5、查看生成的 html。
```html
<!DOCTYPE HTML>
<html>
<head>
    <title>jsp 模板引擎</title>
</head>
<body>
    <h1>test</h1>
    
        <span>index: 1</span>
    
        <span>index: 2</span>
    
        <span>index: 3</span>
    
        <span>index: 4</span>
    
        <span>index: 5</span>
    
</body>
</html>
```
