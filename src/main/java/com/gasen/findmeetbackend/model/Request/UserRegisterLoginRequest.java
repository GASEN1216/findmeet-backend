package com.gasen.findmeetbackend.model.Request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserRegisterLoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 5091338187011328908L;
    private String userAccount;
    private String password;
}
