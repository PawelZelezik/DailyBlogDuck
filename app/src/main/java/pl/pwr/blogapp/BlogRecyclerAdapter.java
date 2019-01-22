package pl.pwr.blogapp;

import android.content.Context;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public BlogRecyclerAdapter(List<BlogPost> blog_list){

        this.blog_list = blog_list;

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

        String desc_data = blog_list.get(i).getDesc();
        viewHolder.setDescText(desc_data);

        String image_url = blog_list.get(i).getImageUrl();
        viewHolder.setBlogImage(image_url);

        String user_id = blog_list.get(i).getUser_id();
        //User data hire
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");

                    viewHolder.setUserData(userName,userImage);

                }else{

                    //Firebase Exception

                }

            }
        });


        long millisecond = blog_list.get(i).getTimestamp().getTime();
        String dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(millisecond);
        viewHolder.setTime(dateString);

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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setDescText(String descText){

            descView = mView.findViewById(R.id.blog_post_desc);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUrl){

            blogImageView = mView.findViewById(R.id.blog_post_image);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUrl).into(blogImageView);

        }

        public void setTime(String date){

            blogDate = mView.findViewById(R.id.blog_post_date);
            blogDate.setText(date);

        }

        public void setUserData(String userName, String userImage){

            blogUserImage = mView.findViewById(R.id.blog_user_image);
            blogUserName = mView.findViewById(R.id.blog_user_name);

            blogUserName.setText(userName);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.image_user_image_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(userImage).into(blogUserImage);
        }

    }
}
