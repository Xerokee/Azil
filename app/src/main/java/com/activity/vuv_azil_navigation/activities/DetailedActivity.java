package com.activity.vuv_azil_navigation.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
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

public class DetailedActivity extends AppCompatActivity {

    TextView quantity;
    int totalQuantity = 1;
    int totalPrice = 0;

    ImageView detailedImg;
    TextView price,rating,description;
    Button addToCart;
    ImageView addItem,removeItem;
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

        quantity = findViewById(R.id.quantity);
        detailedImg = findViewById(R.id.detailed_img);
        addItem = findViewById(R.id.add_item);
        removeItem = findViewById(R.id.remove_item);

        price = findViewById(R.id.detailed_price);
        rating = findViewById(R.id.detailed_rating);
        description = findViewById(R.id.detailed_dec);

        if (viewAllModel != null){
            Glide.with(getApplicationContext()).load(viewAllModel.getImg_url()).into(detailedImg);
            rating.setText(viewAllModel.getRating());
            description.setText(viewAllModel.getDescription());
            price.setText("Cijena :€"+viewAllModel.getPrice()+"/svaki");

            totalPrice = viewAllModel.getPrice() * totalQuantity;
        }

        addToCart = findViewById(R.id.add_to_cart);
        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addedToCart();
            }
        });
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (totalQuantity < 10){
                    totalQuantity++;
                    quantity.setText(String.valueOf(totalQuantity));
                    totalPrice = viewAllModel.getPrice() * totalQuantity;
                }
            }
        });
        removeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (totalQuantity > 1){
                    totalQuantity--;
                    quantity.setText(String.valueOf(totalQuantity));
                    totalPrice = viewAllModel.getPrice() * totalQuantity;
                }

            }
        });
    }

    private void addedToCart() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String saveCurrentDate, saveCurrentTime;
            Calendar calForDate = Calendar.getInstance();

            SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());

            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
            saveCurrentTime = currentTime.format(calForDate.getTime());

            final HashMap<String, Object> cartMap = new HashMap<>();

            cartMap.put("productName", viewAllModel.getName());
            cartMap.put("productPrice", price.getText().toString());
            cartMap.put("currentDate", saveCurrentDate);
            cartMap.put("currentTime", saveCurrentTime);
            cartMap.put("totalQuantity", quantity.getText().toString());
            cartMap.put("totalPrice", totalPrice);

            firestore.collection("CurrentUser")
                    .document(auth.getCurrentUser().getUid())
                    .collection("AddToAdopt")
                    .add(cartMap)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            Toast.makeText(DetailedActivity.this, "Dodano u Košaricu ", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } else {
            Toast.makeText(DetailedActivity.this, "Niste prijavljeni. Molimo prijavite se prije dodavanja u košaricu!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DetailedActivity.this, HomeActivity.class));
        }
    }
}