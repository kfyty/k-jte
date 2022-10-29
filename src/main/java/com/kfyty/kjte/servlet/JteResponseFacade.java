package com.kfyty.kjte.servlet;

import com.kfyty.kjte.JstlTemplateEngine;
import com.kfyty.kjte.config.JstlTemplateEngineConfig;
import com.kfyty.core.utils.ReflectUtil;
import javassist.ClassPool;
import lombok.SneakyThrows;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.jasper.JspCompilationContext;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.kfyty.kjte.JstlTemplateEngine.CLASS_SUFFIX;
import static com.kfyty.core.utils.CommonUtil.BLANK_LINE_PATTERN;

public class JteResponseFacade extends ResponseFacade {
    private final String jspName;
    private final JstlTemplateEngine templateEngine;
    private final StringWriter stringWriter = new StringWriter();
    private final PrintWriter printWriter = new PrintWriter(stringWriter, true);

    public JteResponseFacade(Response response, Class<?> jspClass, JstlTemplateEngine templateEngine) {
        super(response);
        JstlTemplateEngineConfig config = templateEngine.getConfig();
        String suffix = config.getSuffix() == null || config.getSuffix().isEmpty() ? ".html" : config.getSuffix();
        this.jspName = jspClass.getSimpleName().replaceAll("_jsp$", "") + suffix;
        this.templateEngine = templateEngine;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }

    public String getString() {
        return this.stringWriter.toString().replaceFirst(BLANK_LINE_PATTERN.pattern(), "").replaceAll(BLANK_LINE_PATTERN.pattern(), System.lineSeparator());
    }

    public void doWriteOut() throws IOException {
        String code = this.getString();
        JstlTemplateEngineConfig config = templateEngine.getConfig();
        if (config.getOut() != null) {
            config.getOut().write(code);
            config.getOut().flush();
            return;
        }
        File file = new File(config.getSavePath(), jspName);
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            out.write(code.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
        this.doCompileClassIfNecessary(code);
    }

    @SneakyThrows
    private void doCompileClassIfNecessary(String code) {
        if (!templateEngine.getConfig().isCompiler()) {
            return;
        }
        ClassPool classPool = ClassPool.getDefault();
        JstlTemplateEngineConfig config = templateEngine.getConfig();
        JspCompilationContext compilationContext = templateEngine.getCompilationContext();
        templateEngine.getGenerateComplete().put(compilationContext.getServletClassName(), code);
        this.doInvokeGenerateClass(compilationContext);
        String classPath = config.getSavePath() + compilationContext.getServletPackageName().replace(".", File.separator) + File.separator + compilationContext.getServletClassName() + CLASS_SUFFIX;
        Class<?> clazz = classPool.makeClass(new FileInputStream(classPath)).toClass();
        if (templateEngine.getCompileClass().containsKey(clazz.getName())) {
            throw new IllegalArgumentException("The class already exists !");
        }
        templateEngine.getCompileClass().put(clazz.getName(), clazz);
    }

    private void doInvokeGenerateClass(JspCompilationContext compilationContext) {
        Method method = ReflectUtil.getMethod(compilationContext.getCompiler().getClass(), "generateClass", Map.class);
        ReflectUtil.invokeMethod(compilationContext.getCompiler(), method, new HashMap<>());
    }
}
