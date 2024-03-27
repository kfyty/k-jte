package com.kfyty.kjte.utils;

import jakarta.servlet.ServletContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.jasper.servlet.TldScanner;
import org.apache.tomcat.util.descriptor.tld.TaglibXml;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import org.xml.sax.SAXException;

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
    @Getter
    private static Map<String, TldResourcePath> tldResourcePathMap;

    @Getter
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

}
