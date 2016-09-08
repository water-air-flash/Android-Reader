package psycho.euphoria.funny;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import psycho.euphoria.funny.utils.Constants;
import psycho.euphoria.funny.utils.FileUtilities;
import psycho.euphoria.funny.utils.InstanceProviders;
import psycho.euphoria.funny.utils.TextUtilities;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2014/12/9.
 */
public class DirectoryActivity extends ActionBarActivity {

    private ListView mListView;

    private FileAdapter mFileAdapter;
    private List<String> mPath_List;
    private static final LayoutInflater mLayoutInflater = InstanceProviders.getLayoutInflater();
    SharedPreferences mSharedPreferences;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSharedPreferences = InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES);
        setContentView(R.layout.activity_directory);

        mListView = (ListView) findViewById(R.id.ui_directory_recycler_listview);

        Intent intent = getIntent();
        if (intent != null) {
            mPath_List = intent.getStringArrayListExtra(Constants.DATABASE_FILENAMES);

        }
        mFileAdapter = new FileAdapter(this, mPath_List);
        mListView.setAdapter(mFileAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String fileName = mPath_List.get(i);
                File file = new File(fileName);
                if (file.isFile()) {
                    mSharedPreferences.edit().putString(Constants.KEY_DATABASE, mPath_List.get(i)).commit();

                    final ReadActivity mainActivity = (ReadActivity) InstanceProviders.getContext();
                    mainActivity.reset();
                    DirectoryActivity.this.finish();
                } else if (file.isDirectory()) {
                    InstanceProviders.getSharedPreferences().edit().putString(Constants.KEY_DIRECTORY, fileName).commit();
                    mPath_List = FileUtilities.getFileAbsolutePaths(fileName, ".txt");
                    mFileAdapter = new FileAdapter(DirectoryActivity.this, mPath_List);
                    mListView.setAdapter(mFileAdapter);
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int id = item.getItemId();

        if (id == android.R.id.home) {
            String fullName = InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).getString(Constants.KEY_DIRECTORY, "");
            File file = new File(fullName);
            if (file.getParentFile() != null) {
                String fileName = file.getParentFile().getAbsolutePath();
                InstanceProviders.getSharedPreferences().edit().putString(Constants.KEY_DIRECTORY, fileName).commit();
                mPath_List = FileUtilities.getFileAbsolutePaths(fileName, ".txt");
                mFileAdapter = new FileAdapter(DirectoryActivity.this, mPath_List);
                mListView.setAdapter(mFileAdapter);
            } else {
                finish();
            }
            //
        } else if (id == R.id.action_combine) {
            String fullName = InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).getString(Constants.KEY_DIRECTORY, "");
            File file = new File(fullName);
           final List<String> files = new ArrayList<>();
            file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isFile()) {
                        final String ext = FileUtilities.getFileExtension(pathname.getAbsolutePath());
                        if (!TextUtilities.isEmpty(ext)) {
                            if (ext.equalsIgnoreCase(".txt"))
                                files.add(pathname.getAbsolutePath());
                        }
                    }
                    return false;
                }
            });
            Collections.sort(files, new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {

                    return  lhs.compareToIgnoreCase(rhs);
                }
            });
            String fn = InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).getString(Constants.KEY_DIRECTORY, "")+".txt";
            File ft=new File(fn);


            FileWriter fileWriter= null;
            try {
                fileWriter = new FileWriter(ft,true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedWriter out = new BufferedWriter(fileWriter);


            for (String f:files) {
                try {
                   String content= FileUtilities.readToString(new File(f),"utf-8");
                    out.write( content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(id==R.id.action_orange){
            String fn = InstanceProviders.getSharedPreferences(Constants.SHARED_PREFERENCES).getString(Constants.KEY_DIRECTORY, "");
            FileUtilities.moveFileByCount(fn);
        }
        return true;
    }

    private class FileAdapter extends ArrayAdapter<String> {


        List<String> mPaths;

        public FileAdapter(Context context, List<String> paths) {
            super(context, 0, paths);
            mPaths = paths;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            FileViewHolder holder = null;
            if (view == null) {

                view = mLayoutInflater.inflate(R.layout.directory_list_item, null);

                // view holder
                holder = new FileViewHolder();
                holder.mTextView_Title = (TextView) view
                        .findViewById(R.id.ui_directory_list_item_textview);
                view.setTag(holder);
            } else {
                holder = (FileViewHolder) convertView.getTag();
            }
            // entity
            try {


                holder.mTextView_Title.setText(FileUtilities.getFileName(mPaths.get(position)));
            } catch (Exception e) {
            }
            return view;

        }
    }

    private class FileViewHolder {

        public TextView mTextView_Title;

    }
}
