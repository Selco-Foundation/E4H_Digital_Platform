package org.egov.user.domain.service.otplogin;

/**
 * Short-lived key/value state for OTP login (OTP hashes, counters, blocks, login tickets).
 * Every key carries its own expiry.
 */
public interface OtpLoginStore {

    String get(String key);

    void put(String key, String value, long ttlSeconds);

    /**
     * @return true if the key did not exist and was set
     */
    boolean putIfAbsent(String key, String value, long ttlSeconds);

    /**
     * Increments a counter. The expiry is set only when the counter is created, so the window is fixed
     * from the first increment.
     *
     * @return the value after incrementing
     */
    long increment(String key, long ttlSeconds);

    /**
     * @return true if the key existed and was removed by this call
     */
    boolean delete(String key);
}
