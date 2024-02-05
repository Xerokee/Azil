package com.activity.vuv_azil_navigation.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.vuv_azil_navigation.R;
import com.activity.vuv_azil_navigation.activities.DetailedActivity;
import com.activity.vuv_azil_navigation.models.UserModel;
import com.activity.vuv_azil_navigation.models.ViewAllModel;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ViewAllAdapter extends RecyclerView.Adapter<ViewAllAdapter.ViewHolder>{

    Context context;
    List<ViewAllModel> list;

    public ViewAllAdapter(Context context, List<ViewAllModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewAllAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_all_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewAllAdapter.ViewHolder holder, int position) {
        ViewAllModel model = list.get(position);
        Glide.with(context).load(list.get(position).getImg_url()).into(holder.imageView);
        holder.name.setText(list.get(position).getName());
        holder.description.setText(list.get(position).getDescription());
        holder.rating.setText(list.get(position).getRating());
        holder.adopterName.setText(list.get(position).getAdopterName());

        Log.d("ViewAllAdapter", "Position: " + position + " Name: " + model.getName() + " Adopted: " + model.isAdopted() + " Adopter Name: " + model.getAdopterName());

        if (model.isAdopted() && model.getAdopterId() != null && !model.getAdopterId().isEmpty()) {
            FirebaseFirestore.getInstance().collection("Korisnici").document(model.getAdopterId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (model.isAdopted()) {
                            Log.d("ViewAllAdapter", "Animal at position " + position + " is adopted.");
                            if (model.getAdopterName() == null || model.getAdopterName().isEmpty()) {
                                Log.d("ViewAllAdapter", "Adopter name not set, fetching adopter name for " + model.getName());
                                fetchAdopterName(model.getDocumentId(), holder);
                            } else {
                                Log.d("ViewAllAdapter", "Displaying adopter name for " + model.getName());
                                holder.adopterName.setText("Udomitelj: " + model.getAdopterName());
                            }
                        } else {
                            Log.d("ViewAllAdapter", "Animal at position " + position + " is available for adoption.");
                            holder.adopterName.setText("Dostupno za udomljavanje");
                        }
                    })
                    .addOnFailureListener(e -> holder.adopterName.setText("Udomitelj: Nepoznato"));
        } else {
            holder.adopterName.setText("Dostupno za udomljavanje");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailedActivity.class);
            intent.putExtra("detail", model);
            context.startActivity(intent);
        });
    }

    private void fetchAdopterName(String animalId, final ViewHolder holder) {
        FirebaseFirestore.getInstance().collection("AnimalsForAdoption")
                .whereEqualTo("animalId", animalId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Assume each animal is unique, therefore, we take the first document.
                        DocumentSnapshot adoptionDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String adopterId = adoptionDoc.getString("adopterId");

                        FirebaseFirestore.getInstance().collection("Korisnici")
                                .document(adopterId)
                                .get()
                                .addOnSuccessListener(adopterSnapshot -> {
                                    if (adopterSnapshot.exists()) {
                                        UserModel userModel = adopterSnapshot.toObject(UserModel.class);
                                        holder.adopterName.setText("Udomitelj: " + userModel.getName());
                                        // Update the model in the list with adopter's name
                                        int pos = holder.getAdapterPosition();
                                        if (pos != RecyclerView.NO_POSITION) {
                                            list.get(pos).setAdopterName(userModel.getName());
                                            notifyItemChanged(pos);
                                        }
                                    } else {
                                        holder.adopterName.setText("Udomitelj: Nepoznato");
                                    }
                                })
                                .addOnFailureListener(e -> holder.adopterName.setText("Udomitelj: Nepoznato"));
                    } else {
                        // If there are no adoption documents, set as not adopted.
                        holder.adopterName.setText("Dostupno za udomljavanje");
                    }
                })
                .addOnFailureListener(e -> holder.adopterName.setText("Udomitelj: Nepoznato"));
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView name,description,rating,adopterName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.view_img);
            name = itemView.findViewById(R.id.view_name);
            description = itemView.findViewById(R.id.view_description);
            rating = itemView.findViewById(R.id.view_rating);
            adopterName = itemView.findViewById(R.id.adopter_name);
        }
    }
}

