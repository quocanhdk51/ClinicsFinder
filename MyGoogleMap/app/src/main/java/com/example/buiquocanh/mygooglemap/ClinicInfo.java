/*Acknoledgement: https://www.youtube.com/playlist?list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt*/

package com.example.buiquocanh.mygooglemap;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class ClinicInfo {
    private String id;
    private CharSequence name;
    private CharSequence address;
    private float rating;
    private String impression;
    private String leadPhysician;
    private List<Integer> specializations;
    private int avrPrice;
    private LatLng latLng;
    private String content;

    public ClinicInfo(String id, CharSequence name, CharSequence address, float rating, String impression, String leadPhysician, List<Integer> specializations, int avrPrice, LatLng latLng, String content) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.impression = impression;
        this.leadPhysician = leadPhysician;
        this.specializations = specializations;
        this.avrPrice = avrPrice;
        this.latLng = latLng;
        this.content = content;
    }

    public ClinicInfo() {
        this(null, null, null, -1, null, null, null, -1, null, null);
    }

    public String getLocationId() {
        return id;
    }

    public void setLocationId(String id) {
        this.id = id;
    }

    public CharSequence getLocationName() {
        return name;
    }

    public void setLocationName(CharSequence name) {
        this.name = name;
    }

    public CharSequence getAddress() {
        return address;
    }

    public void setAddress(CharSequence address) {
        this.address = address;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getImpression() {
        return impression;
    }

    public void setImpression(String impression) {
        this.impression = impression;
    }

    public String getLeadPhysician() {
        return leadPhysician;
    }

    public void setLeadPhysician(String leadPhysician) {
        this.leadPhysician = leadPhysician;
    }

    public List<Integer> getSpecializations() {
        return specializations;
    }

    public void setSpecializations(List<Integer> specializations) {
        this.specializations = specializations;
    }

    public int getAvrPrice() {
        return avrPrice;
    }

    public void setAvrPrice(int avrPrice) {
        this.avrPrice = avrPrice;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
