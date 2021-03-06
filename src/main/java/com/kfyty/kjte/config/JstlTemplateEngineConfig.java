package com.kfyty.kjte.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kfyty.kjte.JstlTemplateEngine.DEFAULT_OUT_PUT_TEMP_DIR;

@Data
@Slf4j
public class JstlTemplateEngineConfig {
    private String templatePath;
    private String tempOutPutDir;
    private String savePath;
    private List<File> jspFiles;
    private Writer out;
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
        this.initJspPaths();
    }

    public JstlTemplateEngineConfig addJsp(File jsp) {
        if(jsp != null && jsp.exists() && jsp.isFile()) {
            this.jspFiles.add(jsp);
        }
        return this;
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

    private void initJspPaths() {
        try {
            URL root = Thread.currentThread().getContextClassLoader().getResource("");
            File path = new File(root.getPath() + templatePath);
            if(path.isFile()) {
                this.jspFiles.add(path);
                return;
            }
            File[] files = path.listFiles();
            if(files != null) {
                Arrays.stream(files).filter(File::isFile).filter(e -> e.getName().endsWith(".jsp")).forEach(jspFiles::add);
            }
        } catch (Exception e) {
            log.error("init jsp paths error !", e);
            throw new RuntimeException(e);
        }
    }
}
