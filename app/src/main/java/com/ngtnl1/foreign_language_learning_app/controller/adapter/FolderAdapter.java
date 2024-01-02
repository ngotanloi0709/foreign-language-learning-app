package com.ngtnl1.foreign_language_learning_app.controller.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.model.Folder;

import java.util.List;

import lombok.Getter;

@Getter
public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder>{
    public interface OnFolderItemClickListener {
        void onItemLongClick(int position, Folder folder);
        void onItemClick(int position, Folder folder);
    }

    private OnFolderItemClickListener onFolderItemClickListener;
    List<Folder> items;
    private int longClickedPosition = -1;

    public void setOnFolderItemClickListener(OnFolderItemClickListener listener) {
        this.onFolderItemClickListener = listener;
    }

    public FolderAdapter(List<Folder> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));

        holder.itemView.setOnLongClickListener(v -> {
            longClickedPosition = holder.getAdapterPosition();
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewItemFolderName;

        ViewHolder(View itemView) {
            super(itemView);

            textViewItemFolderName = itemView.findViewById(R.id.textViewItemFolderName);
        }

        void bind(Folder folder) {
            textViewItemFolderName.setText(folder.getName());

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onFolderItemClickListener != null) {
                    onFolderItemClickListener.onItemLongClick(position, items.get(position));
                    return true;
                }
                return false;
            });

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onFolderItemClickListener != null) {
                    onFolderItemClickListener.onItemClick(position, items.get(position));
                }
            });
        }
    }
}
