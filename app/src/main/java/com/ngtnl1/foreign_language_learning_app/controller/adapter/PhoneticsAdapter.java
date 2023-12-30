package com.ngtnl1.foreign_language_learning_app.controller.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.service.AppStatusService;
import com.ngtnl1.foreign_language_learning_app.model.Phonetic;

import java.util.List;

public class PhoneticsAdapter extends RecyclerView.Adapter<PhoneticsAdapter.ViewHolder> {
    private Context context;
    private List<Phonetic> phoneticList;
    private AppStatusService appStatusService;

    public PhoneticsAdapter(Context context, List<Phonetic> phonetics, AppStatusService appStatusService) {
        this.context = context;
        this.phoneticList = phonetics;
        this.appStatusService = appStatusService;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_phonetic, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.textViewItemPhonetic.setText(phoneticList.get(position).getText());
        holder.imageButtonItemPhoneticPlayAudio.setOnClickListener(v -> {
            MediaPlayer player = new MediaPlayer();

            if (!appStatusService.isOnline()) {
                Toast.makeText(context, "Không có kết nối Internet!", Toast.LENGTH_SHORT).show();
                return;
            }

            try{
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource(phoneticList.get(position).getAudio());
                player.prepare();
                player.start();
            } catch (Exception e){
                e.printStackTrace();
                Toast.makeText(context, "Lỗi! Không thể phát bản ghi âm thanh của từ vựng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return phoneticList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewItemPhonetic;
        public ImageButton imageButtonItemPhoneticPlayAudio;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItemPhonetic = itemView.findViewById(R.id.textViewItemPhonetic);
            imageButtonItemPhoneticPlayAudio = itemView.findViewById(R.id.imageButtonItemPhoneticPlayAudio);
        }
    }
}
