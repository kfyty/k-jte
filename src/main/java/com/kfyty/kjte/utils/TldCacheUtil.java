package com.kfyty.kjte.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.jasper.servlet.TldScanner;
import org.apache.tomcat.util.descriptor.tld.TaglibXml;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.Map;

/**
 * 描述: TldCacheUtil
 *
 * @author kfyty
 * @date 2021/1/23 14:51
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class TldCacheUtil {
    private static Map<String, TldResourcePath> tldResourcePathMap;
    private static Map<TldResourcePath, TaglibXml> tldResourcePathTaglibXmlMap;

    public static boolean isAvailable() {
        return getTldResourcePathMap() != null && getTldResourcePathTaglibXmlMap() != null;
    }

    public static void loadCache(ServletContext servletContext) throws IOException, SAXException {
        if (isAvailable()) {
            return;
        }
        TldScanner tldScanner = new TldScanner(servletContext, true, false, true);
        tldScanner.setClassLoader(TldCacheUtil.class.getClassLoader());
        tldScanner.scan();
        tldResourcePathMap = tldScanner.getUriTldResourcePathMap();
        tldResourcePathTaglibXmlMap = tldScanner.getTldResourcePathTaglibXmlMap();
    }

    public static Map<String, TldResourcePath> getTldResourcePathMap() {
        return tldResourcePathMap;
    }

    public static Map<TldResourcePath, TaglibXml> getTldResourcePathTaglibXmlMap() {
        return tldResourcePathTaglibXmlMap;
    }
}
