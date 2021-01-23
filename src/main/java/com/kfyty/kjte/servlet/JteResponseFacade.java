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
    private static final String BLANK_LINE = "(?m)^\\s*$" + System.lineSeparator();

    private final String jspName;
    private final JstlTemplateEngineConfig config;
    private final StringWriter stringWriter = new StringWriter();
    private final PrintWriter printWriter = new PrintWriter(stringWriter, true);

    public JteResponseFacade(Response response, Class<?> jspClass, JstlTemplateEngineConfig config) {
        super(response);
        this.jspName = jspClass.getSimpleName().replaceAll("_jsp$", "") + ".html";
        this.config = config;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }

    public String getString() {
        return this.stringWriter.toString().replaceAll(BLANK_LINE, System.lineSeparator());
    }

    public void saveHtml() throws IOException {
        if(config.getOut() != null) {
            config.getOut().write(this.getString());
            config.getOut().flush();
            return;
        }
        File file = new File(config.getSavePath(), jspName);
        try(BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            out.write(this.getString().getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }
}
