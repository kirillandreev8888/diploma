package com.example.kirill.client1;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class DayActivity extends AppCompatActivity {

    public RecyclerView recyclerView;
    public FloatingActionButton floatingActionButton;
    Socket socket = Globals.socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        //костыли
        final Context context = this;



        //setting
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingbutton);


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            try {
                actionBar.setTitle(firstUpperCase(new SimpleDateFormat("EEEE, d MMMM yyyy").format(new SimpleDateFormat("dd/MM/yyyy").parse(Globals.clickedDay))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        //socket setup

        socket.on("upd.eventlist", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        Type itemsListType = new TypeToken<List<MainActivity.MyEvent>>() {}.getType();
                        Globals.events = new Gson().fromJson(data, itemsListType);
                        recyclerView.setAdapter(new RecyclerViewAdapter(context, Globals.getdaylist()));
                    }
                });
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
                            if (Globals.isDay){
                                startActivity(new Intent(DayActivity.this, MainActivity.class));
                                finish();
                            }
                        }
                    });
                }
            }
        });

        //dialog settings

        View addEventDialogView = LayoutInflater.from(context).inflate(R.layout.add_event_dialog_layout, null);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
        mDialogBuilder.setView(addEventDialogView);

        final EditText addEventEditText = (EditText) addEventDialogView.findViewById(R.id.addEventEditText);
        final EditText addEventDescEditText = (EditText) addEventDialogView.findViewById(R.id.addEventDescEditText);
        Button addEventSubmitButton = (Button) addEventDialogView.findViewById(R.id.addEventSubmitButton);

        mDialogBuilder.setCancelable(true);
        mDialogBuilder.setTitle("Добавить новое событие");

        final AlertDialog alertDialog = mDialogBuilder.create();

        //dialog logic
        addEventSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals.podgon.date = Globals.clickedDay;
                Globals.podgon.nickname = Globals.currentlyObservedUser;
                Globals.podgon.author = Globals.login;
                Globals.podgon.eventname = addEventEditText.getText().toString();
                Globals.podgon.eventdesc = addEventDescEditText.getText().toString();

                String jsonToSend = new Gson().toJson(Globals.podgon);
                socket.emit("req.addevent", jsonToSend);
                alertDialog.cancel();
            }
        });




        //data work

        recyclerView.setAdapter(new RecyclerViewAdapter(context, Globals.getdaylist()));

        //UI setting

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Globals.permission.equals("READ")){
                    Toast.makeText(context, "У вас нет прав на добавление событий", Toast.LENGTH_SHORT).show();
                }else{
                    addEventEditText.setText("");
                    addEventDescEditText.setText("");
                    alertDialog.show();
                }
            }
        });




    }

    public String firstUpperCase(String word){
        if(word == null || word.isEmpty()) return "";//или return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Globals.isDay = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Globals.isDay =true;
    }
}

