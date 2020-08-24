package com.advikonsignagekeyboardplayer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.advikonsignagekeyboardplayer.models.Songs;
import com.advikonsignagekeyboardplayer.R;

import java.util.ArrayList;

/**
 * Created by ParasMobile on 6/17/2016.
 */
public class SongAdapter extends BaseAdapter {
    ArrayList<Songs> SongsModal;
    Context context;
    Typeface font;
    Typeface fontBold;


    public SongAdapter(Context context, ArrayList<Songs> SongsModal) {
        this.context = context;
        this.SongsModal = SongsModal;
        fontBold = Typeface.createFromAsset(this.context.getAssets(),this.context.getString(R.string.century_font_bold));
        font = Typeface.createFromAsset(this.context.getAssets(),this.context.getString(R.string.century_font));

    }

    @Override
    public int getCount() {
        return SongsModal.size();
    }

    @Override
    public Object getItem(int position) {
        return SongsModal.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    public class ViewHolder {

        TextView textview1,textview2;
        RelativeLayout rlBackground;

    }

   /* @Override
    public boolean isEnabled(int position) {
        return false;
    }*/




    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            // get layout from list_item.xml ( Defined Below )
            convertView = inflater.inflate(R.layout.song_list_item, null);

            holder = new ViewHolder();
            holder.textview1 = (TextView) convertView.findViewById(R.id.songtitle);
            holder.textview2 = (TextView) convertView.findViewById(R.id.songArtist);
            holder.rlBackground = (RelativeLayout) convertView.findViewById(R.id.background);

            holder.textview1.setTypeface(fontBold);
            holder.textview2.setTypeface(font);

            holder.textview1.setTextColor(Color.WHITE);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Songs modal = (Songs)getItem(position);

        holder.textview1.setText(modal.getTitle());
        holder.textview2.setText(modal.getAr_Name());

        holder.rlBackground.setBackgroundColor(Color.TRANSPARENT);

        return convertView;
    }
}