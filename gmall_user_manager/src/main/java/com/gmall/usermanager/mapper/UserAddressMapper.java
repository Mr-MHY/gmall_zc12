package com.gmall.usermanager.mapper;

import com.gmall.bean.UserAddress;

import java.util.List;

public interface UserAddressMapper {
    List<UserAddress> select(UserAddress userAddress);
}
