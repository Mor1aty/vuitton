package com.moriaty.vuitton.util;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.DefaultJavaScriptErrorListener;
import com.moriaty.vuitton.constant.Constant;

import java.io.IOException;
import java.net.URL;

/**
 * <p>
 * Html unit 工具
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/27 下午11:49
 */
public class HtmlUnitUtil {
    private HtmlUnitUtil() {

    }

    public static HtmlPage getPage(String url) throws IOException {
        try (WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setActiveXNative(false);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setTimeout(Constant.Network.CONNECT_TIMEOUT);
            webClient.setJavaScriptTimeout(Constant.Network.CONNECT_TIMEOUT);
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            webClient.setJavaScriptErrorListener(new IgnoreJavaScriptError());
            return webClient.getPage(url);
        }
    }

    public static class IgnoreJavaScriptError extends DefaultJavaScriptErrorListener {

        @Override
        public void scriptException(HtmlPage htmlPage, ScriptException e) {
            // Do nothing
        }

        @Override
        public void loadScriptError(HtmlPage htmlPage, URL url, Exception e) {
            // Do nothing
        }

        @Override
        public void warn(String s, String s1, int i, String s2, int i1) {
            // Do nothing
        }
    }
}
