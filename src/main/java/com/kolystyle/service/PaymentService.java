package com.kolystyle.service;

import com.kolystyle.domain.Payment;
import com.kolystyle.domain.UserPayment;

public interface PaymentService {

	Payment setByUserPayment(UserPayment userPayment, Payment payment);
}
