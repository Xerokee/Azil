package com.activity.vuv_azil_navigation;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.activity.vuv_azil_navigation.adapters.MyAdoptedAnimalsAdapter;
import com.activity.vuv_azil_navigation.models.AnimalModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyOrdersFragment extends Fragment {

    public MyOrdersFragment() {
        // Required empty public constructor
    }

    private RecyclerView recyclerView;
    private MyAdoptedAnimalsAdapter adapter;
    private List<AnimalModel> adoptedAnimalsList;
    private TextView newAnimalsTextView;
    private ImageView newAnimalsImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_orders, container, false);

        // Initialize your TextView and ImageView
        newAnimalsTextView = root.findViewById(R.id.new_animals_textview);
        newAnimalsImageView = root.findViewById(R.id.new_animals_img);

        recyclerView = root.findViewById(R.id.adopted_animals_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adoptedAnimalsList = new ArrayList<>();
        adapter = new MyAdoptedAnimalsAdapter(getContext(), adoptedAnimalsList);
        recyclerView.setAdapter(adapter);

        fetchAdoptedAnimals();

        return root;
    }

    private void fetchAdoptedAnimals() {
        FirebaseFirestore.getInstance().collection("AnimalsForAdoption")
                .whereEqualTo("adopted", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    adoptedAnimalsList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        AnimalModel animal = snapshot.toObject(AnimalModel.class);
                        if (animal != null) {
                            animal.setDocumentId(snapshot.getId()); // Set the document ID here
                            adoptedAnimalsList.add(animal);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (adoptedAnimalsList.isEmpty()) {
                        newAnimalsTextView.setVisibility(View.VISIBLE);
                        newAnimalsImageView.setVisibility(View.VISIBLE);
                    } else {
                        newAnimalsTextView.setVisibility(View.GONE);
                        newAnimalsImageView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }
}