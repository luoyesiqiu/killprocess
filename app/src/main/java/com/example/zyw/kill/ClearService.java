package com.example.zyw.kill;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

/**
 * Created by zyw on 2016/1/9.
 */
public class ClearService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onStart(Intent intent, int startId)
    {
        //如果大于4.4
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
           //clearService();
            killUsage();
        }
        else
        {
            killProcess();
        }
        super.onStart(intent, startId);
    }

    /*
    *获取可用内存大小
     */
    private long getAvailMemory(Context context)
    {
        // 获取android当前可用内存大小
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem / (1024 * 1024);
    }

    /*
    *获取usage，5.x用
    */
    public void  killUsage()
    {
        List<UsageStats> list=getUsageStatsList(this);
        long beforeMem = getAvailMemory(ClearService.this);

        int count = 0;


        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(int i=0;i<list.size();i++)
        {

            UsageStats us=list.get(i);
           // UsageStatsManager usm=getUsageStatsManager(this);
           // am.killBackgroundProcesses(us.getPackageName());
            if(us.getPackageName().equals("com.android.systemui")) {
                continue;
            }
            else if(us.getPackageName().equals("com.example.zyw.kill"))
            {
                continue;
            }
            forceStopPackage(am, us.getPackageName());
            count++;
        }

        long afterMem = getAvailMemory(ClearService.this);
        toast("清理了" + count + "个程序,释放了：" + (afterMem - beforeMem) + "M");
    }

    /*
    *获取usage，5.x用
     */
    public static List<UsageStats> getUsageStatsList(Context context){
        UsageStatsManager usm = getUsageStatsManager(context);
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.YEAR, -1);
        long startTime = calendar.getTimeInMillis();

        List<UsageStats> usageStatsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        return usageStatsList;
    }

    @SuppressWarnings("ResourceType")
    private static UsageStatsManager getUsageStatsManager(Context context){
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService("usagestats");
        return usm;
    }

    /*
    *结束服务，5.x用
     */
    public void clearService()
    {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfos = am.getRunningServices(Integer.MAX_VALUE);

        long beforeMem = getAvailMemory(ClearService.this);

        int count = 0;
        if (serviceInfos != null)
        {
            for (int i = 0; i < serviceInfos.size(); ++i)
            {
                ActivityManager.RunningServiceInfo appProcessInfo = serviceInfos.get(i);

                am.killBackgroundProcesses(appProcessInfo.process);
                count++;
            }
        }

        long afterMem = getAvailMemory(ClearService.this);
        toast("清理了" + count + "个程序,释放了：" + (afterMem - beforeMem) + "M");

    }

    /*
    *结束进程，4.x版本用
     */
    public void killProcess()
    {
        ActivityManager activityManger=(ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list=activityManger.getRunningAppProcesses();//得到正在运行的进程信息
        long beforeMem = getAvailMemory(ClearService.this);

        int count = 0;
        if (list != null) {
            for (int i=0;i < list.size();i++)
            {
                ActivityManager.RunningAppProcessInfo apinfo=list.get(i);
                String[] pkgList=apinfo.pkgList;
                if (apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE)
                {
                    for (int j=0;j < pkgList.length;j++)
                    {
                        activityManger.killBackgroundProcesses(pkgList[j]);
                        count++;
                    }
                }
            }

        }
        long afterMem = getAvailMemory(ClearService.this);

        toast( "清理了" + count + "个程序,释放了："+ (afterMem - beforeMem) + "M");
    }

    public void toast(CharSequence text)
    {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    public void forceStopPackage(ActivityManager am,String packageName) {
        try {
            Method forceStopPackage = am.getClass().getDeclaredMethod("forceStopPackage", String.class);
            forceStopPackage.setAccessible(true);
            forceStopPackage.invoke(am, packageName);
        } catch (Exception e) {

        }
    }
}
