package psycho.euphoria.funny.chooser;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import psycho.euphoria.funny.R;
import psycho.euphoria.funny.utils.CollectionsUtilities;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by Administrator on 2015/1/22.
 */
public class FileChooserAdapter extends BaseAdapter {


    private String mInitializeDirectory;
    private ArrayList<Pair<Boolean, File>> mList;

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private static final int sLayoutId = R.layout.file_chooser_item;


    public FileChooserAdapter(String mInitializeDirectory, String extension, Context context) {
        this.mInitializeDirectory = mInitializeDirectory;
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mList = getFileSystem(mInitializeDirectory, extension);


    }


    public void setInitializeDirectory(String mInitializeDirectory, String extension) {
        this.mInitializeDirectory = mInitializeDirectory;
        mList = getFileSystem(mInitializeDirectory, extension);
        this.notifyDataSetChanged();
    }

    public static ArrayList<Pair<Boolean, File>> getFileSystem(String dir, String extension) {


        final ArrayList<Pair<Boolean, File>> list = new ArrayList<>();

        if (dir == null)
            return list;
        final File file = new File(dir);
        if (!file.exists() || !file.isDirectory()) return list;

       /* list.add(Pair.create(true, file));*/

        final File[] files = file.listFiles();
        if (CollectionsUtilities.isEmptyArray(files))
            return list;

        for (File f : files) {
            Pair<Boolean, File> pair = null;
            if (f.isDirectory()) {
                pair = Pair.create(true, f);
            } else if (f.isFile()) {

                if (f.getName().toLowerCase().endsWith(extension)) {
                    pair = Pair.create(false, f);
                }

            }
            if (pair != null)
                list.add(pair);
        }

        final Collator collator = Collator.getInstance(Locale.CHINA);
        Collections.sort(list, new Comparator<Pair<Boolean, File>>() {
            @Override
            public int compare(Pair<Boolean, File> p1, Pair<Boolean, File> p2) {
                if ((p1.first && p2.first) || (!p1.first && !p2.first)) {

                   /* Log.e("Collections.sort", TextUtilities.logFormat("collator.compare(p1.second.getName(), p2.second.getName())",collator.compare(p1.second.getName(), p2.second.getName())));
                  */  return collator.compare(p1.second.getName(), p2.second.getName());
                } else {

                    return p1.first ? -1 :1;
                }
            }
        });
        return list;

    }

    public ArrayList<Pair<Boolean, File>> getList() {

        return mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {

       View view=v;
        final ViewHolder viewHolder;
        if (view == null) {

            viewHolder = new ViewHolder();
            view = mLayoutInflater.inflate(R.layout.file_chooser_item, null);

            viewHolder.nTextView = (TextView) view.findViewById(R.id.ui_file_chooser_title);
            viewHolder.nImageView = (ImageView) view.findViewById(R.id.ui_file_chooser_image);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        final Pair<Boolean, File> pair = mList.get(i);
        viewHolder.nImageView.setImageResource(pair.first ? R.drawable.fileicon_folder : R.drawable.fileicon_document);
        viewHolder.nTextView.setText(pair.second.getName());
        return view;
    }

    private static class ViewHolder {
        public ImageView nImageView;
        public TextView nTextView;
    }
}
