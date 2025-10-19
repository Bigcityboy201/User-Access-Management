package com.r2s.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Integer id;

    @Column(nullable = false,unique = true)
    private String roleName;

    private String description;

    @Column(columnDefinition = "bit default 1")
    private boolean isActive;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;
}
