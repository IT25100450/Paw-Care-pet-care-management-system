package com.example.pawcare.dto;

import lombok.Data;

@Data
public class SignupRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String contactNo;
    private String houseNo = "";
    private String streetName = "";
    private String city = "";
    private String province = "";
    private String postalCode = "";
}
