package psycho.euphoria.funny.inject;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2015/1/16.
 */
public class Injector {

    public static void inject(Object handler) {

        Class<?> handlerType = handler.getClass();


        final ContentView contentView = handlerType.getAnnotation(ContentView.class);
        if (contentView != null) {
            try {
                Method setContentViewMethod = handlerType.getMethod("setContentView", int.class);
                setContentViewMethod.invoke(handler, contentView.value());
            } catch (Throwable e) {


            }
        }

        Field[] fields = handlerType.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                InjectView injectView = field.getAnnotation(InjectView.class);
                if (injectView != null) {
                    try {


                        final View view = ((Activity) handler).findViewById(injectView.value());
                        if (view != null) {
                            field.setAccessible(true);
                            field.set(handler, view);
                        }

                    } catch (Throwable e) {
                    }
                }
            }
        }

    }
}
