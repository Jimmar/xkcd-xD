package jimmar.net.xkcdxd;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import jimmar.net.xkcdxd.classes.Strip;
import jimmar.net.xkcdxd.helpers.connectionClient;


public class comicPageFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    WebView wv;
    Strip currentStrip;
    Strip latestStrip;
    boolean touchSate = false;

    private float MOVE_THRESHOLD_DP;
    private float mDownPosX;
    private float mDownPosY;

    TextView comicNumber;

    CheckBox favoriteBtn;
    ImageButton prevComicBtn;
    ImageButton nextComicBtn;
    ImageButton latestComicBtn;

    public comicPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MOVE_THRESHOLD_DP = 20 * getActivity().getResources().getDisplayMetrics().density;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        wv = (WebView) rootView.findViewById(R.id.webView);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setDisplayZoomControls(false);
        wv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        if (touchSate) {
                            touchSate = false;
                            break;
                        }
                    case MotionEvent.ACTION_DOWN:
                        touchSate = true;
                        mDownPosX = event.getX();
                        mDownPosY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (touchSate) {
//                            Toast.makeText(getActivity(), currentStrip.getAlt(), Toast.LENGTH_LONG).show();
                            showAltText();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getX() - mDownPosX) > MOVE_THRESHOLD_DP || Math.abs(event.getY() - mDownPosY) > MOVE_THRESHOLD_DP)
                            touchSate = false;
                        break;
                }
                return false;
            }
        });
        fetchComic();
        comicNumber = (TextView) rootView.findViewById(R.id.comic_number);
        comicNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumberPicker();
            }
        });

        favoriteBtn = (CheckBox) rootView.findViewById(R.id.favorite_toggle);
        prevComicBtn = (ImageButton) rootView.findViewById(R.id.prev_comic_btn);
        nextComicBtn = (ImageButton) rootView.findViewById(R.id.next_comic_btn);
        latestComicBtn = (ImageButton) rootView.findViewById(R.id.latest_comic_btn);

        favoriteBtn.setOnCheckedChangeListener(this);
        prevComicBtn.setOnClickListener(this);
        nextComicBtn.setOnClickListener(this);
        latestComicBtn.setOnClickListener(this);
        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void fetchComic(final int number) {
        final String POST_NUMBER = number == -1 ? "" : Integer.toString(number) + "/";
        connectionClient.get(POST_NUMBER, null, new JsonHttpResponseHandler() {
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                super.onSuccess(statusCode, headers, response);
                Strip strip = new Strip((response));
                if (number == -1)
                    latestStrip = strip;
                displayComic(strip);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(getActivity(),
                        getString(R.string.toast_connection_failed),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(getActivity(),
                        getString(R.string.toast_connection_failed),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(getActivity(),
                        getString(R.string.toast_connection_failed),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinish() {

                super.onFinish();
            }
        });

    }

    public void fetchComic() {
        fetchComic(-1);
    }

    public void displayComic(Strip comic) {
        currentStrip = comic;
        wv.loadUrl(comic.getImage_url().toString());
        comicNumber.setText(Integer.toString(comic.getNum()));
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(comic.getSafe_title());
    }

    public void showNumberPicker() {
        if (latestStrip == null)
            return;
        final Dialog d = new Dialog(getActivity());
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.number_picker_dialog);
        Button b1 = (Button) d.findViewById(R.id.submit_btn);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(latestStrip.getNum());
        np.setMinValue(1);
        np.setValue(currentStrip.getNum());
        np.setWrapSelectorWheel(true);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comicNumber.setText(String.valueOf(np.getValue()));
                fetchComic(np.getValue());
                d.dismiss();
            }
        });
        d.show();
    }

    public void showAltText() {
        if (currentStrip == null)
            return;
        final Dialog d = new Dialog(getActivity());
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.alt_text_dialog);
        TextView tv = (TextView) d.findViewById(R.id.alt_text);
        tv.setText(currentStrip.getAlt());
        Button showMoreBtn = (Button) d.findViewById(R.id.show_more_btn);
        if (currentStrip.getLink().length() < 3)
            showMoreBtn.setVisibility(View.GONE);
        else {
            showMoreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentStrip.getLink()));
                    startActivity(browserIntent);
                    d.dismiss();
                }
            });
        }
        d.show();
    }

    @Override
    public void onClick(View v) {
        if (v == prevComicBtn) {
            fetchComic(currentStrip.getNum() - 1);
        } else if (v == nextComicBtn) {
            if (currentStrip.getNum() < latestStrip.getNum())
                fetchComic(currentStrip.getNum() + 1);
        } else if (v == latestComicBtn) {
            fetchComic();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //TODO save as favorite/remove from favorite
    }
}
