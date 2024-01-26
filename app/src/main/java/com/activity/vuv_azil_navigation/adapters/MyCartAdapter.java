package com.activity.vuv_azil_navigation.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.vuv_azil_navigation.R;
import com.activity.vuv_azil_navigation.models.MyCartModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.my_cart_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyCartModel cartModel = cartModelList.get(position);
        Glide.with(context).load(cartModelList.get(position).getimg_url()).into(holder.img_url);
        Log.d("MyCartAdapter", "MyCartModel at position " + position + ": " + cartModel.getAnimalName() + ", " + cartModel.getAnimalType() + ", " + cartModel.getCurrentDate() + ", " + cartModel.getCurrentTime() + ", " + cartModel.getimg_url() + ", " + cartModel.getAnimalId());

        holder.name.setText(cartModelList.get(position).getAnimalName());
        holder.type.setText(cartModelList.get(position).getAnimalType());
        holder.date.setText(cartModelList.get(position).getCurrentDate());
        holder.time.setText(cartModelList.get(position).getCurrentTime());

        holder.deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(position);
            }
        });

        holder.updateItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateDialog(position);
            }
        });

        if (cartModel.isAdopted()) {
            holder.adoptButton.setEnabled(false);
            holder.adoptButton.setText("Udomljeno");
        } else {
            holder.adoptButton.setEnabled(true);
            holder.adoptButton.setText("Udomi sad");
        }
    }

    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Potvrda brisanja");
        builder.setMessage("Jeste li sigurni da želite izbrisati ovu stavku iz liste?");

        builder.setPositiveButton("Da", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem(position);
            }
        });

        builder.setNegativeButton("Ne", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing or handle accordingly
            }
        });

        builder.show();
    }

    private void deleteItem(int position) {
        firestore.collection("AnimalsForAdoption")
                .document(cartModelList.get(position).getAnimalId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            cartModelList.remove(position); // Use 'position' instead of 'cartModelList.get(position)'
                            notifyDataSetChanged();
                            Toast.makeText(context, "Lista izbrisana", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Greška: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

        private void showUpdateDialog(int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.update_dialog, null);
            builder.setView(view);

            EditText edtName = view.findViewById(R.id.edt_updated_name);
            EditText edtType = view.findViewById(R.id.edt_updated_type);
            EditText edtDate = view.findViewById(R.id.edt_updated_date);
            EditText edtTime = view.findViewById(R.id.edt_updated_time);
            EditText edtimg_url = view.findViewById(R.id.edt_updated_img_url);

            // Set the existing values to EditTexts
            MyCartModel cartModel = cartModelList.get(position);
            edtName.setText(cartModel.getAnimalName());
            edtType.setText(cartModel.getAnimalType());
            edtDate.setText(cartModel.getCurrentDate());
            edtTime.setText(cartModel.getCurrentTime());
            edtimg_url.setText(cartModel.getimg_url());

            builder.setPositiveButton("Ažuriraj", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String updatedName = edtName.getText().toString().trim();
                    String updatedType = edtType.getText().toString().trim();
                    String updatedDate = edtDate.getText().toString().trim();
                    String updatedTime = edtTime.getText().toString().trim();
                    String updatedimg_url = edtimg_url.getText().toString().trim();

                    // Check if any field is empty
                    if (TextUtils.isEmpty(updatedName) || TextUtils.isEmpty(updatedType) ||
                            TextUtils.isEmpty(updatedDate) || TextUtils.isEmpty(updatedTime)) {
                        Toast.makeText(context, "Molimo popunite sva polja", Toast.LENGTH_SHORT).show();
                    } else {
                        // Update Firestore document
                        updateFirestoreDocument(position, updatedName, updatedType, updatedDate, updatedTime, updatedimg_url);
                    }
                }
            });

            builder.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

    private void updateFirestoreDocument(int position, String updatedName, String updatedType, String updatedDate, String updatedTime, String updatedimg_url) {
        // Update Firestore document with the new information
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
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Update the corresponding item in the list
                            cartModel.setAnimalName(updatedName);
                            cartModel.setAnimalType(updatedType);
                            cartModel.setCurrentDate(updatedDate);
                            cartModel.setCurrentTime(updatedTime);
                            cartModel.setimg_url(updatedimg_url);

                            // Notify the adapter that the data has changed
                            notifyDataSetChanged();

                            Toast.makeText(context, "Lista ažurirana", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Greška: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return cartModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name,type,date,time;
        ImageView img_url;
        ImageView deleteItem;
        ImageView updateItem;
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

            adoptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        MyCartModel selectedAnimal = cartModelList.get(position);
                        showAdoptionDialog(selectedAnimal, position);
                    }
                }
            });
        }

        private void showAdoptionDialog(MyCartModel selectedAnimal, int position) {
            // Ovdje ćemo pretpostaviti da imate metodu koja dohvaća listu mogućih udomitelja iz Firebase-a.
            // Kada dohvatite listu, prikažite AlertDialog sa svim udomiteljima.
            List<String> adopterNames = new ArrayList<>(); // Ovo trebate napuniti stvarnim podacima
            // Dohvatite listu udomitelja iz Firebase-a i napunite adopterNames.

            CharSequence[] adoptersArray = adopterNames.toArray(new CharSequence[0]);
            new AlertDialog.Builder(context)
                    .setTitle("Odaberite udomitelja")
                    .setItems(adoptersArray, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 'which' je indeks odabranog udomitelja. Ovdje ćemo pretpostaviti da imate ID-eve udomitelja.
                            String adopterId = "ID_od_adoptera"; // Ovdje bi trebali umetnuti stvarni ID odabranog udomitelja
                            adoptAnimal(selectedAnimal, adopterId, position);
                        }
                    })
                    .show();
        }

        private void adoptAnimal(MyCartModel animal, String adopterId, int position) {
            Map<String, Object> adoptionUpdates = new HashMap<>();
            adoptionUpdates.put("adopted", true); // Pretpostavljamo da imate 'adopted' polje u Firestore dokumentu
            adoptionUpdates.put("adopterId", adopterId); // I 'adopterId' polje u Firestore dokumentu

            firestore.collection("AnimalsForAdoption").document(animal.getAnimalId())
                    .update(adoptionUpdates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Ažuriranje UI-a nakon uspješnog udomljavanja
                            Toast.makeText(context, "Životinja je udomljena!", Toast.LENGTH_SHORT).show();
                            animal.setAdopted(true); // Ovdje ažuriramo model da je životinja udomljena
                            notifyItemChanged(position); // Obavještavamo adapter da je došlo do promjene
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // U slučaju neuspjeha, prikazujemo poruku o grešci
                            Toast.makeText(context, "Udomljavanje nije uspjelo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}