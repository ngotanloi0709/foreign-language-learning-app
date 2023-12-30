package com.ngtnl1.foreign_language_learning_app.service;

import android.content.Context;
import android.widget.Toast;

import com.ngtnl1.foreign_language_learning_app.common.OnFetchDataListener;
import com.ngtnl1.foreign_language_learning_app.model.APIResponse;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

@Singleton
public class VocabularyInfoRequestManager {
    private Context context;

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    @Inject
    public VocabularyInfoRequestManager(Context context) {
        this.context =  context;
    }

    public void getWordMeaning(OnFetchDataListener listener, String word){
        CallDictionary callDictionary = retrofit.create(CallDictionary.class);
        Call<List<APIResponse>> call = callDictionary.callMeanings(word);

        try{
            call.enqueue(new Callback<List<APIResponse>>() {
                @Override
                public void onResponse(Call<List<APIResponse>> call, Response<List<APIResponse>> response) {
                    if (response.isSuccessful()){
                        listener.OnFetchData(response.body().get(0), response.message());
                    } else {
                        listener.OnError("Không tìm thấy dữ liệu của từ vựng!");
                    }
                }

                @Override
                public void onFailure(Call<List<APIResponse>> call, Throwable t) {
                    listener.OnError("Không tìm thấy dữ liệu của từ vựng!");
                }
            });
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context,"Đã có lỗi xảy ra!!!", Toast.LENGTH_SHORT).show();
        }
    }
    public interface CallDictionary {
        @GET("entries/en/{word}")
        Call<List<APIResponse>> callMeanings(
            @Path("word") String word
        );
    }
}
