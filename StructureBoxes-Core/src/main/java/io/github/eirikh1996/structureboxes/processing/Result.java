package io.github.eirikh1996.structureboxes.processing;

import org.jetbrains.annotations.NotNull;

public final class Result {
    public static final Result SUCCESS = new Result(true);
    public static final Result FAILURE = new Result(false);

    public static Result of(boolean success) {
        return new Result(success);
    }
    public static Result of(boolean success, @NotNull String message) {
        return new Result(success, message);
    }

    public static Result fail(String message) {
        return new Result(false, message);
    }

    public static Result fail() {
        return new Result(false);
    }

    public static Result success() {
        return new Result(true);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Result)) {
            return false;
        }
        Result res = (Result) obj;
        return success == res.success;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    private final boolean success;
    private final String message;
    private Result(final boolean success) {
        this(success, "");
    }
    private Result(final boolean success, final String message) {
        this.message = message;
        this.success = success;
    }
}
