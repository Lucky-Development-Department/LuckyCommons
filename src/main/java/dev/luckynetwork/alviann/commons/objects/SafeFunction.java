package dev.luckynetwork.alviann.commons.objects;

public interface SafeFunction<T> {

    /** Invokes the function. */
    T invoke() throws Throwable;

}
