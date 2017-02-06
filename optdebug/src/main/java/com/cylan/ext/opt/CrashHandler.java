package com.cylan.ext.opt;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by cylan-hunt on 17-2-6.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static CrashHandler INSTANCE = new CrashHandler();
    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private Map<String, String> infoMap = new HashMap();
    private DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    private static String path = "";

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            Class var0 = CrashHandler.class;
            synchronized (CrashHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CrashHandler();
                }
            }
        }

        return INSTANCE;
    }

    private static void setPath(String dir) {
        path = dir;
    }

    public void init(Context context, String dir) {
        if (TextUtils.isEmpty(dir)) {
            throw new NullPointerException("you must define crash dir");
        } else {
            setPath(dir);
            this.mContext = context;
            this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }

    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            if (!this.handleException(ex) && this.mDefaultHandler != null) {
                this.mDefaultHandler.uncaughtException(thread, ex);
            } else {
                SystemClock.sleep(3000L);
                Process.killProcess(Process.myPid());
                System.exit(1);
            }
        } catch (UnsupportedEncodingException var4) {
            var4.printStackTrace();
        }

    }

    private boolean handleException(Throwable ex) throws UnsupportedEncodingException {
        if (ex == null) {
            return false;
        } else {
            this.collectDeviceInfo(this.mContext);
            this.saveCrashInfo2File(ex);
            return true;
        }
    }

    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager fields = ctx.getPackageManager();
            PackageInfo pi = fields.getPackageInfo(ctx.getPackageName(), 1);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                this.infoMap.put("versionName", versionName);
                this.infoMap.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException var9) {
            ;
        }

        Field[] var10 = Build.class.getDeclaredFields();
        Field[] var11 = var10;
        int var12 = var10.length;

        for (int var13 = 0; var13 < var12; ++var13) {
            Field field = var11[var13];

            try {
                field.setAccessible(true);
                this.infoMap.put(field.getName(), field.get((Object) null).toString());
            } catch (Exception var8) {
                ;
            }
        }

    }

    private String saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        Iterator writer = this.infoMap.entrySet().iterator();

        String result;
        while (writer.hasNext()) {
            Map.Entry printWriter = (Map.Entry) writer.next();
            String cause = (String) printWriter.getKey();
            result = (String) printWriter.getValue();
            sb.append(cause + "=" + result + "\n");
        }

        StringWriter var14 = new StringWriter();
        PrintWriter var15 = new PrintWriter(var14);
        ex.printStackTrace(var15);

        for (Throwable var16 = ex.getCause(); var16 != null; var16 = var16.getCause()) {
            var16.printStackTrace(var15);
        }

        var15.close();
        result = var14.toString();
        sb.append(result);

        try {
            String e = this.formatter.format(Long.valueOf(System.currentTimeMillis()));
            String fileName = e + "_crash.txt";
            if (Environment.getExternalStorageState().equals("mounted")) {
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                FileOutputStream fos = new FileOutputStream(path + "/" + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
                File[] file = dir.listFiles();
                if (file.length > 20) {
                    Arrays.sort(file, new Comparator<File>() {
                        public int compare(File file, File t1) {
                            return file.lastModified() < t1.lastModified() ? -1 : (file.lastModified() > t1.lastModified() ? 1 : 0);
                        }
                    });

                    for (int i = 0; i < file.length / 4; ++i) {
                        file[i].delete();
                    }
                }
            }

            return fileName;
        } catch (Exception var13) {
            var13.printStackTrace();
            return null;
        }
    }
}
