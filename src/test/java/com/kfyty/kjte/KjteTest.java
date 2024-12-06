package com.kfyty.kjte;

import com.kfyty.kjte.config.JstlTemplateEngineConfig;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class KjteTest {

    @Test
    public void test() {
        String save = "C:\\Users\\xingmai\\Desktop";
        JstlTemplateEngineConfig config = new JstlTemplateEngineConfig(save, "/template");
        config.putVar("title", "test");

        JstlTemplateEngine engine = new JstlTemplateEngine(config);
        List<Class<?>> load = engine.load();

        JstlRenderEngine renderEngine = new JstlRenderEngine(engine, load);
        renderEngine.doRenderTemplate();
    }

    @Test
    public void test2() {
        JstlTemplateEngineConfig config = new JstlTemplateEngineConfig("/template/HelloImpl.jsp");
        config.setCompiler(true);
        config.setSuffix(".java");
        JstlTemplateEngine engine = new JstlTemplateEngine(config);
        JstlRenderEngine renderEngine = new JstlRenderEngine(engine, engine.load());
        renderEngine.doRenderTemplate();

        Class<?> clazz = engine.getCompileClass().get("com.kfyty.kjte.HelloImpl");
        Hello hello = (Hello) ReflectUtil.newInstance(clazz);
        Assert.assertEquals(hello.hello(" world"), "hello world");
    }
}
