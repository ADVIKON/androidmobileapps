package com.advikonsignagekeyboardplayer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.advikonsignagekeyboardplayer.models.Playlist;
import com.advikonsignagekeyboardplayer.R;

import java.util.ArrayList;

/**
 * Created by ParasMobile on 6/17/2016.
 */
public class PlaylistAdapter extends BaseAdapter {
    ArrayList<Playlist> playlistModal;
    Context context;
    public int currentlyPlayingAt = 0;
    Typeface font;
    boolean isSideMenu;

    public PlaylistAdapter(Context context, ArrayList<Playlist> playlistModal, boolean isSideMenu) {
        this.context = context;
        this.playlistModal = playlistModal;
        font = Typeface.createFromAsset(this.context.getAssets(),this.context.getString(R.string.century_font));
        this.isSideMenu = isSideMenu;
    }

    @Override
    public int getCount() {
        return playlistModal.size();
    }

    @Override
    public Object getItem(int position) {
        return playlistModal.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {

        TextView textview;
        ImageView playIcon;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            // get layout from list_item.xml ( Defined Below )
            convertView = inflater.inflate(R.layout.playlist_item, null);
            holder = new ViewHolder();
            holder.textview = (TextView) convertView.findViewById(R.id.grid_item_label);
            holder.textview.setTypeface(font);
            holder.playIcon = (ImageView) convertView.findViewById(R.id.play_icon);
            convertView.setTag(holder);
            convertView.setSelected(true);
            convertView.setPressed(true);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Playlist modal = (Playlist)getItem(position);

        if (position == currentlyPlayingAt){
            holder.textview.setTextColor(Color.WHITE);
            holder.playIcon.setImageResource(R.drawable.selected_playlist);
        } else {

            if (!this.isSideMenu){
                holder.textview.setTextColor(Color.GRAY);
                holder.playIcon.setImageResource(R.drawable.list_icon);
            } else {
                holder.textview.setTextColor(Color.WHITE);
                holder.playIcon.setImageResource(R.drawable.list_icon);
            }
        }
        holder.textview.setText(modal.getsplPlaylist_Name());

        return convertView;
    }
}