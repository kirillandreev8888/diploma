package com.example.kirill.client1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URISyntaxException;

public class LoginActivity extends AppCompatActivity {


    public String url= "http://192.168.1.68:3000";
    private Socket socket;

    Button signUpButton;
    Button signInButton;
    EditText loginText;
    EditText passwordText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        signUpButton = (Button) findViewById(R.id.signUpButton);
        signInButton = (Button) findViewById(R.id.signInButton);
        loginText = (EditText) findViewById(R.id.loginText);
        passwordText = (EditText) findViewById(R.id.passwordText);

        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle("Авторизация");
        }



        try {
            socket = IO.socket(url);
            socket.connect();
            Globals.socket = socket;
        } catch (URISyntaxException e)
        {
            e.printStackTrace();
        }

        socket.on("ans.signin", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        if (data.equals("CORRECT")){
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            //intent.putExtra("login", loginText.getText().toString());
                            Globals.login = loginText.getText().toString();
                            Globals.currentlyObservedUser = loginText.getText().toString();
                            startActivity(intent);
                        }else{
                            Toast.makeText(LoginActivity.this, data, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        socket.on("ans.signup", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        if (data.equals("SUCCESS")){
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            //intent.putExtra("login", loginText.getText().toString());
                            Globals.login = loginText.getText().toString();
                            Globals.currentlyObservedUser = loginText.getText().toString();
                            startActivity(intent);
                        }else{
                            Toast.makeText(LoginActivity.this, data, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    User user = new User(loginText.getText().toString(), passwordText.getText().toString());
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    Log.i("GSON", gson.toJson(user));
                    socket.emit("req.signup", gson.toJson(user));
                } catch (Exception e)
                {
                    e.printStackTrace();
                    //Toast.makeText(MainActivity.this,"Нет подключения к серверу",Toast.LENGTH_SHORT).show();
                }

            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    User user = new User(loginText.getText().toString(), passwordText.getText().toString());
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    Log.i("GSON", gson.toJson(user));
                    socket.emit("req.signin", gson.toJson(user));
                } catch (Exception e)
                {
                    e.printStackTrace();
                    //Toast.makeText(MainActivity.this,"Нет подключения к серверу",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }



    public class User {
        public String login;
        public String password;

        public User(String login, String password){
            this.login = login;
            this.password = password;
        }
    }
}
