package com.example.kirill.client1;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import java.util.List;

public class FriendsViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<Friend> friendList;
    Socket socket = Globals.socket;
    AlertDialog.Builder ad;

    String temp;

    public FriendsViewAdapter(Context context, List<Friend> friendList){
        this.context = context;
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.friends_recycler_layout, viewGroup, false);
        return new Item(row);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
        ((Item)viewHolder).friend_name.setText(friendList.get(i).name);
        String tempStr = friendList.get(i).permission;
        int tempNum = 0;
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
        switch (friendList.get(i).permission){
            case "READ":
                tempNum = 0;
                break;
            case "ADD":
                tempNum = 1;
                break;
            case "FULL":
                tempNum = 2;
                break;
        }
        ((Item)viewHolder).friend_permission.setText(tempStr);

        //настройка диалога отзыва прав доступа
        String[] choiceItems = {"Только просмотр","Просмотр и добавление","Полный доступ"};
        final String[] choiceCodes = {"READ","ADD","FULL"};
        ad = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        ad
                .setTitle("Редактировать права доступа пользователя "+((Item)viewHolder).friend_name.getText().toString())
                //.setMessage("Вы хотите отозвать доступ у пользователя "+((Item)viewHolder).friend_name.getText().toString()+"?")
                .setSingleChoiceItems(choiceItems, tempNum, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        temp = choiceCodes[which];
                    }
                })
                .setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String toSend = new Gson().toJson(new Friend(((Item)viewHolder).friend_name.getText().toString(), temp));
                        socket.emit("req.editallowed", toSend);
                    }
                })
//                .setNeutralButton("Отмена", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                })
                .setNegativeButton("Отозвать права", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        socket.emit("req.delallowed", ((Item)viewHolder).friend_name.getText().toString());
                    }
                })
                .setCancelable(true);
        final AlertDialog readyDialog = ad.create();
        ((Item)viewHolder).singleFriendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!((Item)viewHolder).friend_name.getText().toString().equals("Нет пользователей в списке")){
                    readyDialog.show();
                }
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
            friend_name = (TextView) itemView.findViewById(R.id.friend_name);
            friend_permission = (TextView) itemView.findViewById(R.id.friend_permission);
            singleFriendLayout = (LinearLayout) itemView.findViewById(R.id.connectToSigleFriendLayout);

        }
    }
}
