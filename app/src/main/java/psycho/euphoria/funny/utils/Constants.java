package psycho.euphoria.funny.utils;

import android.graphics.Color;
import android.os.Environment;

/**
 * Created by Administrator on 2014/12/9.
 */
public interface Constants {

    final String KEY_DIRECTORY="directory";

    final String KEY_COUNT_LINE="count_line";
    final String DATABASE_FILENAME = Environment.getExternalStorageDirectory() + "/psycho/datas";
    final String IMPORT_DIRECTORY = Environment.getExternalStorageDirectory() + "/psycho/books/";

    final String DATABASE_FILENAMES = "DATABASE_FILENAMES";
    final String SHARED_PREFERENCES = "psycho_pre";
    final String KEY_DATABASE = "psycho_pre";
    final String INTENT_TITLE = "INTENT_TITLE";
    final String YOUDAO_API = "http://fanyi.youdao.com/openapi.do?keyfrom=baiduasd&key=2007739785&type=data&doctype=json&version=1.1&q=";


    final String PRE_SCROLL_Y= "scroll_y";
    final String PRE_TEXT_SIZE = "text_size";
    final String PRE_TEXT_LINE_SPACING = "text_line_spacing";
    final String CLIP_DESCRIPTION = "psycho.euphoria.funny.Clip_Description";
    final String DIRECTORY_MUSIC = Environment.getExternalStorageDirectory() + "/psycho/mp3/";


    final int HIGHLIGHT = Color.YELLOW;
  /*  final pattern clean_html=pattern.compile("");*/
}
