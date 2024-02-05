package com.activity.vuv_azil_navigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activity.vuv_azil_navigation.models.UserModel;
import com.activity.vuv_azil_navigation.ui.profile.ProfileFragment;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    ProgressBar progressBar;
    FirebaseAuth auth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(profileUpdateReceiver,
                new IntentFilter(ProfileFragment.ACTION_PROFILE_UPDATED));

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_category, R.id.nav_profile,R.id.nav_new_products,
                R.id.nav_my_orders,R.id.nav_my_carts)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View headerView = navigationView.getHeaderView(0);
        TextView headerName = headerView.findViewById(R.id.nav_header_name);
        TextView headerEmail = headerView.findViewById(R.id.nav_header_email);
        CircleImageView headerImg = headerView.findViewById(R.id.nav_header_img);

        updateNavigationHeader(headerName, headerEmail, headerImg);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            database.getReference().child("Korisnici").child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel userModel = snapshot.getValue(UserModel.class);

                            if (userModel != null) {
                                headerName.setText(userModel.getName());
                                headerEmail.setText(userModel.getEmail());

                                Glide.with(MainActivity.this).load(userModel.getProfileImg()).into(headerImg);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle onCancelled
                        }
                    });
        } else {
            // Handle the case where UID is null
            // You might want to sign out the user or redirect to the login screen
        }
    }

    private BroadcastReceiver profileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ProfileFragment.ACTION_PROFILE_UPDATED)) {
                NavigationView navigationView = findViewById(R.id.nav_view);
                View headerView = navigationView.getHeaderView(0);
                TextView headerName = headerView.findViewById(R.id.nav_header_name);
                TextView headerEmail = headerView.findViewById(R.id.nav_header_email);
                CircleImageView headerImg = headerView.findViewById(R.id.nav_header_img);
                updateNavigationHeader(headerName, headerEmail, headerImg);
            }
        }
    };

    private void updateNavigationHeader(TextView headerName, TextView headerEmail, CircleImageView headerImg) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Korisnici").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserModel userModel = documentSnapshot.toObject(UserModel.class);
                            if (userModel != null) {
                                headerName.setText(userModel.getName());
                                headerEmail.setText(userModel.getEmail());
                                if (userModel.getProfileImg() != null) {
                                    Glide.with(this).load(userModel.getProfileImg()).into(headerImg);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle the error
                    });
        } else {
            // Handle the case where currentUser is null
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(profileUpdateReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}