package com.nadim.catopay;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import javax.xml.transform.sax.TemplatesHandler;

public class HomeScreen extends Fragment {
    ListView lv;
    BaseAdapter ba;
    ArrayList<ApiCaller.PaymentItem>list;
    int count=5;
    Thread th;
    ApiCaller ac;
    Handler hand;
    TextView name,balance,holdbalance;
    ProgressDialog pd;
    Thread onemore;
    ArrayList<ApiCaller.PaymentMethods> pm;
    Dialog d;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vi= inflater.inflate(R.layout.fragment_home_screen, container, false);
        list=new ArrayList<>();
        ac=new ApiCaller(getContext());
        hand=new Handler();
        lv=vi.findViewById(R.id.home_sceen_listview);
        ba=new BaseAdapter() {
            @Override
            public int getCount() {
                if(count>list.size()){
                    return list.size();
                }else{
                    return count;
                }
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
                Button refund=view.findViewById(R.id.transaction_list_item_refund_button);
                refund.setVisibility(View.GONE);


                TextView tranid,method,status,description,commission;
                commission=view.findViewById(R.id.transaction_list_item_commision_textview);
                commission.setText(""+list.get(i).getCommission());
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

        name=vi.findViewById(R.id.home_screen_name_textview);
        balance=vi.findViewById(R.id.home_screen_amount_textview);
        holdbalance=vi.findViewById(R.id.home_screen_hold_balance_textview);

        Thread th=new Thread(){
            @Override
            public void run() {
                try {
                    ApiCaller.ProfileItem pi=ac.getProfile();
                    hand.post(new Runnable() {
                        @Override
                        public void run() {
                            name.setText(pi.getName());
                            balance.setText(""+String.format("%.2f",pi.getBalance()));
                            holdbalance.setText("Balance on Hold : "+String.format("%.2f",pi.getHoldamount()));
                        }
                    });

                }catch (Exception e){}
            }
        };
        th.start();

        TabLayout tl=vi.findViewById(R.id.home_screen_tab_layout);
        tl.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getText().toString().equals("Recent Transactions")){
                    count=5;
                    try{
                        ba.notifyDataSetChanged();
                    }catch (Exception e){}
                }else{
                    count=list.size();
                    try{
                        ba.notifyDataSetChanged();
                    }catch (Exception e){}
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



//        TabItem ti=vi.findViewById(R.id.home_screen_recent_tab);
//        ti.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                count=5;
//                try {
//                    ba.notifyDataSetChanged();
//                }catch (Exception e){}
//            }
//        });
//
//        vi.findViewById(R.id.home_screen_all_tab).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                count=list.size();
//                try {
//                    ba.notifyDataSetChanged();
//                }catch (Exception e){}
//            }
//        });

        lv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });
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

        vi.findViewById(R.id.home_screen_withdraw_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd=new ProgressDialog(HomeScreen.this.getContext());
                pd.setCancelable(false);
                pd.setMessage("Please wait.Loading...");
                pd.show();
                onemore=new Thread(){
                    @Override
                    public void run() {
                        pm=new ArrayList<>();
                        try {
                            pm.addAll(ac.loadWithdrawList());
                        }catch (Exception e){}
                       hand.post(new Runnable() {
                           @Override
                           public void run() {
try {
    d = new Dialog(HomeScreen.this.getContext());
    d.setContentView(R.layout.withdrawpopup);
    d.setTitle("Create withdraw request");

    Spinner sp = d.findViewById(R.id.withdrawpopup_method_spinner);
    d.findViewById(R.id.withdrawpopup_submit_button).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            EditText amount,method;
            amount=view.getRootView().findViewById(R.id.withdrawpopup_amount_edittext);
            method=view.getRootView().findViewById(R.id.withdrawpopup_payment_method);
            if(amount.getText().toString().isEmpty()||method.getText().toString().isEmpty()){
                return;
            }
            Thread th=new Thread(){
                @Override
                public void run() {
                    try {


                        ac.createWithdraw(method.getText().toString(), Integer.parseInt(amount.getText().toString()));
                        hand.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(HomeScreen.this.getContext(), "Your request submitted.", Toast.LENGTH_SHORT).show();
                                d.dismiss();
                            }
                        });
                    }catch (Exception e){
                        hand.post(new Runnable() {
                            @Override
                            public void run() {
                                if(e.getMessage().equalsIgnoreCase("400")) {
                                    Toast.makeText(HomeScreen.this.getContext(), "Insufficient balance" , Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            };
            th.start();

        }
    });


    pm.add(new ApiCaller.PaymentMethods("custom", "Custom"));
    ArrayList<String> list = new ArrayList<>();

    for (ApiCaller.PaymentMethods pp : pm) {
        list.add(pp.getTitle());
    }
    pd.dismiss();
    ArrayAdapter<String> aa = new ArrayAdapter<>(HomeScreen.this.getContext(), android.R.layout.simple_spinner_item, list);
    sp.setAdapter(aa);
    sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            ApiCaller.PaymentMethods pp = pm.get(i);
            EditText method = view.getRootView().findViewById(R.id.withdrawpopup_payment_method);
            if (pp.getId().equalsIgnoreCase("custom")) {
                method.setEnabled(true);
                method.setText("");
            } else {
                method.setEnabled(false);
                method.setText(pp.getId());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }



    });
    d.show();
}catch (Exception e){
    int a=10;
}
                           }
                       });

                    }
                };
                onemore.start();


            }
        });



        return vi;
    }


}