/*
package com.example.osmtest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class routeDataAdapter extends RecyclerView.Adapter<routeDataAdapter.routeViewHolder> {

    private String[] data;

    public static class routeViewHolder extends RecyclerView.ViewHolder {

        public TextView tv;

        public routeViewHolder(TextView curView) {
            super(curView);
            tv = curView;
        }
    }

    public routeDataAdapter(String[] routes){

        data = routes;
    }

    public routeDataAdapter.routeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_single_card_view, parent, false);

       // return new routeViewHolder(card);

    }

    public void onBindViewHolder(routeViewHolder h, int position){

        h.tv.setText(data[position]);

    }

    public int getItemCount() {

        return  data.length;

    }

    }

*/






