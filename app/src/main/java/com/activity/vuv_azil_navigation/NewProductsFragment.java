package com.activity.vuv_azil_navigation;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.activity.vuv_azil_navigation.R;
import com.activity.vuv_azil_navigation.models.ViewAllModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NewProductsFragment extends Fragment {

    private EditText etName, etDescription, etRating, etImgUrl, etType;
    private FloatingActionButton btnAddAnimal;
    private LinearLayout animalFormContainer;
    private FloatingActionButton fabAddAnimal;

    public NewProductsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_new_products, container, false);

        etName = root.findViewById(R.id.editTextName);
        etDescription = root.findViewById(R.id.editTextDescription);
        etRating = root.findViewById(R.id.editTextRating);
        etImgUrl = root.findViewById(R.id.editTextImgUrl);
        etType = root.findViewById(R.id.editTextType);
        animalFormContainer = root.findViewById(R.id.animalFormContainer);

        fabAddAnimal = root.findViewById(R.id.fabAddAnimal);
        fabAddAnimal.setOnClickListener(v -> toggleFormVisibility());

        Button btnSubmitAnimal = root.findViewById(R.id.buttonSubmitAnimal);
        btnSubmitAnimal.setOnClickListener(v -> addNewAnimal());

        return root;
    }

    private void toggleFormVisibility() {
        if (animalFormContainer.getVisibility() == View.GONE) {
            Log.d("NewProductsFragment", "Prikazivanje obrasca");
            animalFormContainer.setVisibility(View.VISIBLE);
        } else {
            Log.d("NewProductsFragment", "Skrivanje obrasca");
            animalFormContainer.setVisibility(View.GONE);
            clearForm();
        }
    }

    private void addNewAnimal() {
        checkIfUserIsAdmin(() -> {
            String name = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String rating = etRating.getText().toString().trim();
            String imgUrl = etImgUrl.getText().toString().trim();
            String type = etType.getText().toString().trim();

            Log.d("NewProductsFragment", "Uneseni podaci: " +
                    "Ime: " + name +
                    ", Opis: " + description +
                    ", Ocjena: " + rating +
                    ", URL slike: " + imgUrl +
                    ", Tip: " + type);

            if (name.isEmpty() || description.isEmpty() || rating.isEmpty() || type.isEmpty()) {
                Toast.makeText(getContext(), "Molimo ispunite sve podatke", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Float.parseFloat(rating); // Provjerite je li ocjena valjan broj
            } catch (NumberFormatException e) {
                Log.e("NewProductsFragment", "Greška pri provjeri ocjene: " + e.getMessage());
                Toast.makeText(getContext(), "Ocjena mora biti broj", Toast.LENGTH_SHORT).show();
                return;
            }

        ViewAllModel newAnimal = new ViewAllModel(name, description, rating, imgUrl, type);
        FirebaseFirestore.getInstance().collection("AllAnimals")
                .add(newAnimal)
                .addOnSuccessListener(documentReference -> {
                    Log.d("NewProductsFragment", "Životinja uspješno dodana");
                    Toast.makeText(getContext(), "Životinja uspješno dodana", Toast.LENGTH_SHORT).show();
                    toggleFormVisibility();
                })
                .addOnFailureListener(e -> {
                    Log.e("NewProductsFragment", "Greška pri dodavanju životinje: " + e.getMessage());
                    Toast.makeText(getContext(), "Došlo je do greške: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }, () -> {
            Toast.makeText(getContext(), "Samo admini mogu dodavati životinje.", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkIfUserIsAdmin(Runnable onAdmin, Runnable onNonAdmin) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("Korisnici").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && Boolean.TRUE.equals(documentSnapshot.getBoolean("isAdmin"))) {
                            onAdmin.run();
                        } else {
                            onNonAdmin.run();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška pri provjeri statusa admina", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "Niste prijavljeni.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        etName.setText("");
        etDescription.setText("");
        etRating.setText("");
        etImgUrl.setText("");
        etType.setText("");
    }
}
