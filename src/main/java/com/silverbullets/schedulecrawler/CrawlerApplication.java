package com.silverbullets.schedulecrawler;

import com.silverbullets.schedulecrawler.crawler.AircraftCrawler;
import com.silverbullets.schedulecrawler.crawler.ScheduleCrawler;

public class CrawlerApplication {

    public static void main(String[] args) {

        AircraftCrawler ac = new AircraftCrawler();
        ac.start();

        ScheduleCrawler sc = new ScheduleCrawler();
        sc.start();
    }

}
