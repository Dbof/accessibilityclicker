package com.davidebove.app.accessibilityclicker;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.accessibility.AccessibilityManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!accessibilityEnable(this)) {
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, 0);
        }
    }


    public static boolean accessibilityEnable(Context context) {
        boolean enable = false;
        AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceList = null;
        if (manager != null) {
            serviceList = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
            if (serviceList.isEmpty())
                return false;

            for (AccessibilityServiceInfo serviceInfo : serviceList) {
                String id = serviceInfo.getId();
                if ("com.davidebove.app.accessibilityclicker/.ClickerService".equals(id))
                    enable = true;
            }
        }
        return enable;
    }
}
