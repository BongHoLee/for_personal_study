package com.demo.mapper;

import com.demo.domain.Member;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {
    List<Member> selectAllMembers();
    int insertMember(Member member);
    void removeMember(Member member);
}
