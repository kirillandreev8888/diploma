package com.example.kirill.client1;

import android.support.v7.app.AlertDialog;

import com.github.nkzawa.socketio.client.Socket;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Globals {

    //global vars
    public static Socket socket;
    public static String login;
    public static List<MainActivity.MyEvent> events;
    public static List<Notification> notifs;
    public static String clickedDay;
    public static String currentlyObservedUser;
    public static String permission;

    //podgons
    public static MainActivity.MyEvent podgon;
    public static MainActivity.MyEvent podgon2;
    public static AlertDialog readyDialog;
    public static boolean isMain;
    public static boolean isDay;

    public static List<MainActivity.MyEvent> getdaylist(){
        List<MainActivity.MyEvent> daylist = new ArrayList<>();
        for (int i=0; i<events.size(); i++){
            if (events.get(i).date.equals(clickedDay)){
                daylist.add(events.get(i));
            }
        }
        return daylist;
    }



}
