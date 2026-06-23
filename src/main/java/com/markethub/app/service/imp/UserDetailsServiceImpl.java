package com.markethub.app.service.imp;

import com.markethub.app.model.User;
import com.markethub.app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UsernameNotFoundException("Username " + userName + " not found"));
        log.debug("Loaded user username={}", userName);
        return new org.springframework.security.core.userdetails.User(
                user.getUserName(), user.getPassword(), getAuthorities(user));
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private static Collection<? extends GrantedAuthority> getAuthorities(User user) {
        if (user.getRoles() == null) {
            return AuthorityUtils.NO_AUTHORITIES;
        }
        String[] userRoles = user.getRoles().stream()
                .map(role -> role.getRoleType())
                .toArray(String[]::new);
        return AuthorityUtils.createAuthorityList(userRoles);
    }
}
