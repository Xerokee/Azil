// MyAdoptedAnimalsAdapter.java
package com.activity.vuv_azil_navigation.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.vuv_azil_navigation.R;
import com.activity.vuv_azil_navigation.models.AnimalModel;
import com.activity.vuv_azil_navigation.models.MyCartModel;
import com.activity.vuv_azil_navigation.models.UserModel;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAdoptedAnimalsAdapter extends RecyclerView.Adapter<MyAdoptedAnimalsAdapter.ViewHolder> {
    private Context context;
    private List<AnimalModel> adoptedAnimalsList;
    FirebaseFirestore firestore;

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
        Glide.with(context).load(animal.getImg_url()).into(holder.animalImage);
        holder.tvAdoptedStatus.setText(animal.isAdopted() ? "Udomljeno" : "Dostupno za udomljavanje");
        holder.returnButton.setOnClickListener(v -> {
            String documentId = adoptedAnimalsList.get(position).getDocumentId(); // Koristite documentId
            checkIfUserIsAdmin(documentId, position, () -> returnAnimal(documentId, position));
        });

        getAdopterNameById(animal.getAdopterId(), holder.adopterName);
    }

    @Override
    public int getItemCount() {
        return adoptedAnimalsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView animalName, animalType, tvAdoptedStatus, adopterName;
        ImageView animalImage;
        Button returnButton;

        public ViewHolder(View itemView) {
            super(itemView);
            animalName = itemView.findViewById(R.id.textViewAnimalName);
            animalType = itemView.findViewById(R.id.textViewAnimalType);
            animalImage = itemView.findViewById(R.id.imageViewAnimal);
            tvAdoptedStatus = itemView.findViewById(R.id.tvAdoptedStatus);
            adopterName = itemView.findViewById(R.id.textViewAdopterName);
            returnButton = itemView.findViewById(R.id.returnButton); // Dodajemo gumb za vraćanje
        }
    }

    private void checkIfUserIsAdmin(String documentId, int position, Runnable onAdminAction) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("Korisnici").document(user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists() && Boolean.TRUE.equals(document.getBoolean("isAdmin"))) {
                                onAdminAction.run(); // If user is admin, perform the admin action
                            } else {
                                showAdminOnlyDialog(); // If user is not admin, show admin only dialog
                            }
                        } else {
                            Toast.makeText(context, "Greška pri provjeri statusa admina", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(context, "Niste prijavljeni.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAdminOnlyDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Administratorska Akcija")
                .setMessage("Samo admini mogu vraćati životinje.")
                .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
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

    private void returnAnimal(String documentId, int position) {
        if (documentId == null) {
            Log.e("ReturnAnimal", "Document ID is null for position: " + position);
            Toast.makeText(context, "Error: Document ID is null.", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("AnimalsForAdoption").document(documentId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot animalSnapshot = transaction.get(docRef);
            if (animalSnapshot.exists() && animalSnapshot.getBoolean("adopted")) {
                transaction.update(docRef, "adopted", false);
                transaction.update(docRef, "adopterId", FieldValue.delete());
                Log.d("ReturnAnimal", "Animal with ID: " + documentId + " has been returned.");
            } else {
                throw new FirebaseFirestoreException("No such document to update or animal is not adopted",
                        FirebaseFirestoreException.Code.ABORTED);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            adoptedAnimalsList.remove(position); // Uklanja element iz liste
            notifyItemRemoved(position); // Obavještava adapter o promjeni
            Toast.makeText(context, "Životinja je vraćena u azil", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e("ReturnAnimal", "Failed to return animal with ID: " + documentId, e);
            Toast.makeText(context, "Greška pri vraćanju: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    public void updateList(List<AnimalModel> newList) {
        adoptedAnimalsList.clear();
        adoptedAnimalsList.addAll(newList);
        notifyDataSetChanged();
    }
}


