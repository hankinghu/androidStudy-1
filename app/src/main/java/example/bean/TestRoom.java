package example.bean;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "test_room")
public class TestRoom {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String id;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "phone")
    public String phone;
    @ColumnInfo(name = "sex")
    public int sex;
    @ColumnInfo(name = "updateTime")
    public long updateTime;
    @Ignore
    public int age;

    public TestRoom() {
    }

    public TestRoom(@NonNull String id, String name, String phone, int sex, long updateTime, int age) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.sex = sex;
        this.updateTime = updateTime;
        this.age = age;
    }

    @Override
    public String toString() {
        return "TestRoom{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", sex=" + sex +
                ", updateTime=" + updateTime +
                ", age=" + age +
                '}';
    }
}
