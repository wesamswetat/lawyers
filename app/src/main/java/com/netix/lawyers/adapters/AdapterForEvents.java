package com.netix.lawyers.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.netix.lawyers.Constants;
import com.netix.lawyers.R;
import com.netix.lawyers.moulds.EventsForCalendar;

import java.util.ArrayList;

/**
 * Created by wesamswetat on 8/1/17.
 */

public class AdapterForEvents extends ArrayAdapter<EventsForCalendar> {

    public AdapterForEvents(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }

    public AdapterForEvents(Context context, ArrayList<EventsForCalendar> calendarEventses) {
        super(context, 0, calendarEventses);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_one_line_for_adepter, parent, false);

        final EventsForCalendar event = getItem(position);

        TextView eventText = convertView.findViewById(R.id.eventText);
        eventText.setText(event.getEventSummary());
        eventText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send event to ShowFullEvent Activity
//                Intent intent = new Intent(getContext(), ShowFullEvent.class);
//                Bundle args = new Bundle();
//                args.putSerializable(Constants.EVENT_OBJECT, event);
//                intent.putExtra(Constants.EVENT_OBJECT, args);
//                getContext().startActivity(intent);
            }
        });

        return convertView;
    }

    @Nullable
    @Override
    public EventsForCalendar getItem(int position) {
        return super.getItem(position);
    }
}
