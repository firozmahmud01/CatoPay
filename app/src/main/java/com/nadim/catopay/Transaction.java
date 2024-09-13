package com.nadim.catopay;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class Transaction extends Fragment {


    int limit=-1;
    public void setLimit(int limit){
        this.limit=limit;
    }
Handler hand;
    public void setHandler(Handler hand){
        this.hand=hand;
    }
    ListView lv;
    BaseAdapter ba;
    ArrayList<ApiCaller.PaymentItem> list;
    ApiCaller ac;
Thread th;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vi= inflater.inflate(R.layout.fragment_transaction, container, false);

        SharedPreferences sp= getContext().getSharedPreferences(ApiCaller.database, Context.MODE_PRIVATE);
        ac=new ApiCaller(getContext());

        list=new ArrayList<>();

        lv=vi.findViewById(R.id.transaction_listview);


        ba=new BaseAdapter() {
            @Override
            public int getCount() {
                if(limit!=-1){
                    return list.size()<limit?list.size():limit;
                }else{
                    return list.size();
                }
            };

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
                    view=inflater.inflate(R.layout.transactionlistitem,null);
                }
                TextView amount=view.findViewById(R.id.transaction_list_item_amount_textview);
                if(list.get(i).getType().equalsIgnoreCase("DEBIT")){
                    amount.setTextColor(Color.RED);
                    amount.setText("-"+list.get(i).getAmount());
                }else{
                    amount.setTextColor(Color.BLUE);
                    amount.setText(""+list.get(i).getAmount());
                }

                TextView tranid,method,status,description;
                tranid=view.findViewById(R.id.transactionlistitem_transaction_id_textview);
                tranid.setText(list.get(i).getUid());
                method=view.findViewById(R.id.transactionlistitem_method_textview);
                method.setText(list.get(i).getMethod());
                status=view.findViewById(R.id.transactionlistitem_status_textview);
                status.setText(list.get(i).getStatus());
                description=view.findViewById(R.id.transactionlistitem_description_textview);
                description.setText(list.get(i).getDescription());



                return view;
            }
        };


        lv.setAdapter(ba);
        th=new Thread(){
            @Override
            public void run() {
                try {
                    list=ac.getTransList();
                    ba.notifyDataSetChanged();
                } catch (Exception e) {

                }
            }
        };
        th.start();




        return vi;
    }
}