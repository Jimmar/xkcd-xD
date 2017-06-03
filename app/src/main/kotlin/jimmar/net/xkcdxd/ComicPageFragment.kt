package jimmar.net.xkcdxd

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
import android.view.*
import android.webkit.WebView
import android.widget.*
import com.nispok.snackbar.Snackbar
import com.nispok.snackbar.SnackbarManager
import com.nispok.snackbar.enums.SnackbarType
import jimmar.net.xkcdxd.classes.Strip
import jimmar.net.xkcdxd.helpers.RestAPI
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * Created by Jimmar on 2/6/17.
 */

class ComicPageFragment : Fragment() {
    lateinit var wv: WebView
    var currentStrip: Strip? = null
    var latestStrip: Strip? = null
    var touchState = false

    val EXPLAIN_URL = "http://www.explainxkcd.com/wiki/index.php/"

    private var MOVE_THRESHOLD_DP: Float = 0.toFloat()
    private var mDownPosX: Float = 0.toFloat()
    private var mDownPosY: Float = 0.toFloat()

    lateinit var comicNumber: TextView

    lateinit var favoriteBtn: CheckBox
    lateinit var prevComicBtn: ImageButton
    lateinit var nextComicBtn: ImageButton
    lateinit var latestComicBtn: ImageButton

    lateinit var reloadDialog: Dialog
    internal var bitmap: Bitmap? = null

    internal var isShare = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MOVE_THRESHOLD_DP = 20 * activity.resources.displayMetrics.density
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_main, container, false)
        wv = rootView.findViewById(R.id.webView) as WebView
        wv.settings.builtInZoomControls = true
        wv.settings.displayZoomControls = false

        //TODO: redo this into a better looking code
        wv.setOnTouchListener { v, event ->
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    touchState = true
                    mDownPosX = event.x
                    mDownPosY = event.y
                }
                MotionEvent.ACTION_UP -> if (touchState) {
                    showAltText()
                }
                MotionEvent.ACTION_MOVE -> if (Math.abs(event.x - mDownPosX) > MOVE_THRESHOLD_DP || Math.abs(event.y - mDownPosY) > MOVE_THRESHOLD_DP)
                    touchState = false
            }
            false
        }

        comicNumber = rootView.findViewById(R.id.comic_number) as TextView
        comicNumber.setOnClickListener { showNumberPicker() }

        favoriteBtn = rootView.findViewById(R.id.favorite_toggle) as CheckBox
        prevComicBtn = rootView.findViewById(R.id.prev_comic_btn) as ImageButton
        nextComicBtn = rootView.findViewById(R.id.next_comic_btn) as ImageButton
        latestComicBtn = rootView.findViewById(R.id.latest_comic_btn) as ImageButton

        favoriteBtn.setOnClickListener { saveFavorite(favoriteBtn.isChecked) }
        prevComicBtn.setOnClickListener { fetchComic(currentStrip!!.num - 1) }
        nextComicBtn.setOnClickListener { fetchComic(currentStrip!!.num + 1) }
        latestComicBtn.setOnClickListener { fetchComic(-1) }

        reloadDialog = Dialog(activity)
        reloadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        reloadDialog.setContentView(R.layout.dialog_loading)
        reloadDialog.setCancelable(false)

        var num = -1
        if (arguments != null)
            num = arguments.getInt("comicNumber", 0)

        fetchComic(num)

        return rootView
    }

    fun fetchComic(number: Int) {
        if (number > latestStrip?.num ?: Int.MAX_VALUE) {
            SnackbarManager.show(Snackbar.with(activity)
                    .text(getString(R.string.toast_latest_comic))
                    .duration(Snackbar.SnackbarDuration.LENGTH_SHORT))
            return
        }
        wv.loadUrl("about:blank")
        wv.clearHistory()

        val restApi: RestAPI = RestAPI(onSuccess = { displayComic(it) }, onFailure = { fetchingComicFailed() })
        reloadDialog.show()
        if (number == -1) {
            //TODO save latest comic in latestComic
            restApi.getLatestStrip().enqueue(restApi)
        } else {
            restApi.getStrip(number).enqueue(restApi)
        }
    }

    fun displayComic(comic: Strip) {
        currentStrip = comic
        //if latest strip is null then this is the first strip
        if (latestStrip == null)
            latestStrip = currentStrip

        if (comic.link.length > 3)
            SnackbarManager.show(Snackbar.with(activity)
                    .text(getString(R.string.toast_full_version_available))
                    .type(SnackbarType.MULTI_LINE))
        wv.loadUrl(comic.img)
        comicNumber.text = comic.num.toString()
        (activity as MainActivity).setMTitle(comic.safe_title)
        (activity as MainActivity).restoreActionBar()
        favoriteBtn.isChecked = MainActivity.Companion.favorites.binarySearch(
                "${currentStrip!!.num} - ${currentStrip!!.safe_title}") >= 0
        reloadDialog.dismiss()
    }

    fun fetchingComicFailed() {
        SnackbarManager.show(
                Snackbar.with(activity)
                        .text(getString(R.string.toast_connection_failed))
                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT))
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.comic_page, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id) {
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

    fun showNumberPicker() {
        if (latestStrip == null)
            return
        val d = Dialog(activity)
        d.setTitle(getString(R.string.number_picker_title))
        d.setContentView(R.layout.dialog_number_picker)
        val b1 = d.findViewById((R.id.submit_btn)) as Button
        val np = d.findViewById(R.id.numberPicker1) as NumberPicker
        np.maxValue = latestStrip!!.num
        np.minValue = 1
        np.value = currentStrip!!.num
        np.wrapSelectorWheel = true
        b1.setOnClickListener {
            comicNumber.text = np.value.toString()
            fetchComic(np.value)
            d.dismiss()
        }
        d.show()
    }

    fun showAltText() {
        if (currentStrip == null)
            return
        val d = Dialog(activity)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d.setContentView(R.layout.dialog_alt_text)
        val tv = d.findViewById(R.id.alt_text) as TextView
        tv.text = currentStrip!!.alt
        val showMoreBtn = d.findViewById(R.id.show_more_btn) as Button
        val explainedButton = d.findViewById(R.id.explained_btn) as Button

        showMoreBtn.setOnClickListener { openLinkAndDismissDialog(currentStrip!!.link, d) }
        explainedButton.setOnClickListener {
            openLinkAndDismissDialog(EXPLAIN_URL + currentStrip!!.num, d)
        }

        if (currentStrip!!.link.length < 3) showMoreBtn.visibility = View.GONE

        d.show()
    }


    fun openLinkAndDismissDialog(link: String, dialog: Dialog?) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(browserIntent)
        dialog?.dismiss()
    }


    fun saveFavorite(isChecked: Boolean) {
        val favName = "${currentStrip!!.num} - ${currentStrip!!.safe_title}"
        if (isChecked)
            MainActivity.Companion.favorites.add(favName)
        else {
            val index = MainActivity.Companion.favorites.binarySearch(favName)
            MainActivity.Companion.favorites.removeAt(index)
        }
        MainActivity.Companion.favorites.sort()
    }

    fun shareCurrentComicAsLink() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "${getString(R.string.share_comic_link)}\nhttp://xkcd.com/${currentStrip!!.num}")
        sendIntent.type = "text/plain"
        val openInChooser = Intent.createChooser(sendIntent, "Share on...")
        startActivity(openInChooser)
    }

    fun shareCurrentComicAsPicture() {
        val outputDir = activity.externalCacheDir
        val outputFile = File.createTempFile("shareTemp", "png", outputDir)
        saveBitmapToDisk(bitmap, outputFile.absolutePath)

        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText("Alt text", currentStrip!!.alt)

        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/jpeg"
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(outputFile.absolutePath))
        Toast.makeText(activity, getString(R.string.toast_alt_text_copied),
                Toast.LENGTH_SHORT).show()
        startActivity(Intent.createChooser(share, "Share Image"))
    }

    fun saveBitmapToDisk(bmp: Bitmap?, fileDestinationPath: String): String {
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

        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(imageFile.absolutePath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        activity.sendBroadcast(mediaScanIntent)
        reloadDialog.dismiss()
        SnackbarManager.show(
                Snackbar.with(activity)
                        .text("saved image " + imageFileName + " to " + storageDir.absolutePath)
                        .type(SnackbarType.MULTI_LINE)
                        .duration(Snackbar.SnackbarDuration.LENGTH_LONG))
        bitmap = null
    }

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
