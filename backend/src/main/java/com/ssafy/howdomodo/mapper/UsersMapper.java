package com.ssafy.howdomodo.mapper;

import java.sql.SQLException;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.howdomodo.domain.Users;

@Mapper
public interface UsersMapper {
	
	public int join(Users user) throws SQLException;
}