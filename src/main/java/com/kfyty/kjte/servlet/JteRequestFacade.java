package com.kfyty.kjte.servlet;

import com.kfyty.kjte.config.JstlTemplateEngineConfig;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.session.StandardSession;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpSession;

public class JteRequestFacade extends RequestFacade {
    private final JstlTemplateEngineConfig config;
    private final StandardSession session = new StandardSession(new StandardManager());

    public JteRequestFacade(Request request, JstlTemplateEngineConfig config) {
        super(request);
        this.config = config;
    }

    @Override
    public String getServletPath() {
        return config.getTemplatePath();
    }

    @Override
    public HttpSession getSession() {
        return session;
    }

    @Override
    public Object getAttribute(String name) {
        return config.getVariables().get(name);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return new JteRequestDispatcher(path, config);
    }
}
