package com.librarymanagement.service;

import com.librarymanagement.dto.MemberRequest;
import com.librarymanagement.dto.MemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {

    MemberResponse registerMember(MemberRequest request);

    MemberResponse getMemberById(Long id);

    MemberResponse getMemberByEmail(String email);

    Page<MemberResponse> getAllMembers(Pageable pageable);

    Page<MemberResponse> searchMembers(String query, Pageable pageable);

    MemberResponse updateMember(Long id, MemberRequest request);

    void deleteMember(Long id);
}
