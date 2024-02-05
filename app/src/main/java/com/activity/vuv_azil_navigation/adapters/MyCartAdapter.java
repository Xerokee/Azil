package com.activity.vuv_azil_navigation.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.vuv_azil_navigation.R;
import com.activity.vuv_azil_navigation.models.MyCartModel;
import com.activity.vuv_azil_navigation.models.UserModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.ViewHolder> {

    Context context;
    List<MyCartModel> cartModelList;
    FirebaseFirestore firestore;
    FirebaseAuth auth;

    public MyCartAdapter(Context context, List<MyCartModel> cartModelList) {
        this.context = context;
        this.cartModelList = cartModelList;
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_cart_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyCartModel cartModel = cartModelList.get(position);

        if (cartModel.getimg_url() != null && !cartModel.getimg_url().isEmpty()) {
            Glide.with(context).load(cartModel.getimg_url()).into(holder.img_url);
        } else {
            holder.img_url.setImageResource(R.drawable.profile);
        }

        Glide.with(context).load(cartModel.getimg_url()).into(holder.img_url);
        holder.name.setText(cartModel.getAnimalName());
        holder.type.setText(cartModel.getAnimalType());
        holder.date.setText(cartModel.getCurrentDate());
        holder.time.setText(cartModel.getCurrentTime());

        holder.deleteItem.setOnClickListener(v -> checkIfUserIsAdminThenRun(
                () -> showDeleteConfirmationDialog(position),
                () -> Toast.makeText(context, "Samo admini mogu brisati životinje.", Toast.LENGTH_SHORT).show()
        ));

        holder.updateItem.setOnClickListener(v -> checkIfUserIsAdminThenRun(
                () -> showUpdateDialog(position),
                () -> Toast.makeText(context, "Samo admini mogu ažurirati životinje.", Toast.LENGTH_SHORT).show()
        ));

        if (cartModel.isAdopted()) {
            holder.adoptButton.setEnabled(false);
            holder.adoptButton.setText("Udomljeno");
        } else {
            holder.adoptButton.setEnabled(true);
            holder.adoptButton.setText("Udomi sad");
            holder.adoptButton.setOnClickListener(view -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    MyCartModel selectedAnimal = cartModelList.get(adapterPosition);
                    if (!selectedAnimal.isAdopted()) {
                        checkIfUserIsAdmin(selectedAnimal, adapterPosition);
                    } else {
                        Toast.makeText(context, "Životinja je već udomljena.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void checkIfUserIsAdminThenRun(Runnable onAdmin, Runnable onNonAdmin) {
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
                    .addOnFailureListener(e -> Toast.makeText(context, "Greška pri provjeri statusa admina", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(context, "Niste prijavljeni.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Potvrda brisanja");
        builder.setMessage("Jeste li sigurni da želite izbrisati ovu stavku iz liste?");

        builder.setPositiveButton("Da", (dialog, which) -> deleteItem(position));

        builder.setNegativeButton("Ne", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void deleteItem(int position) {
        firestore.collection("AnimalsForAdoption")
                .document(cartModelList.get(position).getAnimalId())
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cartModelList.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Lista izbrisana", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Greška: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showUpdateDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.update_dialog, null);
        builder.setView(view);

        EditText edtName = view.findViewById(R.id.edt_updated_name);
        EditText edtType = view.findViewById(R.id.edt_updated_type);
        EditText edtDate = view.findViewById(R.id.edt_updated_date);
        EditText edtTime = view.findViewById(R.id.edt_updated_time);
        EditText edtimg_url = view.findViewById(R.id.edt_updated_img_url);

        MyCartModel cartModel = cartModelList.get(position);
        edtName.setText(cartModel.getAnimalName());
        edtType.setText(cartModel.getAnimalType());
        edtDate.setText(cartModel.getCurrentDate());
        edtTime.setText(cartModel.getCurrentTime());
        edtimg_url.setText(cartModel.getimg_url());

        builder.setPositiveButton("Ažuriraj", (dialog, which) -> {
            String updatedName = edtName.getText().toString().trim();
            String updatedType = edtType.getText().toString().trim();
            String updatedDate = edtDate.getText().toString().trim();
            String updatedTime = edtTime.getText().toString().trim();
            String updatedimg_url = edtimg_url.getText().toString().trim();

            if (TextUtils.isEmpty(updatedName) || TextUtils.isEmpty(updatedType) || TextUtils.isEmpty(updatedDate) || TextUtils.isEmpty(updatedTime)) {
                Toast.makeText(context, "Molimo popunite sva polja", Toast.LENGTH_SHORT).show();
            } else {
                updateFirestoreDocument(position, updatedName, updatedType, updatedDate, updatedTime, updatedimg_url);
            }
        });

        builder.setNegativeButton("Odustani", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void updateFirestoreDocument(int position, String updatedName, String updatedType, String updatedDate, String updatedTime, String updatedimg_url) {
        MyCartModel cartModel = cartModelList.get(position);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("animalName", updatedName);
        updateData.put("animalType", updatedType);
        updateData.put("currentDate", updatedDate);
        updateData.put("currentTime", updatedTime);
        updateData.put("img_url", updatedimg_url);

        firestore.collection("AnimalsForAdoption")
                .document(cartModel.getAnimalId())
                .update(updateData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cartModel.setAnimalName(updatedName);
                        cartModel.setAnimalType(updatedType);
                        cartModel.setCurrentDate(updatedDate);
                        cartModel.setCurrentTime(updatedTime);
                        cartModel.setimg_url(updatedimg_url);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Lista ažurirana", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Greška: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfUserIsAdmin(final MyCartModel selectedAnimal, final int adapterPosition) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            firestore.collection("Korisnici").document(user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists() && Boolean.TRUE.equals(document.getBoolean("isAdmin"))) {
                                Log.d("MyCartAdapter", "User is admin");
                                showAdoptionDialog(selectedAnimal, adapterPosition);
                            } else {
                                Log.d("MyCartAdapter", "User is not admin");
                                Toast.makeText(context, "Samo admini mogu udomljavati životinje.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("MyCartAdapter", "Error checking admin status", task.getException());
                            Toast.makeText(context, "Greška pri provjeri statusa admina", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(context, "Nije moguće provjeriti status admina", Toast.LENGTH_SHORT).show();
        }
    }


    public interface OnAdminCheckListener {
        void onAdminChecked(boolean isAdmin);
    }

    private void showAdoptionDialog(MyCartModel selectedAnimal, int position) {
        firestore.collection("Korisnici").whereEqualTo("isAdmin", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> adopterNames = new ArrayList<>();
                    List<String> adopterIds = new ArrayList<>();

                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        UserModel userModel = snapshot.toObject(UserModel.class);
                        if (userModel != null && !userModel.getIsAdmin()) {
                            adopterNames.add(userModel.getName());
                            adopterIds.add(snapshot.getId());
                        }
                    }

                    if (adopterNames.isEmpty()) {
                        Toast.makeText(context, "Trenutno nema dostupnih udomitelja.", Toast.LENGTH_SHORT).show();
                        return; // Exit if there are no adopters
                    }

                    CharSequence[] adoptersArray = adopterNames.toArray(new CharSequence[0]);
                    new AlertDialog.Builder(context)
                            .setTitle("Odaberite udomitelja")
                            .setItems(adoptersArray, (dialog, which) -> {
                                String selectedUserId = adopterIds.get(which);
                                adoptAnimal(selectedAnimal.getAnimalId(), selectedUserId, adopterNames.get(which));
                            })
                            .show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Ne može se dohvatiti lista udomitelja: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void adoptAnimal(String animalId, String adopterId, String adopterName) {
        Map<String, Object> adoptionUpdates = new HashMap<>();
        adoptionUpdates.put("adopted", true);
        adoptionUpdates.put("adopterId", adopterId);

        firestore.collection("AnimalsForAdoption").document(animalId)
                .update(adoptionUpdates)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Životinja je udomljena za korisnika " + adopterName, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Udomljavanje nije uspjelo: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    @Override
    public int getItemCount() {
        return cartModelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, type, date, time;
        ImageView img_url;
        ImageView deleteItem, updateItem;
        Button adoptButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.product_name);
            type = itemView.findViewById(R.id.product_type);
            date = itemView.findViewById(R.id.current_date);
            time = itemView.findViewById(R.id.current_time);
            img_url = itemView.findViewById(R.id.img_url);
            deleteItem = itemView.findViewById(R.id.delete);
            updateItem = itemView.findViewById(R.id.update);
            adoptButton = itemView.findViewById(R.id.adoptButton);
        }
    }
}