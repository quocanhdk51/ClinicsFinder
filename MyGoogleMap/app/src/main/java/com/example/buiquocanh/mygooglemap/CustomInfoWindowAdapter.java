/*Acknoledgement: https://www.youtube.com/playlist?list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt*/
package com.example.buiquocanh.mygooglemap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View window;
    private Context mContext;

    public CustomInfoWindowAdapter(Context context) {
        this.mContext = context;
        window = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    private void rendowWindowText(Marker marker, View view) {
        String title = marker.getTitle();
        TextView iwTitle = view.findViewById(R.id.title);

        if (!title.equals(""))
            iwTitle.setText(title);

        String snippet = marker.getSnippet();
        TextView iwSnippet = view.findViewById(R.id.snippet);

        if (!snippet.equals(""))
            iwSnippet.setText(snippet);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        rendowWindowText(marker, window);
        return window;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendowWindowText(marker, window);
        return window;
    }
}
