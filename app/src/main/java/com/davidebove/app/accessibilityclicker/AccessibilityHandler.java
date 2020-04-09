package com.davidebove.app.accessibilityclicker;

import android.view.accessibility.AccessibilityNodeInfo;

public interface AccessibilityHandler {
    boolean detectAppInfoScreen(AccessibilityNodeInfo nodeInfo);
    boolean detectAppPermissionsScreen(AccessibilityNodeInfo nodeInfo);
    boolean detectPermissionDetails(AccessibilityNodeInfo nodeInfo);
    boolean detectAccessibilitySettings(AccessibilityNodeInfo nodeInfo);
    boolean detectAccessibilityService(AccessibilityNodeInfo nodeInfo);
    boolean detectAccessibilityModal(AccessibilityNodeInfo nodeInfo);

    void togglePermissions(AccessibilityNodeInfo nodeInfo, boolean enable);
    void togglePermissionDetails(AccessibilityNodeInfo nodeInfo, boolean enable);
    void toggleAccessibility(AccessibilityNodeInfo nodeInfo, boolean enable);
    void toggleAccessibilityModal(AccessibilityNodeInfo nodeInfo, boolean enable);

}
