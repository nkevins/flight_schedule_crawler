package com.silverbullets.schedulecrawler.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "airline")
public class Airline {

    @Id
    private String icao;

    private String iata;
    private String name;
    private String url;
    private boolean active;

    @OneToMany(mappedBy = "airline")
    private List<Aircraft> aircrafts;

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public String getIata() {
        return iata;
    }

    public void setIata(String iata) {
        this.iata = iata;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Aircraft> getAircrafts() {
        return aircrafts;
    }
}
