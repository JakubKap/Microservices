package com.mycompany.customer;

import com.amigoscode.notification.NotificationRequest;
import com.mycompany.amqp.RabbitMQMessageProducer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final RestTemplate restTemplate;
    private final RabbitMQMessageProducer rabbitMQMessageProducer;

    public void registerCustomer(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();
        //todo: check if email valid
        //todo: check if email not taken
        //todo: check if fraudster

        customerRepository.saveAndFlush(customer);

        //old approach
//        FraudCheckResponse fraudCheckResponse = restTemplate.getForObject(
//          "http://localhost:8081/api/v1/fraud-check/{customerId}",
//                FraudCheckResponse.class,
//                customer.getId()
//        );

        //todo: can be direct call to FraudClient
        FraudCheckResponse fraudCheckResponse = restTemplate.getForObject(
                "http://FRAUD/api/v1/fraud-check/{customerId}",
                FraudCheckResponse.class,
                customer.getId()
        );

        if (fraudCheckResponse.isFraudster()) {
            throw new IllegalStateException("fraudster");
        }
        //todo: send notification

        final NotificationRequest notificationRequest = new NotificationRequest(
                customer.getId(),
                customer.getEmail(),
                String.format("Hi %s, welcome to MyCompany app...",
                        customer.getFirstName())
        );
        rabbitMQMessageProducer.publish(
                notificationRequest,
                "internal.exchange",
                "internal.notification.routing-key"
        );

    }
}