package com.shubham.childtracker.interfaces;

public interface OnPermissionExplanationListener {
    void onOk(int requestCode);

    void onCancel(int switchId);
}
