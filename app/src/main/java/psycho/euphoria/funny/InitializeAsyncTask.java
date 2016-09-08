package psycho.euphoria.funny;

import android.os.AsyncTask;
import psycho.euphoria.funny.utils.Constants;
import psycho.euphoria.funny.utils.FileUtilities;

/**
 * Created by Administrator on 2015/1/22.
 */
public class InitializeAsyncTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... voids) {
        FileUtilities.checkDirectories(Constants.DATABASE_FILENAME);
        FileUtilities.checkDirectories(Constants.DATABASE_FILENAME, Constants.IMPORT_DIRECTORY);
        FileUtilities.checkDirectories(Constants.DIRECTORY_MUSIC);


        return null;
    }
}
