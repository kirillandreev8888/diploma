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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<MainActivity.MyEvent> eventList;
    Socket socket = Globals.socket;

    //data "input"
    public RecyclerViewAdapter(Context context, List<MainActivity.MyEvent> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.events_recycler_layout, viewGroup, false);
        return new Item(row);
    }

    @Override

    //логика работы элементов RecyclerView
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
        ((Item)viewHolder).event_name.setText(eventList.get(i).eventname);
        ((Item)viewHolder).event_desc.setText(eventList.get(i).eventdesc);
        ((Item)viewHolder).author_name.setText(eventList.get(i).author);
        ((Item)viewHolder).colorLayout.setBackgroundColor(Color.parseColor(getColor(eventList.get(i).author)));
        //удаление записи по долгому нажатию
        ((Item)viewHolder).singleEventLayout.setOnLongClickListener(new View.OnLongClickListener() { //ДОБАВИТЬ подтверждение удаления
            @Override
            public boolean onLongClick(View v) {
                if (Globals.permission.equals("READ")||(Globals.permission.equals("ADD")&&(!((Item)viewHolder).author_name.getText().toString().equals(Globals.login)))){
                    Toast.makeText(context, "У вас нет прав изменять чужие события", Toast.LENGTH_SHORT).show();
                }else{

                    Globals.podgon.date = Globals.clickedDay;
                    Globals.podgon.nickname = Globals.currentlyObservedUser;
                    Globals.podgon.author = ((Item)viewHolder).author_name.getText().toString() ;
                    Globals.podgon.eventname = ((Item)viewHolder).event_name.getText().toString();
                    Globals.podgon.eventdesc = ((Item)viewHolder).event_desc.getText().toString();

                    String jsonToSend = new Gson().toJson(Globals.podgon);
                    socket.emit("req.delevent", jsonToSend);
                }
                return false;
            }
        });

        //Edit dialog setting
        View editEventDialogView = LayoutInflater.from(context).inflate(R.layout.add_event_dialog_layout, null);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
        mDialogBuilder.setView(editEventDialogView);

        final EditText editEventEditText = (EditText) editEventDialogView.findViewById(R.id.addEventEditText);
        final EditText editEventDescEditText = (EditText) editEventDialogView.findViewById(R.id.addEventDescEditText);
        Button editEventSubmitButton = (Button) editEventDialogView.findViewById(R.id.addEventSubmitButton);
        editEventSubmitButton.setText("Редактировать");

        mDialogBuilder.setCancelable(true);
        mDialogBuilder.setTitle("Редактировать событие");

        final AlertDialog alertDialog = mDialogBuilder.create();

        editEventSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals.podgon.date = Globals.clickedDay;
                Globals.podgon.nickname = Globals.currentlyObservedUser;
                Globals.podgon.author = ((Item)viewHolder).author_name.getText().toString() ;
                Globals.podgon.eventname = ((Item)viewHolder).event_name.getText().toString();
                Globals.podgon.eventdesc = ((Item)viewHolder).event_desc.getText().toString();

                Globals.podgon2.date = Globals.podgon.date;
                Globals.podgon2.nickname = Globals.podgon.nickname;
                Globals.podgon2.author = Globals.podgon.author;
                Globals.podgon2.eventname = editEventEditText.getText().toString();
                Globals.podgon2.eventdesc = editEventDescEditText.getText().toString();

                List<MainActivity.MyEvent> editList = new ArrayList<>();
                editList.add(Globals.podgon);
                editList.add(Globals.podgon2);

                String listToSend = new Gson().toJson(editList);
                socket.emit("req.editevent", listToSend);
                alertDialog.cancel();
            }
        });

        //редактирование по нажатию
        ((Item)viewHolder).singleEventLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Globals.permission.equals("READ")||(Globals.permission.equals("ADD")&&(!((Item)viewHolder).author_name.getText().toString().equals(Globals.login)))){
                    Toast.makeText(context, "У вас нет прав изменять чужие события", Toast.LENGTH_SHORT).show();
                }else{
                    editEventEditText.setText(((Item)viewHolder).event_name.getText().toString());
                    editEventDescEditText.setText(((Item)viewHolder).event_desc.getText().toString());
                    alertDialog.show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    //layout assignment
    public class Item extends RecyclerView.ViewHolder{
        TextView event_name;
        TextView event_desc;
        TextView author_name;
        LinearLayout singleEventLayout;
        LinearLayout colorLayout;

        public Item(@NonNull View itemView) {
            super(itemView);
            event_name = (TextView)itemView.findViewById(R.id.event_name);
            event_desc = (TextView)itemView.findViewById(R.id.event_desc);
            author_name = (TextView)itemView.findViewById(R.id.author_name);
            colorLayout = (LinearLayout)itemView.findViewById(R.id.colorLayout);
            singleEventLayout = (LinearLayout) itemView.findViewById(R.id.singleEventLayout);
        }
    }

    public String getColor(String login){
        if (login.equals(""))
            return "#000000";
        char[] mas = login.toCharArray();
        int sum=0;

        for (char el:mas) {
            sum+=(int)el;
        }
        String s1 = Integer.toHexString(sum % 256);
        if (s1.length()==1){
            s1+='0';
        }
        String s2 = Integer.toHexString(sum*(int)mas[0] % 256);
        if (s2.length()==1){
            s2+='0';
        }
        String s3 = Integer.toHexString(sum*(int)mas[mas.length-1] % 256);
        if (s3.length()==1){
            s3+='0';
        }
        return '#'+s1+s2+s3;
    }
}
