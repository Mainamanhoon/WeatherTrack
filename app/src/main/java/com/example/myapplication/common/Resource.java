package com.example.myapplication.common;



public class Resource<T> {
    private final Status status;
    private final T data;
    private final String message;
    private final Throwable throwable;

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    private Resource(Status status, T data, String message, Throwable throwable) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.throwable = throwable;
    }

    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null, null);
    }

    public static <T> Resource<T> error(String message, T data) {
        return new Resource<>(Status.ERROR, data, message, null);
    }

    public static <T> Resource<T> error(String message, T data, Throwable throwable) {
        return new Resource<>(Status.ERROR, data, message, throwable);
    }

    public static <T> Resource<T> loading(T data) {
        return new Resource<>(Status.LOADING, data, null, null);
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null, null);
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Resource<?> resource = (Resource<?>) obj;

        if (status != resource.status) {
            return false;
        }
        if (data != null ? !data.equals(resource.data) : resource.data != null) {
            return false;
        }
        return message != null ? message.equals(resource.message) : resource.message == null;
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "status=" + status +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }
}