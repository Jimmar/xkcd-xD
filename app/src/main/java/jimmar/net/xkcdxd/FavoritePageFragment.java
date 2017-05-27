package jimmar.net.xkcdxd;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jimmar on 1/13/15.
 */
public class FavoritePageFragment extends ListFragment {

    ArrayAdapter<String> adapter;
    //TODO create a class for the favorites

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorite, container, false);


        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, MainActivity.Companion.getFavorites());
        Log.d("xkcdxd", "Size is " + MainActivity.Companion.getFavorites().size());
//        adapter = new FavAdapter(getActivity(),R.layout.fav_row ,MainActivity.favorites);
        getListView().setAdapter(adapter);

        if (adapter.getCount() > 0)
            getView().findViewById(R.id.no_favorites_tv).setVisibility(View.GONE);
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setMTitle("Favorites");
            ((MainActivity) getActivity()).restoreActionBar();
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Fragment fragment = new ComicPageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("comicNumber", Integer.parseInt(MainActivity.Companion.getFavorites().get(position).toString().split("-")[0].trim()));
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).switchContent(fragment);
    }

    public class FavAdapter extends ArrayAdapter<Integer> implements View.OnClickListener {

        public FavAdapter(Context context, int resourceId, List<Integer> favorites) {
            super(context, resourceId, favorites);

        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.fav_row, parent, false);
            //TODO add a click listener for the favorite icon star
            final CheckBox fav_star = (CheckBox) convertView.findViewById(R.id.favorite_toggle);
            fav_star.setChecked(true);
            final int current_item = getItem(position);

            TextView favorite_number = (TextView) convertView.findViewById(R.id.favorite_number);
            favorite_number.setText(Integer.toString(getItem(position)));

//            fav_star.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //TODO save as favorite/remove from favorite
//                    if (fav_star.isChecked()) {
//                        MainActivity.favorites.add(current_item);
//                        Collections.sort(MainActivity.favorites);
//                    } else {
//                        int index = Collections.binarySearch(MainActivity.favorites, current_item);
//                        MainActivity.favorites.remove(index);
//                        Collections.sort(MainActivity.favorites);
//                    }
//                }
//            });
//            convertView.setOnClickListener(this);
            return convertView;
        }

        @Override
        public void onClick(View v) {
            Log.d("xkcdxd", "view clicked");
        }

    }

}
