package com.shubham.childtracker.interfaces;

public interface OnConfirmationListener {
    void onConfirm();

    void onConfirmationCancel();

    void onModeSelected(String parentEmail);
}
