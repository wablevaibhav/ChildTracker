package com.shubham.childtracker.interfaces;

public interface OnPasswordResetListener {
    void onOkClicked(String email);

    void onCancelClicked();
}
