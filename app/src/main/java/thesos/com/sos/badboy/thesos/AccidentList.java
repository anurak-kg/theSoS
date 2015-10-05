package thesos.com.sos.badboy.thesos;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.ArrayList;

public class AccidentList extends ArrayAdapter<Accident> {

    private final Context context;
    private final ArrayList<Accident> itemsArrayList;

    public AccidentList(Context context, ArrayList<Accident> itemsArrayList) {

        super(context, R.layout.listviewsingle, itemsArrayList);

        this.context = context;
        this.itemsArrayList = itemsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.listviewsingle, parent, false);

        TextView listAccidentTypeTxt = (TextView) rowView.findViewById(R.id.listAccidentTypeTxt);

        Accident accident = itemsArrayList.get(position);
        listAccidentTypeTxt.setText(accident.getAccidentType());


        return rowView;
    }
}