package com.kfyty.kjte.servlet;

import com.kfyty.kjte.config.JstlTemplateEngineConfig;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class JteServletConfig implements ServletConfig {
    private final String jspName;
    private final ServletContext servletContext;
    private final Map<String, String> params = new HashMap<>();

    {
        params.put("suppressSmap", "true");
    }

    public JteServletConfig(String jspName, ServletContext servletContext, JstlTemplateEngineConfig config) {
        this.jspName = jspName.endsWith(".jsp") ? jspName : jspName + ".jsp";
        this.servletContext = servletContext;
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
