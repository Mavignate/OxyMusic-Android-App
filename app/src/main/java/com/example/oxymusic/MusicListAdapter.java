package com.example.oxymusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MusicListAdapter extends BaseAdapter {

    Context context;
    List<Music> playlist;
    LayoutInflater inflater;

    public MusicListAdapter(Context m_context, List<Music> m_playlist){
        this.context = m_context;
        playlist = m_playlist;
        inflater = LayoutInflater.from(m_context);
    }

    @Override
    public int getCount() {
        return playlist.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = inflater.inflate(R.layout.item, null);

        TextView autorLabel = (TextView) convertView.findViewById(R.id.autor);
        TextView nameLabel = (TextView) convertView.findViewById(R.id.name);

        autorLabel.setText(playlist.get(position).getAuthor());
        nameLabel.setText(playlist.get(position).getName());

        return convertView;
    }

}
