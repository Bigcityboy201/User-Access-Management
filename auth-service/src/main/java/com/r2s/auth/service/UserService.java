package com.r2s.auth.service;

import com.r2s.auth.dto.request.SignInRequest;
import com.r2s.auth.dto.request.SignUpRequest;
import com.r2s.auth.dto.response.SignInResponse;

public interface UserService {
    Boolean signUp(final SignUpRequest user);
    SignInResponse signIn(final SignInRequest request);
}
