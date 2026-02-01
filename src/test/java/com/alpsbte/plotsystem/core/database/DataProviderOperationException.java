package com.alpsbte.plotsystem.core.database;

/**
 * Default {@value #DEFAULT_MESSAGE}
 *
 * @see #get() get default instance.
 */
public class DataProviderOperationException extends UnsupportedOperationException {
    protected static final String DEFAULT_MESSAGE = "DataProvider not available in test suite which is required for this method call.";
    protected static DataProviderOperationException instance;

    public static DataProviderOperationException get() {
        if(instance == null)
            instance = new DataProviderOperationException(DEFAULT_MESSAGE);

        return instance;
    }

    public DataProviderOperationException(String message) {
        super(message);
    }
}
