package com.prasad.banking.dto;

import jakarta.validation.constraints.*;

public class CustomerRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Please provide a valid 10-digit Indian mobile number")
    private String phone;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    public CustomerRequest() {
    }

    public CustomerRequest(String firstName, String lastName, String email, String phone, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public static CustomerRequestBuilder builder() {
        return new CustomerRequestBuilder();
    }

    public static class CustomerRequestBuilder {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;

        public CustomerRequestBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public CustomerRequestBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public CustomerRequestBuilder email(String email) { this.email = email; return this; }
        public CustomerRequestBuilder phone(String phone) { this.phone = phone; return this; }
        public CustomerRequestBuilder address(String address) { this.address = address; return this; }

        public CustomerRequest build() {
            return new CustomerRequest(firstName, lastName, email, phone, address);
        }
    }
}
