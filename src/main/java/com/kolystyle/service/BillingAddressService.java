package com.kolystyle.service;

import com.kolystyle.domain.BillingAddress;
import com.kolystyle.domain.UserBilling;

public interface BillingAddressService {
	
	BillingAddress setByUserBilling(UserBilling userBilling, BillingAddress billingAddress);
}
