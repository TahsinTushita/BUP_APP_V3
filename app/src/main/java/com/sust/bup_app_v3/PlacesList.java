package com.sust.bup_app_v3;

import java.util.ArrayList;

public class PlacesList {
    ArrayList<Places> placesArrayList;

    public PlacesList() {
    }

    public PlacesList(ArrayList<Places> placesArrayList) {
        this.placesArrayList = placesArrayList;
    }

    public ArrayList<Places> getPlacesArrayList() {
        return placesArrayList;
    }

    public void setPlacesArrayList(ArrayList<Places> placesArrayList) {
        this.placesArrayList = placesArrayList;
    }
}
