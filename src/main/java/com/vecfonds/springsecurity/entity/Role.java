package com.vecfonds.springsecurity.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "role")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

}
