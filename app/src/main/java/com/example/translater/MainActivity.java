package com.example.translater;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText sourceLanguage;
    private TextView destinationLanguage;
    private MaterialButton sourceLanguageChooseBtn, destinationLanguageChooseBtn, translateBtn;
    private Translator translator;
    private TranslatorOptions translatorOptions;
    private ProgressDialog progressDialog;
    private ArrayList<ModelLangugage> languageArrayList;
    private static final String TAG = "MAIN_TAG";

    private String sourceLanguageCode = "en";
    private String sourceLanguageTitle = "English";
    private String destinationLanguageCode = "ta";
    private String destinationLanguageTitle = "Tamil";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sourceLanguage = findViewById(R.id.sourceLanguage);
        destinationLanguage = findViewById(R.id.destinationLangugage);
        sourceLanguageChooseBtn = findViewById(R.id.sourceLanguageChooseBtn);
        destinationLanguageChooseBtn = findViewById(R.id.destinationLanguageChooseBtn);
        translateBtn = findViewById(R.id.translateBtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadAvailableLanguage();

        sourceLanguageChooseBtn.setOnClickListener(v -> sourceLanguageChoose());
        destinationLanguageChooseBtn.setOnClickListener(v -> destinationLanguageChoose());
        translateBtn.setOnClickListener(v -> validateData());
    }

    private String sourceLanguageText = "";

    private void validateData() {
        sourceLanguageText = sourceLanguage.getText().toString().trim();
        Log.d(TAG, "validateData:sourceLanguageText:" + sourceLanguageText);

        if (sourceLanguageText.isEmpty()) {
            Toast.makeText(this, "Enter text to translate..", Toast.LENGTH_SHORT).show();
        } else {
            startTranslation();
        }
    }

    private void startTranslation() {
        progressDialog.setMessage("Processing language model....");
        progressDialog.show();

        translatorOptions = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguageCode)
                .setTargetLanguage(destinationLanguageCode)
                .build();

        translator = Translation.getClient(translatorOptions);

        DownloadConditions downloadConditions = new DownloadConditions.Builder()
                .requireCharging()
                .build();

        translator.downloadModelIfNeeded(downloadConditions)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "onSuccess: model ready, starting translate...");
                    progressDialog.setMessage("Translating...");

                    translator.translate(sourceLanguageText)
                            .addOnSuccessListener(translatedText -> {
                                Log.d(TAG, "onSuccess: translatedText: " + translatedText);
                                progressDialog.dismiss();
                                destinationLanguage.setText(translatedText);
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Failed to translate due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Failed to ready model due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sourceLanguageChoose() {
        PopupMenu popupMenu = new PopupMenu(this, sourceLanguageChooseBtn);
        for (int i = 0; i < languageArrayList.size(); i++) {
            popupMenu.getMenu().add(Menu.NONE, i, i, languageArrayList.get(i).languageTitle);
        }
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(item -> {
            int position = item.getItemId();

            sourceLanguageCode = languageArrayList.get(position).languageCode;
            sourceLanguageTitle = languageArrayList.get(position).languageTitle;

            sourceLanguageChooseBtn.setText(sourceLanguageTitle);
            sourceLanguage.setHint("Enter " + sourceLanguageTitle);

            Log.d(TAG, "onMenuItemClick: " + sourceLanguageCode);
            Log.d(TAG, "onMenuItemClick: " + sourceLanguageTitle);

            return true;
        });
    }

    private void destinationLanguageChoose() {
        PopupMenu popupMenu = new PopupMenu(this, destinationLanguageChooseBtn);
        for (int i = 0; i < languageArrayList.size(); i++) {
            popupMenu.getMenu().add(Menu.NONE, i, i, languageArrayList.get(i).languageTitle);
        }
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(item -> {
            int position = item.getItemId();

            destinationLanguageCode = languageArrayList.get(position).languageCode;
            destinationLanguageTitle = languageArrayList.get(position).languageTitle;

            destinationLanguageChooseBtn.setText(destinationLanguageTitle);
            destinationLanguage.setHint("Enter " + destinationLanguageTitle);

            Log.d(TAG, "onMenuItemClick: " + destinationLanguageCode);
            Log.d(TAG, "onMenuItemClick: " + destinationLanguageTitle);

            return true;
        });
    }

    private void loadAvailableLanguage() {
        languageArrayList = new ArrayList<>();

        List<String> languageCodeList = TranslateLanguage.getAllLanguages();
        for (String languageCode : languageCodeList) {
            String languageTitle = new Locale(languageCode).getDisplayLanguage();

            Log.d(TAG, "loadAvailableLanguage: languageCode " + languageCode);
            Log.d(TAG, "loadAvailableLanguage: languageTitle " + languageTitle);

            ModelLangugage modelLanguage = new ModelLangugage(languageCode, languageTitle);
            languageArrayList.add(modelLanguage);
        }
    }
}
