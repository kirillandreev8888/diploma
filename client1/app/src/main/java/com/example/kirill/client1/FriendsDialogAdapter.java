package com.example.kirill.client1;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.Socket;

import java.util.List;

public class FriendsDialogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<FriendWithStatus> friendList;
    Socket socket = Globals.socket;
    AlertDialog.Builder ad;
    TextView textView;
    Button button;
    String tempStr;

    String temp;

    public FriendsDialogAdapter(Context context, List<FriendWithStatus> friendList, TextView textView, Button button){
        this.context = context;
        this.friendList = friendList;
        this.textView = textView;
        this.button = button;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.connect_recycler_layout, viewGroup, false);
        return new Item(row);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
        ((Item)viewHolder).friend_name.setText(friendList.get(i).name);
        if (friendList.get(i).isOnline){
            ((Item)viewHolder).friend_name.setTextColor(Color.GREEN);
        }else{
            ((Item)viewHolder).friend_name.setTextColor(Color.RED);
        }
        tempStr = friendList.get(i).permission;
        switch (friendList.get(i).permission){
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
        ((Item)viewHolder).friend_permission.setText(tempStr);

        //настройка диалога отзыва прав доступа

        ((Item)viewHolder).singleFriendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socket.emit("req.changelogin", friendList.get(i).name);
                Globals.permission = friendList.get(i).permission;
                Globals.currentlyObservedUser = friendList.get(i).name;
                textView.setText("Календарь "+Globals.currentlyObservedUser+" - "+tempStr);
                button.setText("Вернуться к себе");
                socket.emit("req.eventlist", Globals.currentlyObservedUser);
                Globals.readyDialog.cancel();
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public class Item extends RecyclerView.ViewHolder{
        TextView friend_name;
        TextView friend_permission;
        LinearLayout singleFriendLayout;

        public Item(@NonNull View itemView) {
            super(itemView);
            friend_name = (TextView) itemView.findViewById(R.id.connectToFriend_name);
            friend_permission = (TextView) itemView.findViewById(R.id.connectToFriend_permission);
            singleFriendLayout = (LinearLayout) itemView.findViewById(R.id.connectToSigleFriendLayout);

        }
    }
}
