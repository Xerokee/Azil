package com.activity.vuv_azil_navigation;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.activity.vuv_azil_navigation.adapters.MyAdoptedAnimalsAdapter;
import com.activity.vuv_azil_navigation.models.AnimalModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyAdoptedFragment extends Fragment {

    public MyAdoptedFragment() {
        // Required empty public constructor
    }

    private RecyclerView recyclerView;
    private MyAdoptedAnimalsAdapter adapter;
    private List<AnimalModel> adoptedAnimalsList;
    private TextView newAnimalsTextView;
    private ImageView newAnimalsImageView;
    private boolean isSearchSuccessful = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_adopted, container, false);

        newAnimalsTextView = root.findViewById(R.id.new_animals_textview);
        newAnimalsImageView = root.findViewById(R.id.new_animals_img);

        recyclerView = root.findViewById(R.id.adopted_animals_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adoptedAnimalsList = new ArrayList<>();
        adapter = new MyAdoptedAnimalsAdapter(getContext(), adoptedAnimalsList);
        recyclerView.setAdapter(adapter);

        fetchAdoptedAnimals();
        EditText searchBox = root.findViewById(R.id.search_box);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    fetchAdoptedAnimals();
                    isSearchSuccessful = false;
                } else {
                    searchAdoptersByName(s.toString());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
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
                            animal.setDocumentId(snapshot.getId());
                            adoptedAnimalsList.add(animal);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateUIBasedOnSearchResults();

                    if (adoptedAnimalsList.isEmpty()) {
                        newAnimalsTextView.setVisibility(View.VISIBLE);
                        newAnimalsImageView.setVisibility(View.VISIBLE);
                    } else {
                        newAnimalsTextView.setVisibility(View.GONE);
                        newAnimalsImageView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                });
    }

    private void searchAdopters(String adopterIdQuery) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("AnimalsForAdoption")
                .whereEqualTo("adopterId", adopterIdQuery)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<AnimalModel> newAdoptedAnimalsList = new ArrayList<>();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        AnimalModel animal = snapshot.toObject(AnimalModel.class);
                        if (animal != null) {
                            animal.setDocumentId(snapshot.getId());
                            newAdoptedAnimalsList.add(animal);
                        }
                    }
                    adapter.updateList(newAdoptedAnimalsList);
                    updateUIBasedOnSearchResults();
                })
                .addOnFailureListener(e -> {
                    Log.e("SearchAdopters", "Error fetching adopted animals by adopter ID", e);
                });
    }

    private void searchAdoptersByName(String searchText) {
        if (searchText.trim().isEmpty()) {
            fetchAdoptedAnimals();
            return;
        }

        String startText = searchText.toLowerCase();
        String endText = startText + '\uf8ff';

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Korisnici")
                .orderBy("nameLowerCase")
                .startAt(startText)
                .endAt(endText)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> adopterIds = new ArrayList<>();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        adopterIds.add(snapshot.getId());
                    }
                    if (!adopterIds.isEmpty()) {
                        fetchAnimalsForAdopters(adopterIds);
                        isSearchSuccessful = true;
                    } else {
                        adoptedAnimalsList.clear();
                        adapter.notifyDataSetChanged();
                        isSearchSuccessful = false;
                        updateUIBasedOnSearchResults();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SearchAdoptersByName", "Error fetching adopters by name", e);
                    adoptedAnimalsList.clear();
                    adapter.notifyDataSetChanged();
                    updateUIBasedOnSearchResults();
                });
    }


    private void updateUIBasedOnSearchResults() {
        if (adoptedAnimalsList.isEmpty() && isSearchSuccessful) {
            newAnimalsTextView.setVisibility(View.VISIBLE);
            newAnimalsImageView.setVisibility(View.VISIBLE);
            newAnimalsTextView.setText("Nema pronađenih životinja za odabranog udomitelja.");
        } else {
            newAnimalsTextView.setVisibility(View.GONE);
            newAnimalsImageView.setVisibility(View.GONE);
        }
    }

    private void fetchAnimalsForAdopters(List<String> adopterIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (!adopterIds.isEmpty()) {
            db.collection("AnimalsForAdoption")
                    .whereIn("adopterId", adopterIds)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        adoptedAnimalsList.clear();
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            AnimalModel animal = snapshot.toObject(AnimalModel.class);
                            if (animal != null) {
                                adoptedAnimalsList.add(animal);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Log.e("FetchAnimals", "Error getting documents: ", e));
        }
    }
}