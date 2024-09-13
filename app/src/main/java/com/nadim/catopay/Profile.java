package com.nadim.catopay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Date;

public class Profile extends Fragment {

    Handler hand;
    ApiCaller ac;
    TextView name,balance,holdbalance,phone,email;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vi = inflater.inflate(R.layout.fragment_profile, container, false);
        hand=new Handler();
        ac=new ApiCaller(getContext());
        vi.findViewById(R.id.profile_logout_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getContext().getSharedPreferences(ApiCaller.database, Context.MODE_PRIVATE);
                SharedPreferences.Editor she = sp.edit();
                she.putBoolean("active", true);
                if (ActiveStatus.isrunning) {
                    getContext().stopService(new Intent(getContext(), ActiveStatus.class));
                }
                she.putString("email", null);
                she.apply();
                startActivity(new Intent(getContext(), MainActivity.class));
                try {
                    getActivity().finish();
                } catch (Exception e) {
                }
            }
        });
        SharedPreferences sp = getContext().getSharedPreferences(ApiCaller.database, Context.MODE_PRIVATE);

        Date timedate = new Date(System.currentTimeMillis());
        int hhh = timedate.getHours();
        TextView dtv = vi.findViewById(R.id.profile_morning_textview);
        if (hhh >= 6 && hhh < 12) {
            dtv.setText("Good Morning");
        } else if (hhh >= 12 && hhh < 18) {
            dtv.setText("Good Afternoon");
        } else if (hhh >= 18 && hhh < 20) {
            dtv.setText("Good Evening");
        } else {
            dtv.setText("Good Night");
        }


        name=vi.findViewById(R.id.profile_name_textview);
        balance=vi.findViewById(R.id.profile_balance_textview);
        holdbalance=vi.findViewById(R.id.profile_hold_balance_textview);
        phone=vi.findViewById(R.id.profile_phone_number);
        email=vi.findViewById(R.id.profile_email_textview);
        email.setText(sp.getString("email",""));
        Switch active=vi.findViewById(R.id.profile_active_status_switch);
        active.setChecked(sp.getBoolean("active",true));
        active.setText(sp.getBoolean("active",true)?"ON":"OFF");
        active.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                active.setText(b?"ON":"OFF");
                SharedPreferences.Editor she=sp.edit();
                she.putBoolean("active",b);
                she.apply();
                if(!ActiveStatus.isrunning&&b){
                    getContext().startService(new Intent(getContext(), ActiveStatus.class));
                }else if(!b&&ActiveStatus.isrunning){
                    getContext().stopService(new Intent(getContext(),ActiveStatus.class));
                }
            }
        });


        Thread th=new Thread(){
            @Override
            public void run() {
                try {
                    ApiCaller.ProfileItem pi=ac.getProfile();
                    hand.post(new Runnable() {
                        @Override
                        public void run() {
                            name.setText(pi.getName());
                            phone.setText(pi.getPhone().isEmpty()?"N/A":pi.getPhone());
                            balance.setText(""+String.format("%.2f",pi.getBalance()));
                            holdbalance.setText("Balance on Hold : "+String.format("%.2f",pi.getHoldamount()));
                        }
                    });

                }catch (Exception e){}
            }
        };
        th.start();



        return vi;
    }
}