package fr.rostand.drone.model;

import com.google.gson.annotations.SerializedName;

public class FlightPlan {
    @SerializedName("id")
    private long mId;
    @SerializedName("name")
    private String mName;

    @SerializedName("lat1")
    private double mLat1;
    @SerializedName("lon1")
    private double mLon1;
    @SerializedName("lat2")
    private double mLat2;
    @SerializedName("lon2")
    private double mLon2;

    @SerializedName("latImgNb")
    private long mLatImgNb;
    @SerializedName("lonImgNb")
    private long mLonImgNb;

    public FlightPlan() {
        this(0, "TestHorsConnexion", 50.696622, 3.197290, 50.696801, 3.197521);
    }

    public FlightPlan(long id, String name, double lat1, double lon1, double lat2, double lon2) {
        mId = id;
        mName = name;
        mLat1 = lat1;
        mLon1 = lon1;
        mLat2 = lat2;
        mLon2 = lon2;
        mLatImgNb = 0;
        mLonImgNb = 0;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public double getLat1() {
        return mLat1;
    }

    public void setLat1(double lat1) {
        mLat1 = lat1;
    }

    public double getLon1() {
        return mLon1;
    }

    public void setLon1(double lon1) {
        mLon1 = lon1;
    }

    public double getLat2() {
        return mLat2;
    }

    public void setLat2(double lat2) {
        mLat2 = lat2;
    }

    public double getLon2() {
        return mLon2;
    }

    public void setLon2(double lon2) {
        mLon2 = lon2;
    }

    public long getLatImgNb() {
        return mLatImgNb;
    }

    public void setLatImgNb(long latImgNb) {
        mLatImgNb = latImgNb;
    }

    public long getLonImgNb() {
        return mLonImgNb;
    }

    public void setLonImgNb(long lonImgNb) {
        mLonImgNb = lonImgNb;
    }

    @Override
    public String toString() {
        return "FlightPlan{" +
                "mId=" + mId +
                ", mName='" + mName + '\'' +
                ", mLat1=" + mLat1 +
                ", mLon1=" + mLon1 +
                ", mLat2=" + mLat2 +
                ", mLon2=" + mLon2 +
                ", mLatImgNb=" + mLatImgNb +
                ", mLonImgNb=" + mLonImgNb +
                '}';
    }
}
