package com.kfyty.kjte.servlet;

import com.kfyty.kjte.config.JstlTemplateEngineConfig;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Options;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.servlet.JspServletWrapper;
import org.apache.tomcat.Jar;

import javax.servlet.ServletContext;
import java.io.InputStream;
import java.util.Date;

public class JteJspCompilationContext extends JspCompilationContext {
    private final JstlTemplateEngineConfig config;

    public JteJspCompilationContext(String jspUri, Options options, ServletContext context, JspServletWrapper jsw, JspRuntimeContext rctxt, JstlTemplateEngineConfig config) {
        super(jspUri, options, context, jsw, rctxt);
        this.config = config;
    }

    @Override
    public InputStream getResourceAsStream(String res) {
        return this.getClass().getResourceAsStream(res);
    }

    @Override
    public Long getLastModified(String resource, Jar tagJar) {
        return new Date().getTime();
    }

    @Override
    public String getOutputDir() {
        return config.getTempOutPutDir();
    }
}
