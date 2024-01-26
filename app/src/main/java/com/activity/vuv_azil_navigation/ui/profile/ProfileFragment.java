package com.activity.vuv_azil_navigation.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.activity.vuv_azil_navigation.R;
import com.activity.vuv_azil_navigation.activities.HomeActivity;
import com.activity.vuv_azil_navigation.models.UserModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    CircleImageView profileImg;
    EditText name, email, password;
    Button update, logout;

    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseDatabase database;

    public static final String ACTION_PROFILE_UPDATED = "com.activity.vuv_azil_navigation.PROFILE_UPDATED";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        loadUserProfile();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        profileImg = root.findViewById(R.id.profile_img);
        name = root.findViewById(R.id.profile_name);
        email = root.findViewById(R.id.profile_email);
        password = root.findViewById(R.id.profile_password);
        update = root.findViewById(R.id.update);
        logout = root.findViewById(R.id.logout);

        logout.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red));

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Odjava korisnika i preusmjeravanje
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        // Check if the user is authenticated
        if (auth.getCurrentUser() != null) {
            // User is authenticated, proceed with database operations

            String uid = FirebaseAuth.getInstance().getUid();
            if (uid != null) {
                database.getReference().child("Korisnici").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                UserModel userModel = snapshot.getValue(UserModel.class);

                                if (userModel != null) {
                                    name.setText(userModel.getName());
                                    email.setText(userModel.getEmail());

                                    if (userModel.getProfileImg() != null) {
                                        Glide.with(getContext()).load(userModel.getProfileImg()).into(profileImg);
                                    } else {
                                        Glide.with(getContext()).load(R.drawable.profile).into(profileImg);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle the error
                            }
                        });
            }
        }

        // Listener za promjenu slike profila
        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kod za odabir nove slike
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 33);
            }
        });

        // Listener za gumb ažuriranja
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserProfile();
            }
        });

        return root;
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Korisnici").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserModel userModel = documentSnapshot.toObject(UserModel.class);
                            if (userModel != null) {
                                name.setText(userModel.getName());
                                email.setText(userModel.getEmail());
                                if (userModel.getProfileImg() != null) {
                                    Glide.with(getContext()).load(userModel.getProfileImg()).into(profileImg);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void updateUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Korisnik nije prijavljen.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        String newName = name.getText().toString().trim();
        String newEmail = email.getText().toString().trim();
        String newPassword = password.getText().toString().trim();

        updateUserEmailAndName(currentUser, newEmail, newName, uid);

        if (!newPassword.isEmpty()) {
            currentUser.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Lozinka ažurirana", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }


    private void updateUserEmailAndName(FirebaseUser currentUser, String newEmail, String newName, String uid) {
        currentUser.updateEmail(newEmail)
                .addOnSuccessListener(aVoid -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> userUpdates = new HashMap<>();
                    userUpdates.put("name", newName);
                    userUpdates.put("email", newEmail);

                    db.collection("Korisnici").document(uid)
                            .update(userUpdates)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "Profil ažuriran", Toast.LENGTH_SHORT).show();
                                // Šalje Broadcast samo ovdje
                                Intent intent = new Intent(ACTION_PROFILE_UPDATED);
                                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 33 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri profileUri = data.getData();
            profileImg.setImageURI(profileUri);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(getContext(), "Greška: Korisnik nije prijavljen.", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = currentUser.getUid();
            StorageReference reference = storage.getReference().child("profile_picture").child(uid);

            reference.putFile(profileUri).addOnSuccessListener(taskSnapshot -> {
                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("Korisnici").document(uid)
                            .update("profileImg", uri.toString())
                            .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Slika Profila Ažurirana", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
            });
        }
    }
}
