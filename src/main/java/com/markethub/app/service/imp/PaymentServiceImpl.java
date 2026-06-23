package com.markethub.app.service.imp;

import com.markethub.app.exception.ResourceNotFoundException;
import com.markethub.app.model.Payment;
import com.markethub.app.repository.PaymentRepository;
import com.markethub.app.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    @Transactional
    public Payment addNewPayment(Payment payment) {
        log.info("Adding payment type={}", payment.getType());
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findPaymentById(long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    @Override
    @Transactional
    public Payment updateById(long id, Payment payment) {
        payment.setPaymentId(id);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void deletePaymentById(long id) {
        paymentRepository.deleteById(id);
    }
}
