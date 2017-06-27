package com.silverbullets.schedulecrawler.crawler;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.silverbullets.schedulecrawler.dataaccess.PersistenceManager;
import com.silverbullets.schedulecrawler.entity.Aircraft;
import com.silverbullets.schedulecrawler.entity.Airline;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class AircraftCrawler {

    private EntityManager em;

    public AircraftCrawler() {
        em = PersistenceManager.INSTANCE.getEntityManager();
    }

    public void start() {
        // Get airlines to be crawled from DB
        TypedQuery<Airline> query =
                em.createQuery("SELECT a FROM Airline a", Airline.class);
        List<Airline> results = query.getResultList();

        for (Airline a : results) {
            crawlAircraft(a.getUrl(), a);

            try
            {
                Random randomGenerator = new Random();
                Thread.sleep(randomGenerator.nextInt(2000) + 1000);
            }
            catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void crawlAircraft(String url, Airline airline) {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.waitForBackgroundJavaScript(1000);

        try {
            HtmlPage page = webClient.getPage(url);

            List<?> aircraftTypeList = page.getByXPath("//li[@class='parent']");

            for (int i = 0; i < aircraftTypeList.size(); i++) {
                HtmlListItem li = (HtmlListItem) aircraftTypeList.get(i);

                DomText text = (DomText) li.getByXPath(".//div/div/text()[last()]").get(0);
                String aircraftType = text.getWholeText().trim();

                List<?> regList = li.getByXPath(".//ul/li/a[@class='regLinks']");
                for (int j = 0; j < regList.size(); j++) {
                    HtmlAnchor reg = (HtmlAnchor) regList.get(j);
                    String aircraftRegistration = reg.getTextContent().trim();

                    updateAircraftToDB(aircraftType, aircraftRegistration, airline);
                }
            }
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void updateAircraftToDB(String acType, String reg, Airline airline) {
        TypedQuery<Aircraft> query =
                em.createQuery("SELECT a FROM Aircraft a WHERE a.reg = ?1", Aircraft.class);
        query.setParameter(1, reg);
        List<Aircraft> results = query.getResultList();

        if (results.size() == 0) {
            Aircraft a = new Aircraft();
            a.setAirline(airline.getIcao());
            a.setReg(reg);
            a.setType(acType);
            a.setCreatedDate(new Date());
            a.setLastUpdatedDate(new Date());

            em.getTransaction().begin();
            em.persist(a);
            em.getTransaction().commit();
        } else {
            Aircraft a = results.get(0);
            a.setLastUpdatedDate(new Date());

            em.getTransaction().begin();
            em.persist(a);
            em.getTransaction().commit();
        }
    }
}
