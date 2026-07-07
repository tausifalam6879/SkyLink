package com.skylink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "passengers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "booking_id",
            nullable = false
    )
    private Booking booking;

    @Column(
            name = "first_name",
            nullable = false,
            length = 100
    )
    private String firstName;

    @Column(
            name = "last_name",
            nullable = false,
            length = 100
    )
    private String lastName;

    @Column(
            name = "date_of_birth",
            nullable = false
    )
    private LocalDate dateOfBirth;

    @Column(
            nullable = false,
            length = 20
    )
    private String gender;

    @Column(
            name = "passport_number",
            length = 50
    )
    private String passportNumber;

    @Column(
            name = "nationality",
            nullable = false,
            length = 100
    )
    private String nationality;

    @Column(
            name = "seat_number",
            length = 20
    )
    private String seatNumber;

    @Column(
            nullable = false
    )
    private boolean active;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime currentTime =
                LocalDateTime.now();

        createdAt = currentTime;
        updatedAt = currentTime;

        if (!active) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}