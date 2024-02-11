package com.activity.vuv_azil_navigation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.vuv_azil_navigation.adapters.MyAdoptionAdapter;
import com.activity.vuv_azil_navigation.models.MyAdoptionModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyAnimalsFragment extends Fragment {

    FirebaseFirestore db;
    FirebaseAuth auth;
    TextView overTotalAmount;

    RecyclerView recyclerView;
    MyAdoptionAdapter cartAdapter;
    List<MyAdoptionModel> cartModelList;

    public MyAnimalsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_my_adoptions, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        overTotalAmount = root.findViewById(R.id.textView7);

        cartModelList = new ArrayList<>();
        cartAdapter = new MyAdoptionAdapter(getActivity(),cartModelList);
        recyclerView.setAdapter(cartAdapter);

        db.collection("AnimalsForAdoption") // Remove the UID constraint
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            cartModelList.clear(); // Clear the list before adding items
                            for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                String animalId = documentSnapshot.getId();
                                MyAdoptionModel cartModel = documentSnapshot.toObject(MyAdoptionModel.class);
                                cartModel.setAnimalId(animalId);
                                cartModelList.add(cartModel);

                                // Log the content of each cartModel
                                Log.d("MyCartsFragment", "CartModel: " + cartModel.getAnimalName() + ", " + cartModel.getAnimalType() + ", " + cartModel.getCurrentDate() + ", " + cartModel.getCurrentTime() + ", " + cartModel.getAnimalId());
                            }
                            Log.d("MyCartsFragment", "Number of items: " + cartModelList.size());
                            cartAdapter.notifyDataSetChanged();
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            // Log the error
                            Log.e("MyCartsFragment", "Error fetching documents: ", task.getException());
                            Toast.makeText(getActivity(), "Greška: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return root;
    }
}