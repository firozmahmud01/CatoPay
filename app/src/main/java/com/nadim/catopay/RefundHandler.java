package com.nadim.catopay;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.logging.Handler;

public class RefundHandler {
    private Context context;
    private Handler hand;
    public RefundHandler(Context context,Handler hand){
        this.context=context;
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
    private void showMainScreen(){
        ProgressDialog pd=new ProgressDialog(this.context);
        pd.setCancelable(false);
        pd.setMessage("Please wait,Loading...");
        pd.show();

        Dialog d=new Dialog(this.context);
        d.setContentView(R.layout.refund_popup_screen);
        ArrayList<String>accounttype=new ArrayList<>();
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
        accounttype.add("Personal Account");
        accounttype.add("Business Account");
        accounttype.add("Agent Account");
        accounttype.add("Merchant Account");
        Spinner accountt=d.findViewById(R.id.refund_popup_account_type_spinner);
        accountt.setAdapter(new ArrayAdapter<String>(this.context, android.R.layout.simple_spinner_item,accounttype));









    }


}
