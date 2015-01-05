package jimmar.net.xkcdxd.classes;

/**
 * Created by Jimmar on 1/3/15.
 */


import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class Strip {
    String month;
    int num;
    String link;
    String year;
    String news;
    String safe_title;
    String transcript;
    String alt;
    String img;
    URL image_url;
    String title;
    String day;

    public Strip(JSONObject obj) {
        try {
            month = obj.getString("month");
            num = obj.getInt("num");
            link = obj.getString("link");
            year = obj.getString("year");
            news = obj.getString("news");
            safe_title = obj.getString("safe_title");
            transcript = obj.getString("transcript");
            alt = obj.getString("alt");
            img = obj.getString("img");
            image_url = new URL(img);
            title = obj.getString("title");
            day = obj.getString("day");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getMonth() {
        return month;
    }

    public int getNum() {
        return num;
    }

    public String getLink() {
        return link;
    }

    public String getYear() {
        return year;
    }

    public String getNews() {
        return news;
    }

    public String getSafe_title() {
        return safe_title;
    }

    public String getTranscript() {
        return transcript;
    }

    public String getAlt() {
        return alt;
    }

    public String getImg() {
        return img;
    }

    public URL getImage_url() {
        return image_url;
    }

    public String getTitle() {
        return title;
    }

    public String getDay() {
        return day;
    }
}
