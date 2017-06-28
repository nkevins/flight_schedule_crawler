package com.silverbullets.schedulecrawler.crawler;

import com.gargoylesoftware.htmlunit.*;
import com.google.gson.*;
import com.silverbullets.schedulecrawler.dataaccess.PersistenceManager;
import com.silverbullets.schedulecrawler.entity.Aircraft;
import com.silverbullets.schedulecrawler.entity.RawSchedule;
import com.silverbullets.schedulecrawler.entity.Schedule;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ScheduleCrawler {

    private EntityManager em;

    public ScheduleCrawler() {
        em = PersistenceManager.INSTANCE.getEntityManager();
    }

    public void start() {
        // Get airlines to be crawled from DB
        TypedQuery<Aircraft> query =
                em.createQuery("SELECT a FROM Aircraft a", Aircraft.class);
        List<Aircraft> results = query.getResultList();

        for (Aircraft a : results) {
            crawlSchedule(a);

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

    private void crawlSchedule(Aircraft aircraft) {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.waitForBackgroundJavaScript(5000);

        try {
            String url = "https://api.flightradar24.com/common/v1/flight/list.json?query=" + aircraft.getReg().toLowerCase() +"&fetchBy=reg&page=1&limit=100&token=";
            WebRequest requestSettings = new WebRequest(new URL(url), HttpMethod.GET);
            requestSettings.setAdditionalHeader("Accept", "application/json, text/plain, */*");
            requestSettings.setAdditionalHeader("Accept-Encoding", "gzip, deflate, sdch, br");
            requestSettings.setAdditionalHeader("Accept-Language", "en-GB,en-US;q=0.8,en;q=0.6");
            requestSettings.setAdditionalHeader("Connection", "keep-alive");
            requestSettings.setAdditionalHeader("Host", "api.flightradar24.com");
            requestSettings.setAdditionalHeader("Origin", "https://www.flightradar24.com");
            requestSettings.setAdditionalHeader("Referer", "https://www.flightradar24.com/data/aircraft/" + aircraft.getReg().toLowerCase());
            requestSettings.setAdditionalHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");

            Page page = webClient.getPage(requestSettings);
            String content = page.getWebResponse().getContentAsString();

            JsonElement element = new JsonParser().parse(content);
            JsonObject object = element.getAsJsonObject();
            object = object.getAsJsonObject("result").getAsJsonObject("response");
            JsonArray dataArray = object.getAsJsonArray("data");

            for (JsonElement s : dataArray) {
                try {
                    JsonObject data = s.getAsJsonObject();

                    if (data.getAsJsonObject("identification").get("callsign").isJsonNull()) {
                        continue;
                    }

                    String flightNo = data.getAsJsonObject("identification").get("callsign").getAsString();
                    String departure = data.getAsJsonObject("airport").getAsJsonObject("origin").getAsJsonObject("code")
                            .get("icao").getAsString();
                    String departureIata = data.getAsJsonObject("airport").getAsJsonObject("origin").getAsJsonObject("code")
                            .get("iata").getAsString();
                    String destination = data.getAsJsonObject("airport").getAsJsonObject("destination")
                            .getAsJsonObject("code").get("icao").getAsString();
                    String destinationIata = data.getAsJsonObject("airport").getAsJsonObject("destination")
                            .getAsJsonObject("code").get("iata").getAsString();
                    long departureTimeEpoch = data.getAsJsonObject("time").getAsJsonObject("scheduled")
                            .get("departure").getAsLong();
                    long arrivalTimeEpoch = data.getAsJsonObject("time").getAsJsonObject("scheduled")
                            .get("arrival").getAsLong();
                    Date departureTime = new Date(departureTimeEpoch * 1000);
                    Date arrivalTime = new Date(arrivalTimeEpoch * 1000);

                    RawSchedule sch = new RawSchedule();
                    sch.setAircraftReg(aircraft.getReg());
                    sch.setAircraftType(aircraft.getType());
                    sch.setAirline(aircraft.getAirline());
                    sch.setDeparture(departure);
                    sch.setDepartureIata(departureIata);
                    sch.setDestination(destination);
                    sch.setDestinationIata(destinationIata);
                    sch.setEta(arrivalTime);
                    sch.setEtd(departureTime);
                    sch.setFlightNo(flightNo);
                    sch.setFlightDate(departureTime);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(departureTime);
                    sch.setDay(calendar.get(Calendar.DAY_OF_WEEK));

                    sch.setCreatedDate(new Date());
                    sch.setLastUpdatedDate(new Date());

                    saveToDatabase(sch);
                } catch(Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void saveToDatabase(RawSchedule s) {
        // Save raw schedule
        TypedQuery<RawSchedule> query =
                em.createQuery("SELECT s FROM RawSchedule s WHERE s.flightNo = ?1 and s.flightDate = ?2", RawSchedule.class);
        query.setParameter(1, s.getFlightNo());
        query.setParameter(2, s.getFlightDate());
        List<RawSchedule> results = query.getResultList();

        if (results.size() == 0) {
            em.getTransaction().begin();
            em.persist(s);
            em.getTransaction().commit();
        } else {
            RawSchedule sch = results.get(0);
            sch.setAircraftReg(s.getAircraftReg());
            sch.setAircraftType(s.getAircraftType());
            sch.setAirline(s.getAirline());
            sch.setDeparture(s.getDeparture());
            sch.setDepartureIata(s.getDepartureIata());
            sch.setDestination(s.getDestination());
            sch.setDestinationIata(s.getDestinationIata());
            sch.setEta(s.getEta());
            sch.setEtd(s.getEtd());
            sch.setFlightNo(s.getFlightNo());
            sch.setFlightDate(s.getFlightDate());
            sch.setDay(s.getDay());
            sch.setLastUpdatedDate(new Date());

            em.getTransaction().begin();
            em.persist(sch);
            em.getTransaction().commit();
        }

        // Save summary schedule
        TypedQuery<Schedule> summaryQuery =
                em.createQuery("SELECT s FROM Schedule s WHERE s.flightNo = ?1", Schedule.class);
        summaryQuery.setParameter(1, s.getFlightNo());
        List<Schedule> summaryResult = summaryQuery.getResultList();

        if (summaryResult.size() == 0) {
            Schedule sch = new Schedule();
            sch.setAircraftReg(s.getAircraftReg());
            sch.setAircraftType(s.getAircraftType());
            sch.setAirline(s.getAirline());
            sch.setDeparture(s.getDeparture());
            sch.setDepartureIata(s.getDepartureIata());
            sch.setDestination(s.getDestination());
            sch.setDestinationIata(s.getDestinationIata());
            sch.setEta(s.getEta());
            sch.setEtd(s.getEtd());
            sch.setFlightNo(s.getFlightNo());
            sch.setCreatedDate(new Date());
            sch.setLastFlownDate(s.getFlightDate());

            em.getTransaction().begin();
            em.persist(sch);
            em.getTransaction().commit();
        } else {
            Schedule sch = summaryResult.get(0);
            if (s.getFlightDate().after(sch.getLastFlownDate())) {
                sch.setAircraftReg(s.getAircraftReg());
                sch.setAircraftType(s.getAircraftType());
                sch.setAirline(s.getAirline());
                sch.setDeparture(s.getDeparture());
                sch.setDepartureIata(s.getDepartureIata());
                sch.setDestination(s.getDestination());
                sch.setDestinationIata(s.getDestinationIata());
                sch.setEta(s.getEta());
                sch.setEtd(s.getEtd());
                sch.setFlightNo(s.getFlightNo());
                sch.setLastFlownDate(s.getFlightDate());

                em.getTransaction().begin();
                em.persist(sch);
                em.getTransaction().commit();
            }
        }
    }
}
