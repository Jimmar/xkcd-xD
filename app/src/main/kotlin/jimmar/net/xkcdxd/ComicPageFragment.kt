package jimmar.net.xkcdxd

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.nispok.snackbar.Snackbar
import com.nispok.snackbar.SnackbarManager
import com.nispok.snackbar.enums.SnackbarType
import jimmar.net.xkcdxd.classes.Strip
import jimmar.net.xkcdxd.helpers.RestAPI
import kotlinx.android.synthetic.main.dialog_alt_text.*
import kotlinx.android.synthetic.main.dialog_number_picker.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*


/**
 * Created by Jimmar on 2/6/17.
 */

const val EXPLAIN_URL = "http://www.explainxkcd.com/wiki/index.php/"
const val XKCD_URL = "http://xkcd.com/"

class ComicPageFragment : Fragment() {
    var currentStrip: Strip? = null

    private lateinit var wv: WebView
    private var latestStrip: Strip? = null

    private var comicNumber: TextView? = null

    private var favoriteBtn: CheckBox? = null

    lateinit var reloadDialog: Dialog
    internal var bitmap: Bitmap? = null

    internal var isShare = false

    private var clickTime = 0L
    private val clickTimeThreshold = 70

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wv = webView
        with(wv) {
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            setOnTouchListener { v, event ->
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        clickTime = Calendar.getInstance().timeInMillis
                    }
                    MotionEvent.ACTION_UP -> {
                        if (Calendar.getInstance().timeInMillis - clickTime < clickTimeThreshold) showAltText()
                    }
                }
                v?.onTouchEvent(event) ?: true
            }
        }

        comicNumber = comic_number
        comicNumber?.setOnClickListener { showNumberPicker() }
        favoriteBtn = favorite_toggle
        favoriteBtn?.setOnClickListener { saveFavorite(favoriteBtn?.isChecked) }
        prev_comic_btn?.setOnClickListener { fetchComic(currentStrip!!.num - 1) }
        next_comic_btn?.setOnClickListener { fetchComic(currentStrip!!.num + 1) }
        latest_comic_btn?.setOnClickListener { fetchComic(-1) }

        reloadDialog = Dialog(activity!!).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_loading)
            setCancelable(false)
        }

        val num = arguments?.getInt("comicNumber", 0) ?: -1
        fetchComic(num)

    }

    private fun fetchComic(number: Int) {
        if (number > latestStrip?.num ?: Int.MAX_VALUE) {
            SnackbarManager.show(Snackbar.with(activity)
                    .text(getString(R.string.toast_latest_comic))
                    .duration(Snackbar.SnackbarDuration.LENGTH_SHORT))
            return
        }
        wv.loadUrl("about:blank")
        wv.clearHistory()

        val restApi = RestAPI(onSuccess = { displayComic(it) }, onFailure = { fetchingComicFailed() })

        reloadDialog.show()

        if (number == -1)
            restApi.getLatestStrip().enqueue(restApi)
        else
            restApi.getStrip(number).enqueue(restApi)
    }

    private fun displayComic(comic: Strip) {
        currentStrip = comic
        //if latest strip is null then this is the first strip
        latestStrip = latestStrip ?: currentStrip

        if (comic.link.length > 3)
            SnackbarManager.show(Snackbar.with(activity)
                    .text(getString(R.string.toast_full_version_available))
                    .type(SnackbarType.MULTI_LINE))

        wv.loadUrl(comic.img)

        comicNumber?.text = comic.num.toString()

        (activity as MainActivity).setMTitle(comic.safe_title)
        (activity as MainActivity).restoreActionBar()

        favoriteBtn?.isChecked = MainActivity.favorites.binarySearch(
                "${currentStrip!!.num} - ${currentStrip!!.safe_title}") >= 0

        reloadDialog.dismiss()
    }

    private fun fetchingComicFailed() {
        SnackbarManager.show(
                Snackbar.with(activity)
                        .text(getString(R.string.toast_connection_failed))
                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT))
        reloadDialog.dismiss()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.comic_page, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_share_pic -> {
                isShare = true
                FetchImage().execute()
            }
            R.id.action_share_link -> shareCurrentComicAsLink()
            R.id.action_refresh -> if (currentStrip == null) fetchComic(-1) else fetchComic(currentStrip!!.num)
            R.id.action_save -> {
                isShare = false
                FetchImage().execute()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showNumberPicker() {
        if (latestStrip == null)
            return
        Dialog(activity!!).apply {
            setTitle(getString(R.string.number_picker_title))
            setContentView(R.layout.dialog_number_picker)
            val np = numberPicker1
            with(np) {
                maxValue = latestStrip!!.num
                minValue = 1
                value = currentStrip!!.num
                wrapSelectorWheel = true
            }

            submit_btn.setOnClickListener {
                comicNumber?.text = np.value.toString()
                fetchComic(np.value)
                dismiss()
            }
            show()
        }
    }

    private fun showAltText() {
        if (currentStrip == null)
            return
        Dialog(activity!!).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_alt_text)
            alt_text.text = currentStrip!!.alt

            show_more_btn.setOnClickListener { openLinkAndDismissDialog(XKCD_URL + currentStrip!!.num, this) }
            explained_btn.setOnClickListener { openLinkAndDismissDialog(EXPLAIN_URL + currentStrip!!.num, this) }

            if (currentStrip!!.link.length < 3) show_more_btn.visibility = View.GONE

            show()
        }
    }


    private fun openLinkAndDismissDialog(link: String, dialog: Dialog?) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(browserIntent)
        dialog?.dismiss()
    }


    private fun saveFavorite(isChecked: Boolean?) {
        if (isChecked == null) {
            Log.e("xkcdxd", "error, is checked in null")
            return
        }
        val favName = "${currentStrip!!.num} - ${currentStrip!!.safe_title}"
        if (isChecked)
            MainActivity.favorites.add(favName)
        else {
            val index = MainActivity.favorites.binarySearch(favName)
            MainActivity.favorites.removeAt(index)
        }
        MainActivity.favorites.sort()
    }

    private fun shareCurrentComicAsLink() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "${getString(R.string.share_comic_link)}\n$XKCD_URL${currentStrip!!.num}")
        sendIntent.type = "text/plain"
        val openInChooser = Intent.createChooser(sendIntent, "Share on...")
        startActivity(openInChooser)
    }

    //TODO fix this, doesn't seem to work anymore
    private fun shareCurrentComicAsPicture() {
        val outputDir = activity!!.externalCacheDir
        val outputFile = File.createTempFile("shareTemp", "png", outputDir)
        saveBitmapToDisk(bitmap, outputFile.absolutePath)

        val clipboard = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText("Alt text", currentStrip!!.alt)

        val share = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, Uri.parse(outputFile.absolutePath))
        }

        Toast.makeText(activity, getString(R.string.toast_alt_text_copied),
                Toast.LENGTH_SHORT).show()
        startActivity(Intent.createChooser(share, "Share Image"))
    }

    private fun saveBitmapToDisk(bmp: Bitmap?, fileDestinationPath: String): String {
        val out = FileOutputStream(fileDestinationPath)
        bmp?.compress(Bitmap.CompressFormat.PNG, 100, out)

        out.close()
        return fileDestinationPath
    }

    fun saveComicBitmapToDisk() {
        val imageFileName = "xkcd_$currentStrip.num.png"
        val storageDir = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getString(R.string.app_name))

        if (!storageDir.exists())
            storageDir.mkdirs()

        val imageFile = File("${storageDir.absoluteFile}/$imageFileName")

        if (imageFile.exists()) {
            SnackbarManager.show(
                    Snackbar.with(activity)
                            .text(getString(R.string.toast_comic_already_downloaded))
                            .duration(Snackbar.SnackbarDuration.LENGTH_SHORT))
            reloadDialog.dismiss()
            return
        }
        saveBitmapToDisk(bitmap, imageFile.absolutePath)

        val f = File(imageFile.absolutePath)
        val contentUri = Uri.fromFile(f)
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply { data = contentUri }
        activity!!.sendBroadcast(mediaScanIntent)

        reloadDialog.dismiss()
        SnackbarManager.show(
                Snackbar.with(activity)
                        .text("saved image " + imageFileName + " to " + storageDir.absolutePath)
                        .type(SnackbarType.MULTI_LINE)
                        .duration(Snackbar.SnackbarDuration.LENGTH_LONG))
        bitmap = null
    }

    //TODO turn this into private or a standalone class
    inner class FetchImage : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            val postMediaUrl = URL(currentStrip!!.img)
            val inputStream = postMediaUrl.openStream()
            bitmap = BitmapFactory.decodeStream(inputStream)
            return null
        }

        override fun onPreExecute() {
            reloadDialog.show()
            super.onPreExecute()
        }

        override fun onPostExecute(result: Void?) {
            if (isShare) shareCurrentComicAsPicture() else saveComicBitmapToDisk()
            reloadDialog.dismiss()
            super.onPostExecute(result)
        }

        override fun onCancelled() {
            reloadDialog.dismiss()
            super.onCancelled()
        }
    }
}
