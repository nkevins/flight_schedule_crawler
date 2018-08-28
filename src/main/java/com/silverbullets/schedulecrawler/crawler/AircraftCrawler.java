package com.silverbullets.schedulecrawler.crawler;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.silverbullets.schedulecrawler.dataaccess.PersistenceManager;
import com.silverbullets.schedulecrawler.entity.Aircraft;
import com.silverbullets.schedulecrawler.entity.Airline;
import com.silverbullets.schedulecrawler.proxy.ProxyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class AircraftCrawler {

    private EntityManager em;
    private ProxyData proxyData;
    final Logger logger = LoggerFactory.getLogger(AircraftCrawler.class);

    public AircraftCrawler(ProxyData proxyData) {
        em = PersistenceManager.INSTANCE.getEntityManager();
        this.proxyData = proxyData;
    }

    public void start() {
        // Get airlines to be crawled from DB
        TypedQuery<Airline> query =
                em.createQuery("SELECT a FROM Airline a WHERE active = true", Airline.class);
        List<Airline> results = query.getResultList();

        results.parallelStream().forEach(a -> {
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
        });
    }

    private void crawlAircraft(String url, Airline airline) {
        logger.info("Crawling aircraft for airline " + airline.getIcao());

        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.waitForBackgroundJavaScript(1000);

        if (proxyData.isUseProxy()) {
            ProxyConfig proxyConfig = new ProxyConfig(proxyData.getIp(), proxyData.getPort());
            webClient.getOptions().setProxyConfig(proxyConfig);
        }

        try {
            HtmlPage page = webClient.getPage(url);

            List<?> dom = page.getByXPath("//dl[@id='list-aircraft']");
            HtmlDefinitionList fleetTable = (HtmlDefinitionList) dom.get(0);

            String aircraftType = "";
            for (int i = 0; i < fleetTable.getChildNodes().getLength(); i++) {
                DomNode rowElement = fleetTable.getChildNodes().get(i);

                if (rowElement.getClass() == HtmlDefinitionTerm.class) {
                    HtmlDefinitionTerm dt = (HtmlDefinitionTerm) rowElement;
                    if (dt.getAttribute("class").equals("header"))
                        continue;

                    aircraftType = dt.getFirstChild().getTextContent().trim();
                }

                if (rowElement.getClass() == HtmlDefinitionDescription.class) {
                    HtmlDefinitionDescription dd = (HtmlDefinitionDescription) rowElement;

                    List<?> regs = dd.getElementsByTagName("a");

                    for (int j = 0; j < regs.size(); j++) {
                        HtmlAnchor link = (HtmlAnchor) regs.get(j);

                        if (!link.getAttribute("class").equals("regLinks"))
                            continue;

                        String aircraftRegistration = link.getTextContent().trim();
                        updateAircraftToDB(aircraftType, aircraftRegistration, airline);
                    }
                }
            }
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            logger.error("Error when crawling aircraft", ex);
        }

        logger.info("End of crawling aircraft for airline " + airline.getIcao());
    }

    private void updateAircraftToDB(String acType, String reg, Airline airline) {
        EntityManager em2 = PersistenceManager.INSTANCE.getEntityManager();

        TypedQuery<Aircraft> query =
                em2.createQuery("SELECT a FROM Aircraft a WHERE a.reg = ?1", Aircraft.class);
        query.setParameter(1, reg);
        List<Aircraft> results = query.getResultList();

        if (results.size() == 0) {
            Aircraft a = new Aircraft();
            a.setAirline(airline);
            a.setReg(reg);
            a.setType(acType);
            a.setCreatedDate(new Date());
            a.setLastUpdatedDate(new Date());

            em2.getTransaction().begin();
            em2.persist(a);
            em2.getTransaction().commit();
        } else {
            Aircraft a = results.get(0);
            a.setLastUpdatedDate(new Date());

            em2.getTransaction().begin();
            em2.persist(a);
            em2.getTransaction().commit();
        }
    }
}
