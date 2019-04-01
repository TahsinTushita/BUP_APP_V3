package com.sust.bup_app_v3;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder> {

    ArrayList<Places> placesArrayList;
    Context context;

    public PlacesAdapter(Context context,ArrayList<Places> places) {
        this.context = context;
        this.placesArrayList = places;
    }

    @NonNull
    @Override
    public PlacesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list,viewGroup,false);
        return new PlacesAdapter.PlacesViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull PlacesViewHolder placesViewHolder, int i) {
        Places places = placesArrayList.get(i);
        placesViewHolder.setDetails(places);
    }

    @Override
    public int getItemCount() {
        return placesArrayList.size();
    }

    public class PlacesViewHolder extends RecyclerView.ViewHolder {
        TextView cropName;
        public PlacesViewHolder(@NonNull View itemView) {
            super(itemView);
            cropName = itemView.findViewById(R.id.cropNameid);
        }

        public void setDetails(Places places){
            cropName.setText(places.getCrop());
        }
    }
}
