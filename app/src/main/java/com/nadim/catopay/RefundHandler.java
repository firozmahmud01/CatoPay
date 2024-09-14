package com.nadim.catopay;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ThemedSpinnerAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class RefundHandler {
    private Context context;
    private Handler hand;
    private String txid;
    private double minamount;
    public RefundHandler(Context context,Handler hand,String txid,double minamount){
        this.context=context;
        this.minamount=minamount;
        this.txid=txid;
        this.hand=hand;

    }

    public void show(){
        AlertDialog ad=(new AlertDialog.Builder(this.context))
                .setMessage("Are you sure want to refund?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        showMainScreen();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();
        ad.show();

    }

    private String accounttypes="";
    private Spinner paymentmethod;
    private Dialog d;
    private ApiCaller ac;
    private ArrayList<String>accounttype;
    private HashMap<String,String>paymentmethods;
    private ArrayList<String> pmtype;
    private void showMainScreen(){
        ProgressDialog pd=new ProgressDialog(this.context);
        pd.setCancelable(false);
        pd.setMessage("Please wait,Loading...");
        pd.show();
        ac=new ApiCaller(this.context);

        this.d=new Dialog(this.context);
        d.setContentView(R.layout.refund_popup_screen);
        accounttype=new ArrayList<>();
        pmtype=new ArrayList<>();
//        [
//        {
//            label: "Personal Account",
//                    value: "PERSONAL_ACCOUNT",
//        },
//        {
//            label: "Business Account",
//                    value: "BUSINESS_ACCOUNT",
//        },
//        {
//            label: "Agent Account",
//                    value: "AGENT_ACCOUNT",
//        },
//        {
//            label: "Merchant Account",
//                    value: "MERCHANT_ACCOUNT",
//        },
//];
        pmtype.add("PERSONAL_ACCOUNT");
        pmtype.add("BUSINESS_ACCOUNT");
        pmtype.add("AGENT_ACCOUNT");
        pmtype.add("MERCHANT_ACCOUNT");
        accounttype.add("Personal Account");
        accounttype.add("Business Account");
        accounttype.add("Agent Account");
        accounttype.add("Merchant Account");
        Spinner accountt=d.findViewById(R.id.refund_popup_account_type_spinner);
        accountt.setAdapter(new ArrayAdapter<String>(this.context, android.R.layout.simple_spinner_item,accounttype));
        d.findViewById(R.id.refund_popup_refund_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText amount,refundreason,banknumber;
                Spinner accounttype,paymentmethod;
                amount=view.getRootView().findViewById(R.id.refund_pop_up_amount_edittext);
                refundreason=view.getRootView().findViewById(R.id.refund_popup_refund_reason);
                banknumber=view.getRootView().findViewById(R.id.refund_popup_screen_bank_edittext);
                if(amount.getText().toString().isEmpty()||refundreason.getText().toString().isEmpty()||banknumber.getText().toString().isEmpty())return;
                double camount=Double.parseDouble(amount.getText().toString());
                if(camount<=0||camount>minamount){
                    amount.setError("Insufficient balance!");
                    return ;
                }
                accounttype=view.getRootView().findViewById(R.id.refund_popup_account_type_spinner);
                paymentmethod=view.getRootView().findViewById(R.id.refund_popup_payment_method_spinner);
                String accounttyp= RefundHandler.this.pmtype.get(accounttype.getSelectedItemPosition());
                String peymentmet=RefundHandler.this.paymentmethods.get((String)paymentmethod.getSelectedItem());
                pd.show();
                Thread th=new Thread(){
                    @Override
                    public void run() {
                        try {
                            ac.requestforrefund(Double.parseDouble(amount.getText().toString()), txid, peymentmet, refundreason.getText().toString(), accounttyp, banknumber.getText().toString());
                            hand.post(new Runnable() {
                                @Override
                                public void run() {
                                    pd.dismiss();
                                    d.dismiss();
                                    Toast.makeText(RefundHandler.this.context,"Refund request complete!",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }catch (Exception e){
                            hand.post(new Runnable() {
                                @Override
                                public void run() {

                                    pd.dismiss();
                                }
                            });
                        }
                    }
                };

                th.start();


            }
        });



        Thread th=new Thread(){
            @Override
            public void run() {
                try {
                    RefundHandler.this.paymentmethods=ac.refundpaymentMethod();
                    Set<String> keys=RefundHandler.this.paymentmethods.keySet();
                    ArrayList<String>kkk=new ArrayList<>();
                    Iterator<String> iii=keys.iterator();
                    while(iii.hasNext()){
                        kkk.add(iii.next());
                    }
                    RefundHandler.this.hand.post(new Runnable() {
                        @Override
                        public void run() {
                            Spinner pm=d.findViewById(R.id.refund_popup_payment_method_spinner);
                            pm.setAdapter(new ArrayAdapter<String>(RefundHandler.this.context,android.R.layout.simple_spinner_item,kkk));
                            pd.dismiss();
                            RefundHandler.this.d.show();
                        }
                    });


                } catch (Exception e) {

                }
            }
        };
        th.start();







    }


}
