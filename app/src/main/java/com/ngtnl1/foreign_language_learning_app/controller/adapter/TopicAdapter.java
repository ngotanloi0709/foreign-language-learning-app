package com.ngtnl1.foreign_language_learning_app.controller.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.model.Topic;

import java.util.List;

import lombok.Getter;

@Getter
public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {
    public interface OnTopicItemClickListener {
        void onItemLongClick(int position, Topic topic);
        void onItemClick(int position, Topic topic);
    }

    private OnTopicItemClickListener onTopicItemClickListener;
    List<Topic> items;
    private boolean isPublicView;
    private int longClickedPosition = -1;

    public void setOnTopicItemClickListener(OnTopicItemClickListener listener) {
        this.onTopicItemClickListener = listener;
    }

    public TopicAdapter(List<Topic> items, boolean isPublicView) {
        this.items = items;
        this.isPublicView = isPublicView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic, parent, false);
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
        TextView textViewItemTopicName;
        TextView textViewItemTopicDescription;
        TextView textViewItemTopicOwner;
        TextView textViewItemTopicIsPublic;
        TextView textViewItemTopicView;

        ViewHolder(View itemView) {
            super(itemView);

            textViewItemTopicName = itemView.findViewById(R.id.textViewItemTopicName);
            textViewItemTopicDescription = itemView.findViewById(R.id.textViewItemTopicDescription);
            textViewItemTopicOwner = itemView.findViewById(R.id.textViewItemTopicOwner);
            textViewItemTopicIsPublic = itemView.findViewById(R.id.textViewItemTopicIsPublic);
            textViewItemTopicView = itemView.findViewById(R.id.textViewItemTopicView);

        }

        void bind(Topic topic) {
            textViewItemTopicName.setText(topic.getName());
            if (topic.getDescription() == null || topic.getDescription().isEmpty()) {
                textViewItemTopicDescription.setText("Không có mô tả");
            } else {
                textViewItemTopicDescription.setText(topic.getDescription());
            }
            if (isPublicView) {
                textViewItemTopicOwner.setVisibility(View.VISIBLE);
                textViewItemTopicOwner.setText(topic.getUserId());
            } else {
                textViewItemTopicOwner.setVisibility(View.GONE);
            }
            textViewItemTopicIsPublic.setText(topic.isPublic() ? "Công khai" : "Riêng tư");
            textViewItemTopicView.setText("Lượt truy cập: "+ topic.getViews());

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();

                if (position != RecyclerView.NO_POSITION && onTopicItemClickListener != null) {
                    onTopicItemClickListener.onItemLongClick(position, items.get(position));
                    return true;
                }
                return false;
            });

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onTopicItemClickListener != null) {
                    onTopicItemClickListener.onItemClick(position, items.get(position));
                }
            });
        }
    }
}
