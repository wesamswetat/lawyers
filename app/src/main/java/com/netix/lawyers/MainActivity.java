package com.netix.lawyers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.netix.lawyers.adapters.ViewPagerAdapter;
import com.netix.lawyers.fragment.CalendarFragment;
import com.netix.lawyers.fragment.PDFViewerFragment;
import com.netix.lawyers.fragment.WebViewFragment;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    PDFViewerFragment pdfViewerFragment = new PDFViewerFragment();
    CalendarFragment calendarFragment = new CalendarFragment();
    WebViewFragment webViewFragment = new WebViewFragment();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_dashboard:
                    viewPager.setCurrentItem(1);
                    calendarFragment.initGoogleAcuont();
                    return true;
                case R.id.navigation_notifications:
                    viewPager.setCurrentItem(2);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


        viewPagerAdapter.addFragments(pdfViewerFragment);
        viewPagerAdapter.addFragments(calendarFragment);
        viewPagerAdapter.addFragments(webViewFragment);

        viewPager.setAdapter(viewPagerAdapter);
        // Never allow swiping to switch between pages --- return true if return foals its enabled
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }



}
