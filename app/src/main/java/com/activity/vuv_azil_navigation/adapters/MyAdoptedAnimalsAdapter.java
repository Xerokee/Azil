// MyAdoptedAnimalsAdapter.java
package com.activity.vuv_azil_navigation.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.vuv_azil_navigation.R;
import com.activity.vuv_azil_navigation.models.AnimalModel;
import com.activity.vuv_azil_navigation.models.UserModel;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MyAdoptedAnimalsAdapter extends RecyclerView.Adapter<MyAdoptedAnimalsAdapter.ViewHolder> {
    private Context context;
    private List<AnimalModel> adoptedAnimalsList;

    public MyAdoptedAnimalsAdapter(Context context, List<AnimalModel> adoptedAnimalsList) {
        this.context = context;
        this.adoptedAnimalsList = adoptedAnimalsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_adopted_animal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnimalModel animal = adoptedAnimalsList.get(position);
        holder.animalName.setText(animal.getAnimalName());
        holder.animalType.setText(animal.getAnimalType());
        Glide.with(context).load(animal.getImgUrl()).into(holder.animalImage);
        holder.tvAdoptedStatus.setText(animal.isAdopted() ? "Udomljeno" : "Dostupno za udomljavanje");

        getAdopterNameById(animal.getAdopterId(), holder.adopterName);
    }

    @Override
    public int getItemCount() {
        return adoptedAnimalsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView animalName, animalType, tvAdoptedStatus, adopterName;
        ImageView animalImage;

        public ViewHolder(View itemView) {
            super(itemView);
            animalName = itemView.findViewById(R.id.textViewAnimalName);
            animalType = itemView.findViewById(R.id.textViewAnimalType);
            animalImage = itemView.findViewById(R.id.imageViewAnimal);
            tvAdoptedStatus = itemView.findViewById(R.id.tvAdoptedStatus);
            adopterName = itemView.findViewById(R.id.textViewAdopterName);
        }
    }

    private void getAdopterNameById(String adopterId, final TextView adopterNameTextView) {
        if (adopterId == null || adopterId.trim().isEmpty()) {
            adopterNameTextView.setText("Nepoznato");
            Log.d("AdopterInfo", "Adopter ID is null or empty");
            return;
        }

        FirebaseFirestore.getInstance().collection("Korisnici").document(adopterId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel userModel = documentSnapshot.toObject(UserModel.class);
                        if (userModel != null && userModel.getName() != null) {
                            adopterNameTextView.setText(userModel.getName());
                            Log.d("AdopterInfo", "Adopter name found: " + userModel.getName());
                        } else {
                            adopterNameTextView.setText("Nepoznato");
                            Log.d("AdopterInfo", "UserModel is null or name is null");
                        }
                    } else {
                        adopterNameTextView.setText("Nepoznato");
                        Log.d("AdopterInfo", "Document does not exist for ID: " + adopterId);
                    }
                })
                .addOnFailureListener(e -> {
                    adopterNameTextView.setText("Nepoznato");
                    Log.e("AdopterInfo", "Error fetching adopter info", e);
                });
    }
}


