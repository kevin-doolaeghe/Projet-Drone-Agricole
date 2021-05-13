package fr.rostand.project.drone.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
public class FlightPlan implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Size(max = 30)
    @Column(unique = true)
    private String name;

    @NotNull
    @Column(precision = 10)
    private double lat1;

    @NotNull
    @Column(precision = 10)
    private double lon1;

    @NotNull
    @Column(precision = 10)
    private double lat2;

    @NotNull
    @Column(precision = 10)
    private double lon2;

    @Column(columnDefinition = "long default 0")
    private long latImgNb;

    @Column(columnDefinition = "long default 0")
    private long lonImgNb;

    public FlightPlan() {

    }

    public FlightPlan(@NotNull @Size(max = 30) String name, @NotNull double lat1, @NotNull double lon1, @NotNull double lat2, @NotNull double lon2) {
        this.name = name;
        this.lat1 = lat1;
        this.lon1 = lon1;
        this.lat2 = lat2;
        this.lon2 = lon2;
        this.latImgNb = 0;
        this.lonImgNb = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat1() {
        return lat1;
    }

    public void setLat1(double lat1) {
        this.lat1 = lat1;
    }

    public double getLon1() {
        return lon1;
    }

    public void setLon1(double lon1) {
        this.lon1 = lon1;
    }

    public double getLat2() {
        return lat2;
    }

    public void setLat2(double lat2) {
        this.lat2 = lat2;
    }

    public double getLon2() {
        return lon2;
    }

    public void setLon2(double lon2) {
        this.lon2 = lon2;
    }

    public long getLatImgNb() {
        return latImgNb;
    }

    public void setLatImgNb(long latImgNb) {
        this.latImgNb = latImgNb;
    }

    public long getLonImgNb() {
        return lonImgNb;
    }

    public void setLonImgNb(long lonImgNb) {
        this.lonImgNb = lonImgNb;
    }
}
