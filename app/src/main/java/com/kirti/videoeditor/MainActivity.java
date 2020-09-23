package com.kirti.videoeditor;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import VideoHandle.EpEditor;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public String Videopath;
    VideoView vv;
    BottomNavigationView bot_nav;
    EpVideo epVideo;
    File f;
    ArrayAdapter speed;
    String[] speedarray = {"0.25", "0.50", "0.75", "1.0", "1.25", "1.50", "1.75", "2.0", "2.25", "2.50", "2.75", "3.0", "3.25", "3.50", "3.75", "4.0"};
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bot_nav = findViewById(R.id.bot_nav);
        vv = findViewById(R.id.vv);
        pd = new ProgressDialog(this);
        pd.setMessage("Wait......");
        pd.setCancelable(false);
        speed = new ArrayAdapter(this, android.R.layout.simple_list_item_1, speedarray);
        vv.setMediaController(new MediaController(this));
        chooseFile();
        f = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        bot_nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (Videopath != null) {
                    if (item.getTitle().equals("Trim")) {
                        Trim(Videopath);
                    } else if (item.getTitle().equals("Speed")) {
                        Speed(Videopath);
                    } else if (item.getTitle().equals("Add Music")) {
                        Intent intent = new Intent();
                        intent.setType("audio/*");
                        //intent.setType("audio/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        // intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, 102);


                    }

                }
                return false;
            }
        });

    }


    private void chooseFile() {
        Intent intent = new Intent();
        intent.setType("video/*");
        //intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                Videopath = UriUtils.getPath(MainActivity.this, data.getData());
                vv.setVideoPath(Videopath);
                vv.start();


            }
        } else if (requestCode == 102) {
            if (resultCode == RESULT_OK) {
                final String audiopath = UriUtils.getPath(MainActivity.this, data.getData());
                MediaPlayer mp = new MediaPlayer();
                System.out.println("==========requestaudio===========");
                final String[] Duration = new String[1];
                try {
                    mp.setDataSource(audiopath);
                    mp.prepare();
                    mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(final MediaPlayer mp) {

                            vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp1) {
                                    if (mp1.getDuration() < mp.getDuration()) {
                                        Duration[0] = String.valueOf(mp1.getDuration());
                                    } else {
                                        Duration[0] = String.valueOf(mp.getDuration());
                                    }
                                    System.out.println(mp1.getDuration() + "=======" + mp.getDuration() + "========duration=======" + Duration[0]);
                                    addaudio(audiopath, Integer.valueOf(Duration[0]) / 1000, mp.getDuration() / 1000);
                                }
                            });


                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    public void Trim(String Videopath) {
        epVideo = new EpVideo(Videopath);
        final Dialog d = new Dialog(MainActivity.this);
        d.setContentView(R.layout.dialog_trim);
        d.setCancelable(false);
        d.create();
        ImageView iv_done, iv_close;
        final RangeSeekBar rsb;
        d.show();
        iv_done = d.findViewById(R.id.img_right);
        iv_close = d.findViewById(R.id.img_wrong);
        rsb = d.findViewById(R.id.rsb);
        rsb.setRangeValues(0, vv.getDuration() / 1000);
        System.out.println("==============vvdue======" + vv.getDuration());
        iv_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.show();
                int min, max, duration;
                min = Integer.valueOf(String.valueOf(rsb.getSelectedMinValue()));
                max = Integer.valueOf(String.valueOf(rsb.getSelectedMaxValue()));
                duration = max - min;
                epVideo.clip(min, duration);
                exe(epVideo);
                d.dismiss();
            }
        });
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
    }

    public void Speed(final String Videopath) {
        final Dialog d = new Dialog(MainActivity.this);
        d.setContentView(R.layout.dialog_speed);
        //d.setCancelable(false);
        d.create();
        final Spinner sp_speed;
        ImageView bt_ok, bt_can;
        d.show();
        sp_speed = d.findViewById(R.id.sp_speed);
        bt_ok = d.findViewById(R.id.img_right2);
        bt_can = d.findViewById(R.id.img_wrong2);
        sp_speed.setAdapter(speed);
        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.show();
                Float f1 = Float.valueOf(sp_speed.getSelectedItem().toString());
                System.out.println("==========fff========" + f1);
                EpEditor.changePTS(Videopath, f.getPath() + "/2.mp4", 2.0f, EpEditor.PTS.ALL, new OnEditorListener() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.dismiss();
                                d.dismiss();
                                MainActivity.this.Videopath = f.getPath() + "/2.mp4";
                                vv.setVideoPath(MainActivity.this.Videopath);
                                vv.start();
                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        pd.dismiss();
                    }

                    @Override
                    public void onProgress(float progress) {

                    }
                });

            }
        });
        bt_can.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

    }

    public void exe(EpVideo epVideo) {
        EpEditor.OutputOption outputOption = new EpEditor.OutputOption(f.getPath() + "/1.mp4");
        outputOption.frameRate = 30;//frame rate, default 30
        outputOption.bitRate = 10;//bit rate, default 10
        EpEditor.exec(epVideo, outputOption, new OnEditorListener() {
            @Override
            public void onSuccess() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Videopath = f.getPath() + "/1.mp4";
                        vv.setVideoPath(Videopath);
                        vv.start();
                        pd.dismiss();

                    }
                });
            }

            @Override
            public void onFailure() {

            }

            @Override
            public void onProgress(float progress) {

            }
        });

    }

    public void addaudio(String Audiopath, final int Duration, int audiodue) {
        epVideo = new EpVideo(Audiopath);
        final Dialog d = new Dialog(MainActivity.this);
        d.setContentView(R.layout.dialog_trim);
        d.setCancelable(false);
        d.create();
        ImageView iv_done, iv_close;
        final RangeSeekBar rsb;
        d.show();
        iv_done = d.findViewById(R.id.img_right);
        iv_close = d.findViewById(R.id.img_wrong);
        rsb = d.findViewById(R.id.rsb);
        rsb.setRangeValues(0, audiodue);
        rsb.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                rsb.setSelectedMaxValue(Integer.valueOf(String.valueOf(minValue)) + Integer.valueOf(Duration));
            }
        });
        iv_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.show();
                int min, max, duration;
                min = Integer.valueOf(String.valueOf(rsb.getSelectedMinValue()));
                max = Integer.valueOf(String.valueOf(rsb.getSelectedMaxValue()));
                duration = max - min;
                epVideo.clip(min, Duration);
                d.dismiss();
                exeaudio(epVideo);

            }
        });
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });


    }

    public void exeaudio(EpVideo epVideo) {

        EpEditor.OutputOption outputOption = new EpEditor.OutputOption(f.getPath() + "/1.mp3");
        outputOption.frameRate = 30;//frame rate, default 30
        outputOption.bitRate = 10;//bit rate, default 10
        EpEditor.exec(epVideo, outputOption, new OnEditorListener() {
            @Override
            public void onSuccess() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String audiopath = f.getPath() + "/1.mp3";
                        EpEditor.music(Videopath, audiopath, f.getPath() + "/1.mp4", 1, 0.7f, new OnEditorListener() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        pd.dismiss();
                                        Videopath = f.getPath() + "/1.mp4";
                                        vv.setVideoPath(f.getPath() + "/1.mp4");
                                        vv.start();
                                    }
                                });
                            }

                            @Override
                            public void onFailure() {
                                pd.dismiss();
                            }

                            @Override
                            public void onProgress(float progress) {

                            }
                        });
                        pd.dismiss();

                    }
                });
            }

            @Override
            public void onFailure() {

            }

            @Override
            public void onProgress(float progress) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals("Upload")) {
            String s = Calendar.getInstance().getTimeInMillis() + ".mp4";
            final StorageReference vref = FirebaseStorage.getInstance().getReference().child(s);

            UploadTask uploadTask = vref.putFile(Uri.parse("file://" + Videopath));
            final ProgressDialog d = new ProgressDialog(MainActivity.this);
            d.setCancelable(false);
            d.create();
            d.show();
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    d.setMessage("Uploading ......." + progress);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    d.dismiss();
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    vref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            d.dismiss();
                            Toast.makeText(MainActivity.this, "Video Uploaded", Toast.LENGTH_SHORT).show();
                            Map<String, Object> video = new HashMap<>();
                            video.put("url", String.valueOf(uri));
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("Video").add(video).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                }
                            });
                            startActivity(new Intent(MainActivity.this, Main.class));

                            finish();

                        }

                    });

                }
            });
        }
        return super.onOptionsItemSelected(item);
    }
}

