package pl.pwr.blogapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar newPostToolbar;
    private ImageView newPostImage;
    private EditText newPostDesc;
    private Button newPostBtn;
    private ProgressBar newPostProgres;
    private Uri postImageUri = null;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    private FirebaseAuth mAuth;
    private String current_user_id;



    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = firebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        newPostImage = findViewById(R.id.new_post_image);
        newPostDesc = findViewById(R.id.new_post_desc);
        newPostBtn = findViewById(R.id.post_btn);
        newPostProgres = findViewById(R.id.new_post_progress);


        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BringImagePicker();

            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String desc = newPostDesc.getText().toString();

                if(!TextUtils.isEmpty(desc) && postImageUri != null){

                    newPostProgres.setVisibility(View.VISIBLE);

                    String randomName = FieldValue.serverTimestamp().toString();
                    StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");
                    filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful()){

                                Task<Uri> uri_image = task.getResult().getStorage().getDownloadUrl();
                                while (!uri_image.isComplete()) ;
                                String download_uri = uri_image.getResult().toString();

                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("imageUrl", download_uri);
                                postMap.put("desc", desc);
                                postMap.put("user_id", current_user_id);
                                postMap.put("timestamp", FieldValue.serverTimestamp());


                                firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {

                                        if(task.isSuccessful()){

                                            Toast.makeText(NewPostActivity.this, "Post was added", Toast.LENGTH_LONG).show();
                                            finish();

                                        }else{

                                        }

                                        newPostProgres.setVisibility(View.INVISIBLE);

                                    }
                                });


                            }else{

                                newPostProgres.setVisibility(View.INVISIBLE);

                            }

                        }
                    });

                }

            }
        });

    }

    private void BringImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512,512)
                .start(NewPostActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();

                newPostImage.setImageURI(postImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }

}
