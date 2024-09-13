package com.nadim.catopay;

import static java.util.Collections.singletonMap;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.Telephony;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Orders extends Fragment {
    public class smsitem{
        private String address,body;

        public smsitem(String address, String body) {
            this.address = address;
            this.body = body;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }


    public ArrayList<smsitem> readSms() throws Exception {
        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor cursor = contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                null,
                null,
                null,
                null);



        ArrayList<smsitem> res=new ArrayList<>();

//int a=0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
//            a++;
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE));
                    if (type.equals("2")) continue;
                    String address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    if (!address.equalsIgnoreCase("NAGAD") && !address.equalsIgnoreCase("bkash") && !address.equalsIgnoreCase("Rocket")) continue;
                    String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    res.add(new smsitem(address, body));
                }catch (Exception e){}
            } while (cursor.moveToNext());
        }
        try {

            if (cursor != null) {
                cursor.close();
            }
        }catch (Exception e){}
//        Toast.make(getContext(), ""+a, Toast.LENGTH_SHORT).show();
        return res;
    }











    ListView lv;
    BaseAdapter ba;
    ArrayList<smsitem> list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vi= inflater.inflate(R.layout.fragment_orders, container, false);
        list=new ArrayList<>();
        try {
            list=readSms();
        } catch (Exception e) {

        }
        ba=new BaseAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if(view==null){
                    view=getLayoutInflater().inflate(R.layout.templist,null);
                }
                TextView add,body;
                add=view.findViewById(R.id.tmplist_address);
                body=view.findViewById(R.id.templist_body);
                add.setText(list.get(i).getAddress());
                body.setText(list.get(i).getBody());
                return view;
            }
        };

        lv=vi.findViewById(R.id.orderlistview);
        lv.setAdapter(ba);


        return vi;
    }
}