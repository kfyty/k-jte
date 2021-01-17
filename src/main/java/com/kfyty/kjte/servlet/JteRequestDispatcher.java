package com.kfyty.kjte.servlet;

import com.kfyty.kjte.JstlRenderEngine;
import com.kfyty.kjte.JstlTemplateEngine;
import com.kfyty.kjte.config.JstlTemplateEngineConfig;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JteRequestDispatcher implements RequestDispatcher {
    private final String path;
    private final JstlTemplateEngineConfig config;

    public JteRequestDispatcher(String path, JstlTemplateEngineConfig config) {
        this.path = path;
        this.config = config;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        // 生成 include 指令配置
        JstlTemplateEngineConfig includeConfig = new JstlTemplateEngineConfig(config.getTempOutPutDir(), config.getTemplatePath() + path);
        includeConfig.setTemplatePath(config.getTemplatePath() + path.substring(0, path.lastIndexOf("/")));
        includeConfig.putVar(config.getVariables());

        // 生成 include 文件的字节码
        JstlTemplateEngine engine = new JstlTemplateEngine(includeConfig);
        List<String> includeClass = engine.compiler();

        // 渲染 include 指令文件并写入 response
        JstlRenderEngine renderEngine = new JstlRenderEngine(includeClass, includeConfig);
        renderEngine.doRenderHtml();
        JteResponseFacade responseFacade = (JteResponseFacade) renderEngine.getResponseFacade();
        byte[] bytes = responseFacade.getStringWriter().toString().getBytes(StandardCharsets.ISO_8859_1);
        response.getWriter().print(new String(bytes, StandardCharsets.UTF_8));
    }
}
