package com.nadim.catopay;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class Home extends AppCompatActivity {
    ViewPager vp;
    FragmentPagerAdapter pa;

//    HomeScreen hs;
//    Transaction ts;
//    Orders or;
//    Profile pro;
    Handler hand;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


//        hs=new HomeScreen();
//        ts=new Transaction();
//        or=new Orders();
//        pro=new Profile();

        if(!SMSSender.isrunning){

                startService(new Intent(Home.this,SMSSender.class));

//            startService(new Intent(Home.this,SMSSender.class));
        }

        hand=new Handler();
        vp=findViewById(R.id.home_viewpager);
        pa=new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                switch (position){
                    case 0:
                        return new HomeScreen();

                    case 1:
                        Transaction tl= new Transaction();
                        tl.setHandler(hand);
                        return tl;
                    case 2:
                        return new Orders();

                    default:
                        return new Profile();
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };


        vp.setAdapter(pa);

        TabLayout tl=findViewById(R.id.home_tablayout);
//        tl.setSelectedTabIndicatorColor();
        tl.setupWithViewPager(vp);
        try {
            tl.getTabAt(0).setIcon(R.drawable.homeicon);
            tl.getTabAt(1).setIcon(R.drawable.transaction);
            tl.getTabAt(2).setIcon(R.drawable.folder);
            tl.getTabAt(3).setIcon(R.drawable.profile);
        }catch (Exception e){
            int a=10;
        }



    }
}