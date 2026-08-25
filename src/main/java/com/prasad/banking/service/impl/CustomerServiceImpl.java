package com.prasad.banking.service.impl;

import com.prasad.banking.dto.CustomerRequest;
import com.prasad.banking.dto.CustomerResponse;
import com.prasad.banking.entity.Customer;
import com.prasad.banking.exception.CustomerNotFoundException;
import com.prasad.banking.exception.DuplicateResourceException;
import com.prasad.banking.repository.CustomerRepository;
import com.prasad.banking.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("Creating customer with email: {}", request.getEmail());

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "A customer with email '" + request.getEmail() + "' already exists.");
        }

        String customerCode = generateCustomerCode();

        Customer customer = Customer.builder()
                .customerCode(customerCode)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully: {} ({})", customerCode, savedCustomer.getId());

        return mapToResponse(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        log.info("Fetching all customers");
        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        log.info("Fetching customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        log.info("Updating customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (!customer.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException(
                        "Email '" + request.getEmail() + "' is already used by another customer.");
            }
        }

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer {} updated successfully", id);

        return mapToResponse(updatedCustomer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);

        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }

        customerRepository.deleteById(id);
        log.info("Customer {} deleted successfully", id);
    }

    private String generateCustomerCode() {
        long count = customerRepository.count() + 1;
        return String.format("CUST-%04d", count);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
