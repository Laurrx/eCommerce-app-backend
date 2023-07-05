package com.qual.store.model;

import com.qual.store.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "image_model")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageModel extends BaseEntity<Long> {

    @Column(unique = true)
    private String name;
    
    private String type;

    @Column(name = "pic_byte", length = 1000)
    private byte[] picByte;

    @OneToOne(mappedBy = "image")
    private Product product;
}
