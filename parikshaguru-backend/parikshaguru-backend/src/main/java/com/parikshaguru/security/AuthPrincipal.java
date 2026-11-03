package com.parikshaguru.security;

import com.parikshaguru.entity.enums.Role;

public record AuthPrincipal(Long id, String email, Role role) {}
