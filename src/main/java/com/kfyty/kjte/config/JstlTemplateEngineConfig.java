package com.kfyty.kjte.config;

import com.kfyty.kjte.servlet.JteResponseFacade;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import jakarta.servlet.jsp.JspFactory;
import jakarta.servlet.jsp.PageContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.jasper.runtime.JspFactoryImpl;

import java.io.File;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.kfyty.kjte.JstlTemplateEngine.DEFAULT_OUT_PUT_TEMP_DIR;

@Data
@Slf4j
public class JstlTemplateEngineConfig {
    static {
        JspFactory.setDefaultFactory(new JspFactoryImpl() {

            @Override
            public void releasePageContext(PageContext pc) {
                try {
                    pc.getOut().flush();
                    ((JteResponseFacade) pc.getResponse()).write();
                    super.releasePageContext(pc);
                } catch (Exception e) {
                    log.error("releasePageContext error !", e);
                }
            }
        });
    }

    /**
     * 对于生成的 java 文件，是否执行二次编译
     */
    private boolean compiler;

    /**
     * 生成文件后缀，默认: .html
     */
    private String suffix;

    /**
     * 模板路径
     */
    private String templatePath;

    /**
     * 临时输出文件夹，用于保存临时生成的 servlet 文件
     */
    private String tempOutPutDir;

    /**
     * 目标文件保存路径
     */
    private String savePath;

    /**
     * 解析出的 jsp 文件对象
     */
    private List<File> jspFiles;

    /**
     * 输出目标
     */
    private Writer out;

    /**
     * 渲染变量，全局有效
     */
    private Map<Object, Object> variables;

    public JstlTemplateEngineConfig(String templatePath) {
        this(DEFAULT_OUT_PUT_TEMP_DIR, templatePath, DEFAULT_OUT_PUT_TEMP_DIR);
    }

    public JstlTemplateEngineConfig(String savePath, String templatePath) {
        this(savePath, templatePath, DEFAULT_OUT_PUT_TEMP_DIR);
    }

    public JstlTemplateEngineConfig(String templatePath, List<File> jspFiles) {
        this.templatePath = templatePath;
        this.tempOutPutDir = DEFAULT_OUT_PUT_TEMP_DIR;
        this.savePath = DEFAULT_OUT_PUT_TEMP_DIR;
        this.jspFiles = jspFiles;
        this.variables = new HashMap<>();
    }

    public JstlTemplateEngineConfig(String savePath, String templatePath, String tempOutPutDir) {
        this.savePath = savePath;
        this.templatePath = templatePath;
        this.tempOutPutDir = tempOutPutDir;
        this.jspFiles = new ArrayList<>();
        this.variables = new HashMap<>();
        this.scanJspPaths();
    }

    public JstlTemplateEngineConfig addJsp(File jsp) {
        if (jsp != null && jsp.exists() && jsp.isFile()) {
            this.jspFiles.add(jsp);
        }
        return this;
    }

    public String getTemplatePath() {
        String path = templatePath;
        if (templatePath.endsWith(".jsp")) {
            int index = templatePath.lastIndexOf("\\");
            if (index == -1) {
                index = templatePath.lastIndexOf("/");
            }
            path = templatePath.substring(0, index);
        }
        return path.endsWith("\\") || path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    public String getSavePath() {
        return savePath.endsWith(File.separator) ? savePath : savePath + File.separator;
    }

    public JstlTemplateEngineConfig putVar(Object key, Object value) {
        this.variables.put(key, value);
        return this;
    }

    public JstlTemplateEngineConfig putVar(Map<Object, Object> variables) {
        this.variables.putAll(variables);
        return this;
    }

    public JstlTemplateEngineConfig clearVar() {
        this.variables.clear();
        return this;
    }

    protected void scanJspPaths() {
        String path = this.templatePath;
        if (!path.endsWith(".jsp")) {
            if (path.endsWith("\\") || path.endsWith("/")) {
                path += "*.jsp";
            } else {
                path += "/*.jsp";
            }
        }
        Set<URL> urls = IOUtil.scanFiles(CommonUtil.removePrefix("/", path), this.getClass().getClassLoader());
        for (URL url : urls) {
            jspFiles.add(new File(url.getFile()));
        }
    }
}
