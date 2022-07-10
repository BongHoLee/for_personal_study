package hellojpa.custom;

import hellojpa.Member;
import hellojpa.custom.memberrepository.MemberRepository;

public class TransientClass {

    public static void main(String[] args) {
        MemberRepository memberRepository = new MemberRepository();
        Member member = new Member();
        member.setId(200L);
        member.setName("leebongho2");
        Long insertedId = memberRepository.insert(member);
        Member findMember = memberRepository.findById(insertedId);
        Member andUpdate = memberRepository.findAndUpdateData(insertedId);
    }
}
