package com.kfyty.kjte;

import com.kfyty.kjte.config.JstlTemplateEngineConfig;
import com.kfyty.kjte.servlet.JteJspCompilationContext;
import com.kfyty.kjte.servlet.JteServletConfig;
import com.kfyty.kjte.utils.TldCacheUtil;
import javassist.ClassPool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.jasper.EmbeddedServletOptions;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.compiler.Compiler;
import org.apache.jasper.compiler.JDTCompiler;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.compiler.TldCache;
import org.apache.jasper.runtime.JspFactoryImpl;
import org.apache.jasper.servlet.JspCServletContext;
import org.apache.jasper.servlet.JspServlet;
import org.apache.jasper.servlet.JspServletWrapper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class JstlTemplateEngine {
    public static final String CLASS_SUFFIX = ".class";
    public static final String DEFAULT_OUT_PUT_TEMP_DIR = System.getProperty("java.io.tmpdir");

    private JspServletWrapper jspServletWrapper;

    @Getter
    private Compiler compiler;

    @Getter
    private JspCompilationContext compilationContext;

    @Getter
    private final JstlTemplateEngineConfig config;

    @Getter
    private final Map<String, String> generateComplete;

    @Getter
    private final Map<String, Class<?>> compileClass;

    public JstlTemplateEngine(JstlTemplateEngineConfig config) {
        this.config = config;
        this.generateComplete = new HashMap<>();
        this.compileClass = new HashMap<>();
    }

    static {
        JspFactory.setDefaultFactory(new JspFactoryImpl());
    }

    private void initEngine(final File jsp) {
        try {
            String relativePath = config.getTemplatePath() + "/" + jsp.getName();
            // 初始化 servletContext
            PrintWriter printWriter = new PrintWriter(new ByteArrayOutputStream());
            ServletContext servletContext = new JspCServletContext(printWriter, jsp.toURI().toURL(), this.getClass().getClassLoader(), true, true);

            // 初始化 servletConfig
            ServletConfig servletConfig = new JteServletConfig(jsp.getName(), servletContext, config);

            // 初始化 jspServlet
            JspServlet jspServlet = new JspServlet();
            jspServlet.init(servletConfig);

            // 初始化 options，并初始化 jstl 标签数据
            TldCacheUtil.loadCache(servletContext);
            TldCache tldCache = new TldCache(servletContext, TldCacheUtil.getTldResourcePathMap(), TldCacheUtil.getTldResourcePathTaglibXmlMap());
            EmbeddedServletOptions options = new EmbeddedServletOptions(jspServlet, servletContext);
            options.setTldCache(tldCache);

            // 初始化 jspRuntimeContext
            JspRuntimeContext jspRuntimeContext = new JspRuntimeContext(servletContext, options);

            // 初始化 compilationContext
            this.compilationContext = new JteJspCompilationContext(relativePath, options, servletContext, null, jspRuntimeContext, this);

            // 初始化 jspServletWrapper
            this.jspServletWrapper = new JspServletWrapper(jspServlet, options, relativePath, jspRuntimeContext);

            // 初始化 compiler
            this.compiler = new JDTCompiler();
        } catch (Exception e) {
            log.error("JstlTemplateEngine error !");
            throw new RuntimeException(e);
        }
    }

    public List<String> compile() {
        return compile(config.getTempOutPutDir());
    }

    public List<Class<?>> load() {
        return load(config.getTempOutPutDir());
    }

    public List<String> compile(String tempOutPutDir) {
        try {
            config.setTempOutPutDir(tempOutPutDir);
            List<String> result = new ArrayList<>();
            for (File jspFile : this.config.getJspFiles()) {
                this.initEngine(jspFile);
                compiler.init(compilationContext, jspServletWrapper);
                compiler.compile(true);
                String classPath = this.calcClassPath(tempOutPutDir, config.getTemplatePath());
                result.add(classPath);
            }
            return result;
        } catch (Exception e) {
            log.error("compiler jsp error !", e);
            throw new RuntimeException(e);
        }
    }

    public List<Class<?>> load(String tempOutPutDir) {
        ClassPool classPool = ClassPool.getDefault();
        return compile(tempOutPutDir).stream()
                .map(e -> {
                    try {
                        String clazz = e.replace(CLASS_SUFFIX, "").replace(".", "_") + CLASS_SUFFIX;
                        return classPool.makeClass(new FileInputStream(clazz)).toClass();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }).collect(Collectors.toList());
    }

    private String calcClassPath(String tempOutPutDir, String templatePath) {
        if(!tempOutPutDir.endsWith(File.separator)) {
            tempOutPutDir = tempOutPutDir + File.separator;
        }
        if(!templatePath.startsWith(File.separator)) {
            templatePath = File.separator + (templatePath.startsWith("/") ? templatePath.substring(1) : templatePath);
        }
        if(!templatePath.endsWith(File.separator)) {
            templatePath = templatePath + File.separator;
        }
        return tempOutPutDir +
                "org" + File.separator + "apache" + File.separator + "jsp" +
                templatePath + this.compilationContext.getServletClassName();
    }
}
