package hellojpa.custom;

import hellojpa.Member;
import hellojpa.custom.memberrepository.MemberRepository;

public class TransientClass {

    public static void main(String[] args) {
        MemberRepository memberRepository = new MemberRepository();
        Member member = new Member();
        member.setId(400L);
        member.setName("leebongho2");
        Long insertId = memberRepository.insert(member);
        memberRepository.findById(insertId);
        memberRepository.findAndUpdateData(insertId);
        memberRepository.findAndUpdateName(insertId);

    }
}
