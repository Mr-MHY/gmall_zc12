package com.gmall.usermanager.mapper;

import com.gmall.bean.UserAddress;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserAddressMapper extends Mapper<UserAddress> {
    List<UserAddress> select(UserAddress userAddress);
}
