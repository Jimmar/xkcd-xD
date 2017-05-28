package jimmar.net.xkcdxd;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import jimmar.net.xkcdxd.classes.Strip;
import jimmar.net.xkcdxd.helpers.connectionClient;


public class ComicPageFragment extends Fragment implements View.OnClickListener {

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

    Dialog reloadDialog;

    Bitmap bitmap;

    boolean isShare = false;

    public ComicPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MOVE_THRESHOLD_DP = 20 * getActivity().getResources().getDisplayMetrics().density;
        setHasOptionsMenu(true);
        Log.d("xkcdxd", "comic page fragment created");
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

        favoriteBtn.setOnClickListener(this);
        prevComicBtn.setOnClickListener(this);
        nextComicBtn.setOnClickListener(this);
        latestComicBtn.setOnClickListener(this);

        reloadDialog = new Dialog(getActivity());
        reloadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        reloadDialog.setContentView(R.layout.dialog_loading);
        reloadDialog.setCancelable(false);
        int num = 0;
        try {
            num = getArguments().getInt("comicNumber");
        } catch (Exception e) {

        }

        if (num == 0)
            fetchComic();
        else
            fetchComic(num);
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
        wv.loadUrl("about:blank");
        wv.clearHistory();
        connectionClient.get(POST_NUMBER, null, new JsonHttpResponseHandler() {
            public void onStart() {
                reloadDialog.show();
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Gson gson = new Gson();
                Strip strip = gson.fromJson(response.toString(), Strip.class);
                if (strip == null)
                    Log.d("xkcdxd", "strip is null");
                if (number == -1)
                    latestStrip = strip;
                displayComic(strip);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
//                Toast.makeText(getActivity(),
//                        getString(R.string.toast_connection_failed),
//                        Toast.LENGTH_LONG).show();
                SnackbarManager.show(
                        Snackbar.with(getActivity())
                                .text(getString(R.string.toast_connection_failed)).duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
//                Toast.makeText(getActivity(),
//                        getString(R.string.toast_connection_failed),
//                        Toast.LENGTH_LONG).show();
                SnackbarManager.show(
                        Snackbar.with(getActivity())
                                .text(getString(R.string.toast_connection_failed)).duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
//                Toast.makeText(getActivity(),
//                        getString(R.string.toast_connection_failed),
//                        Toast.LENGTH_LONG).show();
                SnackbarManager.show(
                        Snackbar.with(getActivity())
                                .text(getString(R.string.toast_connection_failed)).duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE));
            }

            @Override
            public void onFinish() {
                reloadDialog.dismiss();
                super.onFinish();
            }
        });

    }

    public void fetchComic() {
        fetchComic(-1);
    }

    public void displayComic(Strip comic) {
        currentStrip = comic;

        if (comic.getLink().length() > 3)
//            Toast.makeText(getActivity(), getString(R.string.toast_full_version_available), Toast.LENGTH_SHORT).show();
            SnackbarManager.show(
                    Snackbar.with(getActivity())
                            .text(getString(R.string.toast_full_version_available)).type(SnackbarType.MULTI_LINE).duration(Snackbar.SnackbarDuration.LENGTH_LONG));
        wv.loadUrl(comic.getImg());
        comicNumber.setText(Integer.toString(comic.getNum()));
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setMTitle(comic.getSafe_title());
            ((MainActivity) getActivity()).restoreActionBar();
        }

        if (Collections.binarySearch(MainActivity.Companion.getFavorites(), currentStrip.getNum() + " - " + currentStrip.getSafe_title()) >= 0) {
            favoriteBtn.setChecked(true);
        } else {
            favoriteBtn.setChecked(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        if (!((MainActivity) getActivity()).mNavigationDrawerFragment.isDrawerOpen())
        inflater.inflate(R.menu.comic_page, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share_pic:
                isShare = true;
                new FetchImage().execute();
                break;
            case R.id.action_share_link:
                shareCurrentComicAsLink();
                break;
            case R.id.action_refresh:
                if (currentStrip == null)
                    fetchComic();
                else
                    fetchComic(currentStrip.getNum());
                break;
            case R.id.action_save:
                isShare = false;
                new FetchImage().execute();
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void showNumberPicker() {
        if (latestStrip == null)
            return;
        final Dialog d = new Dialog(getActivity());
        d.setTitle(getString(R.string.number_picker_title));
        d.setContentView(R.layout.dialog_number_picker);
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
        d.setContentView(R.layout.dialog_alt_text);
        TextView tv = (TextView) d.findViewById(R.id.alt_text);
        tv.setText(currentStrip.getAlt());
        Button showMoreBtn = (Button) d.findViewById(R.id.show_more_btn);
        Button explainedButton = (Button) d.findViewById(R.id.explained_btn);
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
        explainedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String explained_link = String.format("http://www.explainxkcd.com/wiki/index.php/%s", Integer.toString(currentStrip.getNum()));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(explained_link));
                startActivity(browserIntent);
                d.dismiss();
            }
        });
        d.show();
    }

    @Override
    public void onClick(View v) {
        if (v == prevComicBtn) {
            fetchComic(currentStrip.getNum() - 1);
        } else if (v == nextComicBtn) {
            if (currentStrip.getNum() < latestStrip.getNum())
                fetchComic(currentStrip.getNum() + 1);
            else
//                Toast.makeText(getActivity(), getString(R.string.toast_latest_comic), Toast.LENGTH_SHORT).show();
                SnackbarManager.show(
                        Snackbar.with(getActivity())
                                .text(getString(R.string.toast_latest_comic)).duration(Snackbar.SnackbarDuration.LENGTH_SHORT));

        } else if (v == latestComicBtn) {
            fetchComic();
        } else if (v == favoriteBtn) {
            saveFavorite(favoriteBtn.isChecked());
        }
    }

    public void saveFavorite(boolean isChecked) {
        //TODO save as favorite/remove from favorite
        if (isChecked) {
            MainActivity.Companion.getFavorites().add(currentStrip.getNum() + " - " + currentStrip.getSafe_title());
            Collections.sort(MainActivity.Companion.getFavorites());
        } else {
            int index = Collections.binarySearch(MainActivity.Companion.getFavorites(), currentStrip.getNum() + " - " + currentStrip.getSafe_title());
            MainActivity.Companion.getFavorites().remove(index);
            Collections.sort(MainActivity.Companion.getFavorites());
        }
    }

    public void shareCurrentComicAsLink() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_comic_link) + "\n" + "http://xkcd.com/" + currentStrip.getNum());
        sendIntent.setType("text/plain");
        Intent openInChooser = Intent.createChooser(sendIntent, "Share on...");
        startActivity(openInChooser);
    }

    /**
     * shares a comic as a picture, gets called after fetch Image
     */
    public void shareCurrentComicAsPicture() {
        //TODO save current comic to disk and share
        File outputDir = getActivity().getExternalCacheDir(); // context being the Activity pointer
        File outputFile = null;
        try {
            outputFile = File.createTempFile("shareTemp", "png", outputDir);
            saveBitmapToDisk(bitmap, outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //copies alt text to the clipboard
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Alt text", currentStrip.getAlt());
        clipboard.setPrimaryClip(clip);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(outputFile.getAbsolutePath()));
        Toast.makeText(getActivity(), getString(R.string.toast_alt_text_copied), Toast.LENGTH_SHORT).show();
        startActivity(Intent.createChooser(share, "Share Image"));
    }

    /**
     * saves a Bitmap to disk
     *
     * @param bmp                 the to be saved
     * @param fileDestinationPath the full path destination including file name
     * @return the path to the saved file, or null if unable to save
     */

    public String saveBitmapToDisk(Bitmap bmp, String fileDestinationPath) {
        String path = "";

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fileDestinationPath);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            path = fileDestinationPath;
        } catch (Exception e) {
            e.printStackTrace();
            path = null;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path;
    }


    public void saveComicBitmapToDisk() {
        String imageFileName = "xkcd_" + currentStrip.getNum() + ".png";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getString(R.string.app_name));
        if (!storageDir.exists())
            storageDir.mkdirs();

        File imageFile = new File(storageDir.getAbsolutePath() + "/" + imageFileName);
        if (imageFile.exists()) {
//            Toast.makeText(getActivity(), getString(R.string.toast_comic_already_downloaded), Toast.LENGTH_SHORT).show();
            SnackbarManager.show(
                    Snackbar.with(getActivity())
                            .text(getString(R.string.toast_comic_already_downloaded))
                            .duration(Snackbar.SnackbarDuration.LENGTH_SHORT));
            reloadDialog.dismiss();
            return;
        }

        saveBitmapToDisk(bitmap, imageFile.getAbsolutePath());

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imageFile.getAbsolutePath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);

        reloadDialog.dismiss();
//        Toast.makeText(getActivity(), "saved image " + imageFileName + " to " + storageDir.getAbsolutePath(), Toast.LENGTH_LONG).show();
        SnackbarManager.show(
                Snackbar.with(getActivity())
                        .text("saved image " + imageFileName + " to " + storageDir.getAbsolutePath())
                        .type(SnackbarType.MULTI_LINE).duration(Snackbar.SnackbarDuration.LENGTH_LONG));
        bitmap = null;
    }

    /**
     * fetches image and saves it in bitmap
     */
    private class FetchImage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL postMediaUrl = new URL(currentStrip.getImg());
                InputStream in = postMediaUrl.openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (MalformedURLException e) {
                Log.e("xkcdxd", "Failed in creating URL");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("xkcdxd", "Failed in Decoding Bitmap");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("xkcdxd", "Unknown exception");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            reloadDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (isShare)
                shareCurrentComicAsPicture();
            else
                saveComicBitmapToDisk();
            reloadDialog.dismiss();
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onCancelled(Void aVoid) {
            reloadDialog.dismiss();
            super.onCancelled(aVoid);
        }
    }

}

