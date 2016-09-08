package psycho.euphoria.funny;

import android.app.Service;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;
import psycho.euphoria.funny.utils.Constants;
import psycho.euphoria.funny.utils.InstanceProviders;
import psycho.euphoria.funny.utils.TextUtilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Administrator on 2016/7/11.
 */
public class AutoSaveService  extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }
    private String mContent="";
    @Override
    public void onCreate(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final ClipboardManager clipboardManager;
        try {
            clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(CLIPBOARD_SERVICE);
            if (clipboardManager == null)
                return START_STICKY;
        } catch (Exception exception) {
            return START_STICKY;
        }
        try {

            clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {

                    try {
                        String t = InstanceProviders.getTextFromClipboardManger(InstanceProviders.getClipboardManager());

                        if (mContent.equals(t))return;
                        else
                        mContent=t;
                        try {
                            String fullName = InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).getString(Constants.KEY_DIRECTORY, "");
                            File file=new File(fullName);

                            String filename=fullName+"/"+ TextUtilities.paddingLeft(file.listFiles().length+1,3,"0")+".txt";
                           /* String n = t.split("\n")[0];
                            if (n.trim().length() < 1) return;

                            n = n.replaceAll("[\\\\/:\\*\\?\"<>\\|]+", "-");
                            String filename = Environment.getExternalStorageDirectory() + "/psycho/datas/" + n + ".txt";*/

                            FileWriter fileWriter=new FileWriter(file);
                            BufferedWriter out = new BufferedWriter(fileWriter);
                            out.write( mContent);
                            out.close();
                            Toast.makeText(InstanceProviders.getContext(),filename,Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                        }
                    } catch (Exception e) {
                    }
                }
            });
        } catch (Exception exception) {
        }

        return START_STICKY;

    }
}
