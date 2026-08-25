package com.prasad.banking.service;

import com.prasad.banking.dto.CustomerRequest;
import com.prasad.banking.dto.CustomerResponse;
import com.prasad.banking.entity.Customer;
import com.prasad.banking.exception.CustomerNotFoundException;
import com.prasad.banking.exception.DuplicateResourceException;
import com.prasad.banking.repository.CustomerRepository;
import com.prasad.banking.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for CustomerServiceImpl.
 *
 * @ExtendWith(MockitoExtension.class):
 *   Enables Mockito in JUnit 5. No Spring context needed — pure Java tests.
 *   Faster, isolated, don't require a database.
 *
 * @Mock: Creates a mock (fake) CustomerRepository.
 *   We define exactly what it returns — no real DB calls.
 *
 * @InjectMocks: Creates a real CustomerServiceImpl and injects the mocks.
 *   This lets us test the service logic in complete isolation.
 *
 * Why mock the repository?
 *   Unit tests should test ONE thing (the service logic).
 *   The repository is tested separately (integration tests).
 *   Mocking makes tests fast and independent of DB state.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private CustomerRequest validRequest;
    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        // Reusable test data — runs before EACH test method
        validRequest = CustomerRequest.builder()
                .firstName("Vaibhavi")
                .lastName("Shingade")
                .email("vaibhavi@email.com")
                .phone("9876543210")
                .address("Pune, Maharashtra")
                .build();

        savedCustomer = Customer.builder()
                .id(1L)
                .customerCode("CUST-0001")
                .firstName("Vaibhavi")
                .lastName("Shingade")
                .email("vaibhavi@email.com")
                .phone("9876543210")
                .address("Pune, Maharashtra")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // -----------------------------------------------------------------------
    // CREATE CUSTOMER TESTS
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Create customer — success")
    void createCustomer_Success() {
        // ARRANGE — set up mock behavior
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.count()).thenReturn(0L);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        // ACT — call the method under test
        CustomerResponse response = customerService.createCustomer(validRequest);

        // ASSERT — verify the result
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("vaibhavi@email.com");
        assertThat(response.getFirstName()).isEqualTo("Vaibhavi");
        assertThat(response.getCustomerCode()).isEqualTo("CUST-0001");

        // Verify that save() was called exactly once
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Create customer — duplicate email throws DuplicateResourceException")
    void createCustomer_DuplicateEmail_ThrowsException() {
        // ARRANGE — simulate email already existing
        when(customerRepository.existsByEmail("vaibhavi@email.com")).thenReturn(true);

        // ACT & ASSERT — verify exception is thrown with correct message
        assertThatThrownBy(() -> customerService.createCustomer(validRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("vaibhavi@email.com");

        // Verify save() was NEVER called (we reject before saving)
        verify(customerRepository, never()).save(any(Customer.class));
    }

    // -----------------------------------------------------------------------
    // GET CUSTOMER TESTS
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Get customer by ID — found")
    void getCustomerById_Found() {
        // ARRANGE
        when(customerRepository.findById(1L)).thenReturn(Optional.of(savedCustomer));

        // ACT
        CustomerResponse response = customerService.getCustomerById(1L);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("vaibhavi@email.com");
    }

    @Test
    @DisplayName("Get customer by ID — not found throws CustomerNotFoundException")
    void getCustomerById_NotFound_ThrowsException() {
        // ARRANGE — no customer with ID 99
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> customerService.getCustomerById(99L))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    @DisplayName("Get all customers — returns list")
    void getAllCustomers_ReturnsList() {
        // ARRANGE
        when(customerRepository.findAll()).thenReturn(List.of(savedCustomer));

        // ACT
        List<CustomerResponse> result = customerService.getAllCustomers();

        // ASSERT
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("vaibhavi@email.com");
    }

    // -----------------------------------------------------------------------
    // UPDATE CUSTOMER TESTS
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Update customer — success")
    void updateCustomer_Success() {
        // ARRANGE
        CustomerRequest updateRequest = CustomerRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .email("vaibhavi@email.com") // same email — no duplicate check needed
                .phone("9876543210")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        // ACT
        CustomerResponse response = customerService.updateCustomer(1L, updateRequest);

        // ASSERT
        assertThat(response).isNotNull();
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Update customer — not found throws CustomerNotFoundException")
    void updateCustomer_NotFound_ThrowsException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateCustomer(99L, validRequest))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // DELETE CUSTOMER TESTS
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Delete customer — success")
    void deleteCustomer_Success() {
        // ARRANGE
        when(customerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(1L);

        // ACT — should not throw
        assertThatCode(() -> customerService.deleteCustomer(1L))
                .doesNotThrowAnyException();

        // ASSERT — verify deleteById was called
        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete customer — not found throws CustomerNotFoundException")
    void deleteCustomer_NotFound_ThrowsException() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> customerService.deleteCustomer(99L))
                .isInstanceOf(CustomerNotFoundException.class);

        verify(customerRepository, never()).deleteById(anyLong());
    }
}
