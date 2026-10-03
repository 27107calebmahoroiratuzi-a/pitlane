package rw.ac.auca.garagerepairshopmanagementsystem.security;

import jakarta.persistence.*;
import rw.ac.auca.garagerepairshopmanagementsystem.model.Garage;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "app_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_app_user_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_app_user_email", columnNames = "email")
        }
)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "app_user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    private boolean enabled = true;

    @ManyToOne
    @JoinColumn(name = "garage_id", foreignKey = @ForeignKey(name = "fk_app_user_garage"))
    private Garage garage;

    public AppUser() {
    }

    public AppUser(String username, String email, String password, Set<Role> roles) {
        this(username, email, password, roles, null);
    }

    public AppUser(String username, String email, String password, Set<Role> roles, Garage garage) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles == null ? new HashSet<>() : new HashSet<>(roles);
        this.garage = garage;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Garage getGarage() {
        return garage;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles == null ? new HashSet<>() : new HashSet<>(roles);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setGarage(Garage garage) {
        this.garage = garage;
    }
}
