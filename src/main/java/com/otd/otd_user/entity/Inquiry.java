package com.otd.otd_user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.otd.configuration.enumcode.model.EnumInquiryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inquiry extends CreatedAt{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String senderName;

    @Column(nullable = false)
    private String senderEmail;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "status")
    @Convert(converter = EnumInquiryStatus.CodeConverter.class)
    private EnumInquiryStatus status = EnumInquiryStatus.PENDING;

    @Column
    private String reply;

    @ManyToOne(optional = true)
    @JoinColumn(name = "admin_id", nullable = true)
    private User adminId;

    @Column
    private LocalDateTime replyAt;

    // xml용 status값 필드
    @Transient
    @JsonIgnore
    private String statusCode;
}