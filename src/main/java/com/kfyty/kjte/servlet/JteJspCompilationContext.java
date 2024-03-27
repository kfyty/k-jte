package com.kfyty.kjte.servlet;

import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.kjte.JstlTemplateEngine;
import com.kfyty.kjte.config.JstlTemplateEngineConfig;
import jakarta.servlet.ServletContext;
import org.apache.jasper.EmbeddedServletOptions;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Options;
import org.apache.jasper.compiler.Compiler;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.servlet.JspServletWrapper;
import org.apache.tomcat.Jar;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Optional;

public class JteJspCompilationContext extends JspCompilationContext {
    private final JstlTemplateEngine templateEngine;

    public JteJspCompilationContext(String jspUri, Options options, ServletContext context, JspServletWrapper jsw, JspRuntimeContext rctxt, JstlTemplateEngine templateEngine) {
        super(jspUri, options, context, jsw, rctxt);
        this.templateEngine = templateEngine;
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
        return templateEngine.getConfig().getTempOutPutDir();
    }

    @Override
    public Compiler getCompiler() {
        return Optional.ofNullable(super.getCompiler()).orElse(templateEngine.getCompiler());
    }

    @Override
    public String getServletJavaFileName() {
        if (returnSuper()) {
            return super.getServletJavaFileName();
        }
        JstlTemplateEngineConfig config = templateEngine.getConfig();
        return config.getSavePath() + this.getServletClassName() + ".java";
    }

    @Override
    public String getServletClassName() {
        if (returnSuper()) {
            return super.getServletClassName();
        }
        return super.getServletClassName().replaceAll("_jsp$", "");
    }

    @Override
    public Options getOptions() {
        if (returnSuper()) {
            return super.getOptions();
        }
        File java = new File(templateEngine.getConfig().getSavePath());
        EmbeddedServletOptions options = (EmbeddedServletOptions) super.getOptions();
        Field field = ReflectUtil.getField(options.getClass(), "scratchDir");
        ReflectUtil.setFieldValue(options, field, java);
        return options;
    }

    @Override
    public String getServletPackageName() {
        if (returnSuper()) {
            return super.getServletPackageName();
        }
        String pack = "package";
        String code = templateEngine.getGenerateComplete().get(super.getServletClassName());
        int index = code.indexOf(pack);
        String packageName = code.substring(index + pack.length(), code.indexOf(';', index));
        return this.mkdirIfNecessary(packageName.trim());
    }

    private boolean returnSuper() {
        return !templateEngine.getConfig().isCompiler() || templateEngine.getGenerateComplete().get(super.getServletClassName()) == null;
    }

    private String mkdirIfNecessary(String packageName) {
        File dir = new File(templateEngine.getConfig().getSavePath() + packageName.replace(".", File.separator));
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("create dir failed: " + dir.getAbsolutePath());
        }
        return packageName;
    }
}
