package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.DoctorSlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT ds FROM DoctorSlot ds
            WHERE ds.doctorName = :doctorName
            AND ds.slotDate = :slotDate
            AND ds.slotTime = :slotTime
            """)
    Optional<DoctorSlot> findSlotForUpdate(
            @Param("doctorName") String doctorName,
            @Param("slotDate") LocalDate slotDate,
            @Param("slotTime") LocalTime slotTime
    );
}
