package com.ngtnl1.foreign_language_learning_app.controller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.model.Meaning;

import java.util.List;

public class MeaningsAdapter extends RecyclerView.Adapter<MeaningsAdapter.ViewHolder> {
    private Context context;
    protected List<Meaning> meaningList;

    public MeaningsAdapter(Context context, List<Meaning> meaningList) {
        this.context = context;
        this.meaningList = meaningList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_meaning,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textViewItemMeaningPartOfSpeech.setText("Loại từ: "+ meaningList.get(position).getPartOfSpeech());
        holder.recyclerViewDefinitions.setHasFixedSize(true);
        holder.recyclerViewDefinitions.setLayoutManager(new GridLayoutManager(context, 1));
        holder.recyclerViewDefinitions.setAdapter(new DefinitionsAdapter(context, meaningList.get(position).getDefinitions()));
    }

    @Override
    public int getItemCount() {
        return meaningList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewItemMeaningPartOfSpeech;
        public RecyclerView recyclerViewDefinitions;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItemMeaningPartOfSpeech = itemView.findViewById(R.id.textViewItemMeaningPartOfSpeech);
            recyclerViewDefinitions = itemView.findViewById(R.id.recyclerViewDefinitions);
        }
    }
}
