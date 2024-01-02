package com.ngtnl1.foreign_language_learning_app.controller.adapter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.model.Topic;
import com.ngtnl1.foreign_language_learning_app.model.Vocabulary;
import com.ngtnl1.foreign_language_learning_app.service.AppStatusService;
import com.ngtnl1.foreign_language_learning_app.service.TopicService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Getter;

@Getter
public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.ViewHolder> {
    public interface OnVocabularyItemClickListener {
        void onItemLongClick(int position, Vocabulary vocabulary);
        void onItemClick(int position, Vocabulary vocabulary);
    }

    private OnVocabularyItemClickListener onVocabularyItemClickListener;
    private AppStatusService appStatusService;
    private TopicService topicService;
    private Context context;
    private TextToSpeech tts;
    private List<Vocabulary> items;
    private boolean isLoadByPublic;
    private boolean isLoadByNoted;
    private int longClickedPosition = -1;

    public VocabularyAdapter(List<Vocabulary> items, AppStatusService appStatusService, TopicService topicService, Context context, boolean isLoadByPublic, boolean isLoadByNoted) {
        this.items = items;
        this.appStatusService = appStatusService;
        this.context = context;
        this.isLoadByPublic = isLoadByPublic;
        this.isLoadByNoted = isLoadByNoted;
        this.topicService = topicService;

        this.tts = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    public void setOnVocabularyItemClickListener(OnVocabularyItemClickListener listener) {
        this.onVocabularyItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vocabulary, parent, false);
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
        TextView textViewItemVocabularyWord;
        TextView textViewItemVocabularyVietNamMeaning;
        TextView textViewItemVocabularyPhonetic;
        TextView textViewItemVocabularyType;
        TextView textViewItemVocabularyDescription;
        TextView textViewItemVocabularyExample;
        Button buttonItemVocabularyPronunciation;
        ImageView imageViewStar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewItemVocabularyWord = itemView.findViewById(R.id.textViewItemVocabularyWord);
            textViewItemVocabularyVietNamMeaning = itemView.findViewById(R.id.textViewItemVocabularyVietNamMeaning);
            textViewItemVocabularyPhonetic = itemView.findViewById(R.id.textViewItemVocabularyPhonetic);
            textViewItemVocabularyType = itemView.findViewById(R.id.textViewItemVocabularyType);
            textViewItemVocabularyDescription = itemView.findViewById(R.id.textViewItemVocabularyDescription);
            textViewItemVocabularyExample = itemView.findViewById(R.id.textViewItemVocabularyExample);
            buttonItemVocabularyPronunciation = itemView.findViewById(R.id.buttonItemVocabularyPronunciation);
            imageViewStar = itemView.findViewById(R.id.imageViewItemVocabularyStar);

            if (isLoadByPublic) {
                imageViewStar.setVisibility(View.GONE);
            }
        }

        void bind(Vocabulary vocabulary) {
            textViewItemVocabularyWord.setText(vocabulary.getWord());
            textViewItemVocabularyVietNamMeaning.setText(vocabulary.getMeaning());
            if (vocabulary.getPhonetic() == null || vocabulary.getPhonetic().isEmpty()) {
                textViewItemVocabularyPhonetic.setText("Không có phiên âm");
            } else {
                textViewItemVocabularyPhonetic.setText(vocabulary.getPhonetic());
            }
            textViewItemVocabularyType.setText(vocabulary.getType());
            if (vocabulary.getDescription() == null || vocabulary.getDescription().isEmpty()) {
                textViewItemVocabularyDescription.setText("Không có mô tả");
            } else {
                textViewItemVocabularyDescription.setText(vocabulary.getDescription());
            }
            if (vocabulary.getExample() == null || vocabulary.getExample().isEmpty()) {
                textViewItemVocabularyExample.setText("Không có ví dụ");
            } else {
                textViewItemVocabularyExample.setText(vocabulary.getExample());
            }

            imageViewStar.setImageResource(vocabulary.isNoted() ? R.drawable.ic_star : R.drawable.ic_star_outline);


            imageViewStar.setOnClickListener(v -> {
                updateVocabularyNotedStatus(vocabulary);
            });
            
            buttonItemVocabularyPronunciation.setOnClickListener(v -> {
                try {
                    tts.speak(vocabulary.getWord(), TextToSpeech.QUEUE_FLUSH, null, null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(context, "Lỗi! Không thể phát bản ghi âm thanh của từ vựng", Toast.LENGTH_SHORT).show();
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();

                if (position != RecyclerView.NO_POSITION && onVocabularyItemClickListener != null) {
                    onVocabularyItemClickListener.onItemLongClick(position, items.get(position));
                    return true;
                }
                return false;
            });

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onVocabularyItemClickListener != null) {
                    onVocabularyItemClickListener.onItemClick(position, items.get(position));
                }
            });
        }

        public void updateVocabularyNotedStatus(Vocabulary vocabulary) {
            topicService.getTopicById(vocabulary.getTopicId()).addOnSuccessListener(documentSnapshot -> {
                Topic topic = documentSnapshot.toObject(Topic.class);
                if (topic != null) {
                    for (Vocabulary v : topic.getVocabularies()) {
                        if (v.getWord().equals(vocabulary.getWord())) {
                            vocabulary.setNoted(!vocabulary.isNoted());
                            imageViewStar.setImageResource(vocabulary.isNoted() ? R.drawable.ic_star : R.drawable.ic_star_outline);

                            v.setNoted(vocabulary.isNoted());

                            if (isLoadByNoted) {
                                items.remove(vocabulary);
                                notifyDataSetChanged();
                            }
                            break;
                        }
                    }
                    topicService.update(topic.getId(), topic);
                }
            });
        }
    }
}

