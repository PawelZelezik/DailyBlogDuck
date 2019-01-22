package pl.pwr.blogapp;


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
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        photoListView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        photoListView.setAdapter(blogRecyclerAdapter);
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            photoListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){

                        String desc = lastVisible.getString("desc");
                        Toast.makeText(container.getContext(), "Reached : " + desc, Toast.LENGTH_SHORT);
                        loadMorePost();

                    }else{
                        Toast.makeText(container.getContext(), "Reached : ??? ", Toast.LENGTH_SHORT);
                    }

                }
            });


            Query firstQuery = firebaseFirestore.collection( "Posts").orderBy("timestamp" , Query.Direction.DESCENDING).limit(3);
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent( QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        if(isFirstPageFirstLoad) {

                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                        }
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);

                            if(isFirstPageFirstLoad) {

                                blog_list.add(blogPost);

                            }else {
                                blog_list.add(0, blogPost);
                            }

                            blogRecyclerAdapter.notifyDataSetChanged();

                        }
                    }

                    isFirstPageFirstLoad = false;
                }
            });
        }

        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePost(){
        Query nextQuery = firebaseFirestore.collection( "Posts")
                .orderBy("timestamp" , Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);
        nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent( QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                if(!queryDocumentSnapshots.isEmpty()) {


                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                            blog_list.add(blogPost);

                            blogRecyclerAdapter.notifyDataSetChanged();

                        }
                    }
                }
            }
        });
    }
}
