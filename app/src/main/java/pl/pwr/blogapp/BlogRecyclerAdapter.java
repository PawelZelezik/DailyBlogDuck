package pl.pwr.blogapp;

import android.content.Context;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;


public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public List<User> userList;

    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public BlogRecyclerAdapter(List<BlogPost> blog_list, List<User> userList){

        this.blog_list = blog_list;
        this.userList = userList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_list_item, viewGroup, false);

        context = viewGroup.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        viewHolder.setIsRecyclable(false);


        final String blogPostId = blog_list.get(i).BlogPostId;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String desc_data = blog_list.get(i).getDesc();
        viewHolder.setDescText(desc_data);

        String image_url = blog_list.get(i).getImageUrl();
        String thumbUrl = blog_list.get(i).getThumbUrl();
        viewHolder.setBlogImage(image_url, thumbUrl);

        //User data
        String userName = userList.get(i).getName();
        String userImage = userList.get(i).getImage();

        viewHolder.setUserData(userName, userImage);

        //Time post
        long millisecond = blog_list.get(i).getTimestamp().getTime();
        String dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(millisecond);
        viewHolder.setTime(dateString);

        //Get Likes Count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if(queryDocumentSnapshots != null){

                    if(!queryDocumentSnapshots.isEmpty()){

                        int count = queryDocumentSnapshots.size();
                        viewHolder.updateLikesCount(count);

                    }else{

                        viewHolder.updateLikesCount(0);

                    }
                }


            }
        });

        //Get Likes
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if(documentSnapshot != null) {
                    if (documentSnapshot.exists()) {

                        viewHolder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));

                    } else {

                        viewHolder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_gray));

                    }
                }

            }
        });


        //Set or delete Likes
        viewHolder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {
                            if (!task.getResult().exists()) {

                                Map<String, Object> likesMap = new HashMap<>();
                                likesMap.put("timestamp", FieldValue.serverTimestamp());
                                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);

                            } else {

                                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();
                            }

                        }
                    }
                });
            }
        });

        viewHolder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(context, CommentActivity.class);
                commentIntent.putExtra("blogPostId", blogPostId);
                context.startActivity(commentIntent);
            }
        });

        //Get Comments Count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if(queryDocumentSnapshots != null){

                    if(!queryDocumentSnapshots.isEmpty()){

                        int count = queryDocumentSnapshots.size();
                        viewHolder.updateCommentsCount(count);

                    }else{
                        viewHolder.updateCommentsCount(0);
                    }
                }


            }
        });

    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView descView;
        private ImageView blogImageView;
        private TextView blogDate;
        private TextView blogUserName;
        private CircleImageView blogUserImage;
        private ImageView blogLikeBtn;
        private TextView blogLikeCount;
        private ImageView blogCommentBtn;
        private TextView blogCommentsCount;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);
            blogCommentBtn = mView.findViewById(R.id.blog_comment_btn);


        }

        public void setDescText(String descText){

            descView = mView.findViewById(R.id.blog_post_desc);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUrl, String thumbUrl){


            blogImageView = mView.findViewById(R.id.blog_post_image);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUrl).thumbnail(
                    Glide.with(context).load(thumbUrl)
            ).into(blogImageView);

        }

        public void setTime(String date){

            blogDate = mView.findViewById(R.id.blog_post_date);
            blogDate.setText(date);

        }

        public void setUserData(String userName, String userImage){

            blogUserImage = mView.findViewById(R.id.comment_image);
            blogUserName = mView.findViewById(R.id.blog_user_name);

            blogUserName.setText(userName);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.image_user_image_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(userImage).into(blogUserImage);
        }

        public void updateLikesCount(int count){

            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            String countString = String.valueOf(count);
            if(count <= 1){

                blogLikeCount.setText(countString + " Like");

            }else{

                blogLikeCount.setText(countString + " Likes");

            }
        }

        public void updateCommentsCount(int count) {

            blogCommentsCount = mView.findViewById(R.id.blog_comment_count);
            String countString = String.valueOf(count);
            if(count <= 1){

                blogCommentsCount.setText(countString + " Comment");

            }else{

                blogCommentsCount.setText(countString + " Comments");

            }
        }
    }
}
