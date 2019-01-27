package pl.pwr.blogapp;


import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView photoListView;
    private List<BlogPost> blog_list;
    private List<User> userList;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        photoListView = view.findViewById(R.id.photo_post_view);
        blog_list = new ArrayList<>();
        userList = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list, userList);
        photoListView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        photoListView.setAdapter(blogRecyclerAdapter);
        firebaseAuth = FirebaseAuth.getInstance();
        photoListView.setHasFixedSize(true);

        if(firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            photoListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled( RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){

                        loadMorePost();

                    }

                }
            });


            Query firstQuery = firebaseFirestore.collection( "Posts").orderBy("timestamp" , Query.Direction.DESCENDING).limit(3);
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent( QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                    if(queryDocumentSnapshots != null) {
                        if (!queryDocumentSnapshots.isEmpty()) {

                            if (isFirstPageFirstLoad) {

                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                                userList.clear();

                            }

                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {


                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String blogPostId = doc.getDocument().getId();
                                    final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                    String blogUserId = doc.getDocument().getString("user_id");
                                    firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if(task.isSuccessful()){

                                                User user = task.getResult().toObject(User.class);

                                                if (isFirstPageFirstLoad) {

                                                    userList.add(user);
                                                    blog_list.add(blogPost);

                                                } else {

                                                    userList.add(0, user);
                                                    blog_list.add(0, blogPost);
                                                }

                                                blogRecyclerAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                }
                            }

                            isFirstPageFirstLoad = false;
                        }

                    }
                }
            });
        }

        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePost(){

        if(firebaseAuth.getCurrentUser() != null) {

            Query nextQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);
            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                    if (queryDocumentSnapshots != null) {
                        if (!queryDocumentSnapshots.isEmpty()) {

                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String blogPostId = doc.getDocument().getId();
                                    final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    String blogUserId = doc.getDocument().getString("user_id");

                                    firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if(task.isSuccessful()){

                                                User user = task.getResult().toObject(User.class);

                                                userList.add(user);
                                                blog_list.add(blogPost);

                                                blogRecyclerAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });

                                }
                            }
                        }
                    }
                }
            });
        }
    }
}
