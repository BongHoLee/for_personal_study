package com.demo;

import java.util.List;
import com.demo.domain.Member;
import com.demo.mapper.MemberMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberController {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @GetMapping("/getMember")
    public String getMember() {
        MemberMapper mapper = sqlSessionTemplate.getMapper(MemberMapper.class);
        List<Member> members = mapper.selectAllMembers();

        return members.toString();
    }
}
