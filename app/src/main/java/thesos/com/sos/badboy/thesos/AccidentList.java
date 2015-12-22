package thesos.com.sos.badboy.thesos;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.parse.ParseObject;

import java.util.ArrayList;

public class AccidentList extends ArrayAdapter<Accident> {

    private final Context context;
    private final ArrayList<Accident> itemsArrayList;
    private int lastPosition = -1;

    public AccidentList(Context context, ArrayList<Accident> itemsArrayList) {

        super(context, R.layout.listviewsingle, itemsArrayList);

        this.context = context;
        this.itemsArrayList = itemsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listviewsingle, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //Animation bot and top
        Animation animation = AnimationUtils.loadAnimation(getContext(), (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        convertView.startAnimation(animation);
        lastPosition = position;


        Accident accident = itemsArrayList.get(position);
        viewHolder.listAccidentTypeTxt.setText(accident.getAccidentType());
        viewHolder.accidentListVictim.setText(accident.getVictimName());
        viewHolder.listAccidentLocation.setText(accident.getAddress());
        viewHolder.victimTextView.setText(accident.getVictimName());
        //โหลดรูปภาพ
        Glide.with(this.context)
                .load(accident.getUri())
                .asBitmap()
                .centerCrop()
                .error(R.drawable.no_photo_grey)
                .placeholder(R.drawable.spinner)
                .into(new BitmapImageViewTarget(viewHolder.imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        viewHolder.imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
        return convertView;
    }

    private class ViewHolder {
        public TextView listAccidentTypeTxt;
        public TextView accidentListVictim;
        public TextView listAccidentLocation;
        public TextView victimTextView;
        public ImageView imageView;

        public ViewHolder(View convertView) {
            listAccidentTypeTxt = (TextView) convertView.findViewById(R.id.listAccidentTypeTxt);
            accidentListVictim = (TextView) convertView.findViewById(R.id.accidentListVictim);
            listAccidentLocation = (TextView) convertView.findViewById(R.id.listAccidentLocation);
            imageView = (ImageView) convertView.findViewById(R.id.listAccidentImages);
            victimTextView = (TextView) convertView.findViewById(R.id.victimeNameTextView);
        }
    }
}