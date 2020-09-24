package com.kirti.videoeditor;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Main extends AppCompatActivity {
    FirebaseFirestore db;
    List<String> vurl;
    FloatingActionButton fab_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        vurl = new ArrayList<>();
        fab_add = findViewById(R.id.fab_add);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main.this, MainActivity.class));
            }
        });
        db = FirebaseFirestore.getInstance();
        db.collection("Video")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                vurl.add(String.valueOf(document.get("url")));
                                System.out.println("=======doc======" + document.get("url"));
                                Log.d("TAG", document.getId() + " => " + document.getData());
                            }

                            RecyclerView.Adapter a = new RecyclerView.Adapter<ViewHolder>() {
                                @NonNull
                                @Override
                                public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);

                                    return new ViewHolder(v);
                                }

                                @Override
                                public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
                                    holder.pb.setVisibility(View.VISIBLE);
                                    holder.vv.setVideoPath(vurl.get(position));

                                    holder.vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                        @Override
                                        public void onPrepared(MediaPlayer mp) {
                                            holder.pb.setVisibility(View.GONE);
                                            mp.start();
                                            mp.setLooping(true);
                                        }
                                    });

                                }

                                @Override
                                public int getItemCount() {
                                    return vurl.size();
                                }
                            };
                            RecyclerView rv = findViewById(R.id.rv_video);
                            rv.setHasFixedSize(true);
                            rv.setLayoutManager(new LinearLayoutManager(Main.this));
                            rv.setItemViewCacheSize(20);
                            rv.setDrawingCacheEnabled(true);
                            rv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

                            rv.setAdapter(a);
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        VideoView vv;
        ProgressBar pb;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vv = itemView.findViewById(R.id.vv1);
            pb = itemView.findViewById(R.id.progressBar);
        }
    }
}

/*

 */
