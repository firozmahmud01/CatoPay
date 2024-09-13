package com.nadim.catopay;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class ApiCaller {
    public static final String host="https://api.catopay.com/api/v1";
    public static final String database="AllData";
    public static final String cookie="usertoken";

    private SharedPreferences sp;
    private SharedPreferences.Editor spe;
    public ApiCaller(Context context){
        sp=context.getSharedPreferences(database,Context.MODE_PRIVATE);
        spe=sp.edit();
    }


    private String sendGET(String endpoint) throws Exception{
        URL url=new URL(host+endpoint);
        HttpsURLConnection con= (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setDoInput(true);
        con.addRequestProperty("Accept","*/*");
        if(sp.getString("token",null)!=null){
            con.addRequestProperty("Authorization","Bearer "+sp.getString("token",""));
        }
        InputStream is=con.getInputStream();

        con.connect();
        byte b[]=new byte[1024*10];
        String res="";
        int total;
        while((total=is.read(b,0,b.length))!=-1){
            res+=new String(b,0,total);
        }
        try {
            is.close();
            con.disconnect();
        }catch (Exception e){}
        return res;
    }
//    Authorization
    private String sendPOST(String endpoint,String body)throws Exception{
        URL url=new URL(host+endpoint);
        HttpsURLConnection con= (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type","application/json");
        if(sp.getString("token",null)!=null){
            con.addRequestProperty("Authorization","Bearer "+sp.getString("token",""));
        }
        con.addRequestProperty("Content-Length",""+body.length());
        con.addRequestProperty("Accept","*/*");

        OutputStream os=con.getOutputStream();
        con.connect();
        os.write(body.getBytes(StandardCharsets.UTF_8));

        int code=con.getResponseCode();

        if(code!=200&&endpoint.equals("/auth/login"))throw new Exception("Wrong username or password");
        if(code>=400&&code<500)throw new Exception("400");
        InputStream is=con.getInputStream();
        byte b[]=new byte[1024*10];
        String res="";
        int total;
        while((total=is.read(b,0,b.length))!=-1){
            res+=new String(b,0,total);
        }
        try {
            is.close();
            con.disconnect();
        }catch (Exception e){}
        return res;
    }





    public void dologin(String email,String pass) throws Exception{
        JSONObject jo=new JSONObject();
        jo.put("email",email);
        jo.put("password",pass);
        JSONObject res=new JSONObject(sendPOST("/auth/login",jo.toString()));
        if(!res.has("success")) {
            throw new Exception("Internal error.");
        }
        if(!res.has("message")) {
            throw new Exception("Internal error");
        }
        if(!res.getBoolean("success")) {
            throw new Exception(res.getString("message"));
        }
        if(!res.has("data")) {
            throw new Exception("Internal error");
        }
        res=res.getJSONObject("data");
        if(!res.has("uid")) {
            throw new Exception("Internal error");
        }
        if(!res.has("role")) {
            throw new Exception("Internal error");
        }
        if(!res.has("token")) {
            throw new Exception("Internal error");
        }
        if(!res.has("email")) {
            throw new Exception("Internal error");
        }


        spe.putString("email",res.getString("email"));
        spe.putString("uid",res.getString("uid"));
        spe.putString("role",res.getString("role"));
        spe.putString("token",res.getString("token"));

        spe.apply();



    }

    public static class PaymentItem{
        private String uid,type,description,method,status;
        private int amount;


        public PaymentItem(String uid, String type, String description, String method, String status, int amount) {
            this.uid = uid;
            this.type = type;
            this.description = description;
            this.method = method;
            this.status = status;
            this.amount = amount;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }

    public void loadTransaction() throws Exception {
        JSONObject res=new JSONObject(sendGET("/wallet/own-transaction-history"));
        JSONArray out=new JSONArray();
        if(res.has("success")){
            if(!res.getBoolean("success"))throw new Exception("Failed to fetch history");
        }else{
            throw new Exception("Failed to fetch payment history");
        }
        if(!res.has("data"))throw new Exception("Internal error");


        JSONArray ja=res.getJSONArray("data");
        for (int i=0;i<ja.length();i++){
            try {
                JSONObject jo = ja.getJSONObject(i);
                String uid = jo.getString("transactionId");
                int amount = jo.getInt("amount");
                String type = jo.getString("type");
                String description = jo.getString("description");
                JSONObject joo = jo.getJSONObject("transaction");
                String method = joo.getString("paymentMethodTitle");
                String status = joo.getString("status");

                JSONObject nobj = new JSONObject();
                nobj.put("uid", uid);
                nobj.put("amount", amount);
                nobj.put("type", type);
                nobj.put("description", description);
                nobj.put("method", method);
                nobj.put("status", status);
                out.put(nobj);
            }catch (Exception e){}





        }


        spe.putString("paymentlist",out.toString());
        spe.apply();


    }
    public ArrayList<PaymentItem> getTransList() throws Exception {
        ArrayList<PaymentItem> pia=new ArrayList<>();
        JSONArray ja=new JSONArray(sp.getString("paymentlist","[]"));



        for(int i=0;i<ja.length();i++){
            JSONObject jo=ja.getJSONObject(i);
            PaymentItem pi=new PaymentItem(jo.getString("uid"),jo.getString("type"),jo.getString("description"),jo.getString("method"),jo.getString("status"),jo.getInt("amount"));
            pia.add(pi);
        }

        return pia;
    }


    public void uploadSMS(String txid,String number,Float amount,String provider) throws Exception {
        JSONObject jo=new JSONObject();
        jo.put("txnId",txid);
        jo.put("amount",amount);
        jo.put("cashOutNumber",number);
        jo.put("provider",provider.toLowerCase());

        String something=sendPOST("/webhooks/payment/confirm",jo.toString());



    }



    public static class ProfileItem{
        private String name,phone;
        private double balance,holdamount;

        public ProfileItem(String name, String phone, double balance, double holdamount) {
            this.name = name;
            this.phone = phone;
            this.balance = balance;
            this.holdamount = holdamount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }

        public double getHoldamount() {
            return holdamount;
        }

        public void setHoldamount(double holdamount) {
            this.holdamount = holdamount;
        }
    }

    public ProfileItem getProfile() throws Exception {
        JSONObject res=new JSONObject(sendGET("/users/profile-info"));
        if(!res.has("data"))throw new Exception("Internal error!");
        res=res.getJSONObject("data");
        if(!res.has("name"))throw new Exception("Internal error!");
        String name=res.getString("name");
        if(!res.has("contactNumber"))throw new Exception("Internal error!");
        String number=res.getString("contactNumber");

        if(!res.has("wallet"))throw new Exception("Internal error!");
        res=res.getJSONObject("wallet");
        if(!res.has("balance"))throw new Exception("Internal error!");
        double balance=res.getDouble("balance");
        if(!res.has("holdAmount"))throw new Exception("Internal error!");
        double holdbalance= res.getDouble("holdAmount");
        return new ProfileItem(name,number,balance,holdbalance);

    }

    public static class PaymentMethods{
        private String id,title;

        public PaymentMethods(String id, String title) {
            this.id = id;
            this.title = title;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public ArrayList<PaymentMethods> loadWithdrawList() throws Exception {
        String list=sendGET("/users/configured-payment-methods");
        ArrayList<PaymentMethods> pm=new ArrayList<>();
        JSONObject jo=new JSONObject(list);
        if(!jo.has("data")) throw new Exception("Internal Error");
        JSONArray ja=jo.getJSONArray("data");
        for(int i=0;i<ja.length();i++) {
            JSONObject jj=ja.getJSONObject(i);
            if (!jj.has("id"))continue;
            if (!jj.has("paymentMethodProfileType"))continue;
            String id=jj.getString("id");
            String type=jj.getString("paymentMethodProfileType");
            if (!jj.has("paymentMethod"))continue;
            jj=jj.getJSONObject("paymentMethod");
            if(!jj.has("providerName"))continue;
            String provider=jj.getString("providerName");
            pm.add(new PaymentMethods(id,type+"("+provider+")"));

        }

        return pm;
    }
    public void createWithdraw(String method,int amount)throws Exception{
        JSONObject jo=new JSONObject();
        jo.put("amount",amount);
        jo.put("paymentMethodId",method);
        String data=sendPOST("/withdraw/create",jo.toString());
        

    }




}
