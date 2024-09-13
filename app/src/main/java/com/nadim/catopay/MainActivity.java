package com.nadim.catopay;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
Handler hand;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        hand=new Handler();

        FirebaseAuth fa=FirebaseAuth.getInstance();
        fa.signInWithEmailAndPassword("admin@email.com","passpass").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful())return;
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.READ_SMS,Manifest.permission.POST_NOTIFICATIONS}, 111);
                } else {
                    dosomething();
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 111) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dosomething();

            }
        }
    }
boolean bo=false;
    ApiCaller ac;
    public void dosomething(){
        if(bo)return;
        bo=true;

        SharedPreferences sp=getSharedPreferences(ApiCaller.database,MODE_PRIVATE);
        String email=sp.getString("email",null);

        if(sp.getBoolean("active",true)&&email!=null) {
            startService(new Intent(MainActivity.this, ActiveStatus.class));
        }

        ac=new ApiCaller(MainActivity.this);

        Thread th=new Thread(){
            @Override
            public void run() {
                try {
                    sleep(1000);
                }catch (Exception e){}
                if(email==null){
                    //            need to login
                    startActivity(new Intent(MainActivity.this,Login.class));
                }else{
                    try {
                        ac.loadTransaction();
                    } catch (Exception e) {
                        int a=1;
                    }
                    startActivity(new Intent(MainActivity.this, Home.class));
                }
                finish();

            }
        };
        th.start();
    }
}