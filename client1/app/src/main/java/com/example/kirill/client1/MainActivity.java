package com.example.kirill.client1;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {



    //CompactCalendarView compactCalendarView;
    long epoch;
    TextView testView;
    AlertDialog.Builder ad;
    Context context;
    Button connectToButton;
    Button friendsListButton;

    //private SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting
        Globals.podgon = new MyEvent();
        Globals.podgon2 = new MyEvent();
        Globals.permission="FULL";
        context = this;

        final Socket socket = Globals.socket;
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(null);
        }
        final CompactCalendarView compactCalendarView = findViewById(R.id.compactcalendar_view);
        compactCalendarView.setFirstDayOfWeek(Calendar.MONDAY);
        testView = findViewById(R.id.testView);

        //UI

        String title;
        final int temp = Integer.parseInt(new SimpleDateFormat("M").format(new Date()));
        String[] monthNames = { "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь" };
        title = monthNames[temp-1];
        title=title+" "+new SimpleDateFormat("yyyy").format(new Date());
        actionBar.setTitle(title);

        if (!Globals.currentlyObservedUser.equals(Globals.login)){
            testView.setText("Календарь пользователя "+Globals.currentlyObservedUser);
        }

        if (!Globals.currentlyObservedUser.equals(Globals.login)){
            socket.emit("req.eventlist", Globals.currentlyObservedUser);
        }else{
            socket.emit("req.eventlist", Globals.login);
        }

        socket.emit("req.notificationslist", "");

        //socket setup
        socket.on("ans.eventlist", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String data = (String) args[0];
                Type itemsListType = new TypeToken<List<MyEvent>>() {}.getType();
                Globals.events = new Gson().fromJson(data, itemsListType);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        compactCalendarView.removeAllEvents();
                        for (int i=0; i<Globals.events.size(); i++){
                            try {
                                epoch = new java.text.SimpleDateFormat("dd/MM/yyyy").parse(Globals.events.get(i).date).getTime();
                                Event ev = new Event(Color.BLUE, epoch);
                                compactCalendarView.addEvent(ev);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });

        socket.on("test", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("LIFE", "I'M WORKING");

            }
        });

        socket.on("upd.permission", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String data = (String) args[0];
                //Log.i("LIFE", "ПРАВА ОБНОВЛЕНЫ");
                final Friend tempPermission = new Gson().fromJson(data, Friend.class);
                if (Globals.currentlyObservedUser.equals(tempPermission.name)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Globals.permission = tempPermission.permission;
                            String tempStr = Globals.permission;
                            switch (Globals.permission){
                                case "READ":
                                    tempStr = "Только просмотр";
                                    break;
                                case "ADD":
                                    tempStr = "Просмотр и добавление";
                                    break;
                                case "FULL":
                                    tempStr = "Полный доступ";
                                    break;
                            }
                            testView.setText("Календарь "+Globals.currentlyObservedUser+" - "+tempStr);
                        }
                    });
                }
            }
        });

        socket.on("upd.permissiondrop", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String data = (String) args[0];
                if (Globals.currentlyObservedUser.equals(data)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectToButton.performClick();
                        }
                    });
                }
            }
        });





        //calendar listeners
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                Context context = getApplicationContext();
                //Toast.makeText(context, Globals.clickedDay, Toast.LENGTH_SHORT).show();
                Globals.clickedDay = new java.text.SimpleDateFormat("dd/MM/yyyy").format(dateClicked);
                Intent intent = new Intent(MainActivity.this, DayActivity.class);
                startActivity(intent);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                if (actionBar != null) {
                    //actionBar.setTitle(dateFormatMonth.format(firstDayOfNewMonth));
                    String title;
                    int temp = Integer.parseInt(new SimpleDateFormat("M").format(firstDayOfNewMonth));
                    String[] monthNames = { "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь" };
                    title = monthNames[temp-1];
                    title=title+" "+new SimpleDateFormat("yyyy").format(firstDayOfNewMonth);
                    actionBar.setTitle(title);
                }

            }
        });

        //dialog settings

        socket.on("ans.friendslist", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() { //TODO: тут могут быть ошибки, потестить
                        String data = (String) args[0]; //TODO: добавить проверку на отсутствие пользователей

                        Log.i("LIFE", data);

                        if (data.equals("[]")){
                            Toast toast = Toast.makeText(context, "У вас нет прав для подключения к какому-либо пользователю", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }else{

                            View connectDialogView = LayoutInflater.from(context).inflate(R.layout.connect_dialog_layout, null);
                            AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
                            mDialogBuilder.setView(connectDialogView);

                            RecyclerView recyclerView = (RecyclerView) connectDialogView.findViewById(R.id.dialogRecycler);
                            recyclerView.setLayoutManager(new LinearLayoutManager(context));
                            recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
                            Type friendsListType = new TypeToken<List<FriendWithStatus>>() {}.getType();
                            List<FriendWithStatus> friendsList = new Gson().fromJson(data, friendsListType);
                            recyclerView.setAdapter(new FriendsDialogAdapter(context, friendsList, testView, connectToButton));

                            mDialogBuilder.setCancelable(true);
                            mDialogBuilder.setTitle("Подключение к пользователю:");
                            Globals.readyDialog = mDialogBuilder.create();

                            if(!((MainActivity) context).isFinishing())
                            {
                                Globals.readyDialog.show();
                            }
                        }
                    }
                });
            }
        });

        //button listeners

        connectToButton = findViewById(R.id.connectToButton);
        friendsListButton = findViewById(R.id.friendsListButton);

        final Button goNotificationsButton = findViewById(R.id.goNotificationsButton);

        connectToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Globals.currentlyObservedUser.equals(Globals.login)){
                    socket.emit("req.changelogin", Globals.login);
                    Globals.permission = "FULL";
                    Globals.currentlyObservedUser = Globals.login;
                    testView.setText("Ваш календарь");
                    connectToButton.setText("Подключитсья к ...");
                    socket.emit("req.eventlist", Globals.currentlyObservedUser);
                }else{
                    socket.emit("req.friendslist", "");
                }
            }
        });

        friendsListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
                startActivity(intent);
            }
        });

        goNotificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                goNotificationsButton.setText("НЕТ НОВЫХ УВЕДОМЛЕНИЙ");

                try{
                    int t = Globals.notifs.size();
                    Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                    startActivity(intent);
                }catch (Exception e){
                    Toast.makeText(context, "У вас нет новых уведомлений", Toast.LENGTH_SHORT).show();
                }
            }
        });
        socket.on("ans.notificationslist", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                final String data = (String) args[0];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Type itemsListType = new TypeToken<List<Notification>>() {}.getType();
                        Globals.notifs = new Gson().fromJson(data, itemsListType);

                        goNotificationsButton.setText(Globals.notifs.size()+" НОВЫХ УВЕДОМЛЕНИЯ");

                    }
                });
            }
        });





    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i("LIFE", "RESUMED");
        Globals.isMain = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("LIFE", "PAUSED");
        Globals.isMain = false;
    }

    //custom event class
    public class MyEvent{
        public String date;
        public String nickname;
        public String author;
        public String eventname;
        public String eventdesc;

        public MyEvent(){

        }

        public MyEvent(String date, String nickname, String author, String eventname, String eventdesc) {
            this.date = date;
            this.nickname = nickname;
            this.author = author;
            this.eventname = eventname;
            this.eventdesc = eventdesc;
        }

        @Override
        public String toString() {
            return eventname+" : "+date;
        }
    }


}
