package psycho.euphoria.funny.utils;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Administrator on 2015/1/21.
 */
public class CollectionsUtilities {
    public static boolean isEmptyList(List list) {

        if (list == null) return true;
        if (list.size() < 1) return true;
        return false;
    }


    public static Object findElement(List list, int itemIndex) {
        if (itemIndex >= 0 && itemIndex < list.size()) {
            return list.get(itemIndex);
        } else {
            return null;
        }
    }

    public static boolean isEmptyArray(Object[] objects) {

        if (objects == null) return true;
        if (objects.length < 1) return true;
        return false;
    }

    public static <T> void forEach(List<T> list, Function<T> function) {

        if (!CollectionsUtilities.isEmptyList(list)) {
            for (T t : list) {
                function.onFunc(t);
            }
        }
    }

    public static <T> List<T> filter(List<T> list, Filter<T> filter) {

        final List<T> l = new ArrayList<>();

        if (!CollectionsUtilities.isEmptyList(list)) {
            for (T t : list) {
                if (!filter.onFilter(t)) {
                    l.add(t);
                }

            }
        }
        return l;
    }

    public interface Function<T> {
        void onFunc(T t);
    }


    public interface Filter<T> {
        boolean onFilter(T t);
    }
}
