package com.netix.lawyers;

import com.google.api.services.calendar.CalendarScopes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by wesamswetat on 8/2/17.
 */

public class Constants {

    // for Calendar Google Api
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final int REQUEST_CANCELED = 0;
    public static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};
    public static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    // save account name to SharedPreferences
    public static final String PREF_ACCOUNT_NAME = "accountName";

    // event send by intent.putExtra key
    public static final String EVENTS_FROM_API = "eventFromApi";
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 41;

    // for send event to ShowFullEvent Activity
    public static final String EVENT_OBJECT = "eventObject";

}
