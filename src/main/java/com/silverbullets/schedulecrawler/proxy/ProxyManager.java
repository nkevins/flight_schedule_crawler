package com.silverbullets.schedulecrawler.proxy;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class ProxyManager {
    final String url = "https://gimmeproxy.com/api/getProxy?port=80&protocol=http&get=true&maxCheckPeriod=3600&supportsHttps=true";
    final String test_url = "https://flightradar24.com";
    final Logger logger = LoggerFactory.getLogger(ProxyManager.class);

    public ProxyData getProxy() {
        boolean success = false;
        ProxyData data = new ProxyData();

        do {
            logger.info("Getting proxy information");

            URLConnection connection = null;
            data.setUseProxy(false);

            try {
                connection = new URL(url).openConnection();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Malformed proxy API URL", e);
            }

            // Get proxy info
            try(Scanner scanner = new Scanner(connection.getInputStream());){
                String response = scanner.useDelimiter("\\A").next();
                logger.info("Proxy response " + response);

                JSONObject json = new JSONObject(response);
                data.setIp(json.getString("ip"));
                data.setPort(json.getInt("port"));
                data.setUseProxy(true);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Error when calling Proxy API", e);
            }

            // Test proxy
            logger.info("Testing proxy configuration");
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            webClient.waitForBackgroundJavaScript(1000);
            ProxyConfig proxyConfig = new ProxyConfig(data.getIp(), data.getPort());
            webClient.getOptions().setProxyConfig(proxyConfig);
            try {
                HtmlPage page = webClient.getPage(test_url);

                if (page.getWebResponse().getStatusCode() == 200) {
                    success = true;
                    logger.info("Proxy configuration test success");
                } else {
                    logger.info("Proxy configuration test failed, get another proxy setting");
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error when testing proxy setting", e);
            }
        } while (!success);

        return data;
    }
}
