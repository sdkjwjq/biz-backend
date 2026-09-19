package org.example.service;

/** 仅用于纪实模块的明确业务错误，不改变旧接口的错误处理。 */
public class WorkRecordException extends RuntimeException {
    private final int code;
    public WorkRecordException(int code, String message) {
        super(message);
        this.code = code;
    }
    public int getCode() { return code; }
}
