package com.zh.programmer.dao;

import com.zh.programmer.domain.Dormitory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 宿舍数据库操作
 */
public class DormitoryDao extends BaseDao<Dormitory> {

    /**
     * 检擦宿舍是否住满
     * @param dormitoryId
     * @return
     */
    public boolean isFull(int dormitoryId) {

        String sql = "select max_number,lived_number from dormitory where id = " + dormitoryId;
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            ResultSet executeQuery = preparedStatement.executeQuery();
            if (executeQuery.next()){
                return executeQuery.getInt("lived_number") >= executeQuery.getInt("max_number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateLivedNumber(int dormitoryId,int number){
        String sql = " update dormitory set lived_number = lived_number ";
        if (number > 0){
            sql += " + " + number + " where id = " + dormitoryId ;
        }else{
            sql += " - " + Math.abs(number) + " where id = " + dormitoryId ;
        }
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
