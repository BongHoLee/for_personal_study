package io.security.corespringsecurity.security.safe;

public enum AccountErrTypeCode {

    IdNotFound("IdNotFound"),
    Locked("Locked"),
    BadCredentials("BadCredentials"),
    PasswordChanged("PasswordChanged"),
    PasswordLocked("PasswordLocked"),
    LongTermLocked("LongTermLocked"),
    OTHER("OTHER");

    private final String errCode;

    AccountErrTypeCode(String errCode) {
        this.errCode= errCode;
    }
}
