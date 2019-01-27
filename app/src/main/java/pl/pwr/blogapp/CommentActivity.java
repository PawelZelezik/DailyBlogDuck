package pl.pwr.blogapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class CommentActivity extends AppCompatActivity {

    private Toolbar commentToolbar;

    private EditText commentField;
    private ImageView commentPostBtn;
    private RecyclerView comment_list;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;
    private List<User> userList;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String blog_post_id;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coments);

        commentToolbar = findViewById(R.id.comment_toolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        currentUserId = firebaseAuth.getCurrentUser().getUid();
        blog_post_id = getIntent().getStringExtra("blogPostId");


        commentField = findViewById(R.id.comment_field);
        commentPostBtn = findViewById(R.id.comment_post_btn);
        comment_list = findViewById(R.id.comment_list);

        //RecycleView Comments List
        commentsList = new ArrayList<>();
        userList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList, userList);
        comment_list.setHasFixedSize(true);
        comment_list.setLayoutManager(new LinearLayoutManager(this));
        comment_list.setAdapter(commentsRecyclerAdapter);

        Query query = firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments/").orderBy("timestamp" , Query.Direction.DESCENDING);
        query.addSnapshotListener(CommentActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if(queryDocumentSnapshots != null) {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                //String commentId = doc.getDocument().getId();
                                final Comments comments = doc.getDocument().toObject(Comments.class);

                                String userId = doc.getDocument().getString("user_id");
                                firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {

                                            User user = task.getResult().toObject(User.class);

                                            userList.add(user);
                                            commentsList.add(comments);
                                            commentsRecyclerAdapter.notifyDataSetChanged();

                                        }
                                    }
                                });
                            }
                        }
                    }

                }

            }
        });


        commentPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String comment_message = commentField.getText().toString();

                if(!comment_message.isEmpty()){

                    Map<String,Object> commentMap = new HashMap<>();
                    commentMap.put("message",comment_message);
                    commentMap.put("user_id",currentUserId);
                    commentMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").add(commentMap)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            if(!task.isSuccessful()){

                                Toast.makeText(CommentActivity.this,"Error :" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                            }else{

                                commentField.setText("");
                            }

                        }
                    });

                }

            }
        });


    }


}
