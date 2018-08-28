package com.silverbullets.schedulecrawler;

import com.silverbullets.schedulecrawler.crawler.AircraftCrawler;
import com.silverbullets.schedulecrawler.crawler.ScheduleCrawler;
import com.silverbullets.schedulecrawler.proxy.ProxyData;
import com.silverbullets.schedulecrawler.proxy.ProxyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrawlerApplication {

    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(CrawlerApplication.class);

        logger.info("Get Proxy info");
        ProxyManager proxyManager = new ProxyManager();
        ProxyData proxyData = proxyManager.getProxy();
        logger.info("End of get proxy info");

        logger.info("Start crawling aircraft");
        AircraftCrawler ac = new AircraftCrawler(proxyData);
        ac.start();
        logger.info("End of crawling aircraft");

        logger.info("Start of crawling schedule");
        ScheduleCrawler sc = new ScheduleCrawler(proxyData);
        sc.start();
        logger.info("End of schedule crawling");
    }

}
