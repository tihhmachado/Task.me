package com.ceunsp.app.projeto.Calendar.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.ceunsp.app.projeto.Calendar.Model.EventData;
import com.ceunsp.app.projeto.R;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends Fragment {

    private static final String TAG = "MainActivity";
    private Calendar currentCalender = Calendar.getInstance(Locale.getDefault());
    private SimpleDateFormat dateFormatForDisplaying = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.getDefault());
    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMM - yyyy", Locale.getDefault());
    private boolean shouldShow = false;
    private CompactCalendarView compactCalendarView;
    private ActionBar toolbar;
    private Date selectedDate;
    private Long timeInMilliseconds;
    private final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ClassCalendar");
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private List<EventData> eventDataList = new ArrayList<EventData>();
    final List<String> mutableBookings = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View mainTabView = inflater.inflate(R.layout.main_tab,container,false);
        final ListView bookingsListView = mainTabView.findViewById(R.id.bookings_listview);
        final Button showPreviousMonthBut = mainTabView.findViewById(R.id.prev_button);
        final Button showNextMonthBut = mainTabView.findViewById(R.id.next_button);
        final Button slideCalendarBut = mainTabView.findViewById(R.id.slide_calendar);
        final Button showCalendarWithAnimationBut = mainTabView.findViewById(R.id.show_with_animation_calendar);
        final FloatingActionButton fab = mainTabView.findViewById(R.id.fab_calendar_activity);
        final ArrayAdapter adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mutableBookings);
        bookingsListView.setAdapter(adapter);


        compactCalendarView = mainTabView.findViewById(R.id.compactcalendar_view);
        compactCalendarView.setUseThreeLetterAbbreviation(false);
        compactCalendarView.setFirstDayOfWeek(Calendar.MONDAY);
        compactCalendarView.setIsRtl(false);
        compactCalendarView.displayOtherMonthDays(false);
        compactCalendarView.invalidate();

        logEventsByMonth(compactCalendarView);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDate != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(selectedDate);
                    timeInMilliseconds = calendar.getTimeInMillis();
                }else{
                    timeInMilliseconds = System.currentTimeMillis();
                }
                Intent intentAddEvent = new Intent(mainTabView.getContext(), EventActivity.class);
                intentAddEvent.putExtra("date", timeInMilliseconds);
                startActivity(intentAddEvent);
            }
        });


        //set initial title
        toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        toolbar.setTitle(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));

        //set title on calendar scroll
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            int index = 0;
            @Override
            public void onDayClick(Date dateClicked) {
                selectedDate = dateClicked;
                RefreshListView(dateClicked);
            }
            public void RefreshListView(Date dateClicked){
                toolbar.setTitle(dateFormatForMonth.format(dateClicked));
                List<Event> bookingsFromMap = compactCalendarView.getEvents(dateClicked);

                eventDataList.clear();
                for (int index = 0; index < bookingsFromMap.size(); index++){
                    Event event = bookingsFromMap.get(index);
                    EventData eventData = (EventData) event.getData();
                    eventDataList.add(eventData);
                }

                if (bookingsFromMap != null) {
                    Log.d(TAG, bookingsFromMap.toString());
                    mutableBookings.clear();

                    for (EventData eventData : eventDataList) {
                        mutableBookings.add((String) eventData.getTitle());
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                toolbar.setTitle(dateFormatForMonth.format(firstDayOfNewMonth));
            }
        });
        
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    eventDataList.clear();
                    compactCalendarView.removeAllEvents();
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        Long time = (Long) postSnapshot.child("Event").child("timeInMillis").getValue();
                        EventData eventData = postSnapshot.child("Event").child("data").getValue(EventData.class);
                        String title = eventData.getTitle();
                        String teste = "";

                        Event event = new Event(R.color.colorAccent, time, eventData);
                        loadEvents(event);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        bookingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intentEditEvent = new Intent(getContext(), EventActivity.class);
            }
        });

        showPreviousMonthBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compactCalendarView.scrollLeft();
            }
        });

        showNextMonthBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compactCalendarView.scrollRight();
            }
        });

        final View.OnClickListener showCalendarOnClickLis = getCalendarShowLis();
        slideCalendarBut.setOnClickListener(showCalendarOnClickLis);

        final View.OnClickListener exposeCalendarListener = getCalendarExposeLis();
        showCalendarWithAnimationBut.setOnClickListener(exposeCalendarListener);

        compactCalendarView.setAnimationListener(new CompactCalendarView.CompactCalendarAnimationListener() {
            @Override
            public void onOpened() {
            }

            @Override
            public void onClosed() {
            }
        });

        return mainTabView;
    }

    @NonNull
    private View.OnClickListener getCalendarShowLis() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!compactCalendarView.isAnimating()) {
                    if (shouldShow) {
                        compactCalendarView.showCalendar();
                    } else {
                        compactCalendarView.hideCalendar();
                    }
                    shouldShow = !shouldShow;
                }
            }
        };
    }

    @NonNull
    private View.OnClickListener getCalendarExposeLis() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!compactCalendarView.isAnimating()) {
                    if (shouldShow) {
                        compactCalendarView.showCalendarWithAnimation();
                    } else {
                        compactCalendarView.hideCalendarWithAnimation();
                    }
                    shouldShow = !shouldShow;
                }
            }
        };
    }

    private void openCalendarOnCreate(View v) {
        final RelativeLayout layout = v.findViewById(R.id.main_content);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < 16) {
                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                compactCalendarView.showCalendarWithAnimation();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setTitle(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));
        // Set to current day on resume to set calendar to latest day
        // toolbar.setTitle(dateFormatForMonth.format(new Date()));
    }

    private void loadEvents(Event event) {
        compactCalendarView.addEvent(event);
        }

    private void logEventsByMonth(CompactCalendarView compactCalendarView) {
        currentCalender.setTime(new Date());
        currentCalender.set(Calendar.DAY_OF_MONTH, 1);
        currentCalender.set(Calendar.MONTH, Calendar.AUGUST);
        List<String> dates = new ArrayList<>();
        for (Event e : compactCalendarView.getEventsForMonth(new Date())) {
            dates.add(dateFormatForDisplaying.format(e.getTimeInMillis()));
        }
        Log.d(TAG, "Events for Aug with simple date formatter: " + dates);
        Log.d(TAG, "Events for Aug month using default local and timezone: " + compactCalendarView.getEventsForMonth(currentCalender.getTime()));
    }

    private void addEvents(int month, int year) {
        currentCalender.setTime(new Date());
        currentCalender.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfMonth = currentCalender.getTime();
        for (int i = 0; i < 6; i++) {
            currentCalender.setTime(firstDayOfMonth);
            if (month > -1) {
                currentCalender.set(Calendar.MONTH, month);
            }
            if (year > -1) {
                currentCalender.set(Calendar.ERA, GregorianCalendar.AD);
                currentCalender.set(Calendar.YEAR, year);
            }
            currentCalender.add(Calendar.DATE, i);
            setToMidnight(currentCalender);
            long timeInMillis = currentCalender.getTimeInMillis();

            List<Event> events = getEvents(timeInMillis, i);

            compactCalendarView.addEvents(events);
        }
    }

    private List<Event> getEvents(long timeInMillis, int day) {
        if (day < 2) {
            return Arrays.asList(new Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + new Date(timeInMillis)));
        } else if ( day > 2 && day <= 4) {
            return Arrays.asList(
                    new Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + new Date(timeInMillis)),
                    new Event(Color.argb(255, 100, 68, 65), timeInMillis, "Event 2 at " + new Date(timeInMillis)));
        } else {
            return Arrays.asList(
                    new Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + new Date(timeInMillis) ),
                    new Event(Color.argb(255, 100, 68, 65), timeInMillis, "Event 2 at " + new Date(timeInMillis)),
                    new Event(Color.argb(255, 70, 68, 65), timeInMillis, "Event 3 at " + new Date(timeInMillis)));
        }
    }

    private void setToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

}