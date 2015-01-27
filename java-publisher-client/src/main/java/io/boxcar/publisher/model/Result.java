package io.boxcar.publisher.model;

/**
 * Created by jpcarlino on 23/01/15.
 */
public class Result {

    String ok;
    String error;

    int errorCode;

    public Result(int errorCode, String error) {
        this.errorCode = errorCode;
        this.error = error;
        this.ok = null;
    }

    public Result(String ok) {
        this.ok = ok;
        this.error = null;
        this.errorCode = 0;
    }

    public String getOk() {
        return ok;
    }

    public void setOk(String ok) {
        this.ok = ok;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

}
