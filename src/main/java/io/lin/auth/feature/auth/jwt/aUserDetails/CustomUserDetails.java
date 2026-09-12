package io.lin.auth.feature.auth.jwt.aUserDetails;

import io.lin.auth.feature.auth.entity.Auth;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final Auth auth;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Auth auth){
        this.auth = auth;
        this.authorities = auth.getRoles().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.name()))
                .toList();
    }

    public Auth getUser(){
        return auth;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return auth.getPassword();
    }

    @Override
    public String getUsername() {
        return auth.getUsername();
    }
}
