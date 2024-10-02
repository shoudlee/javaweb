package com.sky.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptUtil {
    @Autowired
    PasswordEncoder bcryptPassWordEncoder;

    public String encryptPassword(String password) {
        return bcryptPassWordEncoder.encode(password);
    }
    public boolean mathPassword(String rawPwd, String storedPwd){
        return bcryptPassWordEncoder.matches(rawPwd, storedPwd);
    }
}
