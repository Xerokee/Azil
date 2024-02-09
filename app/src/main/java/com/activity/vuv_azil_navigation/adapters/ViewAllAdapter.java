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
        Glide.with(context).load(model.getImg_url()).into(holder.imageView);
        holder.name.setText(model.getName());
        holder.description.setText(model.getDescription());
        holder.rating.setText(model.getRating());

        if (model.isAdopted()) {
            holder.adopterName.setVisibility(View.VISIBLE);
            if (model.getAdopterName() == null || model.getAdopterName().isEmpty()) {
                fetchAdopterName(model.getAdopterId(), holder.adopterName);
            } else {
                holder.adopterName.setText("Udomitelj: " + model.getAdopterName());
            }
        } else {
            holder.adopterName.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailedActivity.class);
            intent.putExtra("detail", model);
            context.startActivity(intent);
        });
    }

    private void fetchAdopterName(String adopterId, final TextView adopterNameTextView) {
        if (adopterId == null || adopterId.trim().isEmpty()) {
            adopterNameTextView.setText("Udomitelj: Nepoznato");
            return;
        }

        FirebaseFirestore.getInstance().collection("Korisnici").document(adopterId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel userModel = documentSnapshot.toObject(UserModel.class);
                        if (userModel != null && userModel.getName() != null) {
                            adopterNameTextView.setText("Udomitelj: " + userModel.getName());
                        } else {
                            adopterNameTextView.setText("Udomitelj: Nepoznato");
                        }
                    } else {
                        adopterNameTextView.setText("Udomitelj: Nepoznato");
                    }
                })
                .addOnFailureListener(e -> adopterNameTextView.setText("Udomitelj: Greška u dohvatu podataka"));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

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

