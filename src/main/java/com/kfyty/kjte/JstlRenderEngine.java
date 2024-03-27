package com.kfyty.kjte;

import com.kfyty.core.utils.ExceptionUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.kjte.servlet.JteRequestFacade;
import com.kfyty.kjte.servlet.JteResponseFacade;
import com.kfyty.kjte.servlet.JteServletConfig;
import com.kfyty.kjte.servlet.JteServletContext;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.jasper.runtime.HttpJspBase;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

@Slf4j
@Getter
public class JstlRenderEngine {
    private HttpJspBase curJspObj;
    private ServletConfig servletConfig;
    private RequestFacade requestFacade;
    private ResponseFacade responseFacade;

    private final JstlTemplateEngine templateEngine;
    private final List<Class<?>> classes;

    public JstlRenderEngine(JstlTemplateEngine templateEngine, List<Class<?>> classes) {
        this.classes = classes;
        this.templateEngine = templateEngine;
    }

    private void initRenderEngine(Class<?> clazz) {
        try {
            // 实例化 jsp 对象
            this.curJspObj = (HttpJspBase) ReflectUtil.newInstance(clazz);

            // 初始化 servletConfig
            PrintWriter printWriter = new PrintWriter(new ByteArrayOutputStream());
            ServletContext servletContext = new JteServletContext(printWriter, this.getClass().getResource("/WEB-INF/web.xml"), this.getClass().getClassLoader(), true, true);
            this.servletConfig = new JteServletConfig(clazz.getSimpleName(), servletContext, templateEngine.getConfig());

            // 初始化 requestFacade
            this.requestFacade = new JteRequestFacade(new Request(new Connector(), new org.apache.coyote.Request()) {

                @Override
                public String getMethod() {
                    return "POST";
                }
            }, templateEngine.getConfig());

            // 初始化 responseFacade
            this.responseFacade = new JteResponseFacade(new Response(new org.apache.coyote.Response()), clazz, templateEngine);
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public void doRenderTemplate() {
        this.doRenderTemplate(templateEngine.getConfig().getSavePath());
    }

    public void doRenderTemplate(String savePath) {
        try {
            templateEngine.getConfig().setSavePath(savePath);
            for (Class<?> clazz : this.classes) {
                this.initRenderEngine(clazz);
                this.curJspObj.init(this.servletConfig);
                this.curJspObj.service(this.requestFacade, this.responseFacade);
                this.curJspObj.destroy();
            }
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}
