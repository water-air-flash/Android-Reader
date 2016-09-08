package psycho.euphoria.funny.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

/**
 * Created by Administrator on 2014/12/8.
 */
public class DatabaseProvider {
    private static final Object mLock = new Object();
    SQLiteDatabase mSQLiteDatabase;
    private static DatabaseProvider mDatabaseProvider;
    private static final String DATABASE_FILENAME = Environment.getExternalStorageDirectory() + "/psycho/psycho.dat";
    private final String LIKE_QUERY_SQL = "select word from dic where key=?";

    private DatabaseProvider() {
        // VIRTUAL USING fts4
        mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(DATABASE_FILENAME, null);
        mSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS dic (key TEXT PRIMARY KEY, word TEXT )");

    }

    private void initialize() {
        mSQLiteDatabase = SQLiteDatabase.openDatabase(DATABASE_FILENAME, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public static DatabaseProvider getInstance() {
        if (mDatabaseProvider == null)
            mDatabaseProvider = new DatabaseProvider();
        return mDatabaseProvider;
    }

    public void executeSql(String sql) {
        if (mSQLiteDatabase == null || !mSQLiteDatabase.isOpen())
            initialize();
        if (mSQLiteDatabase.isOpen())
            mSQLiteDatabase.execSQL(sql);
    }

    public String query(String word) {

        synchronized (mLock) {
            if (word == null)
                return word;
            String result = "";
            if (mSQLiteDatabase == null || !mSQLiteDatabase.isOpen())
                initialize();
            Cursor cursor = mSQLiteDatabase.rawQuery(LIKE_QUERY_SQL, new String[]{word});
            while (cursor.moveToNext()) {
                result += cursor.getString(0);
            }
            cursor.close();

            return result;
        }
    }

    public void insert(String key, String word) {
        synchronized (mLock) {
            if (mSQLiteDatabase == null || !mSQLiteDatabase.isOpen())
                initialize();
            ContentValues contentValues = new ContentValues();
            contentValues.put("key", key);
            contentValues.put("word", word);
            mSQLiteDatabase.insertWithOnConflict("dic", null, contentValues, SQLiteDatabase.CONFLICT_ROLLBACK);
        }
    }

    public void close() {
        if (mSQLiteDatabase != null && mSQLiteDatabase.isOpen()) {
            mSQLiteDatabase.close();
        }
    }

}
