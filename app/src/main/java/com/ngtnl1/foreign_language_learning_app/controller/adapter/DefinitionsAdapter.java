package com.ngtnl1.foreign_language_learning_app.controller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.model.Definition;

import java.util.List;

public class DefinitionsAdapter extends RecyclerView.Adapter<DefinitionsAdapter.ViewHolder> {
    private Context context;

    public DefinitionsAdapter(Context context, List<Definition> definitionList) {
        this.context = context;
        this.definitionList = definitionList;
    }

    private List<Definition> definitionList;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_definition, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textViewItemDefinition.setText("Mô tả: "+ definitionList.get(position).getDefinition());
        holder.textViewItemDefinitionExample.setText("Ví dụ: " + definitionList.get(position).getExample());
        StringBuilder synonyms = new StringBuilder();
        StringBuilder antonyms = new StringBuilder();

        synonyms.append(definitionList.get(position).getSynonyms());
        antonyms.append(definitionList.get(position).getSynonyms());

        holder.textViewItemDefinitionSynonym.setText(synonyms);
        holder.textViewItemDefinitionAntonym.setText(antonyms);

        holder.textViewItemDefinitionSynonym.setSelected(true);
        holder.textViewItemDefinitionAntonym.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return definitionList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewItemDefinition;
        public TextView textViewItemDefinitionExample;
        public TextView textViewItemDefinitionSynonym;
        public TextView textViewItemDefinitionAntonym;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewItemDefinition = itemView.findViewById(R.id.textViewItemDefinition);
            textViewItemDefinitionExample = itemView.findViewById(R.id.textViewItemDefinitionExample);
            textViewItemDefinitionSynonym = itemView.findViewById(R.id.textViewItemDefinitionSynonym);
            textViewItemDefinitionAntonym = itemView.findViewById(R.id.textViewItemDefinitionAntonym);
        }
    }

}
