package br.org.germinare.equipamentos.loan;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    long countByBorrowerIdAndReturnedAtIsNull(UUID borrowerId);

    @Query("""
        select count(l) > 0 from Loan l
         where l.borrowerId = :borrowerId and l.returnedAt is null and l.dueAt < current_timestamp
        """)
    boolean hasOverdueOpenLoan(@Param("borrowerId") UUID borrowerId);

    @Query("""
        select new br.org.germinare.equipamentos.loan.LoanView(
                   l.id, e.assetCode, e.name, l.dueAt,
                   case when l.dueAt < current_timestamp then true else false end)
          from Loan l, Equipment e
         where e.id = l.equipmentId and l.borrowerId = :borrowerId and l.returnedAt is null
         order by l.dueAt, l.id
        """)
    List<LoanView> findOpenViews(@Param("borrowerId") UUID borrowerId);
}
