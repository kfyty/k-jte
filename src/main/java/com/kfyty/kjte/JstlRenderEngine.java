package com.kfyty.kjte;

import com.kfyty.kjte.servlet.JteRequestFacade;
import com.kfyty.kjte.servlet.JteResponseFacade;
import com.kfyty.kjte.servlet.JteServletConfig;
import com.kfyty.kjte.servlet.JteServletContext;
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
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;

@Slf4j
public class JstlRenderEngine {
    private HttpJspBase curJspObj;
    private ServletConfig servletConfig;
    private RequestFacade requestFacade;
    private ResponseFacade responseFacade;

    private final JstlTemplateEngine templateEngine;
    private final List<Class<?>> classes;

    static {
        JspFactory.setDefaultFactory(new JspFactoryImpl() {
            @Override
            public void releasePageContext(PageContext pc) {
                try {
                    pc.getOut().flush();
                    ((JteResponseFacade) pc.getResponse()).doWriteOut();
                } catch (Exception e) {
                    log.error("releasePageContext error !", e);
                }
            }
        });
    }

    public JstlRenderEngine(JstlTemplateEngine templateEngine, List<Class<?>> classes) {
        this.classes = classes;
        this.templateEngine = templateEngine;
    }

    private void initRenderEngine(Class<?> clazz) {
        try {
            // 实例化 jsp 对象
            this.curJspObj = (HttpJspBase) clazz.newInstance();

            // 初始化 servletConfig
            PrintWriter printWriter = new PrintWriter(new ByteArrayOutputStream());
            ServletContext servletContext = new JteServletContext(printWriter, URL.class.getResource("/WEB-INF/web.xml"), this.getClass().getClassLoader(), true, true);
            this.servletConfig = new JteServletConfig(clazz.getSimpleName(), servletContext, templateEngine.getConfig());

            // 初始化 requestFacade
            this.requestFacade = new JteRequestFacade(new Request(new Connector()) {
                {
                    this.setCoyoteRequest(new org.apache.coyote.Request());
                }

                @Override
                public String getMethod() {
                    return "POST";
                }
            }, templateEngine.getConfig());

            // 初始化 responseFacade
            this.responseFacade = new JteResponseFacade(new Response() {
                    private final org.apache.coyote.Response coyoteResponse = new org.apache.coyote.Response();

                    @Override
                    public org.apache.coyote.Response getCoyoteResponse() {
                        return coyoteResponse;
                    }
                }, clazz, templateEngine);
        } catch (Exception e) {
            log.error("initRenderEngine error !");
            throw new RuntimeException(e);
        }
    }

    public ResponseFacade getResponseFacade() {
        return this.responseFacade;
    }

    public void doRenderHtml() {
        this.doRenderHtml(templateEngine.getConfig().getSavePath());
    }

    public void doRenderHtml(String savePath) {
        try {
            templateEngine.getConfig().setSavePath(savePath);
            for (Class<?> clazz : this.classes) {
                this.initRenderEngine(clazz);
                this.curJspObj.init(this.servletConfig);
                this.curJspObj.service(this.requestFacade, this.responseFacade);
                this.curJspObj.destroy();
            }
        } catch (Exception e) {
            log.error("doRenderHtml error !");
            throw new RuntimeException(e);
        }
    }
}
