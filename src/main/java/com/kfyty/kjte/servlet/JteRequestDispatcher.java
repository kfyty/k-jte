package com.kfyty.kjte.servlet;

import com.kfyty.kjte.JstlRenderEngine;
import com.kfyty.kjte.JstlTemplateEngine;
import com.kfyty.kjte.config.JstlTemplateEngineConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.Parameters;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;

public class JteRequestDispatcher implements RequestDispatcher {
    private final String path;
    private final JstlTemplateEngineConfig config;

    public JteRequestDispatcher(String path, JstlTemplateEngineConfig config) {
        this.path = path;
        this.config = config;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws IOException {
        // 生成 include 指令配置
        String includePath = (path.startsWith(config.getTemplatePath()) ? "" : config.getTemplatePath()) + path.substring(0, path.lastIndexOf(".jsp") + 4);
        String templatePath = (path.startsWith(config.getTemplatePath()) ? "" : config.getTemplatePath()) + path.substring(0, path.lastIndexOf("/"));
        JstlTemplateEngineConfig includeConfig = new JstlTemplateEngineConfig(config.getTempOutPutDir(), includePath);
        includeConfig.setTemplatePath(templatePath);
        includeConfig.putVar(config.getVariables());
        this.resolveIncludeParams(request, path, includeConfig);

        // 生成并加载 include 文件的字节码
        JstlTemplateEngine engine = new JstlTemplateEngine(includeConfig);
        List<Class<?>> includeClass = engine.load();

        // 渲染 include 指令文件并写入 response
        JstlRenderEngine renderEngine = new JstlRenderEngine(engine, includeClass);
        renderEngine.doRenderTemplate();
        JteResponseFacade responseFacade = (JteResponseFacade) renderEngine.getResponseFacade();
        response.getWriter().print(responseFacade.getString());
    }

    private void resolveIncludeParams(ServletRequest request, String path, JstlTemplateEngineConfig config) {
        if (!path.contains(".jsp?")) {
            return;
        }
        String params = path.substring(path.lastIndexOf(".jsp") + 5);
        MessageBytes messageBytes = MessageBytes.newInstance();
        messageBytes.setString(params);
        messageBytes.setCharset(Charset.forName(request.getCharacterEncoding()));
        Parameters parameters = new Parameters();
        parameters.setQuery(messageBytes);
        parameters.setCharset(Charset.forName(request.getCharacterEncoding()));
        Enumeration<String> parameterNames = parameters.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String[] values = parameters.getParameterValues(key);
            config.putVar(key, values != null && values.length == 1 ? values[0] : values);
        }
    }
}
