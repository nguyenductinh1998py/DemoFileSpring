package com.spring.file.model;

import javax.persistence.Entity;

import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "file")
public class FileEntity {

    @Id
    private String id;

    private String name;

    private String contentType;

    private Long size;

    @Lob
    private byte[] data;

}