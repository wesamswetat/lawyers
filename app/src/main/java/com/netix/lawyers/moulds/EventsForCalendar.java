package com.netix.lawyers.moulds;

import com.google.api.client.util.DateTime;

/**
 * Created by wesamswetat on 8/1/17.
 */

public class EventsForCalendar {

    private String eventSummary;
    private DateTime startDay;
    private DateTime endDay;

    public EventsForCalendar(String eventSummary, DateTime startDay, DateTime endDay) {
        this.eventSummary = eventSummary;
        this.endDay = endDay;
        this.startDay = startDay;
    }

    public String getEventSummary() {
        return eventSummary;
    }

    public DateTime getStartDay() {
        return startDay;
    }

    public DateTime getEndDay() {
        return endDay;
    }
}
