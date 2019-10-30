package com.unpad.trashcare;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.unpad.trashcare.adapters.PemberitahuanAdapter;
import com.unpad.trashcare.models.Pemberitahuan;

import java.util.ArrayList;

public class PemberitahuanActivity extends AppCompatActivity {

    private final static String TAG = "DaftarWargaActivity";

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView mPemberitahuanList;
    private ArrayList<Pemberitahuan> pemberitahuans = new ArrayList<>();
    private PemberitahuanAdapter mPemberitahuanAdapter;


    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pemberitahuan);

        db = FirebaseFirestore.getInstance();

        mPemberitahuanList = findViewById(R.id.pemberitahuan_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mPemberitahuanList.setLayoutManager(layoutManager);

        swipeRefresh = findViewById(R.id.refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearPemberitahuanList();
                initPemberitahuanList();
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(false);
                    }
                }, 500);
            }
        });
        initPemberitahuanList();
        Toolbar toolbar = findViewById(R.id.toolbarPemberitahuan);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void clearPemberitahuanList() {
        pemberitahuans.clear();
        mPemberitahuanAdapter.notifyDataSetChanged();
    }

    private void initPemberitahuanList() {
        db.collection("pemberitahuan").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    pemberitahuans = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        Pemberitahuan pemberitahuan = document.toObject(Pemberitahuan.class);
                        if (pemberitahuan.getPenerima() == ((UserClient)(getApplicationContext())).getWarga().getId_warga()) {
                            pemberitahuans.add(pemberitahuan);
                        }
                    }
                    mPemberitahuanAdapter = new PemberitahuanAdapter(pemberitahuans);
                    mPemberitahuanList.setAdapter(mPemberitahuanAdapter);
                    mPemberitahuanAdapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
//                    Toast.makeText(this, "gagal", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
