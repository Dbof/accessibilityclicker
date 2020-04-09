package com.davidebove.app.accessibilityclicker;

import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class Android9Handler implements AccessibilityHandler {
    private static final String TAG = Android9Handler.class.getSimpleName();

    private static String PACKAGE_SETTINGS = "com.android.settings";
    private static String PACKAGE_PERMISSIONS = "com.google.android.packageinstaller";
    private static String PERMISSION_LIST_CLASS = "android.widget.ListView";

    @Override
    public boolean detectAppInfoScreen(AccessibilityNodeInfo nodeInfo) {
        String packageName = ""+nodeInfo.getPackageName();
        boolean isAppInfo = nodeInfo.findAccessibilityNodeInfosByText("App Info").size() == 1;
        return packageName.equals(PACKAGE_SETTINGS) && isAppInfo;
    }

    @Override
    public boolean detectAppPermissionsScreen(AccessibilityNodeInfo nodeInfo) {
        String packageName = ""+nodeInfo.getPackageName();
        boolean hasPermissionList = false;

        if (nodeInfo.getChildCount() == 6) {
            AccessibilityNodeInfo permissionsList = null;

            permissionsList = nodeInfo.getChild(5);
            String className = ""+permissionsList.getClassName();
            if (className.equals("android.widget.ListView")) {
                hasPermissionList = true;
            }
        }
        return packageName.equals(PACKAGE_PERMISSIONS) && hasPermissionList;
    }

    @Override
    public boolean detectPermissionDetails(AccessibilityNodeInfo nodeInfo) {
        return false;
    }

    @Override
    public void togglePermissions(AccessibilityNodeInfo nodeInfo, boolean enable) {
        AccessibilityNodeInfo permissionsList = nodeInfo.getChild(5);
        String className = ""+permissionsList.getClassName();
        if (className.equals(PERMISSION_LIST_CLASS)) {
            for (int i = 0; i < permissionsList.getChildCount(); i++) {
                AccessibilityNodeInfo child = permissionsList.getChild(i);
                if (child.getChildCount() == 2 && (child.getChild(1).isChecked() ^ enable))
                    child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    @Override
    public void togglePermissionDetails(AccessibilityNodeInfo nodeInfo, boolean enable) {

    }


    @Override
    public boolean detectAccessibilitySettings(AccessibilityNodeInfo nodeInfo) {
        return false;
    }

    @Override
    public boolean detectAccessibilityService(AccessibilityNodeInfo nodeInfo) {
        String packageName = ""+nodeInfo.getPackageName();
        boolean mightBeService = false;
        if (nodeInfo.getChildCount() == 4 && nodeInfo.getChild(2).getChildCount() == 2) {
            String switchElement = ""+nodeInfo.getChild(2).getChild(1).getClassName();
            mightBeService = "android.widget.Switch".equals(switchElement);
        }
        return packageName.equals(PACKAGE_SETTINGS) && mightBeService;
    }

    @Override
    public boolean detectAccessibilityModal(AccessibilityNodeInfo nodeInfo) {
        String packageName = ""+nodeInfo.getPackageName();
        Rect bounds = new Rect();
        nodeInfo.getBoundsInScreen(bounds);
        if (bounds.left != 0 && bounds.top != 0) {
            if (nodeInfo.getChildCount() == 3) {
                List<AccessibilityNodeInfo> found = nodeInfo.findAccessibilityNodeInfosByText("OK");
                if (found.size() == 1) {
                    return packageName.equals(PACKAGE_SETTINGS);
                }
            }
        }
        return false;
    }

    @Override
    public void toggleAccessibility(AccessibilityNodeInfo nodeInfo, boolean enable) {
        try {
            if (nodeInfo.getChildCount() >= 4) {
                AccessibilityNodeInfo theSwitch = nodeInfo.getChild(2).getChild(1);
                if (theSwitch.isChecked() ^ enable)
                    theSwitch.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        } catch (NullPointerException npe) {
            // ignore
            Log.e(TAG, "Error while toggling service");
        }
    }

    @Override
    public void toggleAccessibilityModal(AccessibilityNodeInfo nodeInfo, boolean enable) {
        String textSearch = "ok";

        List<AccessibilityNodeInfo> allowNodes = nodeInfo.findAccessibilityNodeInfosByText(textSearch);
        for (AccessibilityNodeInfo node : allowNodes) {
            String buttonClass = "" +node.getClassName();
            if ("android.widget.Button".equals(buttonClass))
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }
}
