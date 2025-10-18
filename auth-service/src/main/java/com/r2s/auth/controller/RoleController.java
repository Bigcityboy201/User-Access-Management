package com.r2s.auth.controller;

import com.r2s.auth.entity.Role;
import com.r2s.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }

    @PostMapping
    public Role createRole(@RequestBody CreateRoleRequest request) {
        return roleService.createRole(request.getName(), request.getDescription());
    }

    @GetMapping("/{id}")
    public Role getRoleById(@PathVariable Integer id) {
        return roleService.getRoleById(id);
    }

    @DeleteMapping("/{id}")
    public String deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return "Role deleted successfully";
    }

    // Inner class for request
    public static class CreateRoleRequest {
        private String name;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
