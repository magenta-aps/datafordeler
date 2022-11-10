package dk.magenta.datafordeler.core.role;

public enum SystemRoleGrant {
    Read, // Read objects & commands
    Write, // Write, create or delete objects
    Grant, // Grant other users access to objects
    Custom,
    Execute, // Execute commands
    Stop // Stop commands
}
