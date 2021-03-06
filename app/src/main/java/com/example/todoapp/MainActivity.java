package com.example.todoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.todoapp.adapter.UserAdapter;
import com.example.todoapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    /**
     *  Mendefinisikan variable yang akan di pakai
     */
    private RecyclerView recyclerView;
    private FloatingActionButton btnAdd;
    /**
     *  inisialisasi object firebase firestore
     *  untuk menghubungkan dengan firestore
     */
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<User> list = new ArrayList<>();
    private UserAdapter userAdapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler_view);
        btnAdd = findViewById(R.id.btn_add);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Mengambil data...");
        userAdapter = new UserAdapter(getApplicationContext(), list);
        userAdapter.setDialog(new UserAdapter.Dialog() {
            @Override
            public void onClick(int pos) {
                final CharSequence[] dialogItem = {"Edit", "Hapus"};
                AlertDialog.Builder diaglog = new AlertDialog.Builder(MainActivity.this);
                diaglog.setItems(dialogItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface,int i) {
                        switch (i){
                            /**
                             *  Melemparkan data ke class berikutnya
                             */
                            case 0:
                                Intent intent = new Intent(getApplicationContext(),EditorActivity.class);
                                intent.putExtra("id", list.get(pos).getId());
                                intent.putExtra("name", list.get(pos).getName());
                                intent.putExtra("email", list.get(pos).getEmail());
                                startActivity(intent);
                                break;
                            case 1:
                                /**
                                 * memanggil class delete data
                                 */
                                deleteData(list.get(pos).getId());
                                break;
                        }
                    }
                });
                diaglog.show();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,false);
        RecyclerView.ItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(userAdapter);

        btnAdd.setOnClickListener(v ->{
            startActivity(new Intent(getApplicationContext(), EditorActivity.class));
        });
    }

    /**
     *  Method untuk menampilkan data agar di tampilkan
     *  pada saat aplikasi pertama kali running
     */
    @Override
    protected void onStart(){
        super.onStart();
        getData();
    }

    private void getData() {
        progressDialog.show();
        /**
         * Mengambil data dari firestore
         */
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDateSetChanged")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        list.clear();
                        if (task.isSuccessful()){
                            /**
                             *  Code ini mengambil data dari collection
                             */
                            for(QueryDocumentSnapshot document : task.getResult()){
                                /**
                                 * Data Apa saja yang ingin diambil dari collection
                                 */
                                User user = new User(document.getString("name"), document.getString("email"));
                                user.setId(document.getId());
                                list.add(user);
                            }
                            userAdapter.notifyDataSetChanged();
                        }else {
                            Toast.makeText(MainActivity.this, "Data Gagal di ambil!!!", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    /**
     *  method untuk menghapus daya
     */
    private void  deleteData(String id){
        progressDialog.show();
        db.collection("users").document(id)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "Data Gagal di hapus", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}