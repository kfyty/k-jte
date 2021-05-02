# k-jte
    jsp 模板引擎。无需启动 tomcat，直接根据 jsp 源文件渲染得到 html 页面；如果 jsp 模板生成的是 java 代码，则支持对 java 代码进行二次编译。

## 简介
本项目是一个基于 jsp 的模板引擎。虽然说 jsp 本质是 servlet，但是这个项目达到了不需要 tomcat，
而直接将 jsp 源文件渲染为 html 的功能。因此从某种意义上，可以说它是由 servlet 实现的模板引擎。
另外，借助于 jsp 编译引擎，如果 jsp 生成的是 java 代码，还支持对生成的 java 代码进行二次编译。

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

3.1 在 template 文件夹内创建 jsp 文件，支持 jstl 标签，暂时不支持 jsp 指令；
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

3.2 在 template 文件夹内创建 jsp 文件，此示例为生成 java 代码并二次编译，其中的接口是为了方便调用；
```java
package com.kfyty.jsp.template;

import com.kfyty.demo.template.HelloWorld;

public class HelloWorldImpl implements HelloWorld {
    @Override
    public void hello(String name) {
        System.out.println("hello: " + name);
    }
}
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
        renderHtml();
    }

    /**
     * 渲染 html 示例
     */
    private static void renderHtml() {
        String savePath = "C:\\Users\\fyty\\Desktop";
        JstlTemplateEngineConfig config = new JstlTemplateEngineConfig(savePath, "/template");

        // 模板变量
        config.putVar("title", "test");

        // 模板引擎
        JstlTemplateEngine engine = new JstlTemplateEngine(config);

        // 渲染引擎
        JstlRenderEngine renderEngine = new JstlRenderEngine(engine, engine.load());
        renderEngine.doRenderTemplate();
    }

    /**
     * 二次编译生成的 java 代码示例
     * 该方法运行后将输出：hello: tom
     */
    public static void renderJava() {
        // 设置生成 java 代码的模板，并设置为需要二次编译
        JstlTemplateEngineConfig config = new JstlTemplateEngineConfig("/template/HelloWorldImpl.jsp");
        config.setCompiler(true);
        config.setSuffix(".java");
        
        JstlTemplateEngine engine = new JstlTemplateEngine(config);
        JstlRenderEngine renderEngine = new JstlRenderEngine(engine, engine.load());
        renderEngine.doRenderTemplate();
        
        // 获取二次编译生成 Class 对象并实例化后，以接口调用
        HelloWorld helloWorld = (HelloWorld) engine.getCompileClass().get("com.kfyty.jsp.template.HelloWorldImpl").newInstance();
        helloWorld.hello("tom");
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
