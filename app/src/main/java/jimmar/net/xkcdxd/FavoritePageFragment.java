package jimmar.net.xkcdxd;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Jimmar on 1/13/15.
 */
public class FavoritePageFragment extends ListFragment {

    ArrayAdapter<String> adapter;

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
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, (MainActivity.favorites));
        getListView().setAdapter(adapter);
        if (adapter.getCount() > 0)
            getView().findViewById(R.id.no_favorites_tv).setVisibility(View.GONE);

        if (getActivity() != null) {
            ((MainActivity) getActivity()).mTitle = "Favorites";
            ((MainActivity) getActivity()).restoreActionBar();
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Fragment fragment = new ComicPageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("comicNumber", (int) MainActivity.favorites.get(position));
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).switchContent(fragment);
    }
}
