package com.activity.vuv_azil_navigation.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.vuv_azil_navigation.R;
import com.activity.vuv_azil_navigation.models.ViewAllModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class DetailedActivity extends AppCompatActivity {

    ImageView detailedImg;
    TextView rating,description;
    Button addToCart;
    Toolbar toolbar;
    FirebaseFirestore firestore;
    FirebaseAuth auth;

    ViewAllModel viewAllModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        final Object object = getIntent().getSerializableExtra("detail");
        if (object instanceof ViewAllModel){
            viewAllModel = (ViewAllModel) object;
        }

        detailedImg = findViewById(R.id.detailed_img);
        rating = findViewById(R.id.detailed_rating);
        description = findViewById(R.id.detailed_dec);

        if (viewAllModel != null){
            Glide.with(getApplicationContext()).load(viewAllModel.getImg_url()).into(detailedImg);
            rating.setText(viewAllModel.getRating());
            description.setText(viewAllModel.getDescription());
        }

        addToCart = findViewById(R.id.add_to_cart);
        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addedToCart();
            }
        });
    }

    private void addedToCart() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userUid = currentUser.getUid();
            Log.d("DetailedActivity", "User UID: " + userUid);
            Log.d("DetailedActivity", "Is user authenticated: " + (currentUser.isEmailVerified() ? "Yes" : "No"));
            String saveCurrentDate, saveCurrentTime;
            Calendar calForDate = Calendar.getInstance();

            SimpleDateFormat currentDate = new SimpleDateFormat("dd. MM. yyyy", new Locale("hr", "HR"));
            saveCurrentDate = currentDate.format(calForDate.getTime());

            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss", new Locale("hr", "HR"));
            saveCurrentTime = currentTime.format(calForDate.getTime());

            saveCurrentTime = saveCurrentTime + " sati";

            final HashMap<String, Object> cartMap = new HashMap<>();
            String animalId = UUID.randomUUID().toString();
            cartMap.put("animalId", animalId);
            cartMap.put("animalName", viewAllModel.getName());
            cartMap.put("animalType", viewAllModel.getType());
            cartMap.put("currentDate", saveCurrentDate);
            cartMap.put("currentTime", saveCurrentTime);
            cartMap.put("img_url", viewAllModel.getImg_url());

            firestore.collection("AnimalsForAdoption")
                    .add(cartMap)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(DetailedActivity.this, "Dodano u listu udomljavanja! ", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(DetailedActivity.this, "Greška: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(DetailedActivity.this, "Niste prijavljeni. Molimo prijavite se!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DetailedActivity.this, HomeActivity.class));
        }
    }
}
