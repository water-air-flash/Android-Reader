package psycho.euphoria.funny.provider;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;
import psycho.euphoria.funny.utils.Constants;
import psycho.euphoria.funny.utils.InstanceProviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by Administrator on 2014/12/8.
 */
public class ArticleDatabaseProvider {
    SQLiteDatabase mSQLiteDatabase;

    public ArticleDatabaseProvider() {
        initialize();
    }

    private void initialize() {
        String mDataBaseName = Constants.DATABASE_FILENAME + "/"
                + InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).getString(Constants.KEY_DATABASE, "");

        mSQLiteDatabase = SQLiteDatabase.openDatabase(mDataBaseName, null, SQLiteDatabase.OPEN_READWRITE);
    }


    public String queryNext(String title) {
        ArrayList<String> articles = getArticles();

        int i = articles.indexOf(title);

        if (i + 1 < articles.size())
            return articles.get(i + 1) + "\n" + query(articles.get(i + 1));
        return null;
    }

    public String query(String title) {
        if (!mSQLiteDatabase.isOpen())
            initialize();
        InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).edit().putString(Constants.INTENT_TITLE, title).commit();

        Cursor cursor = mSQLiteDatabase.query("doc", new String[]{"content"}, "title=?", new String[]{title}, null, null, null, "1");
        while (cursor.moveToNext()) {
            return cursor.getString(0);
        }
        cursor.close();

        return null;
    }

    public ArrayList<Pair<String, String>> getAll() {

        ArrayList<Pair<String, String>> articles = new ArrayList<Pair<String, String>>();

        Cursor cursor = mSQLiteDatabase.query("doc", new String[]{"title", "content"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            articles.add(Pair.create(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();


        return articles;
    }

    public ArrayList<String> getArticles() {

        ArrayList<String> articles = new ArrayList<String>();

        Cursor cursor = mSQLiteDatabase.query("doc", new String[]{"title"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            articles.add(cursor.getString(0));
        }
        cursor.close();

        Collections.sort(articles, new Comparator<String>() {
            @Override
            public int compare(String s, String s2) {
                final String[] ss = s.split("-");
                final String[] sss = s2.split("-");

                if (ss.length > 1 && sss.length > 1) {
                    return ss[1].compareToIgnoreCase(sss[1]);
                }
                return 0;
            }
        });
        return articles;
    }
}
