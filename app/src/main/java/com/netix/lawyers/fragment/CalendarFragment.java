package com.netix.lawyers.fragment;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.netix.lawyers.CalendarDecorator.EventDecorator;
import com.netix.lawyers.CalendarDecorator.OneDayDecorator;
import com.netix.lawyers.Constants;
import com.netix.lawyers.R;
import com.netix.lawyers.adapters.AdapterForEvents;
import com.netix.lawyers.moulds.EventsForCalendar;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;


public class CalendarFragment extends Fragment implements OnDateSelectedListener, OnMonthChangedListener, EasyPermissions.PermissionCallbacks {

    GoogleAccountCredential mCredential;
    MaterialCalendarView calendarView;
    ListView eventsListView;
    View calendarFragmentView;
    ArrayList<EventsForCalendar> eventsFromGoogle = new ArrayList<EventsForCalendar>();
    HashMap<String, ArrayList<EventsForCalendar>> eventsPerSelectedDay = new HashMap<>();
    ArrayList<CalendarDay> dates = new ArrayList<>();

    LinearLayout  linearWithCalendar;
    RelativeLayout relativeWithProgressbar;

    public CalendarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        calendarFragmentView = inflater.inflate(R.layout.fragment_calendar, container, false);

        eventsListView = calendarFragmentView.findViewById(R.id.eventsListView);
        relativeWithProgressbar = calendarFragmentView.findViewById(R.id.relativeWithProgressbar);
        linearWithCalendar = calendarFragmentView.findViewById(R.id.linearWithCalendar);
        linearWithCalendar.setVisibility(LinearLayout.GONE);

        calendarView = calendarFragmentView.findViewById(R.id.calendarView);
        calendarView.state().edit()
                .setMinimumDate(CalendarDay.from(1980, 1, 1))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
        calendarView.setOnDateChangedListener(this);
        calendarView.setOnMonthChangedListener(this);

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getContext(), Arrays.asList(Constants.SCOPES))
                .setBackOff(new ExponentialBackOff());

        return calendarFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    // showToast("his app requires Google Play Services. Please install Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case Constants.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(Constants.PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                } else if (resultCode == Constants.REQUEST_CANCELED) {
//                    showToast("לא בוצע הרשמה לחשבון ה GMAIL");
//                    this.finish();
                    Toast.makeText(getActivity(), "wwww", Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    public void initGoogleAcuont() {
        getResultsFromApi();
    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            // showToast("No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    protected void putEventsOnCalendarView() {
        if (eventsFromGoogle == null || eventsFromGoogle.size() == 0) {
            // set text error NO EVENTS
            return;
        }

        for (EventsForCalendar eventsForCalendar : eventsFromGoogle) {
            Calendar calStart = Calendar.getInstance();
            Calendar calEnd = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String startDateCut = (eventsForCalendar.getStartDay().toString()).substring(0, 10);
            String endDateCut = "";
            if (eventsForCalendar.getEndDay() != null) {
                endDateCut = (eventsForCalendar.getEndDay().toString()).substring(0, 10);
            }

            ArrayList<EventsForCalendar> eventsList = new ArrayList<>();

            try {
                //cal.setTime(sdf.parse("Mon Mar 14 16:02:37 GMT 2017"));// all done
                calStart.setTime(sdf.parse(startDateCut));// all done
                if (eventsForCalendar.getEndDay() != null) {
                    calEnd.setTime(sdf.parse(endDateCut));// all done
                } else {
                    calEnd.setTime(sdf.parse(startDateCut));
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

            CalendarDay startDay = CalendarDay.from(calStart);

            long msDiff = calEnd.getTimeInMillis() - calStart.getTimeInMillis();
            long daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff);
            dates.add(startDay);

            if (eventsPerSelectedDay.get(startDateCut) != null) {
                eventsList = eventsPerSelectedDay.get(startDateCut);
                eventsList.add(eventsForCalendar);
                eventsPerSelectedDay.put(startDateCut, eventsList);
                // Toast.makeText(this, calendarData.get(currentData).toString() ,Toast.LENGTH_LONG).show();
            } else {
                eventsList.add(eventsForCalendar);
                eventsPerSelectedDay.put(startDateCut, eventsList);
            }

            while (daysDiff > 0) {
                eventsList = new ArrayList<>();
                calStart.add(Calendar.DATE, 1);
                CalendarDay calStart2 = CalendarDay.from(calStart);
                dates.add(calStart2);
                String currentData = sdf.format(calStart.getTime());
                if (eventsPerSelectedDay.get(currentData) == null) {
                    eventsList.add(eventsForCalendar);
                    eventsPerSelectedDay.put(currentData, eventsList);
                    // Toast.makeText(this, calendarData.get(currentData).toString() ,Toast.LENGTH_LONG).show();
                } else {
                    eventsList = eventsPerSelectedDay.get(currentData);
                    eventsList.add(eventsForCalendar);
                    eventsPerSelectedDay.put(currentData, eventsList);
                    // Toast.makeText(getActivity(), "333333333" ,Toast.LENGTH_LONG).show();
                }

                daysDiff--;
            }

        }

        calendarView.addDecorator(new EventDecorator(Color.RED, dates));
        calendarView.addDecorator(new OneDayDecorator());

        linearWithCalendar.setVisibility(LinearLayout.VISIBLE);
        relativeWithProgressbar.setVisibility(RelativeLayout.GONE);
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(getContext());
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(getContext());
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                getActivity(),
                connectionStatusCode,
                Constants.REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(Constants.REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                getContext(), Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getActivity().getPreferences(Context.MODE_PRIVATE)
                    .getString(Constants.PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        Constants.REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    Constants.REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    // formate the date
    private String getSelectedDatesString() {
        CalendarDay date = calendarView.getSelectedDate();
        if (date == null) {
            return "No Selection";
        }
        return Constants.FORMATTER.format(date.getDate());
    }

    // put events of one date on list view under the calendar
    public void setEventBySelectedDay(String date) {
        // get events by date
        ArrayList<EventsForCalendar> eventsByDate;
        eventsByDate = eventsPerSelectedDay.get(date);
        if (eventsByDate != null) {
            AdapterForEvents adepterForEvents = new AdapterForEvents(getContext(), eventsByDate);
            eventsListView.setAdapter(adepterForEvents);
        } else {
            eventsListView.setAdapter(null);
        }

    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        setEventBySelectedDay(getSelectedDatesString());
    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {

    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, ArrayList<EventsForCalendar>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();

        }

        /**
         * Background task to call Google Calendar API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected ArrayList<EventsForCalendar> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Display an error dialog showing that Google Play Services is missing
         * or out of date.
         *
         * @param connectionStatusCode code describing the presence (or lack of)
         *                             Google Play Services on this device.
         */
        void showGooglePlayServicesAvailabilityErrorDialog(
                final int connectionStatusCode) {
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            Dialog dialog = apiAvailability.getErrorDialog(
                    getActivity(),
                    connectionStatusCode,
                    Constants.REQUEST_GOOGLE_PLAY_SERVICES);
            dialog.show();
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         *
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private ArrayList<EventsForCalendar> getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            ArrayList<EventsForCalendar> eventStrings = new ArrayList<EventsForCalendar>();
            Events events = mService.events().list("primary")
                    //.setMaxResults(10)
                    //.setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();

            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                DateTime end = event.getEnd().getDateTime();
                if (start == null) {
                    // All-day events don't have start times, so just use
                    // the start date.
                    start = event.getStart().getDate();
                }
                if (end == null) {
                    // All-day events don't have end times, so just use
                    // the end date.
                    start = event.getEnd().getDate();
                }
                EventsForCalendar calendarEvent = new EventsForCalendar(event.getSummary(), start, end);
                eventStrings.add(calendarEvent);
            }
            return eventStrings;
        }


        @Override
        protected void onPreExecute() {
            // mProgress.show();
        }

        @Override
        protected void onPostExecute(ArrayList<EventsForCalendar> output) {

            // mProgress.hide();
            if (output == null || output.size() == 0) {
                // showToast("No results returned.");
            } else {

                eventsFromGoogle = output;
                // go to put events to calendar
                putEventsOnCalendarView();
            }

            // CalendarApi.this.startActivity(intent);
        }

        @Override
        protected void onCancelled() {
            // mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            Constants.REQUEST_AUTHORIZATION);
                } else {
                    //showToast("The following error occurred: " + mLastError.toString());
                }
            } else {
                //showToast("Request cancelled.");
            }

        }
    }
}
