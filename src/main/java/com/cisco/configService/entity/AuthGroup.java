package com.cisco.configService.entity;

import com.cisco.configService.model.preConfig.AuthGroupData;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;


@Data
public class AuthGroup {

    @Id
    Long id;

    @UniqueElements
    private String name;

    AuthGroupData.LoginType loginType;

    private String username;

    private byte[] password;

    private byte[] confirmPassword;

    private LocalDate updateDate;
}


