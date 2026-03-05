package com.hrishabh.algocrack.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
@Setter
public abstract class BaseModel {

    @Id // Makes the property a primary key of our table
    @GeneratedValue(strategy = GenerationType.IDENTITY) // it means auto increment
    protected long id;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP) // This tells the springboot about the format of date to be stored
    @CreatedDate // Stores the createdAt date automatically (At the time of object creation)
    protected Date createdAt;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate // tells the spring only handle it when the object is updated
    protected Date updatedAt;

}
