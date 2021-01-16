package com.kfyty.kjte.servlet;

import org.apache.jasper.JasperException;
import org.apache.jasper.servlet.JspCServletContext;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;

import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class JteServletContext extends JspCServletContext {
    private final Map<String, Object> params = new HashMap<>();

    {
        params.put(InstanceManager.class.getName(), new SimpleInstanceManager());
    }

    public JteServletContext(PrintWriter aLogWriter, URL aResourceBaseURL, ClassLoader classLoader, boolean validate, boolean blockExternal) throws JasperException {
        super(aLogWriter, aResourceBaseURL, classLoader, validate, blockExternal);
    }

    @Override
    public Object getAttribute(String name) {
        return params.get(name);
    }
}
