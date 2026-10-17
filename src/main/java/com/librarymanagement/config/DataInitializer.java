package com.librarymanagement.config;

import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.Member;
import com.librarymanagement.entity.MemberStatus;
import com.librarymanagement.repository.BookRepository;
import com.librarymanagement.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    @Override
    public void run(String... args) {
        if (bookRepository.count() == 0) {
            log.info("Seeding initial book catalog into database...");
            List<Book> initialBooks = List.of(
                    Book.builder()
                            .title("Clean Code: A Handbook of Agile Software Craftsmanship")
                            .author("Robert C. Martin")
                            .isbn("9780132350884")
                            .category("Software Engineering")
                            .totalCopies(5)
                            .availableCopies(5)
                            .publicationYear(2008)
                            .build(),
                    Book.builder()
                            .title("Design Patterns: Elements of Reusable Object-Oriented Software")
                            .author("Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides")
                            .isbn("9780201633610")
                            .category("Software Architecture")
                            .totalCopies(3)
                            .availableCopies(3)
                            .publicationYear(1994)
                            .build(),
                    Book.builder()
                            .title("The Pragmatic Programmer: Your Journey To Mastery")
                            .author("David Thomas, Andrew Hunt")
                            .isbn("9780135957059")
                            .category("Software Engineering")
                            .totalCopies(4)
                            .availableCopies(4)
                            .publicationYear(2019)
                            .build(),
                    Book.builder()
                            .title("Effective Java: Best Practices for the Java Platform")
                            .author("Joshua Bloch")
                            .isbn("9780134685991")
                            .category("Java Programming")
                            .totalCopies(5)
                            .availableCopies(5)
                            .publicationYear(2018)
                            .build()
            );
            bookRepository.saveAll(initialBooks);
            log.info("Successfully seeded {} books.", initialBooks.size());
        }

        if (memberRepository.count() == 0) {
            log.info("Seeding initial library members into database...");
            List<Member> initialMembers = List.of(
                    Member.builder()
                            .name("Alice Johnson")
                            .email("alice.johnson@example.com")
                            .phone("+1-555-0199")
                            .membershipDate(LocalDate.now().minusMonths(6))
                            .status(MemberStatus.ACTIVE)
                            .build(),
                    Member.builder()
                            .name("Bob Smith")
                            .email("bob.smith@example.com")
                            .phone("+1-555-0248")
                            .membershipDate(LocalDate.now().minusMonths(3))
                            .status(MemberStatus.ACTIVE)
                            .build(),
                    Member.builder()
                            .name("Charlie Davis")
                            .email("charlie.davis@example.com")
                            .phone("+1-555-0371")
                            .membershipDate(LocalDate.now().minusMonths(1))
                            .status(MemberStatus.ACTIVE)
                            .build()
            );
            memberRepository.saveAll(initialMembers);
            log.info("Successfully seeded {} members.", initialMembers.size());
        }
    }
}
