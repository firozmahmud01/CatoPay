package com.nadim.catopay;

import android.app.Notification;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Telephony;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSSender extends Service {
    public static boolean isrunning=false;


    public class Smsitem{
        private String txnId, cashOutNumber;
        private float amount;
        private String provider;

        public Smsitem(String txnId, String cashOutNumber, float amount, String provider) {
            this.txnId = txnId;
            this.cashOutNumber = cashOutNumber;
            this.amount = amount;
            if(provider.equalsIgnoreCase("bkash")){
                this.provider="bkash".toLowerCase();
            }else{
                this.provider = "others";

            }
        }

        public String getTxnId() {
            return txnId;
        }

        public void setTxnId(String txnId) {
            this.txnId = txnId;
        }

        public String getCashOutNumber() {
            return cashOutNumber;
        }

        public void setCashOutNumber(String cashOutNumber) {
            this.cashOutNumber = cashOutNumber;
        }

        public float getAmount() {
            return amount;
        }

        public void setAmount(float amount) {
            this.amount = amount;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            if(provider.equalsIgnoreCase("bkash")){
                this.provider="bkash".toLowerCase();
            }else{
                this.provider = "others";

            }
        }
    }

    private int indexfinder(String data,String match){
        String ndata=data.toLowerCase();
        String key=match.toLowerCase();
        return ndata.indexOf(key);
    }
    private Matcher regexfind(String body,String expression){
        Pattern pattern=Pattern.compile(expression,Pattern.CASE_INSENSITIVE);
        return pattern.matcher(body);

    }


    public Smsitem smsFilter(String address,String body,String time){
        SharedPreferences sp=getSharedPreferences(ApiCaller.database,MODE_PRIVATE);
        Matcher mmm=regexfind(body,"Cash.Out");
        if(!mmm.find())return null;
        Matcher txidm=regexfind(body,"T[r]*x[n]*ID[:]* [A-Za-z0-9]+");
        if(!txidm.find())return null;
        String trxid= txidm.group();


        trxid=trxid.split(" ")[1];

        if(sp.getBoolean(trxid,false))return null;

        if(address.equalsIgnoreCase("NAGAD")){
//            Amount: Tk 500.00
            Matcher amount=regexfind(body,"Amount[:] Tk [0-9,.]+");
            if(!amount.find())return null;
            Matcher moneyb=regexfind(amount.group(),"[0-9.,]+");
            moneyb.find();
            String money=moneyb.group().replaceAll(",","");
//            txnId
//            cashOutNumber

            Matcher accountnum=regexfind(body,"Customer: [+8]*01[0-9]{9}");
            if(!accountnum.find())return null;
            String number=accountnum.group().replace("Customer: ","");
            return new Smsitem(trxid,number,Float.parseFloat(money),address);

        }else if (address.equalsIgnoreCase("bkash")){
            Matcher mon=regexfind(body,"Cash Out Tk [0-9,.]+");
            if(!mon.find())return null;
            mon=regexfind(mon.group(),"[0-9,.]+");
            mon.find();
            float amount=Float.parseFloat(mon.group().replaceAll(",",""));
            Matcher num=regexfind(body,"from [+8]*01[0-9A-Za-z]{9}");
            if(!num.find())return null;
            String number=num.group().replace("from ","");

            return new Smsitem(trxid,number,amount,address);


        }else if (address.equalsIgnoreCase("Rocket")){
            Matcher num=regexfind(body,"from A[/]C: [+8]*01[0-9]{10}");
            if(!num.find())return null;
            String number=num.group().replace("from A/C: ","");
            Matcher am=regexfind(body,"from A[/]C: [+8]*01[0-9]{10} Tk[0-9,.]+");
            if(!am.find())return null;
            am=regexfind(am.group(),"Tk[0-9,.]+");
            am.find();
            float amount=Float.parseFloat(am.group().replace("Tk","").replaceAll(",",""));

            return new Smsitem(trxid,number,amount,address);
        }
        return null;
    }



    public ArrayList<Smsitem> readSms() throws Exception {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                null,
                null,
                null,
                null);

        ArrayList<Smsitem> list=new ArrayList<>();



        if (cursor != null && cursor.moveToFirst()) {

            do {
                try {
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE));
                    if (type.equals("2")) continue;
                    String address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    if (!address.equalsIgnoreCase("NAGAD") && !address.equalsIgnoreCase("bkash") && !address.equalsIgnoreCase("Rocket"))
                        continue;

                    String date = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));


                    String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    Smsitem si = null;
                    try {
                        si = smsFilter(address, body, date);
                    } catch (Exception e) {
                    }

                    if (si != null) {
                        list.add(si);
                    }
                }catch (Exception e){}
            } while (cursor.moveToNext());
        }
        try {

            if (cursor != null) {
                cursor.close();
            }
        }catch (Exception e){
            
        }
        return list;
    }



    private void startForeground() {

        try {
            Notification notification = (new NotificationCompat.Builder(this,"running_channel"))
                    .setSmallIcon(R.drawable.logo)
                    .setContentText("Collecting sms")
                    .setContentTitle("SMS Monitor")
                    .build();



            startForeground(1,notification);
        } catch (Exception e) {
            int a=10;
        }
        doall();

    }





    ApiCaller ac;
    Thread th=null;
    Handler hand;
    private void doall(){
        hand=new Handler();
        ac=new ApiCaller(getApplicationContext());
        if(th==null) {
            th = new Thread() {
                @Override
                public void run() {
                    while(true){
                        try{
                            sleep(1000);
                        }catch (Exception e){

                        }
                        try {
                            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                            boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
                            if(!connected){
                                continue;
                            }
                        }catch (Exception e){}
                        try {
                            ArrayList<Smsitem> all = readSms();

                            for(Smsitem si:all){
                                try {



                                    ac.uploadSMS(si.getTxnId(),si.getCashOutNumber() , si.getAmount(),si.getProvider());
                                    SharedPreferences.Editor spe=getSharedPreferences(ApiCaller.database,MODE_PRIVATE).edit();
                                    spe.putBoolean(si.getTxnId(),true);
                                    spe.apply();
                                }catch (Exception e){

                                    if(e.getMessage().equals("400")){
                                        SharedPreferences.Editor spe=getSharedPreferences(ApiCaller.database,MODE_PRIVATE).edit();
                                        spe.putBoolean(si.getTxnId(),true);
                                        spe.apply();
                                    }
                                }
                            }
                        }catch (Exception e){
                            int a=10;
                        }



                    }




                }
            };
            th.start();
        }


    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            startForeground();
        }catch (Exception e){
            int b=10;
        }
        isrunning=true;



        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        isrunning=false;
        try{
            if(th!=null) {
                th.destroy();
                th.stop();
            }
        }catch (Exception e){
            try {
                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }catch (Exception ee){}
        }
        th=null;
        super.onDestroy();
    }
}