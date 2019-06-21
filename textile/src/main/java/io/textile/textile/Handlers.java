package io.textile.textile;

import io.textile.pb.Model;

/**
 * Wrapper around common handlers
 */
public class Handlers {

    /**
     * Interface representing an object that can be
     * called to indicate completion
     */
    public interface ErrorHandler {
        /**
         * Called to indicate completion
         */
        void onComplete();

        /**
         * Called in the case of an error
         * @param e The exception
         */
        void onError(Exception e);
    }

    /**
     * Interface representing an object that can be
     * called with a data and media type result
     */
    public interface DataHandler {
        /**
         * Called with a data and meta result
         * @param data The data
         * @param meta The media type
         */
        void onComplete(byte[] data, String media);

        /**
         * Called in the case of an error
         * @param e The exception
         */
        void onError(Exception e);
    }

    /**
     * Interface representing an object that can be
     * called with a Block result
     */
    public interface BlockHandler {
        /**
         * Called with a block result
         * @param block The block
         */
        void onComplete(Model.Block block);

        /**
         * Called in the case of an error
         * @param e The exception
         */
        void onError(Exception e);
    }

    /**
     * Interface representing an object that can be
     * called with a cafe session
     */
    public interface CafeSessionHandler {
        /**
         * Called with a cafe session
         * @param session The cafe session
         */
        void onComplete(Model.CafeSession session);

        /**
         * Called in the case of an error
         * @param e The exception
         */
        void onError(Exception e);
    }

}