package com.example.galleryfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private static final int PICK_IMG_REQUEST =1 ;
    private static final int SPACING =4 ;
    private static int SPAN_COUNT=3;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String uId;
    private StorageReference userImagesRef;
    private FloatingActionButton fab;
    private RecyclerView mRec;
    List<Image> images;
    private ImageAdapter adapter;
    private String link;
    private String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init

        FirebaseApp.initializeApp(this);
        mAuth=FirebaseAuth.getInstance();
        uId=mAuth.getUid();
        Log.e("get user ID ", uId+"");
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        storageReference=firebaseStorage.getReference();
        databaseReference =firebaseDatabase.getReference().child("userID").child("images");
        userImagesRef = storageReference.child("userID").child("images");
        fab=findViewById(R.id.fab);
        mRec=findViewById(R.id.recycler);
        images = new ArrayList<>();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();


            }
        });



        adapter = new ImageAdapter(images, this, new ImageAdapter.OnClickListener() {
            @Override
            public void onClick(int index) {
                Image image = images.get(index);
                download(image.getImagePath());
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, SPAN_COUNT);
        mRec.setLayoutManager(gridLayoutManager);
        mRec.addItemDecoration(new GridItemDecoration(SPAN_COUNT, SPACING, true));
        mRec.setAdapter(adapter);




        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Image image = dataSnapshot.getValue(Image.class);
                images.add(image);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }






    private void download(String imagePath) {

        final ProgressDialog progressDialog = new ProgressDialog(this);

        progressDialog.setMessage("Downloading...");
        progressDialog.show();
        String localFileName = UUID.randomUUID().toString() + ".jpg";
        final File file = new File(getFilesDir(), localFileName);
        storageReference.child(imagePath).getFile(file)
                .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "file Downloaded to " + file.getPath(), Toast.LENGTH_SHORT).show();
                            Log.d("3llomi", "File Downloaded to " + file.getPath());


                        } else {
                            Toast.makeText(MainActivity.this, "download Failed " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("3llomi", "Download Failed " + task.getException().getLocalizedMessage());
                        }
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                progressDialog.setMessage(progress + "%");

            }
        });


    }

    private void pickImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_IMG_REQUEST);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            upload(imageUri);


        } else {
            Toast.makeText(this, "no image selected :/", Toast.LENGTH_SHORT).show();
        }
    }

    private void upload(Uri imageUri) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("uploading your Picture  ...");

        progressDialog.show();
        final String imageName = UUID.randomUUID().toString() + ".jpg";


        userImagesRef.child(imageName).putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {


                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());


                        progressDialog.setMessage(progress + "%");
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                             path = task.getResult().getStorage().getPath();




                            Toast.makeText(MainActivity.this, "Uplaod Succeed", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("Failure:", "upload Failed " + task.getException().getLocalizedMessage());
                            Toast.makeText(MainActivity.this, "Uplaod Failed :( " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                )
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                userImagesRef.child(imageName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.e("",uri+"");
                         link = uri.toString();
                        saveImagePathToDatabase(link, path);


                    }
                });


            }
        })

        ;





    }

    private void saveImagePathToDatabase(String link, String path) {
        Image image = new Image(link, path);
        databaseReference.push().setValue(image);

    }




}
