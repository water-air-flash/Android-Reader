package psycho.euphoria.funny.chooser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import psycho.euphoria.funny.R;
import psycho.euphoria.funny.utils.Constants;
import psycho.euphoria.funny.utils.FileUtilities;
import psycho.euphoria.funny.utils.TextUtilities;

import java.io.File;

/**
 * Created by Administrator on 2015/1/22.
 */
public class FileChooserActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    private ListView mListView;
    public static final String FILE_CHOOSER_FILE_EXTENSION = "file_chooser_file_extension";
    public static final String FILE_CHOOSER_INITIALIZE_DIRECTORY = "file_chooser_initialize_directory";

    private String mDirectory;
    private String mExtension;
    private SharedPreferences mSharedPreferences;

    private FileChooserAdapter mFileChooserAdapter;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.file_chooser_list);
        mListView = (ListView) findViewById(R.id.ui_file_chooser_list);
        mSharedPreferences = getSharedPreferences("file_chooser_pre", MODE_MULTI_PROCESS);
        handleIntent();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initialize();
    }

    private void handleIntent() {
        final Intent intent = getIntent();
        mDirectory = mSharedPreferences.getString(FILE_CHOOSER_INITIALIZE_DIRECTORY, "");
        if (TextUtilities.isEmpty(mDirectory)) {
            mDirectory = intent.getStringExtra(FILE_CHOOSER_INITIALIZE_DIRECTORY);
            if (TextUtilities.isEmpty(mDirectory)) {
                mDirectory = Constants.IMPORT_DIRECTORY;
            }
        }
        mExtension = intent.getStringExtra(FILE_CHOOSER_FILE_EXTENSION);
        if (TextUtilities.isEmpty(mExtension)) {
            mExtension = ".epub";
        }
    }

    private void initialize() {
        mFileChooserAdapter = new FileChooserAdapter(mDirectory, mExtension, this);
        mListView.setAdapter(mFileChooserAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final Pair<Boolean, File> pair = mFileChooserAdapter.getList().get(i);
        if (pair.first) {
            mFileChooserAdapter.setInitializeDirectory(pair.second.getAbsolutePath(), mExtension);
            mDirectory = pair.second.getAbsolutePath();
            mSharedPreferences.edit().putString(FILE_CHOOSER_INITIALIZE_DIRECTORY, mDirectory).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == android.R.id.home) {
            mDirectory = FileUtilities.getParent(mDirectory);
            if (TextUtilities.isEmpty(mDirectory))
                finish();
            else {
                mFileChooserAdapter.setInitializeDirectory(mDirectory, mExtension);
            }
        }
        return true;
    }


}
