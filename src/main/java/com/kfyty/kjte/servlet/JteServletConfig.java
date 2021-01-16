package com.kfyty.kjte.servlet;

import com.kfyty.kjte.config.JstlTemplateEngineConfig;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class JteServletConfig implements ServletConfig {
    private final JstlTemplateEngineConfig config;
    private final String jspName;
    private final ServletContext servletContext;
    private final Map<String, String> params = new HashMap<>();

    {
        params.put("suppressSmap", "true");
    }

    public JteServletConfig(File jsp, ServletContext servletContext, JstlTemplateEngineConfig config) {
        this.jspName = jsp.getName();
        this.servletContext = servletContext;
        this.config = config;
        params.put("scratchdir", config.getTempOutPutDir());
    }

    @Override
    public String getServletName() {
        return this.jspName;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        return this.params.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(params.keySet());
    }
}
