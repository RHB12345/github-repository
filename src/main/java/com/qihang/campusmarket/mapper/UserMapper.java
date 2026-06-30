package com.qihang.campusmarket.mapper;

import com.qihang.campusmarket.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper {

    @Insert("""
            INSERT INTO users(student_no, real_name, nickname, phone, email, password_hash, avatar_url, bio, campus, dormitory, role, status)
            VALUES(#{studentNo}, #{realName}, #{nickname}, #{phone}, #{email}, #{passwordHash}, #{avatarUrl}, #{bio}, #{campus}, #{dormitory}, #{role}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    @Select("""
            SELECT * FROM users
            WHERE phone = #{account} OR email = #{account} OR student_no = #{account}
            LIMIT 1
            """)
    User findByAccount(String account);

    @Select("SELECT COUNT(*) FROM users WHERE student_no = #{studentNo}")
    int countByStudentNo(String studentNo);

    @Select("SELECT COUNT(*) FROM users WHERE phone = #{phone}")
    int countByPhone(String phone);

    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    int countByEmail(String email);

    @Update("""
            UPDATE users
            SET nickname = #{nickname},
                phone = #{phone},
                email = #{email},
                dormitory = #{dormitory},
                bio = #{bio},
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int updateProfile(User user);

    @Update("""
            UPDATE users
            SET password_hash = #{passwordHash},
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    @Select("""
            SELECT *
            FROM users
            ORDER BY created_at DESC
            LIMIT #{limit}
            """)
    List<User> findLatest(@Param("limit") int limit);
}
