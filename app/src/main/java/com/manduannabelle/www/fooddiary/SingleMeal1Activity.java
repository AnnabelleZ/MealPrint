package com.manduannabelle.www.fooddiary;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;

import in.mayanknagwanshi.imagepicker.ImageSelectActivity;

public class SingleMeal1Activity extends UtilityActivity{
    public static final String EXTRA_PATH = "com.manduannabelle.www.fooddiary.EXTRA_PATH";
    public static final String EXTRA_TEXT = "com.manduannabelle.www.fooddiary.EXTRA_TEXT";
    public static final String SHARED_PREFS = "sharedPrefs";
    private ImageView meal1image;
    private Bitmap selectedImage;
    private String imgPath;
    private EditText editTitle;
    private EditText editNote;
    private String meal1_title = "";
    private String meal1_note = "";
    private Boolean imgSet;
    private TextView time;
    private String meal1_time;
    ImageManager imgManager = new ImageManager(SingleMeal1Activity.this);
    String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singlemeal1);

        // to support hiding the keyboard when clicked outside
        setupUI(findViewById(R.id.meal1));

        editTitle = findViewById(R.id.meal1_title);
        editNote = findViewById(R.id.meal1_note);

        // for the date on the toolbar
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        Calendar calendar = Calendar.getInstance();
        currentDate = sharedPreferences.getString("current_date", DateFormat.getDateInstance().format(calendar.getTime()));
        TextView textViewDate = findViewById(R.id.text_view_date);
        textViewDate.setText(currentDate);

        meal1image = findViewById(R.id.meal1_image);
        time = findViewById(R.id.meal1_time);
        loadImageIndicator();
        loadData();
        updateViews();
        takePhoto();
        retake();
        // go back
        ImageButton backButton = findViewById(R.id.toolbar_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToMain();
            }
        });
        setMealTime();
        TimeManager.setDefaultTime(time);
    }

    public void saveDate(String currentDate) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("current_date", currentDate);
        editor.apply();
    }

    public void setMealTime() {
        time.setOnClickListener(new View.OnClickListener() {
            int currentHour;
            int currentMinute;

            @Override
            public void onClick(View view) {
                TimeManager.timePickerImplementer(currentHour, currentMinute, SingleMeal1Activity.this, time);
            }
        });
    }

    public void loadImageIndicator() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        imgSet = sharedPreferences.getBoolean(currentDate + "_meal1_imgSet", false);
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // save data
        editor.putString(currentDate + "_meal1_title", editTitle.getText().toString());
        editor.putString(currentDate + "_meal1_note", editNote.getText().toString());
        editor.putString(currentDate + "_meal1_time", time.getText().toString());

        editor.apply();

        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();
    }

    public void savePhoto() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (selectedImage != null)
            editor.putString(currentDate + "_meal1_imgPath", imgManager.saveImageToInternalStorage(selectedImage, 1, currentDate));
        editor.apply();

        Toast.makeText(this, "Photo saved", Toast.LENGTH_SHORT).show();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        meal1_title = sharedPreferences.getString(currentDate + "_meal1_title", "Breakfast");
        meal1_note = sharedPreferences.getString(currentDate + "_meal1_note", "");
        imgPath = sharedPreferences.getString(currentDate + "_meal1_imgPath", "");
        meal1_time = sharedPreferences.getString(currentDate + "_meal1_time", "");
    }

    public void updateViews() {
        editTitle.setText(meal1_title);
        editNote.setText(meal1_note);
        if (imgSet)
            imgManager.loadImageFromStorage(imgPath, 1, meal1image, currentDate);
        time.setText(meal1_time);
    }

    public void backToMain() {
        saveData();
        loadData();

        // switch from SingleMeal1Activity to MainActivity
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(EXTRA_TEXT, meal1_title);

        intent.putExtra(EXTRA_PATH, imgPath);
        startActivity(intent);
    }

    /* ----------------------------------- PHOTO TAKING ------------------------------- */
    public void takePhoto() {
        // take photo
        if (!imgSet) {
            Intent intent = new Intent(this, ImageSelectActivity.class);
            intent.putExtra(ImageSelectActivity.FLAG_COMPRESS, true);
            intent.putExtra(ImageSelectActivity.FLAG_CAMERA, true);
            intent.putExtra(ImageSelectActivity.FLAG_GALLERY, true);
            startActivityForResult(intent, 1213);
        }
    }

    public void retake() {
        // retake photo
        TextView retake = findViewById(R.id.toolbar_retake);
        retake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgSet = false;
                imgManager.saveImageIndicator(1, imgSet, currentDate);
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(currentDate + "_meal1_imgPath", null);
                selectedImage = null;
                editor.apply();
                imgPath = sharedPreferences.getString(currentDate + "_meal1_imgPath", "");
                meal1image.setImageResource(android.R.color.transparent);
                takePhoto();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1213 && resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra(ImageSelectActivity.RESULT_FILE_PATH);
            selectedImage = BitmapFactory.decodeFile(filePath);
            meal1image.setImageBitmap(selectedImage);
            TimeManager.setDefaultTime(time);
            imgSet = true;
            imgManager.saveImageIndicator(1, imgSet, currentDate);
            savePhoto();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }
}