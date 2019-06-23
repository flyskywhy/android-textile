package io.textile.textile;

import net.gotev.uploadservice.BinaryUploadRequest;
import net.gotev.uploadservice.HttpUploadRequest;
import net.gotev.uploadservice.Logger;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import io.textile.pb.Model.CafeHTTPRequest;
import io.textile.pb.View.Strings;

/**
 * Handles HTTP requests queued by the Textile node
 */
class RequestsHandler {

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private int batchSize;

    RequestsHandler(final int batchSize) {
        this.batchSize = batchSize;
    }

    void flush() {
        final Future<Void> future = executor.submit(() -> {
            try {
                // List a batch of request ids
                final byte[] result = Textile.instance().cafes.cafeRequests(batchSize);
                if (result == null) {
                    return null;
                }
                final Strings ids = Strings.parseFrom(result);

                // Handle requests in parallel
                final List<CompletableFuture<String>> futures = ids.getValuesList()
                        .stream()
                        .map(this::getUpload)
                        .collect(Collectors.toList());
                final CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0]));
                final CompletableFuture<List<String>> uploads = allFutures
                        .thenApply(f -> futures.stream()
                                .map(CompletableFuture::join)
                                .collect(Collectors.toList()));
                uploads.get();

            } catch (Exception e) {
                Logger.error(getClass().getSimpleName(), e.getMessage());
            }
            return null;
        });

        try {
            future.get();
        } catch (Exception e) {
            Logger.error(getClass().getSimpleName(), e.getMessage());
        }
    }

    private CompletableFuture<String> getUpload(String id) {
        return CompletableFuture.supplyAsync(() -> {
            final CompletableFuture<String> inner = new CompletableFuture<>();

            // Write the request to disc
            Textile.instance().cafes.writeCafeRequest(id, (req, e) -> {
                if (e != null) {
                    try {
                        Textile.instance().cafes.cafeRequestNotPending(id);
                        inner.completeExceptionally(e);
                    } catch (final Exception ee) {
                        inner.completeExceptionally(ee);
                    }
                    return;
                }

                // Mark as pending
                try {
                    Textile.instance().cafes.cafeRequestPending(id);
                } catch (final Exception eee) {
                    inner.completeExceptionally(eee);
                    return;
                }

                // Start the upload
                try {
                    final String uploadId = startUpload(id, CafeHTTPRequest.parseFrom(req));
                    inner.complete(uploadId);
                } catch (final Exception ee) {
                    try {
                        Textile.instance().cafes.cafeRequestNotPending(id);
                        inner.completeExceptionally(ee);
                    } catch (final Exception eee) {
                        inner.completeExceptionally(eee);
                    }
                }
            });

            return inner.join();
        });
    }

    private String startUpload(final String id, final CafeHTTPRequest req) throws Exception {
        final UploadNotificationConfig config = new UploadNotificationConfig();
        config.setTitleForAllStatuses("Sync");
        config.setRingToneEnabled(false);
        config.getCompleted().autoClear = true;
        config.getProgress().autoClear = true;
        config.getCancelled().autoClear = true;
        config.getError().autoClear = true;

        final HttpUploadRequest request = new BinaryUploadRequest(Textile.instance().getApplicationContext(), id, req.getUrl())
                .setFileToUpload(req.getPath())
                .setMethod(req.getType().toString())
                .setMaxRetries(2)
                .setNotificationConfig(config)
                .setDelegate(null);

        for (final Map.Entry<String,String> entry : req.getHeadersMap().entrySet()) {
            request.addHeader(entry.getKey(), entry.getValue());
        }

        return request.startUpload();
    }
}
