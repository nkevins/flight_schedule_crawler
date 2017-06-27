package com.silverbullets.schedulecrawler;

import com.silverbullets.schedulecrawler.crawler.AircraftCrawler;

public class CrawlerApplication {

    public static void main(String[] args) {

        AircraftCrawler ac = new AircraftCrawler();
        ac.start();

    }

}
