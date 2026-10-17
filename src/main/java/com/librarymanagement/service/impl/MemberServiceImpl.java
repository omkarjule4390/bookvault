package com.librarymanagement.service.impl;

import com.librarymanagement.dto.MemberRequest;
import com.librarymanagement.dto.MemberResponse;
import com.librarymanagement.entity.BorrowStatus;
import com.librarymanagement.entity.Member;
import com.librarymanagement.exception.BusinessException;
import com.librarymanagement.exception.DuplicateResourceException;
import com.librarymanagement.exception.ResourceNotFoundException;
import com.librarymanagement.mapper.EntityMapper;
import com.librarymanagement.repository.BorrowRepository;
import com.librarymanagement.repository.MemberRepository;
import com.librarymanagement.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final BorrowRepository borrowRepository;
    private final EntityMapper entityMapper;

    @Override
    public MemberResponse registerMember(MemberRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Registering new member with email: {}", email);

        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Member", "email", email);
        }

        Member member = entityMapper.toMemberEntity(request);
        Member savedMember = memberRepository.save(member);
        log.info("Member successfully registered with ID: {}", savedMember.getId());

        return entityMapper.toMemberResponse(savedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMemberById(Long id) {
        log.debug("Fetching member with ID: {}", id);
        Member member = findMemberOrThrow(id);
        return entityMapper.toMemberResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMemberByEmail(String email) {
        log.debug("Fetching member with email: {}", email);
        Member member = memberRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Member", "email", email));
        return entityMapper.toMemberResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MemberResponse> getAllMembers(Pageable pageable) {
        log.debug("Fetching all members with pagination: {}", pageable);
        return memberRepository.findAll(pageable)
                .map(entityMapper::toMemberResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MemberResponse> searchMembers(String query, Pageable pageable) {
        log.debug("Searching members matching query: '{}'", query);
        String trimmed = query != null ? query.trim() : "";
        return memberRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(trimmed, trimmed, pageable)
                .map(entityMapper::toMemberResponse);
    }

    @Override
    public MemberResponse updateMember(Long id, MemberRequest request) {
        log.info("Updating member with ID: {}", id);
        Member member = findMemberOrThrow(id);

        String trimmedEmail = request.getEmail().trim().toLowerCase();
        if (memberRepository.existsByEmailAndIdNot(trimmedEmail, id)) {
            throw new DuplicateResourceException("Member", "email", trimmedEmail);
        }

        entityMapper.updateMemberEntity(member, request);
        Member updatedMember = memberRepository.save(member);
        log.info("Member with ID: {} successfully updated", updatedMember.getId());

        return entityMapper.toMemberResponse(updatedMember);
    }

    @Override
    public void deleteMember(Long id) {
        log.info("Attempting to delete member with ID: {}", id);
        Member member = findMemberOrThrow(id);

        long activeBorrows = borrowRepository.countByMemberIdAndStatus(id, BorrowStatus.BORROWED);
        if (activeBorrows > 0) {
            throw new BusinessException(String.format(
                    "Cannot delete member '%s' (ID: %d) because they have %d active unreturned book(s)",
                    member.getName(), id, activeBorrows));
        }

        memberRepository.delete(member);
        log.info("Member with ID: {} successfully deleted", id);
    }

    private Member findMemberOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));
    }
}
