package com.prasad.banking.service;

import com.prasad.banking.dto.CustomerRequest;
import com.prasad.banking.dto.CustomerResponse;

import java.util.List;

/**
 * CustomerService interface defines the contract for customer operations.
 *
 * Why an interface?
 *   1. Testability: In tests, we can use Mockito to mock this interface
 *      without needing a real database.
 *   2. Loose coupling: The controller depends on the interface, not the
 *      implementation. We can swap implementations without changing the controller.
 *   3. SOLID Principle: Dependency Inversion — depend on abstractions.
 */
public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest request);

    List<CustomerResponse> getAllCustomers();

    CustomerResponse getCustomerById(Long id);

    CustomerResponse updateCustomer(Long id, CustomerRequest request);

    void deleteCustomer(Long id);
}
