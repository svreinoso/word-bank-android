package com.example.samuel.wordbank;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.samuel.wordbank.data.Word;
import com.example.samuel.wordbank.data.WordStatus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private final Context context;
    private List<Word> moviesList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, year, genre, meaning, tvTranslate;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            genre = (TextView) view.findViewById(R.id.genre);
            year = (TextView) view.findViewById(R.id.year);
            meaning = (TextView) view.findViewById(R.id.tvMeaning);
            tvTranslate = (TextView) view.findViewById(R.id.tvTranslate);
        }
    }


    public MyAdapter(List<Word> moviesList, Context context) {
        this.moviesList = moviesList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.word_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Word movie = moviesList.get(position);
        holder.title.setText(movie.getName());
        holder.genre.setText(getStringStatus(movie.getStatus()));
        holder.year.setText(getStringDate(movie.getCreatedDate()));
        holder.meaning.setText(movie.getMeaning());
        holder.tvTranslate.setText(movie.getTranslate());
        switch (movie.getStatus()){
            case WordStatus.ADDED:
                holder.genre.setTextColor(Color.parseColor("#F5293E"));
                break;
            case WordStatus.LEARNED:
                holder.genre.setTextColor(Color.parseColor("#19AF05"));
                break;
            case WordStatus.LEARNING:
                holder.genre.setTextColor(Color.parseColor("#C2C205"));
        }
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }

    public String getStringDate(long date){
        DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        return df.format(date);
    }

    private String getStringStatus(int status) {
        switch (status){
            case WordStatus.ADDED:
                return context.getString(R.string.added);
            case WordStatus.LEARNING:
                return context.getString(R.string.learning);
            case WordStatus.LEARNED:
                return context.getString(R.string.learned);
            default:
                return context.getString(R.string.added);
        }
    }
}
