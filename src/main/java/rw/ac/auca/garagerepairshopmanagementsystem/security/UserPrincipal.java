package rw.ac.auca.garagerepairshopmanagementsystem.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

    private final AppUser appUser;

    public UserPrincipal(AppUser appUser) {
        this.appUser = appUser;
    }

    public static UserPrincipal from(AppUser appUser) {
        return new UserPrincipal(appUser);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return appUser.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.effectiveRole().name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return appUser.getPassword();
    }

    @Override
    public String getUsername() {
        return appUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return appUser.isEnabled();
    }

    public Long getId() {
        return appUser.getId();
    }

    public String getEmail() {
        return appUser.getEmail();
    }

    public Set<Role> getRoles() {
        return appUser.getRoles();
    }

    public Long getGarageId() {
        return appUser.getGarage() == null ? null : appUser.getGarage().getId();
    }

    public String getGarageName() {
        return appUser.getGarage() == null ? null : appUser.getGarage().getName();
    }
}
