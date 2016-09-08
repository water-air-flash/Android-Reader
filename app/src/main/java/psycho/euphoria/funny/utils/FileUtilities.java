package psycho.euphoria.funny.utils;

import android.content.res.AssetManager;
import android.os.Environment;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.Collator;
import java.util.*;

/**
 * Created by Administrator on 2014/12/9.
 */
public class FileUtilities {
    public static final int DEFAULT_BUFFER_SIZE = 32 * 1024; // 32 KB
    /**
     * {@value}
     */
    public static final int DEFAULT_IMAGE_TOTAL_SIZE = 500 * 1024; // 500 Kb
    /**
     * {@value}
     */
    public static final int CONTINUE_LOADING_PERCENTAGE = 75;
    private static final String sCharSet = "UTF-8";

    public static void checkDirectories(String... strings) {
        for (String s : strings) {
            final File file = new File(s);
            if (!file.exists())
                file.mkdirs();
        }
    }

    public static boolean checkFile(String filename) {

        if (TextUtilities.isEmpty(filename))
            return false;
        return new File(filename).exists();

    }

    public class ChineseCharComp implements Comparator {
        public int compare(Object o1, Object o2) {
            Collator myCollator = Collator.getInstance(java.util.Locale.CHINA);
            o1 = getFileName(o1.toString());
            o2 = getFileName(o2.toString());
            if (myCollator.compare(o1, o2) < 0)
                return -1;
            else if (myCollator.compare(o1, o2) > 0)
                return 1;
            else
                return 0;
        }
    }

    public boolean checkFileSystem(File file) {

        try {

            final String canonicalPath = file.getCanonicalPath();
            final String canonicalExternal = Environment.getExternalStorageDirectory()
                    .getCanonicalPath();
            if (canonicalPath.startsWith(canonicalExternal)) {
                if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
                    return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

    public static void copyAsset(String filename) {
        AssetManager assetManager = InstanceProviders.getContext().getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
        }
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            File outFile = new File(Constants.DATABASE_FILENAME, filename);
            out = new FileOutputStream(outFile);
            copyFile(in, out);
        } catch (IOException e) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
// NOOP
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
// NOOP
                }
            }
        }
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static void moveFileByCount(String fullName){
        File dir=new File(fullName);
        File[] lsf=dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory())
                return false;
                return true;
            }
        });
        int index=0;
        String targetDir="";
        for (File f:lsf ) {
            if (index%100==0){
                targetDir=fullName+"/"+TextUtilities.paddingLeft((index/100+1),3,"0");
                new File(targetDir).mkdir();
            }
            index++;
            f.renameTo(new File(targetDir+"/"+f.getName()));
        }
    }
    public static void createDirectory(String filename) {
        if (!new File(filename).exists())
            new File(filename).mkdirs();
    }

    public static ArrayList<String> getFileAbsolutePaths(String directory, String extension) {
        final Collator collator = Collator.getInstance(Locale.CHINA);
        ArrayList<String> paths = new ArrayList<String>();
        File file = new File(directory);
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                final String ext = getFileExtension(f.getAbsolutePath());
                if (!TextUtilities.isEmpty(ext)) {
                    if (ext.equalsIgnoreCase(extension))
                        paths.add(f.getAbsolutePath());
                }
            }else if (f.isDirectory()){
                paths.add(f.getAbsolutePath());
            }
        }
        Collections.sort(paths, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return getFileName(lhs).compareToIgnoreCase(getFileName(rhs));
            }
        });


        return paths;
    }

    public static String getFileExtension(String fileAbsolutePath) {
        final int pos = fileAbsolutePath.lastIndexOf(".");
        return pos > 0 ? fileAbsolutePath.substring(pos).toLowerCase().intern() : "";
    }

    public static String getFileName(String fileAbsolutePath) {
        if (fileAbsolutePath == null)
            return null;
        if (fileAbsolutePath.lastIndexOf("/") > 0)
            return fileAbsolutePath.substring(fileAbsolutePath.lastIndexOf("/") + 1);
        return null;
    }

    public static String getFileNameWithoutExtension(String filename) {
        final int pos = filename.lastIndexOf("/");
        String r = "";
        if (pos != -1) {
            r = filename.substring(pos + 1);

        } else {
            r = filename;
        }
        final int p = r.lastIndexOf(".");
        if (p != -1) {
            return r.substring(0, p);
        }
        return r;
    }

    public static String getUniqueFileName(File file, String pathSeparator) {
        if (!file.exists())
            return file.getAbsolutePath();
        final String[] rs = parseFileName(file.getAbsolutePath(), pathSeparator);
        if (rs[2] == null)
            rs[2] = "";
        int i = 1;
        while (file.exists()) {
            file = new File(rs[0] + rs[1] + " - " + TextUtilities.paddingLeft(i, 3, "0") + rs[2]);
        }
        return file.getAbsolutePath();
    }

    public static boolean isFileExists(String absolutePath) {
        File file = new File(absolutePath);
        return file.exists() && file.isFile();
    }

    public static void moveFile(File file, String destination, boolean isMove) {

        if (file.exists()) {

            FileChannel fileChannelSource = null;
            FileChannel fileChannelDestination = null;

            try {
                fileChannelSource = new FileInputStream(file).getChannel();
                fileChannelDestination = new FileOutputStream(destination).getChannel();
                long count = 0;
                long size = fileChannelSource.size();
                while ((count += fileChannelDestination.transferFrom(fileChannelSource, count, size - count)) < size)
                    ;
                if (isMove) {

                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fileChannelSource != null) {
                    try {
                        fileChannelSource.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fileChannelDestination != null) {
                    try {
                        fileChannelDestination.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


    }


    public static String getParent(String filename) {
        return new File(filename).getParent();
    }

    /*
    c:\\1.txt
    c:\
    1
    .tx
    */
    public static String[] parseFileName(String fileName, String pathSeparator) {
        final String[] r = new String[3];
        final File file = new File(fileName);
        r[0] = file.getParent();
        final int pos = fileName.lastIndexOf(".");
        if (pos != -1) {
            r[2] = fileName.substring(pos);
        }
        final int p = fileName.lastIndexOf(pathSeparator);
        if (p != -1) {
            if (p + 1 < pos) {
                r[1] = fileName.substring(p + 1, pos);
            } else {
                r[1] = fileName.substring(p + 1);
            }
        }
        return r;
    }

    public static String readPlainFile(File file, String charsetName) throws FileNotFoundException {
        return new Scanner(file, charsetName).useDelimiter("\\A").next();
    }

    public static String readToString(InputStream inputStream, String charset) throws IOException {
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

    public static String readToString(File file, String charset) throws IOException {
/*   FileOutputStream outputStream = new FileOutputStream(file);
OutputStreamWriter writer = new OutputStreamWriter(
outputStream, charset);
try {
writer.write("这是要保存的中文字符");
} finally {
writer.close();
}*/
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

    public static boolean copyStream(InputStream is, OutputStream os, CopyListener listener, int bufferSize)
            throws IOException {
        int current = 0;
        int total = is.available();
        if (total <= 0) {
            total = DEFAULT_IMAGE_TOTAL_SIZE;
        }

        final byte[] bytes = new byte[bufferSize];
        int count;
        if (shouldStopLoading(listener, current, total)) return false;
        while ((count = is.read(bytes, 0, bufferSize)) != -1) {
            os.write(bytes, 0, count);
            current += count;
            if (shouldStopLoading(listener, current, total)) return false;
        }
        os.flush();
        return true;
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void readAndCloseStream(InputStream is) {
        final byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
        try {
            while (is.read(bytes, 0, DEFAULT_BUFFER_SIZE) != -1) ;
        } catch (IOException ignored) {
        } finally {
            closeSilently(is);
        }
    }

    public static boolean copyStream(InputStream is, OutputStream os, CopyListener listener) throws IOException {
        return copyStream(is, os, listener, DEFAULT_BUFFER_SIZE);
    }

    private static boolean shouldStopLoading(CopyListener listener, int current, int total) {
        if (listener != null) {
            boolean shouldContinue = listener.onBytesCopied(current, total);
            if (!shouldContinue) {
                if (100 * current / total < CONTINUE_LOADING_PERCENTAGE) {
                    return true; // if loaded more than 75% then continue loading anyway
                }
            }
        }
        return false;
    }

    public static InputStream copyInputStream(InputStream inputStream) {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();

// Fake code simulating the copy
// You can generally do better with nio if you need...
// And please, unlike me, do something about the Exceptions :D
        final byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();

            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Listener and controller for copy process
     */
    public static interface CopyListener {
        /**
         * @param current Loaded bytes
         * @param total   Total bytes for loading
         * @return <b>true</b> - if copying should be continued; <b>false</b> - if copying should be interrupted
         */
        boolean onBytesCopied(int current, int total);
    }
    public static byte[] readStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while( (len=inStream.read(buffer)) != -1){
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

}
