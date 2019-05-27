package com.example.arush.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Game extends AppCompatActivity {

    ArrayList <String> monumentURLs = new ArrayList<>();
    ArrayList <String> monumentNames = new ArrayList<>();
    int chosenMonument = 0, locationOfCorrectAnswer =0;
    String[] answers = new String[4];
    ImageView imageView;
    Button button1, button2, button3, button4;

    static class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                return BitmapFactory.decodeStream(inputStream);

            } catch (MalformedURLException e) {

                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    static class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            URL url;
            HttpURLConnection urlConnection = null;
            String result = "";
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int data = inputStreamReader.read();
                while (data != -1) {
                    char current = (char) data;
                    result+=current;
                    data = inputStreamReader.read();
                }
                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        imageView = findViewById(R.id.imageView);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        DownloadTask task = new DownloadTask();
        String result = null;
        try {
            result = task.
                    execute("https://www.tourmyindia.com/blog/top-monuments-in-india-that-reflect-rich-culture-and-heritage/")
                    .get();

            String[] splitResult = result.split("Here is our list of top monuments in India that are absolutely a must visit:");
            Pattern p = Pattern.compile("class=\"aligncenter size-full wp-image-91[4-9][0-9]\" src=\"(.*?)\" alt=\"");
            Matcher m = p.matcher(splitResult[1]);
            while (m.find()) {
                monumentURLs.add(m.group(1));
            }

            Pattern p1 = Pattern.compile("<h3>(.*?)</h3>\n<p><img class=\"aligncenter size-full wp-image-91[4-9][0-9]\"");

            Matcher m1 = p1.matcher(splitResult[1]);
            while (m1.find()) {
                monumentNames.add(m1.group(1));
            }

        } catch (ExecutionException e) {

            e.printStackTrace();

        } catch (InterruptedException e) {

            e.printStackTrace();
        }

        createNewQuestion();
    }

    public void celebChosen(View view) {
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {

            Toast.makeText(getApplicationContext(),"Correct!!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(),"Wrong! : It was " +
                    monumentNames.get(chosenMonument), Toast.LENGTH_LONG).show();
        }
        createNewQuestion();
    }

    public void createNewQuestion() {
        Random random = new Random();
        chosenMonument = random.nextInt(monumentURLs.size());//chosenMonument = 10;

        ImageDownloader imageTask = new ImageDownloader();
        Bitmap monumentImage;

        try {
            monumentImage = imageTask.execute(monumentURLs.get(chosenMonument)).get();

            imageView.setImageBitmap(monumentImage);

            locationOfCorrectAnswer = random.nextInt(4);
            int incorrectAnswerLocation;
            for (int i=0; i<4; i++) {
                if (i==locationOfCorrectAnswer) {
                    answers[i] = monumentNames.get(chosenMonument);
                } else {
                    incorrectAnswerLocation = random.nextInt(monumentURLs.size());
                    while (incorrectAnswerLocation == chosenMonument) {
                        incorrectAnswerLocation = random.nextInt(monumentURLs.size());
                    }
                    answers[i] = monumentNames.get(incorrectAnswerLocation);
                }
            }
            button1.setText(answers[0]);
            button2.setText(answers[1]);
            button3.setText(answers[2]);
            button4.setText(answers[3]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
