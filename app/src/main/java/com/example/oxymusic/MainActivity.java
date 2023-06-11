package com.example.oxymusic;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    boolean isPlaying = false;
    Button playBtn;
    Button nextBtn;
    Button backBtn;
    Button searchBtn;
    EditText search;
    MediaPlayer mediaPlayer;
    ListView list;
    SwipeRefreshLayout swipeRefreshLayout;
    String ServerUrl;

    List<Music> playlist;
    List<Music> onServer;
    Music nowPlaying;
    ListIterator<Music> playListPostion;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        ServerUrl = sharedPref.getString("OxyMusicServer", "");

        if (ServerUrl.equals(""))
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Server Adress:");

            final EditText input = new EditText(this);
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    ServerUrl = new String(input.getText() + "/api");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("OxyMusicServer", ServerUrl);
                    editor.commit();
                }
            });

            alert.show();
        }

        playBtn =  (Button) this.findViewById(R.id.button1);
        nextBtn = (Button) this.findViewById(R.id.button2);
        backBtn = (Button) this.findViewById(R.id.button3);
        searchBtn = (Button) this.findViewById(R.id.SearchBtn);
        swipeRefreshLayout = (SwipeRefreshLayout) this.findViewById(R.id.refreshLayout);
        list = (ListView) this.findViewById(R.id.list);
        search = (EditText) this.findViewById(R.id.textInputEditText) ;

        mediaPlayer = new MediaPlayer();

        mediaPlayer.setLooping(false);

        getPlayListFromServer(ServerUrl+"/Musics");

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPlayListFromServer(ServerUrl+"/Musics");
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               if (isPlaying)
               {
                   mediaPlayer.stop();
                   isPlaying = false;
               }
               else
               {
                   String src;

                   if (nowPlaying != null)
                        src = ServerUrl+"/play/"+ nowPlaying.getUrl();
                   else {
                       playListPostion = playlist.listIterator(0);
                       src = ServerUrl + "/play/1";
                   }
                   play(src);
               }

            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (playListPostion.hasNext())
                {
                    Music next = playListPostion.next();
                    play(ServerUrl+"/play/"+ next.getUrl());
                }

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playListPostion.hasPrevious())
                    play(ServerUrl+"/play/"+playListPostion.previous().getUrl());
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = search.getText().toString();
                getPlayListFromServer(ServerUrl+"/Search/"+ searchText);
            }
        });

        list.setOnItemClickListener((parent, view, position, id) -> {
            String urlToPlay = playlist.get(position).getUrl();
            Pattern pattern = Pattern.compile("youtube*",Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(urlToPlay);
            if (!matcher.find())
            {
                nowPlaying = playlist.get(position);
                playListPostion = playlist.listIterator(position);
                play(ServerUrl + "/play/" + urlToPlay);
                getPlayListFromServer(ServerUrl+"/Musics");
            }
            else
            {
                try {
                    downloadMusicToServer(playlist.get(position));
                    Toast.makeText(MainActivity.this, "Downloading on Server", Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                getPlayListFromServer(ServerUrl+"/Musics");

            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer player) {
                mediaPlayer.start();
                isPlaying = true;
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (playListPostion.hasNext())
                {
                    Music next = playListPostion.next();
                    play(ServerUrl+"/play/"+ next.getUrl());
                }

            }
        });
    }

    private void play(String src)
    {

        try {
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(src);
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getPlayListFromServer(String Url)
    {
        RequestQueue volleyQueue = Volley.newRequestQueue(MainActivity.this);
        playlist = new ArrayList<>();

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                Request.Method.GET,
                Url,
                null,
                response ->
                {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            String name = response.getJSONObject(i).get("name").toString();
                            String author = response.getJSONObject(i).get("author").toString();
                            String url = response.getJSONObject(i).get("url").toString();
                            playlist.add(new Music(name, author, url));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    MusicListAdapter musicListAdapter = new MusicListAdapter(getApplicationContext(), playlist);
                    list.setAdapter(musicListAdapter);
                },
                (Response.ErrorListener) error -> {

                    Toast.makeText(MainActivity.this, "Error Server: "  + error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    Log.e("MainActivity", "Error get playlist from "+ServerUrl+": " + error.getLocalizedMessage());
                }
        );
        volleyQueue.add(jsonObjectRequest);

        if (Url.equals(ServerUrl + "/Music"))
            onServer = new ArrayList<>(playlist);
    }

    private Music downloadMusicToServer(Music music) throws JSONException
    {
        Log.i("MainActivity","click");
        final String[] newUrl = new String[1];
        RequestQueue volleyQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ServerUrl+"/Download",
                music.toJson(),
                response -> {
                    try {
                       newUrl[0] = response.get("url").toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(MainActivity.this, "Error: Send Request Download", Toast.LENGTH_LONG).show();
                    Log.e("MainActivity", "Download: " + error.getLocalizedMessage());
                });
        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        volleyQueue.add(request);
        music.setUrl(newUrl[0]);
        return music;
    }

}