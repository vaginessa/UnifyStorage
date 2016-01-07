package org.cryse.unifystorage.explorer.utils;

import android.view.Menu;

import java.lang.reflect.Method;

public class MenuUtils {
    public static void showMenuItemIcon(Menu menu) {
        if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
            try {
                Method m = menu.getClass().getDeclaredMethod(
                        "setOptionalIconsVisible", Boolean.TYPE);
                m.setAccessible(true);
                m.invoke(menu, true);
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
