package com.netix.lawyers.CalendarDecorator;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;


public class OneDayDecorator implements DayViewDecorator {
    private CalendarDay date;
    private final Drawable highlightDrawable;
    private static final int color = Color.parseColor("#228BC34A");

    public OneDayDecorator() {
        date = CalendarDay.today();
        highlightDrawable = new ColorDrawable(color);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return date != null && day.equals(date);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new StyleSpan(Typeface.BOLD));
        view.setBackgroundDrawable(highlightDrawable);
        view.addSpan(new RelativeSizeSpan(1.4f));
    }
}
