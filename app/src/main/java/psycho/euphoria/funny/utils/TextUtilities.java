package psycho.euphoria.funny.utils;

import android.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2014/12/10.
 */
public class TextUtilities {
    private static final Pattern pattern = Pattern.compile("[a-z \\-']+", Pattern.CASE_INSENSITIVE);
    private static final Pattern sChinesePattern = Pattern.compile("[\u4E00-\u9FA5]+", Pattern.CASE_INSENSITIVE);
   // private static final Pattern match_numbers = Pattern.compile(".*[a-zA-Z]* \\- ([0-9\\- ]+)", Pattern.CASE_INSENSITIVE);

    public static String getLastMatch(String str) {
       String[] ls= str.split(" \\- ",2);

     /*   Matcher matcher = match_numbers.matcher(str);
        if (matcher.find()) {
            return matcher.group(1);
        }*/
        return ls[ls.length-1];
    }

    /**
     * Formats time in milliseconds to hh:mm:ss string format.
     */
    public static String formatMillis(int millis) {
        String result = "";
        int hr = millis / 3600000;
        millis %= 3600000;
        int min = millis / 60000;
        millis %= 60000;
        int sec = millis / 1000;
        if (hr > 0) {
            result += hr + ":";
        }
        if (min >= 0) {
            if (min > 9) {
                result += min + ":";
            } else {
                result += "0" + min + ":";
            }
        }
        if (sec > 9) {
            result += sec;
        } else {
            result += "0" + sec;
        }
        return result;
    }

    public static String logFormat(Object o, int i) {

        return String.format("%s=%d;\n", o, i);
    }

    public static String getWord(String text) {
        final Matcher matcher = sChinesePattern.matcher(text);
        if (matcher.find())
            return matcher.group(0);
        else {
            final Matcher m = pattern.matcher(text);
            if (m.find())
                return m.group(0);
        }
        return null;
    }

    public static boolean isEmpty(String str) {
        if (str == null)
            return true;
        if (str.trim().length() < 1) {
            return true;
        }
        return false;
    }

    public static String paddingLeft(int i, int length, String c) {
        final int l = length - String.valueOf(i).length();
        if (l > 0) {
            String r = "";
            for (int j = 0; j < l; j++) {
                r += c;
            }
            return r + String.valueOf(i);
        }
        return String.valueOf(i);
    }

    public static String removeEmptyLines(String str) {
        String text = "";
        String[] lines = str.split("\n");
        for (String s : lines) {
            if (s.trim().length() > 0)
                text += s + "\n\n";
        }
        return text;
    }

    public static ArrayList<String> splitByLength(String s, int length) {
        final ArrayList<String> list = new ArrayList<>();
        String r = "";
        final String[] strings = s.split("\n");
        if (CollectionsUtilities.isEmptyArray(strings))
            return list;
        for (String l : strings) {
            if (r.length() >= length) {
                list.add(r);
                r = "";
            } else {
                r += l.trim() + "\n";
            }
        }
        if (!TextUtilities.isEmpty(r))
            list.add(r);
        return list;
    }

    public static Pattern getPurePattern(String text, boolean isCaseInsensitive) {
        if (isCaseInsensitive)
            return Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE);
        else
            return Pattern.compile(Pattern.quote(text));

    }

    public static ArrayList<Pair<String, String>> getFromJson(String json) {
        if (TextUtilities.isEmpty(json)) return null;
        try {
            final JSONObject jsonObject = new JSONObject(json);
            final Iterator<String> iterator = jsonObject.keys();
            final ArrayList<Pair<String, String>> pairs = new ArrayList<>();
            while (iterator.hasNext()) {
                final String key = iterator.next();
                pairs.add(Pair.create(key, jsonObject.getString(key)));
            }
            return pairs;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toJson(ArrayList<Pair<String, String>> pairs) {
        if (CollectionsUtilities.isEmptyList(pairs)) return null;
        final JSONObject jsonObject = new JSONObject();
        try {
            for (Pair<String, String> p : pairs) {

                jsonObject.put(p.first, p.second);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
