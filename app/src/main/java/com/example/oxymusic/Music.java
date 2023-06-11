package com.example.oxymusic;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class Music {

    private Long id;
    private String name;
    private String author;
    private String url;

    public Music(String name, String author, String url)
    {
        this.name = name;
        this.author = author;
        this.url = url;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public String getName() {
        return name;
    }

    public String getUrl()
    {
        return url;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();

        object.put("name",this.name);
        object.put("author",this.author);
        object.put("url", this.url);

        return object;
    }

}
