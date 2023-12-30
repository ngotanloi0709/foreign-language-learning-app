package com.ngtnl1.foreign_language_learning_app.di;

import android.app.Application;
import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.ngtnl1.foreign_language_learning_app.service.AppStatusService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class ApplicationModule {
    @Provides
    @Singleton
    public FirebaseAuth provideFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    @Singleton
    public Context provideContext(Application application) {
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    public StorageReference provideStorageReference() {
        return FirebaseStorage.getInstance().getReference();
    }

    @Provides
    @Singleton
    public Translator provideTranslator() {
        TranslatorOptions options = new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.VIETNAMESE)
                        .build();

        return Translation.getClient(options);
    }
}
