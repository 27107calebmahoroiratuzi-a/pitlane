package rw.ac.auca.garagerepairshopmanagementsystem.security;

public enum Role {
    SYSTEM_ADMIN(50),
    GARAGE_ADMIN(40),
    MANAGER(30),
    STAFF(20),
    USER(10),
    ADMIN(40);

    private final int invitationLevel;

    Role(int invitationLevel) {
        this.invitationLevel = invitationLevel;
    }

    public Role effectiveRole() {
        return this == ADMIN ? GARAGE_ADMIN : this;
    }

    public boolean canInvite(Role invitedRole) {
        Role inviter = effectiveRole();
        Role invitee = invitedRole.effectiveRole();
        return inviter == SYSTEM_ADMIN
                ? invitee == GARAGE_ADMIN
                : inviter.invitationLevel > invitee.invitationLevel;
    }
}
