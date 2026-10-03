package rw.ac.auca.garagerepairshopmanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MechanicRequest {
    @NotBlank
    @Size(min = 3, max = 100)
    private String fullName;

    @NotBlank
    @Size(min = 7, max = 30)
    private String phone;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 100)
    private String specialization;

    private Boolean active;

    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getSpecialization() { return specialization; }
    public Boolean getActive() { return active; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setActive(Boolean active) { this.active = active; }
}