package com.nurun.security;


import com.nurun.repository.UserRepository;


import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String idOrEmail) throws UsernameNotFoundException {

        try {
            Long id = Long.parseLong(idOrEmail);

            return userRepository.findById(id).map(UserPrincipal::new).orElseThrow();
        } catch (Exception e) {
            return userRepository.findByEmail(idOrEmail).map(UserPrincipal::new).orElseThrow();
        }

    }


}
