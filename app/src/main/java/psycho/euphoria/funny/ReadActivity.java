package psycho.euphoria.funny;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.IntegerRes;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;
import psycho.euphoria.funny.components.LTextView;
import psycho.euphoria.funny.utils.*;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static psycho.euphoria.funny.utils.Constants.DATABASE_FILENAME;

/**
 * Created by Administrator on 2015/1/28.
 */
public class ReadActivity extends ActionBarActivity implements Thread.UncaughtExceptionHandler {

    private final Handler sHandler = new Handler();

    private ScrollView mScrollView;
    private int mCountLine = 0;
    private int mFindIndex = 0;
    private LTextView mLTextView;
    private boolean mIsStarted = false;

    public static Pair<Integer, Integer> findNextStart(String fileName, String pattern, int start) throws IOException {
        final String content = readToString(new File(fileName), "utf-8");
        if (start + 1 <= content.length()) {
            final Pattern p = Pattern.compile(pattern);
            Matcher matcher = p.matcher(content);
            if (matcher.find(start))
                return Pair.create(matcher.start(), matcher.end());
        }
        return null;
    }

    public static String readToString(File file, String charset) throws IOException {
        final FileInputStream inputStream = new FileInputStream(file);
        final InputStreamReader reader = new InputStreamReader(
                inputStream, charset);
        final StringBuffer buffer = new StringBuffer();
        final char[] buf = new char[64];
        int count = 0;
        try {
            while ((count = reader.read(buf)) != -1) {
                buffer.append(buf, 0, count);
            }
        } finally {
            reader.close();
        }
        return buffer.toString();
    }

    private void fromSharedPreferences() {
        float textsize = InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).getFloat(Constants.PRE_TEXT_SIZE, 0);
        if (textsize > 0)
            mLTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
        float lsp = InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).getFloat(Constants.PRE_TEXT_LINE_SPACING, 0);
        if (lsp > 0)
            mLTextView.setLineSpacing(mLTextView.getLineSpacingExtra(),lsp);
        mCountLine = InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).getInt(Constants.KEY_COUNT_LINE, 0);


        String fullName = InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).getString(Constants.KEY_DATABASE, "");
        String str = "";
        setTitle(Integer.toString(mCountLine) + " " + FileUtilities.getFileNameWithoutExtension(fullName));
        if (!TextUtilities.isEmpty(fullName)) {

            try {
                final JumpToLine jtl = new JumpToLine(new File(fullName));
                try {
                    if (mCountLine > 0)
                        jtl.seek(mCountLine);
                    while (jtl.hasNext()) {
                        if (str.length() <= 16000) {
                            mCountLine++;
                            String line = jtl.readLine().trim();
                            if (line.length() > 0)
                                str += line + "\n\n";
                        } else {
                            break;
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                } finally {
                    // Close the underlying reader and LineIterator.
                    jtl.close();
                }
            } catch (Exception e) {
            }


        }
        mLTextView.setContentText(str, false);

    }

    @Override
    protected void onCreate(Bundle bundle) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        InstanceProviders.setContext(this);
        super.onCreate(bundle);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            String[] perms = {"android.permission.SYSTEM_ALERT_WINDOW",
                    "android.permission.INTERNET" ,
                    "android.permission. WRITE_EXTERNAL_STORAGE",
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.ACCESS_NETWORK_STATE" };
            int permsRequestCode = 200;
            requestPermissions(perms, permsRequestCode);
        }
        this.setContentView(R.layout.activity_read);
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);

        mLTextView = (LTextView) findViewById(R.id.ui_activity_read_LTextView);
        File file=new File(DATABASE_FILENAME +"/font.ttf");
        if(file.exists())
       mLTextView.setTypeface(Typeface.createFromFile(file));

        fromSharedPreferences();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        new InitializeAsyncTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_read_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {

            final Intent intent = new Intent(this, AutoSaveService.class);
            if (mIsStarted) {
                stopService(intent);
                Toast.makeText(this, "The AutoSaveService have been stopped.", Toast.LENGTH_SHORT).show();
            } else {
                startService(intent);
                Toast.makeText(this, "The AutoSaveService is started.", Toast.LENGTH_SHORT).show();

            }
            mIsStarted = !mIsStarted;
            // finish();
        } else if (id == R.id.action_read_open) {
            Intent intent = new Intent(ReadActivity.this, DirectoryActivity.class);
            String fileName = InstanceProviders.getSharedPreferences().getString(Constants.KEY_DIRECTORY, DATABASE_FILENAME);
            intent.putStringArrayListExtra(Constants.DATABASE_FILENAMES, FileUtilities.getFileAbsolutePaths(fileName, ".txt"));
            this.startActivity(intent);
        } else if (id == R.id.action_read_cat) {

            String fullName = InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).getString(Constants.KEY_DATABASE, "");
            setTitle(Integer.toString(mCountLine) + " " + FileUtilities.getFileNameWithoutExtension(fullName));
            readTextFile();

            // showDrawerLayout();
        } else if (id == R.id.action_read_orientation) {
            final Intent intent = new Intent(this, ChangeOrientationService.class);
            if (DisplayUtilities.isHORIZONTAL()) {
                stopService(intent);
            } else {
                startService(intent);
            }
        } else if (id == R.id.action_read_import) {
            final Dialog dialog = new Dialog(this); // Context, this, etc.
            dialog.setContentView(R.layout.dlg);
            dialog.findViewById(R.id.dialog_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String t = ((EditText) dialog.findViewById(R.id.dialog_info)).getText().toString().trim();
                        mCountLine = Integer.parseInt(t);
                        setTitle(Integer.toString(mCountLine));

                        readTextFile();

                        dialog.dismiss();
                    } catch (Exception exc) {

                    }
                }
            });
            dialog.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } else if (id == R.id.action_text_size) {
            final Dialog dialog = new Dialog(this); // Context, this, etc.
            dialog.setContentView(R.layout.dlg);
            dialog.findViewById(R.id.dialog_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String t = ((EditText) dialog.findViewById(R.id.dialog_info)).getText().toString().trim();
                        try {
                            float textsize = Float.parseFloat(t.trim());
                            if (textsize > 0) {
                                InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).edit().putFloat(Constants.PRE_TEXT_SIZE, textsize).apply();

                                mLTextView.setContentTextSize(textsize);
                            }
                        } catch (Exception e) {
                        }

                        dialog.dismiss();
                    } catch (Exception exc) {

                    }
                }
            });
            dialog.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String t = ((EditText) dialog.findViewById(R.id.dialog_info)).getText().toString().trim();
                        try {
                            float lsp = Float.parseFloat(t.trim());
                            if (lsp > 0) {
                                InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).edit().putFloat(Constants.PRE_TEXT_LINE_SPACING, lsp).apply();

                                mLTextView.setLineSpacing(mLTextView.getLineSpacingExtra(),lsp);
                                mLTextView.updateLayout();
                            }
                        } catch (Exception e) {
                        }

                    } catch (Exception exc) {

                    }


                    dialog.dismiss();
                }
            });
            dialog.show();
        } else if (id == R.id.action_write_file) {
            final Dialog dialog = new Dialog(this); // Context, this, etc.
            dialog.setContentView(R.layout.dlg);
            dialog.findViewById(R.id.dialog_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String t = InstanceProviders.getTextFromClipboardManger(InstanceProviders.getClipboardManager());

                        try {
//                            String fullName = InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).getString(Constants.KEY_DIRECTORY, "");
//                            File file=new File(fullName);
//
//                            String filename=fullName+"/"+TextUtilities.paddingLeft(file.listFiles().length+1,3,"0")+".txt";
//                            String n = t.split("\n")[0];
//                            if (n.trim().length() < 1) return;
//
//                            n = n.replaceAll("[\\\\/:\\*\\?\"<>\\|]+", "-");
                            int n=1;

                            File f=new File( Environment.getExternalStorageDirectory() + "/psycho/datas/" + TextUtilities.paddingLeft(n,3,"0") + ".txt");
                            while (f.exists()){
                                n++;
                                f=new File( Environment.getExternalStorageDirectory() + "/psycho/datas/" + TextUtilities.paddingLeft(n,3,"0") + ".txt");
                            }
                            BufferedWriter out = new BufferedWriter(new FileWriter(f));
                            out.write(t);
                            out.close();
                        } catch (IOException e) {
                        }
                    } catch (Exception e) {
                    }

                    dialog.dismiss();

                }
            });
            dialog.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String t = ((EditText) dialog.findViewById(R.id.dialog_info)).getText().toString().trim();
                    String dir = Environment.getExternalStorageDirectory() + "/psycho/datas/" + t.trim();
                    File file = new File(dir);
                    file.mkdirs();
                    if (file.isDirectory()) {
                        InstanceProviders.getSharedPreferences().edit().putString(Constants.KEY_DIRECTORY, dir).commit();
                        Toast.makeText(InstanceProviders.getContext(), dir, Toast.LENGTH_SHORT).show();
                    }
//                    String t = ((EditText) dialog.findViewById(R.id.dialog_info)).getText().toString().trim();
//                    InstanceProviders.renameFiles(t.trim());
                    dialog.dismiss();
                }
            });
            dialog.show();
        } else if (id == R.id.action_find_file) {
            String fullName = InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).getString(Constants.KEY_DATABASE, "");
            File file = new File(fullName);
            file.delete();
        }
        return true;
    }

    public void reset() {
        mLTextView.setText("");
        mCountLine = 0;
        InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).edit().putInt(Constants.KEY_COUNT_LINE, 0).commit();

    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        final int y = mScrollView.getScrollY();
//        if (y != 0) {
//            outState.putInt(Constants.PRE_SCROLL_Y, y);
//        }
//
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        final int y = savedInstanceState.getInt(Constants.PRE_SCROLL_Y, 0);
//        if (y != 0)
//            mScrollView.post(new Runnable() {
//                public void run() {
//                    mScrollView.scrollTo(0, y);
//                }
//            });
//    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        LogUtilities.logToSharedPreferences(InstanceProviders.getSharedPreferences(), throwable);
    }

    private void readTextFile() {
        InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).edit().putInt(Constants.KEY_COUNT_LINE, mCountLine).commit();
        String fullName = InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).getString(Constants.KEY_DATABASE, "");
        String str = "";

        if (!TextUtilities.isEmpty(fullName)) {

            try {
                final JumpToLine jtl = new JumpToLine(new File(fullName));
                try {
                    if (mCountLine > 0)
                        jtl.seek(mCountLine);
                    while (jtl.hasNext()) {
                        if (str.length() <= 16000) {
                            mCountLine++;
                            String line = jtl.readLine().trim();
                            if (line.length() > 0)
                                str += line + "\n\n";
                        } else {
                            break;
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                } finally {
                    // Close the underlying reader and LineIterator.
                    jtl.close();
                }
            } catch (Exception e) {
            }


        }
        mLTextView.setContentText(str, false);
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.smoothScrollTo(0, 0);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        final int y = InstanceProviders.getSharedPreferences().getInt(Constants.PRE_SCROLL_Y, 0);
        if (y != 0)
            mScrollView.post(new Runnable() {
                public void run() {
                    mScrollView.scrollTo(0, y);
                }
            });
    }

    @Override
    protected void onPause() {
        super.onPause();
        final int y = mScrollView.getScrollY();
        if (y != 0) {
            InstanceProviders.getSharedPreferences().edit().putInt(Constants.PRE_SCROLL_Y, y).commit();
        }

    }


}
