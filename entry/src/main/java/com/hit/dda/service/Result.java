package com.hit.dda.service;

import java.util.Objects;

public class Result {
    private boolean flags;
    private String message;
    private Object data;

    public Result() {
    }

    public Result(boolean flags, String message, Object data) {
        this.flags = flags;
        this.message = message;
        this.data = data;
    }

    public boolean isFlags() {
        return flags;
    }

    public void setFlags(boolean flags) {
        this.flags = flags;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return flags == result.flags &&
                Objects.equals(message, result.message) &&
                Objects.equals(data, result.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flags, message, data);
    }

    @Override
    public String toString() {
        return "Result{" +
                "flags=" + flags +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

}
