package com.example.Expense_Tracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "login_history")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private Users user;

    @Column(name = "login_time")
    private OffsetDateTime loginTime;

    @Column(name = "logout_time")
    private OffsetDateTime logoutTime;

    @Column(name = "ip_address",length = 45)
    private String ipaddress;

    @Column(length = 100)
    private String device;
}
