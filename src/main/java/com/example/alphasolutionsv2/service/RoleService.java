package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Role;
import com.example.alphasolutionsv2.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // Use this when you need a Role with exception handling
    public Role findById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));
    }

    // Use this when you need an Optional<Role>
    public Optional<Role> getRoleById(Long roleId) {
        return roleRepository.findById(roleId);
    }
}
