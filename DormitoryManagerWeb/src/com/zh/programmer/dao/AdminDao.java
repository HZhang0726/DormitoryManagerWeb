package com.zh.programmer.dao;

import com.zh.programmer.domain.Admin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDao extends BaseDao<Admin> {

    /**
     * 根据用户名查找用户信息
     * @param name
     * @return
     */
    public Admin getAdmin(String name){
        Admin admin = null;
        String sql = "select * from admin where name='"+ name +"'";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            ResultSet executeQuery = preparedStatement.executeQuery();
            if (executeQuery.next()){
                admin = new Admin();
                admin.setId(executeQuery.getInt("id"));
                admin.setName(executeQuery.getString("name"));
                admin.setPassword(executeQuery.getString("password"));
                admin.setStatus(executeQuery.getInt("status"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return admin;
    }
}
