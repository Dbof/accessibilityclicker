package com.davidebove.app.accessibilityclicker;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;

import java.util.List;

public class ClickerService extends AccessibilityService {
    private static final String TAG = ClickerService.class.getSimpleName();
    private AccessibilityHandler handler = null;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            handler = new Android10Handler();
        } else {
            handler = new Android9Handler();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e("!!!", ""+event);

        AccessibilityNodeInfo nodeInfo = event.getSource();
        if (nodeInfo != null) {

            if (handler.detectAppInfoScreen(nodeInfo)) {
                // focus on permissions
                List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText("Permissions");
                if (nodes.size() > 0) {
                    nodes.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                }
            } else if (handler.detectAppPermissionsScreen(nodeInfo)) {
                handler.togglePermissions(nodeInfo, true);
            } else if (handler.detectPermissionDetails(nodeInfo)) {
                handler.togglePermissionDetails(nodeInfo, true);
            } else if (handler.detectAccessibilitySettings(nodeInfo)) {
                Log.i("!!!", "You are on the Accessibility Settings Screen");
            } else if (handler.detectAccessibilityService(nodeInfo)) {
                handler.toggleAccessibility(nodeInfo, true);
            } else if (handler.detectAccessibilityModal(nodeInfo)) {
                handler.toggleAccessibilityModal(nodeInfo, true);
            } else {
                Log.e("!!!", ""+event);
            }

            nodeInfo.recycle();
        }
    }

    @Override
    public void onInterrupt() {}
}
