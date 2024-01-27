// MyAdoptedAnimalsAdapter.java
package com.activity.vuv_azil_navigation.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.vuv_azil_navigation.R;
import com.activity.vuv_azil_navigation.models.AnimalModel;
import com.bumptech.glide.Glide;

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
        holder.tvAdoptedStatus.setText(animal.isAdopted() ? "Dostupno za udomljavanje" : "Udomljeno");
    }

    @Override
    public int getItemCount() {
        return adoptedAnimalsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView animalName, animalType, tvAdoptedStatus;
        ImageView animalImage;

        public ViewHolder(View itemView) {
            super(itemView);
            animalName = itemView.findViewById(R.id.textViewAnimalName);
            animalType = itemView.findViewById(R.id.textViewAnimalType);
            animalImage = itemView.findViewById(R.id.imageViewAnimal);
            tvAdoptedStatus = itemView.findViewById(R.id.tvAdoptedStatus);
        }
    }
}
