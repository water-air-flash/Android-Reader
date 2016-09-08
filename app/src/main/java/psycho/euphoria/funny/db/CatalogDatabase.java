package psycho.euphoria.funny.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Pair;
import psycho.euphoria.funny.utils.InstanceProviders;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/1/29.
 */
public class CatalogDatabase extends SQLiteOpenHelper {
    private static final String sDatabaseName = "catalog";

    public CatalogDatabase() {
        super(InstanceProviders.getContext(), sDatabaseName, null, 5);
    }
    public ArrayList<Pair<String, String>> getRecords( String category) {
        final SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        final ArrayList<Pair<String, String>> pairs = new ArrayList<>();
        if (sqLiteDatabase != null) {
            if (sqLiteDatabase.isOpen()) {

                Cursor cursor = sqLiteDatabase.query("catalog", new String[]{"TITLE", "HREF"}, "CATEGORY=?", new String[]{category}, null, null, null);

                if (cursor == null)
                    return pairs;
                while (cursor.moveToNext()) {

                    final Pair<String, String> pair = Pair.create(cursor.getString(1), cursor.getString(0));
                    pairs.add(pair);
                }

                cursor.close();
            }
        }
        return pairs;
    }

    public void insert(List<Pair<String, String>> pairs, String category) {
        final SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        if (pairs != null && sqLiteDatabase != null) {
            if (sqLiteDatabase.isOpen()) {


                final int length = pairs.size();
                if (length < 1)
                    return;
                final SQLiteStatement sqLiteStatement = sqLiteDatabase.compileStatement("insert INTO catalog(HREF,TITLE,CATEGORY) VALUES (?, ?, ?);");
                sqLiteDatabase.beginTransaction();
                try {
                    for (int i = 0; i < length; i++) {
                        sqLiteStatement.bindString(1, pairs.get(i).first);
                        sqLiteStatement.bindString(2, pairs.get(i).second);
                        sqLiteStatement.bindString(3, category);
                        sqLiteStatement.executeInsert();
                    }
                    sqLiteDatabase.setTransactionSuccessful();
                } finally {
                    sqLiteDatabase.endTransaction();
                }
            }
        }
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS catalog(" +
                "HREF TEXT," +
                "TITLE TEXT," +
                "CATEGORY TEXT" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }
}
