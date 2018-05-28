package edu.thanhle.audio.recording;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissions
{
    private final Activity activity;

    public Permissions(final Activity activity)
    {
        this.activity = activity;
    }

    public void request(int requestCode, String... permissions)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            String[] permissionsGranted = permissionsWithStatus(activity, PackageManager.PERMISSION_GRANTED, permissions);

            if (permissionsGranted.length > 0)
            {
                grantPermissions(requestCode, permissionsGranted);
            }

            String[] permissionsNotGranted = permissionsWithStatus(activity, PackageManager.PERMISSION_DENIED, permissions);

            if (permissionsNotGranted.length > 0)
            {
                grantPermissions(requestCode, permissionsNotGranted);
            }
        }
        else
        {
            grantPermissions(requestCode, permissions);
        }
    }

    private void grantPermissions(int requestCode, String... permissions)
    {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    private String[] permissionsWithStatus(Context context, int status, String... permissions)
    {
        List<String> result = new ArrayList<>();

        for (String permission : permissions)
        {
            if (ContextCompat.checkSelfPermission(context, permission) == status)
            {
                result.add(permission);
            }
        }

        return result.toArray(new String[result.size()]);
    }
}
