package com.activity.vuv_azil_navigation.ui.home;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.vuv_azil_navigation.R;
import com.activity.vuv_azil_navigation.adapters.HomeAdapter;
import com.activity.vuv_azil_navigation.adapters.PopularAdapters;
import com.activity.vuv_azil_navigation.adapters.RecommendedAdapter;
import com.activity.vuv_azil_navigation.adapters.ViewAllAdapter;
import com.activity.vuv_azil_navigation.databinding.FragmentHomeBinding;
import com.activity.vuv_azil_navigation.models.HomeCategory;
import com.activity.vuv_azil_navigation.models.PopularModel;
import com.activity.vuv_azil_navigation.models.RecommendedModel;
import com.activity.vuv_azil_navigation.models.UserModel;
import com.activity.vuv_azil_navigation.models.ViewAllModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {

    ScrollView scrollView;
    ProgressBar progressBar;
    RecyclerView popularRec, homeCatRec, recommendedRec;
    FirebaseFirestore db;

    // Popularne Životinje
    List<PopularModel> popularModelList;
    PopularAdapters popularAdapters;

    // Search view
    EditText search_box;
    private List<ViewAllModel> viewAllModelList;
    private RecyclerView recyclerViewSearch;
    private ViewAllAdapter viewAllAdapter;

    // Početna Kategorija
    List<HomeCategory> categoryList;
    HomeAdapter homeAdapter;

    // Predložene Životinje
    List<RecommendedModel> recommendedModelList;
    RecommendedAdapter recommendedAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();

        popularRec = root.findViewById(R.id.pop_rec);
        homeCatRec = root.findViewById(R.id.explore_rec_);
        recommendedRec = root.findViewById(R.id.recommended_rec);
        scrollView = root.findViewById(R.id.scroll_view);
        progressBar = root.findViewById(R.id.progressbar);

        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);

        // Popularne životinje
        popularRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        popularModelList = new ArrayList<>();
        popularAdapters = new PopularAdapters(getActivity(), popularModelList);
        popularRec.setAdapter(popularAdapters);

        db.collection("PopularAnimals")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                PopularModel popularModel = document.toObject(PopularModel.class);
                                popularModelList.add(popularModel);
                                popularAdapters.notifyDataSetChanged();

                                progressBar.setVisibility(View.GONE);
                                scrollView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getActivity(), "Error" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Početna Kategorija
        homeCatRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        categoryList = new ArrayList<>();
        homeAdapter = new HomeAdapter(getActivity(), categoryList);
        homeCatRec.setAdapter(homeAdapter);

        db.collection("HomeCategory")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HomeCategory homeCategory = document.toObject(HomeCategory.class);
                                categoryList.add(homeCategory);
                                homeAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Error" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Predložene Životinje
        recommendedRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        recommendedModelList = new ArrayList<>();
        recommendedAdapter = new RecommendedAdapter(getActivity(), recommendedModelList);
        recommendedRec.setAdapter(recommendedAdapter);

        db.collection("RecommendedAnimals")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                RecommendedModel recommendedModel = document.toObject(RecommendedModel.class);
                                recommendedModelList.add(recommendedModel);
                                recommendedAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Error" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Search view
        recyclerViewSearch = root.findViewById(R.id.search_rec);
        search_box = root.findViewById(R.id.search_box);
        viewAllModelList = new ArrayList<>();
        viewAllAdapter = new ViewAllAdapter(getContext(), viewAllModelList);
        recyclerViewSearch.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSearch.setAdapter(viewAllAdapter);
        recyclerViewSearch.setHasFixedSize(true);
        search_box.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().isEmpty()) {
                    viewAllModelList.clear();
                    viewAllAdapter.notifyDataSetChanged();
                } else {
                    searchAnimal(s.toString());
                }
            }
        });

        return root;
    }

    private void setAdopterListener(final ViewAllModel viewAllModel) {
        if (viewAllModel.getAdopterId() != null && !viewAllModel.getAdopterId().isEmpty()) {
            FirebaseFirestore.getInstance().collection("Korisnici").document(viewAllModel.getAdopterId())
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Listen failed.", e);
                            return;
                        }
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            UserModel userModel = documentSnapshot.toObject(UserModel.class);
                            if (userModel != null && userModel.getName() != null && !userModel.getName().isEmpty()) {
                                viewAllModel.setAdopterName(userModel.getName());
                            } else {
                                viewAllModel.setAdopterName("Nepoznato");
                            }
                        } else {
                            viewAllModel.setAdopterName("Nepoznato");
                        }
                        notifyAdapterChanged(viewAllModel);
                    });
        }
    }

    private void updateRecyclerView(List<ViewAllModel> list) {
        Log.d("HomeFragment", "Updating RecyclerView with " + list.size() + " items.");
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            viewAllModelList.clear();
            viewAllModelList.addAll(list);
            viewAllAdapter.notifyDataSetChanged();
        });
    }

    // Pretraga životinja i postavljanje imena udomitelja
    private void searchAnimal(String keyword) {
        Log.d("HomeFragment", "Searching for: " + keyword);
        db.collection("AllAnimals")
                .orderBy("name")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<ViewAllModel> resultList = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            ViewAllModel viewAllModel = doc.toObject(ViewAllModel.class);
                            if (viewAllModel != null) {
                                String animalId = doc.getId(); // Ovo je ID iz AllAnimals
                                // Sada trebamo pronaći podatke o udomljavanju iz AnimalsForAdoption
                                fetchAdoptionDetails(animalId, viewAllModel, resultList);
                            }
                        }
                    } else {
                        Log.e("HomeFragment", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void fetchAdoptionDetails(String animalId, ViewAllModel viewAllModel, List<ViewAllModel> listToUpdate) {
        db.collection("AnimalsForAdoption")
                .whereEqualTo("animalId", animalId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot adoptionDoc : queryDocumentSnapshots) {
                            // Pretpostavimo da je svaka životinja jedinstvena, stoga uzimamo prvi dokument
                            viewAllModel.setDocumentId(adoptionDoc.getId()); // Postavljamo documentId iz AnimalsForAdoption
                            viewAllModel.setAdopted(adoptionDoc.getBoolean("adopted"));
                            String adopterId = adoptionDoc.getString("adopterId");
                            viewAllModel.setAdopterId(adopterId);

                            // Dobavimo ime udomitelja ako je potrebno
                            if (adopterId != null && !adopterId.isEmpty()) {
                                fetchAdopterName(adopterId, viewAllModel);
                            } else {
                                viewAllModel.setAdopterName("Nepoznato");
                            }
                        }
                        listToUpdate.add(viewAllModel);
                        updateRecyclerView(listToUpdate);
                    } else {
                        // Nema zapisa o udomljavanju, postavimo status na 'nije udomljeno'
                        viewAllModel.setAdopted(false);
                        viewAllModel.setAdopterName("Nepoznato");
                        listToUpdate.add(viewAllModel);
                        updateRecyclerView(listToUpdate);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Error fetching adoption details for animal", e);
                    viewAllModel.setAdopted(false);
                    viewAllModel.setAdopterName("Nepoznato");
                    listToUpdate.add(viewAllModel);
                    updateRecyclerView(listToUpdate);
                });
    }

    private void fetchAdopterName(String adopterId, ViewAllModel viewAllModel) {
        db.collection("Korisnici").document(adopterId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                        String adopterName = documentSnapshot.getString("name");
                        viewAllModel.setAdopterName(adopterName);
                    } else {
                        viewAllModel.setAdopterName("Nepoznato");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Error fetching adopter name", e);
                    viewAllModel.setAdopterName("Nepoznato");
                });
    }

    private void notifyAdapterChanged(ViewAllModel updatedModel) {
        // Run on UI thread to ensure thread safety
        Log.d("HomeFragment", "Notifying adapter of data change.");
        getActivity().runOnUiThread(() -> {
            int index = viewAllModelList.indexOf(updatedModel);
            if (index != -1) {
                viewAllAdapter.notifyItemChanged(index);
            } else {
                Log.d(TAG, "Updated model not found in viewAllModelList. Updating entire dataset.");
                viewAllAdapter.notifyDataSetChanged(); // Fallback to update entire dataset
            }
        });
    }
}
