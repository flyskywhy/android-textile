package io.textile.textile;

import android.content.Context;

import net.gotev.uploadservice.Logger;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import mobile.Mobile_;

public class UploadServiceReceiver extends UploadServiceBroadcastReceiver {

    Mobile_ node;

    UploadServiceReceiver(final Mobile_ node) {
        this.node = node;
    }

    @Override
    public void onProgress(Context context, UploadInfo info) {
        try {
            node.updateCafeRequestProgress(info.getUploadId(), info.getUploadedBytes(), info.getTotalBytes());
        } catch (final Exception e) {
            Logger.error(getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void onError(Context context, UploadInfo info, ServerResponse response, Exception exception) {
        String message = "Request failed (";
        if (response != null) {
            message += "code=" + response.getHttpCode() + " ";
            message += "body=" + response.getBodyAsString() + " ";
        }

        if (exception != null) {
            message += "error=" + exception.getMessage();
        } else {
            message += "error=unknown";
        }
        message += ")";

        try {
            node.failCafeRequest(info.getUploadId(), message);
        } catch (final Exception e) {
            Logger.error(getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void onCompleted(Context context, UploadInfo info, ServerResponse response) {
        try {
            node.completeCafeRequest(info.getUploadId());
        } catch (final Exception e) {
            Logger.error(getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void onCancelled(Context context, UploadInfo info) {
        try {
            node.failCafeRequest(info.getUploadId(), "Request cancelled");
        } catch (final Exception e) {
            Logger.error(getClass().getSimpleName(), e.getMessage());
        }
    }
}
