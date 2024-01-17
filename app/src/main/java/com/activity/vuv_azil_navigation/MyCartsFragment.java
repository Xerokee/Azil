package com.activity.vuv_azil_navigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.vuv_azil_navigation.adapters.MyCartAdapter;
import com.activity.vuv_azil_navigation.models.MyCartModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyCartsFragment extends Fragment {

    FirebaseFirestore db;
    FirebaseAuth auth;
    TextView overTotalAmount;

    RecyclerView recyclerView;
    MyCartAdapter cartAdapter;
    List<MyCartModel> cartModelList;

    public MyCartsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_my_carts, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        overTotalAmount = root.findViewById(R.id.textView7);

        cartModelList = new ArrayList<>();
        cartAdapter = new MyCartAdapter(getActivity(),cartModelList);
        recyclerView.setAdapter(cartAdapter);

        db.collection("AnimalsForAdoption") // Remove the UID constraint
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            cartModelList.clear(); // Clear the list before adding items
                            for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                String animalId = documentSnapshot.getId();
                                MyCartModel cartModel = documentSnapshot.toObject(MyCartModel.class);
                                cartModel.setAnimalId(animalId);
                                cartModelList.add(cartModel);

                                // Log the content of each cartModel
                                Log.d("MyCartsFragment", "CartModel: " + cartModel.getProductName() + ", " + cartModel.getProductType() + ", " + cartModel.getCurrentDate() + ", " + cartModel.getCurrentTime() + ", " + cartModel.getAnimalId());
                            }
                            Log.d("MyCartsFragment", "Number of items: " + cartModelList.size());
                            cartAdapter.notifyDataSetChanged();
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            // Handle the error
                            Toast.makeText(getActivity(), "Greška: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return root;
    }
}