package com.kfyty.kjte;

import com.kfyty.kjte.config.JstlTemplateEngineConfig;
import com.kfyty.kjte.servlet.JteRequestFacade;
import com.kfyty.kjte.servlet.JteResponseFacade;
import com.kfyty.kjte.servlet.JteServletConfig;
import com.kfyty.kjte.servlet.JteServletContext;
import javassist.ClassPool;
import javassist.CtClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.jasper.runtime.HttpJspBase;
import org.apache.jasper.runtime.JspFactoryImpl;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;

@Slf4j
public class JstlRenderEngine {
    private HttpJspBase curJspObj;
    private ServletConfig servletConfig;
    private RequestFacade requestFacade;
    private ResponseFacade responseFacade;

    private final JstlTemplateEngineConfig config;
    private final List<String> classPaths;

    static {
        JspFactory.setDefaultFactory(new JspFactoryImpl() {
            @Override
            public void releasePageContext(PageContext pc) {
                try {
                    pc.getOut().flush();
                    ((JteResponseFacade) pc.getResponse()).saveHtml();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public JstlRenderEngine(List<String> classPaths, JstlTemplateEngineConfig config) {
        this.classPaths = classPaths;
        this.config = config;
    }

    private void initRenderEngine(String classFile) {
        try {
            // 加载 jsp 字节码
            InputStream classStream = new FileInputStream(classFile);
            CtClass ctClass = ClassPool.getDefault().makeClass(classStream);
            Class<?> clazz = ctClass.toClass();
            this.curJspObj = (HttpJspBase) clazz.newInstance();

            // 初始化 servletConfig
            PrintWriter printWriter = new PrintWriter(new ByteArrayOutputStream());
            ServletContext servletContext = new JteServletContext(printWriter, URL.class.getResource("/WEB-INF/web.xml"), this.getClass().getClassLoader(), true, true);
            this.servletConfig = new JteServletConfig(new File(classFile), servletContext, config);

            // 初始化 requestFacade
            this.requestFacade = new JteRequestFacade(new Request(new Connector()) {
                {
                    this.setCoyoteRequest(new org.apache.coyote.Request());
                }

                @Override
                public String getMethod() {
                    return "POST";
                }
            }, config);

            // 初始化 responseFacade
            this.responseFacade = new JteResponseFacade(new Response() {
                    private final org.apache.coyote.Response coyoteResponse = new org.apache.coyote.Response();

                    @Override
                    public org.apache.coyote.Response getCoyoteResponse() {
                        return coyoteResponse;
                    }
                }, classFile, config);
        } catch (Exception e) {
            log.error("initRenderEngine error !", e);
            throw new RuntimeException(e);
        }
    }

    public ResponseFacade getResponseFacade() {
        return this.responseFacade;
    }

    public void doRenderHtml() {
        this.doRenderHtml(config.getSavePath());
    }

    public void doRenderHtml(String savePath) {
        try {
            config.setSavePath(savePath);
            for (String classPath : this.classPaths) {
                this.initRenderEngine(classPath);
                this.curJspObj.init(this.servletConfig);
                this.curJspObj.service(this.requestFacade, this.responseFacade);
                this.curJspObj.destroy();
            }
        } catch (Exception e) {
            log.error("doRenderHtml error !", e);
            throw new RuntimeException(e);
        }
    }
}
