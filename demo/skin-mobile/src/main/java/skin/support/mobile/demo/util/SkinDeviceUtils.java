package skin.support.mobile.demo.util;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import skin.support.utils.Slog;

public class SkinDeviceUtils {
    private final static String TAG = "QMUIDeviceHelper";
    private final static String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_FLYME_VERSION_NAME = "ro.build.display.id";
    private final static String FLYME = "flyme";
    private final static String ZTEC2016 = "zte c2016";
    private final static String ESSENTIAL = "essential";
    private final static String MEIZUBOARD[] = {"m9", "M9", "mx", "MX"};
    private static String sMiuiVersionName;
    private static String sFlymeVersionName;
    private static boolean sIsTabletChecked = false;
    private static boolean sIsTabletValue = false;
    private static final String BRAND = Build.BRAND.toLowerCase();

    static {
        Properties properties = new Properties();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // android 8.0，读取 /system/uild.prop 会报 permission denied
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(new File(Environment.getRootDirectory(), "build.prop"));
                properties.load(fileInputStream);
            } catch (Exception e) {
                Slog.i(TAG, "read file error");
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable e) {
                    }
                }
            }
        }

        Class<?> clzSystemProperties = null;
        try {
            clzSystemProperties = Class.forName("android.os.SystemProperties");
            Method getMethod = clzSystemProperties.getDeclaredMethod("get", String.class);
            // miui
            sMiuiVersionName = getLowerCaseName(properties, getMethod, KEY_MIUI_VERSION_NAME);
            //flyme
            sFlymeVersionName = getLowerCaseName(properties, getMethod, KEY_FLYME_VERSION_NAME);
        } catch (Exception e) {
            Slog.i(TAG, "read SystemProperties error");
        }
    }

    private static boolean _isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * 判断是否为平板设备
     */
    public static boolean isTablet(Context context) {
        if (sIsTabletChecked) {
            return sIsTabletValue;
        }
        sIsTabletValue = _isTablet(context);
        sIsTabletChecked = true;
        return sIsTabletValue;
    }

    /**
     * 判断是否是flyme系统
     */
    public static boolean isFlyme() {
        return !TextUtils.isEmpty(sFlymeVersionName) && sFlymeVersionName.contains(FLYME);
    }

    /**
     * 判断是否是MIUI系统
     */
    public static boolean isMIUI() {
        return !TextUtils.isEmpty(sMiuiVersionName);
    }

    public static boolean isMIUIV5() {
        return "v5".equals(sMiuiVersionName);
    }

    public static boolean isMIUIV6() {
        return "v6".equals(sMiuiVersionName);
    }

    public static boolean isMIUIV7() {
        return "v7".equals(sMiuiVersionName);
    }

    public static boolean isMIUIV8() {
        return "v8".equals(sMiuiVersionName);
    }

    public static boolean isMIUIV9() {
        return "v9".equals(sMiuiVersionName);
    }

    public static boolean isFlymeLowerThan(int majorVersion) {
        return isFlymeLowerThan(majorVersion, 0, 0);
    }

    public static boolean isFlymeLowerThan(int majorVersion, int minorVersion, int patchVersion) {
        boolean isLower = false;
        if (sFlymeVersionName != null && !sFlymeVersionName.equals("")) {
            try {
                Pattern pattern = Pattern.compile("(\\d+\\.){2}\\d");
                Matcher matcher = pattern.matcher(sFlymeVersionName);
                if (matcher.find()) {
                    String versionString = matcher.group();
                    if (versionString.length() > 0) {
                        String[] version = versionString.split("\\.");
                        if (version.length >= 1) {
                            if (Integer.parseInt(version[0]) < majorVersion) {
                                isLower = true;
                            }
                        }

                        if (version.length >= 2 && minorVersion > 0) {
                            if (Integer.parseInt(version[1]) < majorVersion) {
                                isLower = true;
                            }
                        }

                        if (version.length >= 3 && patchVersion > 0) {
                            if (Integer.parseInt(version[2]) < majorVersion) {
                                isLower = true;
                            }
                        }
                    }
                }
            } catch (Throwable ignore) {

            }
        }
        return isMeizu() && isLower;
    }


    public static boolean isMeizu() {
        return isPhone(MEIZUBOARD) || isFlyme();
    }

    /**
     * 判断是否为小米
     * https://dev.mi.com/doc/?p=254
     */
    public static boolean isXiaomi() {
        return Build.MANUFACTURER.equalsIgnoreCase("xiaomi");
    }

    public static boolean isVivo() {
        return BRAND.contains("vivo") || BRAND.contains("bbk");
    }

    public static boolean isOppo() {
        return BRAND.contains("oppo");
    }

    public static boolean isHuawei() {
        return BRAND.contains("huawei") || BRAND.contains("honor");
    }

    public static boolean isEssentialPhone() {
        return BRAND.contains("essential");
    }


    private static boolean isPhone(String[] boards) {
        final String board = android.os.Build.BOARD;
        if (board == null) {
            return false;
        }
        for (String board1 : boards) {
            if (board.equals(board1)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static String getLowerCaseName(Properties p, Method get, String key) {
        String name = p.getProperty(key);
        if (name == null) {
            try {
                name = (String) get.invoke(null, key);
            } catch (Exception ignored) {
            }
        }
        if (name != null) name = name.toLowerCase();
        return name;
    }
}
