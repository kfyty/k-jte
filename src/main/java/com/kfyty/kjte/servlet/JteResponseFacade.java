package com.kfyty.kjte.servlet;

import com.kfyty.kjte.config.JstlTemplateEngineConfig;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.ResponseFacade;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class JteResponseFacade extends ResponseFacade {
    private final String jspName;
    private final JstlTemplateEngineConfig config;
    private final StringWriter stringWriter = new StringWriter();
    private final PrintWriter printWriter = new PrintWriter(stringWriter, true);

    public JteResponseFacade(Response response, String classFile, JstlTemplateEngineConfig config) {
        super(response);
        this.jspName = classFile.substring(classFile.lastIndexOf(File.separator)).replace(".class", ".html");
        this.config = config;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }

    public void saveHtml() throws IOException {
        File file = new File(config.getSavePath(), jspName);
        try(BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            out.write(this.stringWriter.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }
}
