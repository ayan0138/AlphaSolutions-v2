package com.example.alphasolutionsv2.model;

public class Role {
    private Long roleId;
    private String roleName;

    public Role() {
        // Default constructor
    }

    public Role(long roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public Role (String roleName) {
        this.roleName = roleName;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
