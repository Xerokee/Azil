package com.activity.vuv_azil_navigation.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    CircleImageView profileImg;
    EditText name, email, password;
    Button update, logout;

    FirebaseStorage storage;
    FirebaseAuth auth;

    public static final String ACTION_PROFILE_UPDATED = "com.activity.vuv_azil_navigation.PROFILE_UPDATED";
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int IMAGE_PICK_GALLERY_CODE = 101;
    private static final int IMAGE_PICK_CAMERA_CODE = 102;
    private Uri imageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        profileImg = root.findViewById(R.id.profile_img);
        name = root.findViewById(R.id.profile_name);
        email = root.findViewById(R.id.profile_email);
        password = root.findViewById(R.id.profile_password);
        update = root.findViewById(R.id.update);
        logout = root.findViewById(R.id.logout);

        logout.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red));

        profileImg.setOnClickListener(v -> showImagePickDialog());
        logout.setOnClickListener(v -> logoutUser());
        update.setOnClickListener(v -> updateUserProfile());
        loadUserProfile();

        return root;
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showImagePickDialog() {
        String[] options = {"Kamere", "Galerije"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Odaberite sliku iz");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    pickFromCamera();
                }
            } else {
                pickFromGallery();
            }
        });
        builder.create().show();
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean result1 = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result && result1;
    }

    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                imageUri = data.getData();
                uploadProfilePhoto(imageUri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                uploadProfilePhoto(imageUri);
            }
        }
    }

    private void uploadProfilePhoto(Uri uri) {
        if (uri == null) {
            Toast.makeText(getContext(), "Nije odabrana slika", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Korisnik nije prijavljen", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        StorageReference storageReference = storage.getReference().child("profile_pictures").child(uid);
        storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(downloadUri -> {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Korisnici").document(uid).update("profileImg", downloadUri.toString())
                        .addOnSuccessListener(unused -> {
                            Glide.with(getContext()).load(downloadUri.toString()).into(profileImg);
                            Toast.makeText(getContext(), "Slika profila ažurirana", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ACTION_PROFILE_UPDATED);
                            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                        });
            });
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Korisnici").document(currentUser.getUid())
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
                }).addOnFailureListener(e -> Toast.makeText(getContext(), "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Korisnik nije prijavljen.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        String newName = name.getText().toString().trim();

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", newName);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Korisnici").document(uid)
                .update(userUpdates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Profil ažuriran", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ACTION_PROFILE_UPDATED);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show());
}
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromCamera();
                } else {
                    Toast.makeText(getContext(), "Potrebne su dozvole za korištenje kamere", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
