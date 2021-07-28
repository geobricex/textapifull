package com.example.textidentifier;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.IdentifiedLanguage;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView multinetext;
    Button buttonSelect;
    Button buttonCapture;
    Spinner spinner;
    TextView textView;

    String textResult = "";

    InputImage inputImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 202);

        initialize();

        OnClickListener();

        cleanData();

    }

    private void initialize() {
        imageView = findViewById(R.id.imgView);
        buttonSelect = findViewById(R.id.btnSelect);
        buttonCapture = findViewById(R.id.btnCapture);
        multinetext = findViewById(R.id.txtResult);
        multinetext.setText("");

        textView = findViewById(R.id.lblLanguajes);

        spinner = (Spinner) findViewById(R.id.spnLanguajes);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.option, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);//Fill the Spinner
    }

    private void OnClickListener() {
        buttonSelect.setOnClickListener(v -> {
            Intent gallery = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            gallery.setType("Image/*");
            startActivityForResult(gallery, 200);
        });
        buttonCapture.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent gallery = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(gallery, 2000);
            } else
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 2000);
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                //Log.i("Logs", spinner.getSelectedItem().toString());
                if (!multinetext.getText().equals("")) {
                    possibleLanguageIdentifier(textResult, spinner.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

    }

    private void cleanData() {
//        imageView.setImageDrawable(getResources().getDrawable(R.drawable.principaltext));
        multinetext.setText("");
        textView.setText("");
    }

    private void getImage(Uri uriParam) {

        try {
            //1. Prepare the input image
            //https://developers.google.com/ml-kit/vision/text-recognition/android#using-a-file-uri
            inputImage = InputImage.fromFilePath(MainActivity.this, uriParam);

            //2. Get an instance of TextRecognizer
            //https://developers.google.com/ml-kit/vision/text-recognition/android#2.-get-an-instance-of-textrecognizer
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            //3. Process the image
            //https://developers.google.com/ml-kit/vision/text-recognition/android#3.-process-the-image
            Task<Text> result =
                    recognizer.process(inputImage)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text visionText) {
                                    //4. Extract text from blocks of recognized text
                                    //https://developers.google.com/ml-kit/vision/text-recognition/android#4.-extract-text-from-blocks-of-recognized-text
                                    String resultText = visionText.getText();
                                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                                        String blockText = block.getText();
                                        Point[] blockCornerPoints = block.getCornerPoints();
                                        Rect blockFrame = block.getBoundingBox();
                                        for (Text.Line line : block.getLines()) {
                                            String lineText = line.getText();
                                            Point[] lineCornerPoints = line.getCornerPoints();
                                            Rect lineFrame = line.getBoundingBox();
                                            for (Text.Element element : line.getElements()) {
                                                String elementText = element.getText();
                                                Point[] elementCornerPoints = element.getCornerPoints();
                                                Rect elementFrame = element.getBoundingBox();
                                            }
                                        }
                                    }

                                    textResult = resultText;
                                    multinetext.setText(textResult);
                                    possibleLanguageIdentifier(textResult, "es");
                                    cleanData();
//                                    languageIdentifier(textResult);
//                                    translatorLanguage(textResult, "es", "en");
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                        }
                                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getImageBitmap(Bitmap bitmapParam) {

        //1. Prepare the input image
        //https://developers.google.com/ml-kit/vision/text-recognition/android#using-a-bitmap
        inputImage = InputImage.fromBitmap(bitmapParam, 0);

        //2. Get an instance of TextRecognizer
        //https://developers.google.com/ml-kit/vision/text-recognition/android#2.-get-an-instance-of-textrecognizer
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        //3. Process the image
        //https://developers.google.com/ml-kit/vision/text-recognition/android#3.-process-the-image
        Task<Text> result =
                recognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                //4. Extract text from blocks of recognized text
                                //https://developers.google.com/ml-kit/vision/text-recognition/android#4.-extract-text-from-blocks-of-recognized-text
                                String resultText = visionText.getText();
                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    String blockText = block.getText();
                                    Point[] blockCornerPoints = block.getCornerPoints();
                                    Rect blockFrame = block.getBoundingBox();
                                    for (Text.Line line : block.getLines()) {
                                        String lineText = line.getText();
                                        Point[] lineCornerPoints = line.getCornerPoints();
                                        Rect lineFrame = line.getBoundingBox();
                                        for (Text.Element element : line.getElements()) {
                                            String elementText = element.getText();
                                            Point[] elementCornerPoints = element.getCornerPoints();
                                            Rect elementFrame = element.getBoundingBox();
                                        }
                                    }
                                }

                                textResult = resultText;
                                multinetext.setText(textResult);
                                possibleLanguageIdentifier(textResult, "es");
                                cleanData();
//                                    languageIdentifier(textResult);
//                                    translatorLanguage(textResult, "es", "en");
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });

    }

    private void possibleLanguageIdentifier(String textResultParam, String translatorFinish) {
        LanguageIdentifier languageIdentifier =
                LanguageIdentification.getClient();
        languageIdentifier.identifyPossibleLanguages(textResultParam)
                .addOnSuccessListener(new OnSuccessListener<List<IdentifiedLanguage>>() {
                    @Override
                    public void onSuccess(List<IdentifiedLanguage> identifiedLanguages) {
                        boolean flag = true;
                        String languageConfidence = "";
                        for (IdentifiedLanguage identifiedLanguage : identifiedLanguages) {

                            String language = identifiedLanguage.getLanguageTag();
                            float confidence = identifiedLanguage.getConfidence();
                            confidence = confidence * 100;

                            //https://developers.google.com/ml-kit/language/identification/langid-support?hl=es
                            languageConfidence = languageConfidence + "Lenguaje= " + language + " - Porcentaje= " + confidence + "\n";

                            if (flag) {
                                translatorLanguage(textResultParam, language, translatorFinish);
                                flag = false;
                            }
                        }
                        Log.i("Logs", languageConfidence);
                        textView.setText(languageConfidence);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be loaded or other internal error.
                                // ...
                            }
                        });

    }

    private void translatorLanguage(String textResultParam, String tagLanguageStart, String tagLanguageEnd) {
        // Create an English-German translator:
        try {
            TranslatorOptions options =
                    new TranslatorOptions.Builder()
                            .setSourceLanguage(Objects.requireNonNull(TranslateLanguage.fromLanguageTag(tagLanguageStart)))
                            .setTargetLanguage(Objects.requireNonNull(TranslateLanguage.fromLanguageTag(tagLanguageEnd)))
                            .build();
            final Translator translator =
                    Translation.getClient(options);


            DownloadConditions conditions = new DownloadConditions.Builder()
                    .requireWifi()
                    .build();
            translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener(
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void v) {
                                    // Model downloaded successfully. Okay to start translating.
                                    // (Set a flag, unhide the translation UI, etc.)
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Model couldn’t be downloaded or other internal error.
                                    // ...
                                }
                            });

            translator.translate(textResultParam)
                    .addOnSuccessListener(
                            new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(@NonNull String translatedText) {
                                    Log.i("Logs", "Language: " + translatedText);
                                    multinetext.setText("");
                                    multinetext.setText(translatedText);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Error.
                                    // ...
                                }
                            });
        } catch (Exception e) {
            Log.i("Logs", "Language: " + e.toString());

        }
    }

    private void languageIdentifier(String textResultParam) {
        LanguageIdentifier languageIdentifier =
                LanguageIdentification.getClient();
        languageIdentifier.identifyLanguage(textResultParam)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@Nullable String languageCode) {
                                if (languageCode.equals("und")) {
                                    Log.i("Logs", "Can't identify language.");
                                } else {
                                    Log.i("Logs", "Language: " + languageCode);
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be loaded or other internal error.
                                // ...
                            }
                        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (resultCode == RESULT_OK && requestCode == 200) {
                Uri uri = data.getData();
                if (imageView != null) {
                    try {
                        imageView.setImageURI(uri);
                        getImage(uri);
                    } catch (Exception ex) {
                        Log.i("Logs", ex.toString());
                    }
                }
            } else if (resultCode == RESULT_OK && requestCode == 2000) {
                Bundle extras = data.getExtras();
                Bitmap bitmap = (Bitmap) extras.get("data");
                if (imageView != null) {
                    try {
                        imageView.setImageBitmap(bitmap);
                        getImageBitmap(bitmap);
                    } catch (Exception ex) {
                        Log.i("Logs", ex.toString());
                    }
                }
            }
        }
    }
}