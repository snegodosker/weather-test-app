package com.example.testapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<String> dates;
    private ArrayList<String> weathers;

    public RecyclerViewAdapter(Context context ,ArrayList<String> dates, ArrayList<String> weathers) {
        this.dates = dates;
        this.weathers = weathers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.weather.setMovementMethod(new ScrollingMovementMethod());
        holder.date.setText(dates.get(position));
        holder.weather.setText(weathers.get(position));
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView date;
        TextView weather;

        public ViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.dateTextView);
            weather = itemView.findViewById(R.id.weatherTextView);
        }
    }



}
