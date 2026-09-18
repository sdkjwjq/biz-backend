package org.example.utils;

/** 登录后使用业务功能前必须满足的密码规则。 */
public final class PasswordPolicy {
    private PasswordPolicy() { }

    public static boolean requiresChange(String password) {
        return password == null || password.length() < 6
                || !password.matches("(?s).*[A-Z].*")
                || !password.matches("(?s).*[a-z].*")
                || !password.matches("(?s).*[0-9].*");
    }

    public static void validate(String password) {
        if (requiresChange(password)) {
            throw new IllegalArgumentException("新密码至少6位，并同时包含大写字母、小写字母和数字");
        }
    }
}
