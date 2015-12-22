package thesos.com.sos.badboy.thesos;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONObject;


public class UserView extends Fragment {

    private static final String USER_ID = "userId";

    private String userId;

    private OnFragmentInteractionListener mListener;
    private ImageView userphoto;
    private android.widget.RelativeLayout relativeLayout2;
    private android.widget.TextView usertelno;
    private android.widget.TextView useremail;
    private TextView username;
    private String facebookId;
    private android.widget.ProgressBar userprogressbar;


    public static UserView newInstance(String userId) {
        UserView fragment = new UserView();
        Bundle args = new Bundle();
        args.putString(USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    public UserView() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(USER_ID);
            Log.d(TheSosApplication.TAG, "UserView Create UserId: " + userId);
        }
        loadUserData();
    }

    private void loadUserData() {
        if (this.userId != null) {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("objectId", this.userId);
            query.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    if (parseUser != null) {
                        try {
                            Log.d(TheSosApplication.TAG, "parse user = " + parseUser.get("name"));

                            JSONObject profile = parseUser.getJSONObject("profile");


                            facebookId = profile.getString("facebookId");
                            username.setText(parseUser.getString("name"));
                            useremail.setText(profile.getString("email"));
                            if (parseUser.getString("telephone") != null) {
                                usertelno.setText(parseUser.getString("telephone"));
                            }

                            //ดาวน์โหลดข้อมูลจาก Facebook Profile
                            if (facebookId != null) {
                                Glide.with(getActivity())
                                        .load(Helper.getFacebookProfileUrl(facebookId))
                                        .asBitmap()
                                        .centerCrop()
                                        .error(R.drawable.no_photo_grey)
                                        .into(new BitmapImageViewTarget(userphoto) {
                                            @Override
                                            protected void setResource(Bitmap resource) {
                                                RoundedBitmapDrawable circularBitmapDrawable =
                                                        RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                                                circularBitmapDrawable.setCircular(true);
                                                userphoto.setImageDrawable(circularBitmapDrawable);
                                            }
                                        });
                            }
                            userprogressbar.setVisibility(View.GONE);

                        } catch (Exception e1) {
                            Log.d(TheSosApplication.TAG, "Error to get UserView 0x111111");

                            e1.printStackTrace();
                        }
                    } else {
                        Log.d(TheSosApplication.TAG, "Error to get User ");
                    }
                }
            });

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_view, container, false);
        this.userprogressbar = (ProgressBar) view.findViewById(R.id.user_progressbar);
        this.relativeLayout2 = (RelativeLayout) view.findViewById(R.id.relativeLayout2);
        this.username = (TextView) view.findViewById(R.id.user_name);
        this.useremail = (TextView) view.findViewById(R.id.user_email);
        this.usertelno = (TextView) view.findViewById(R.id.user_tel_no);
        this.userphoto = (ImageView) view.findViewById(R.id.user_photo);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
