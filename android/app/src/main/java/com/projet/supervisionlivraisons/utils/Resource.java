package com.projet.supervisionlivraisons.utils;

/**
 * Tri-state container used by ViewModels to expose loading/success/error state
 * to LiveData observers without leaking framework types.
 */
public class Resource<T> {

    public enum State { LOADING, SUCCESS, ERROR }

    public final State  state;
    public final T      data;
    public final String error;

    private Resource(State s, T d, String e) { state = s; data = d; error = e; }

    public static <T> Resource<T> loading()             { return new Resource<>(State.LOADING, null, null); }
    public static <T> Resource<T> success(T data)       { return new Resource<>(State.SUCCESS, data, null); }
    public static <T> Resource<T> error(String message) { return new Resource<>(State.ERROR,  null, message); }
}
