package com.esc.test.apps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.esc.test.apps.activities.BoardActivity;
import com.esc.test.apps.activities.Home;
import com.esc.test.apps.activities.Login;

public class BaseActivity extends AppCompatActivity {

    protected ConstraintLayout baseLayout;
    protected ListView drawerList;
    private DrawerLayout drawerLayout;
    private EndDrawerToggle actionBarDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_main);

        baseLayout = findViewById(R.id.main_frame);
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerList = findViewById(R.id.left_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        String[] menuList = getResources().getStringArray(R.array.menu);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        drawerList.setAdapter(new ArrayAdapter(this, R.layout.drawer_list, menuList));
        drawerList.setOnItemClickListener((parent, view, pos, id) -> openActivity(pos));
        actionBarDrawerToggle = new EndDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer){};
        actionBarDrawerToggle.syncState();
    }

    private void openActivity(int position) {
        drawerLayout.closeDrawer(drawerList);
        switch (position) {
            case 0:
                startActivity(new Intent(this, Home.class));
                break;
            case 1:
                startActivity(new Intent(this, BoardActivity.class));
                break;
            case 2:
                startActivity(new Intent(this, Login.class));
                break;
            /*case 3:
                startActivity(new Intent(this, Categories.class));
                break;
            case 4:
                startActivity(new Intent(this, Markets.class));
                break;*/
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(drawerList)){
            drawerLayout.closeDrawer(drawerList);
        } else {
            super.onBackPressed();
        }
    }
}
