package pl.pwr.blogapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    public List<Comments> commentsList;
    public List<User> userList;

    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public CommentsRecyclerAdapter(List<Comments> commentsList, List<User> userList){

        this.commentsList = commentsList;
        this.userList = userList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment_list_item, viewGroup, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        context = viewGroup.getContext();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        viewHolder.setIsRecyclable(false);

        final String commentMessage = commentsList.get(i).getMessage();
        viewHolder.setComment_message(commentMessage);

        //Time comments
        try {
            long millisecond = commentsList.get(i).getTimestamp().getTime();
            String dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(millisecond);
            viewHolder.setTime(dateString);
        }catch (Exception e){

            //Error message (null object)
            //Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        //User data
        String userName = userList.get(i).getName();
        String userImage = userList.get(i).getImage();

        viewHolder.setUserData(userName, userImage);

    }


    @Override
    public int getItemCount() {

        if(commentsList != null) {

            return commentsList.size();

        }else{

            return 0;

        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView comment_message;
        private TextView commentUser;
        private CircleImageView commentImage;
        private TextView commentDate;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment_message(String message){

            comment_message = mView.findViewById(R.id.comment_message);
            comment_message.setText(message);

        }
        public void setUserData(String userName, String userImage){

            commentImage = mView.findViewById(R.id.comment_image);
            commentUser = mView.findViewById(R.id.comment_username);

            commentUser.setText(userName);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.image_user_image_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(userImage).into(commentImage);
        }

        public void setTime(String date){

            commentDate = mView.findViewById(R.id.comment_date);
            commentDate.setText(date);

        }

    }
}
