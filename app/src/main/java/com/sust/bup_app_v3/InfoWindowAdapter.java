package com.sust.bup_app_v3;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private RecyclerView rv;
    private ArrayList<Places>  al = new ArrayList<>();

    public InfoWindowAdapter(Context context) {
            this.context = context;
    }

        @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        al = (ArrayList<Places>) marker.getTag();
        View v =((Activity) context).getLayoutInflater().inflate(R.layout.infowindow_layout, null);
        rv =v.findViewById(R.id.infoRecyclerviewid);
        PlacesAdapter adapter = new PlacesAdapter(context,al);
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Toast.makeText(context,"Size " + al.size(),Toast.LENGTH_SHORT).show();
        return null;
    }
}
