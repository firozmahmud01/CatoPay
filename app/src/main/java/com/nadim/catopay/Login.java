package com.nadim.catopay;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Login extends AppCompatActivity {

    ProgressDialog pd;
    Thread th;
    Handler hand;
    ApiCaller ac;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ac=new ApiCaller(Login.this);
        hand=new Handler();
        pd=new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);


        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText email,pass;
                email=findViewById(R.id.login_email_edittext);
                pass=findViewById(R.id.login_password_edittext);
                if(email.getText().toString().isEmpty()){
                    email.setError("Fill it to login");
                    return ;
                }
                if(pass.getText().toString().isEmpty()){
                    pass.setError("Fill it to login");
                    return;
                }
                String e=email.getText().toString(),p=pass.getText().toString();
                pd.show();
                th=new Thread(){
                    @Override
                    public void run() {
                        try{
                            ac.dologin(e,p);
                            startActivity(new Intent(Login.this,MainActivity.class));
                            finish();
                        }catch (Exception e){
                            hand.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }finally {
                            pd.dismiss();
                        }
                    }
                };

                th.start();


            }
        });


    }
}