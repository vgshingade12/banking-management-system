package com.prasad.banking.dto;

import java.time.LocalDateTime;

public class CustomerResponse {

    private Long id;
    private String customerCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime createdAt;

    public CustomerResponse() {
    }

    public CustomerResponse(Long id, String customerCode, String firstName, String lastName, String email, String phone, String address, LocalDateTime createdAt) {
        this.id = id;
        this.customerCode = customerCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static CustomerResponseBuilder builder() {
        return new CustomerResponseBuilder();
    }

    public static class CustomerResponseBuilder {
        private Long id;
        private String customerCode;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;
        private LocalDateTime createdAt;

        public CustomerResponseBuilder id(Long id) { this.id = id; return this; }
        public CustomerResponseBuilder customerCode(String customerCode) { this.customerCode = customerCode; return this; }
        public CustomerResponseBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public CustomerResponseBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public CustomerResponseBuilder email(String email) { this.email = email; return this; }
        public CustomerResponseBuilder phone(String phone) { this.phone = phone; return this; }
        public CustomerResponseBuilder address(String address) { this.address = address; return this; }
        public CustomerResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public CustomerResponse build() {
            return new CustomerResponse(id, customerCode, firstName, lastName, email, phone, address, createdAt);
        }
    }
}
