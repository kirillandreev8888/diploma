package com.example.kirill.client1;

import android.content.Context;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    public RecyclerView recyclerView;
    public FloatingActionButton fab;
    Socket socket = Globals.socket;
    AlertDialog alertDialog;
    EditText addFriendEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        //костыли

        final Context context = this;

        //setting

        fab = (FloatingActionButton) findViewById(R.id.friendsFloatingButton);


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_friends);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle("Допущенные пользователи");
        }


        //UI

        socket.emit("req.allowedlist", "");

        //socket setup

        socket.on("upd.allowedlist", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        if (data.equals("SELF")){
                            Toast.makeText(context, "Нельзя добавить себя", Toast.LENGTH_SHORT).show();
                            addFriendEditText.setText("");
                        }else
                        {
                            if (data.equals("NOTFOUND")){
                                Toast.makeText(context, "Пользователь не найден", Toast.LENGTH_SHORT).show();
                            }else {
                                if (data.equals("ALREADY")){
                                    Toast.makeText(context, "Уже есть в списке", Toast.LENGTH_SHORT).show();
                                }else {
                                    Type itemsListType = new TypeToken<List<Friend>>() {}.getType();
                                    List<Friend> allowedList = new Gson().fromJson(data, itemsListType);
                                    recyclerView.setAdapter(new FriendsViewAdapter(context, allowedList));
                                    alertDialog.cancel();
                                }
                            }
                        }
                    }
                });
            }
        });

        //dialog settings

        final View addFriendDialogView = LayoutInflater.from(context).inflate(R.layout.add_friend_dialog_layout, null);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
        mDialogBuilder.setView(addFriendDialogView);
        mDialogBuilder.setTitle("Добавление пользователя в список допущенных");

        addFriendEditText = (EditText) addFriendDialogView.findViewById(R.id.addFriendEditText);
        Button addFriendSubmitButton = (Button) addFriendDialogView.findViewById(R.id.addFriendSubmitButton);
        final RadioGroup addFriendRadioGroup = (RadioGroup) addFriendDialogView.findViewById(R.id.addFriendRadioGroup);

        mDialogBuilder.setCancelable(true);

        alertDialog = mDialogBuilder.create();

        //dialog logic
        addFriendSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int temp = addFriendRadioGroup.getCheckedRadioButtonId();
                if (temp==-1){
                    Toast.makeText(context, "Выберите права доступа", Toast.LENGTH_SHORT).show();
                }else {
                    RadioButton tempButton = addFriendDialogView.findViewById(temp);
                    Log.i("LIFE", Integer.toString(temp));
                    String friendPermisson = "";
                    switch (tempButton.getText().toString()){
                        case "Только просмотр":
                            friendPermisson = "READ";
                            break;
                        case "Просмотр и добавление":
                            friendPermisson = "ADD";
                            break;
                        case "Полный доступ":
                            friendPermisson = "FULL";
                            break;
                    }

                    String toSend = new Gson().toJson(new Friend(addFriendEditText.getText().toString(), friendPermisson));
                    socket.emit("req.addallowed", toSend);
                }
            }
        });




        //data work



        //UI setting

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriendEditText.setText("");
                alertDialog.show();
            }
        });

    }
}
