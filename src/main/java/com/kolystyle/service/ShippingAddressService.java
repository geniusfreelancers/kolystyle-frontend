package com.kolystyle.service;

import com.kolystyle.domain.ShippingAddress;
import com.kolystyle.domain.UserShipping;

public interface ShippingAddressService {

	ShippingAddress setByUserShipping(UserShipping userShipping, ShippingAddress shippingAddress);
}
