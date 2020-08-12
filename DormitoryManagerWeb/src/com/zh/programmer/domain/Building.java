package com.zh.programmer.domain;

/**
 * 楼宇实体
 */
public class Building {
    private int id;
    private String name;//楼宇名称
    private String location;//楼宇所属位置
    private int dormitoryManagerId;//所属宿管

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getDormitoryManagerId() {
        return dormitoryManagerId;
    }

    public void setDormitoryManagerId(int dormitoryManagerId) {
        this.dormitoryManagerId = dormitoryManagerId;
    }

    @Override
    public String toString() {
        return "Building{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", dormitoryManagerId=" + dormitoryManagerId +
                '}';
    }
}
