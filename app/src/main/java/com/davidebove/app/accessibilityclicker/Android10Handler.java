package com.davidebove.app.accessibilityclicker;

import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class Android10Handler implements AccessibilityHandler {
    private static final String TAG = Android10Handler.class.getSimpleName();

    private static String PACKAGE_SETTINGS = "com.android.settings";
    private static final String PACKAGE_PERMISSIONS = "com.google.android.permissioncontroller";
    private static final String PERMISSION_LIST_CLASS = "androidx.recyclerview.widget.RecyclerView";

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

            List<AccessibilityNodeInfo> deniedList = nodeInfo.findAccessibilityNodeInfosByText("Denied");
            if (!deniedList.isEmpty()) {
                permissionsList = deniedList.get(0).getParent().getParent();  // androidx.recyclerview.widget.RecyclerView
                String className = ""+permissionsList.getClassName();
                if ("androidx.recyclerview.widget.RecyclerView".equals(className))
                    hasPermissionList = true;
            }
        }
        return packageName.equals(PACKAGE_PERMISSIONS) && hasPermissionList;
    }

    @Override
    public void togglePermissions(AccessibilityNodeInfo nodeInfo, boolean enable) {
        AccessibilityNodeInfo permissionsList = nodeInfo.getChild(5).getChild(2);
        String className = ""+permissionsList.getClassName();
        if (className.equals(PERMISSION_LIST_CLASS)) {
            // list is structured into ALLOWED and DENIED
            // Every element in list is a linearlayout (index 0) with a textView inside

            // find index of allowed and denied
            int index_allowed = 1, index_denied = 0;
            for (int i = 0; i < permissionsList.getChildCount(); i++) {
                AccessibilityNodeInfo childText = permissionsList.getChild(i).getChild(0);
                String text = ""+childText.getText();
                if (text.toLowerCase().equals("denied"))
                    index_denied = i+1;
            }

            if (enable) {
                // only activate denied ones
                for (int i = index_denied; i < permissionsList.getChildCount(); i++) {
                    AccessibilityNodeInfo element = permissionsList.getChild(i);
                    element.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            } else {
                for (int i = index_allowed; i < index_denied-1; i++) {
                    AccessibilityNodeInfo element = permissionsList.getChild(i);
                    element.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }

            Log.i(TAG, "Permissions: " + permissionsList.getChildCount());
        }
    }

    @Override
    public boolean detectPermissionDetails(AccessibilityNodeInfo nodeInfo) {
        String packageName = ""+nodeInfo.getPackageName();
        List<AccessibilityNodeInfo> allowElements = nodeInfo.findAccessibilityNodeInfosByText("Allow");
        allowElements.addAll(nodeInfo.findAccessibilityNodeInfosByText("Deny"));

        // there can be multiple allow, but only one deny
        return allowElements.size() >= 2;
    }

    @Override
    public void togglePermissionDetails(AccessibilityNodeInfo nodeInfo, boolean enable) {
        String searchText = (enable) ? "Allow" : "Deny";
        List<AccessibilityNodeInfo> radios = nodeInfo.findAccessibilityNodeInfosByText(searchText);
        if (radios.size() > 0) {
            AccessibilityNodeInfo el = radios.get(0);
            String className = ""+el.getClassName();
            if ("android.widget.RadioButton".equals(className)) {
                el.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                nodeInfo.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);

                // TODO: some permissions open another dialog:
                //  "if you disable this, basic functionality bla bla will not work"
            }
        }
    }



    @Override
    public boolean detectAccessibilitySettings(AccessibilityNodeInfo nodeInfo) {
        String packageName = ""+nodeInfo.getPackageName();
        if (nodeInfo.getChildCount() >= 2) {
            AccessibilityNodeInfo title = nodeInfo.getChild(1);
            if ("Accessibility".equals("" + title.getText()))
                return packageName.equals(PACKAGE_SETTINGS);
        }
        return false;
    }

    @Override
    public boolean detectAccessibilityService(AccessibilityNodeInfo nodeInfo) {
        String packageName = ""+nodeInfo.getPackageName();
        if (nodeInfo.getChildCount() >= 4) {
            try {
                String textViewClass = ""+nodeInfo.getChild(1).getClassName();
                String switchClass = ""+nodeInfo.getChild(2).getChild(1).getClassName();
                return packageName.equals(PACKAGE_SETTINGS) && "android.widget.TextView".equals(textViewClass) &&
                        "android.widget.Switch".equals(switchClass);

            } catch (NullPointerException npe) {
                return false;
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
                    theSwitch.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        } catch (NullPointerException npe) {
            // ignore
            Log.e(TAG, "Error while toggling service");
        }
    }

    @Override
    public boolean detectAccessibilityModal(AccessibilityNodeInfo nodeInfo) {
        String packageName = ""+nodeInfo.getPackageName();
        Rect bounds = new Rect();
        //
        nodeInfo.getBoundsInScreen(bounds);
        if (bounds.left != 0 && bounds.top != 0) {
            List<AccessibilityNodeInfo> allowNodes = nodeInfo.findAccessibilityNodeInfosByText("Allow");
            for (AccessibilityNodeInfo node : allowNodes) {
                String buttonClass = "" +node.getClassName();
                if ("android.widget.Button".equals(buttonClass))
                    return PACKAGE_SETTINGS.equals(packageName);
            }
        }
        return false;
    }

    @Override
    public void toggleAccessibilityModal(AccessibilityNodeInfo nodeInfo, boolean enable) {
        String textSearch = (enable) ? "Allow" : "Stop";

        List<AccessibilityNodeInfo> allowNodes = nodeInfo.findAccessibilityNodeInfosByText(textSearch);
        for (AccessibilityNodeInfo node : allowNodes) {
            String buttonClass = "" +node.getClassName();
            if ("android.widget.Button".equals(buttonClass))
                node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        }
    }
}
