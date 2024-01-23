package com.activity.vuv_azil_navigation.ui.profile;

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

import com.activity.vuv_azil_navigation.R;
import com.activity.vuv_azil_navigation.activities.HomeActivity;
import com.activity.vuv_azil_navigation.models.UserModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    CircleImageView profileImg;
    EditText name, email, password;
    Button update, logout;

    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseDatabase database;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

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
                // Logout user
                FirebaseAuth.getInstance().signOut();

                // Redirect to HomeActivity
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
                                    password.setText(userModel.getPassword());

                                    if (userModel.getProfileImg() != null) {
                                        Glide.with(getContext()).load(userModel.getProfileImg()).into(profileImg);
                                    } else {
                                        // Postavite defaultnu sliku profila ako nema dostupne slike
                                        // Glide.with(getContext()).load(R.drawable.default_profile_image).into(profileImg);
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

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 33);
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserProfile();
            }
        });

        return root;
    }

    private void updateUserProfile() {
        // Dohvatite nove podatke iz polja za unos
        String newName = name.getText().toString();
        String newEmail = email.getText().toString();
        String newPassword = password.getText().toString();

        // Ažurirajte korisničke podatke u bazi podataka
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            DatabaseReference userRef = database.getReference().child("Korisnici").child(uid);
            userRef.child("name").setValue(newName);
            userRef.child("email").setValue(newEmail);
            userRef.child("password").setValue(newPassword);

            Toast.makeText(getContext(), "Profil ažuriran", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null) {
            Uri profileUri = data.getData();
            profileImg.setImageURI(profileUri);

            final StorageReference reference = storage.getReference().child("profile_picture")
                    .child(FirebaseAuth.getInstance().getUid());

            reference.putFile(profileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getContext(), "Slika Prenesena", Toast.LENGTH_SHORT).show();

                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            database.getReference().child("Korisnici").child(FirebaseAuth.getInstance().getUid())
                                    .child("profileImg").setValue(profileUri.toString());
                            Toast.makeText(getContext(), "Slika Profila Prenesena", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }
}
