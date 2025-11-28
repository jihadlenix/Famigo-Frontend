package com.example.famigo_android.ui.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.famigo_android.R;

public class FamigoToast {

    public enum ToastType {
        SUCCESS,
        ERROR,
        INFO,
        WARNING
    }

    /**
     * Show an elegant toast message with custom styling
     * @param context The context
     * @param message The message to display
     * @param type The type of toast (SUCCESS, ERROR, INFO, WARNING)
     */
    public static void show(Context context, String message, ToastType type) {
        if (context == null || message == null || message.isEmpty()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView messageView = layout.findViewById(R.id.toastMessage);
        TextView iconView = layout.findViewById(R.id.toastIcon);

        messageView.setText(message);

        // Set icon and color based on type
        switch (type) {
            case SUCCESS:
                iconView.setText("✓");
                iconView.setTextColor(context.getColor(R.color.famigo_green));
                iconView.setVisibility(View.VISIBLE);
                break;
            case ERROR:
                iconView.setText("✕");
                iconView.setTextColor(context.getColor(R.color.error_red));
                iconView.setVisibility(View.VISIBLE);
                break;
            case INFO:
                iconView.setText("ℹ");
                iconView.setTextColor(context.getColor(R.color.famigo_green_dark));
                iconView.setVisibility(View.VISIBLE);
                break;
            case WARNING:
                iconView.setText("⚠");
                iconView.setTextColor(context.getColor(R.color.warning_orange));
                iconView.setVisibility(View.VISIBLE);
                break;
        }

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    /**
     * Show a success toast (green checkmark)
     */
    public static void success(Context context, String message) {
        show(context, message, ToastType.SUCCESS);
    }

    /**
     * Show an error toast (red X)
     */
    public static void error(Context context, String message) {
        show(context, message, ToastType.ERROR);
    }

    /**
     * Show an info toast (blue info icon)
     */
    public static void info(Context context, String message) {
        show(context, message, ToastType.INFO);
    }

    /**
     * Show a warning toast (orange warning icon)
     */
    public static void warning(Context context, String message) {
        show(context, message, ToastType.WARNING);
    }

    /**
     * Show a simple toast (default, no icon)
     */
    public static void show(Context context, String message) {
        show(context, message, ToastType.INFO);
    }
}

