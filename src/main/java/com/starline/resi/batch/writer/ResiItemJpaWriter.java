package com.starline.resi.batch.writer;

import com.starline.resi.dto.resi.ResiUpdateResult;
import com.starline.resi.model.Resi;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@StepScope
public class ResiItemJpaWriter implements ItemWriter<ResiUpdateResult> {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void write(Chunk<? extends ResiUpdateResult> items) {
        if (items.isEmpty()) return;

        // Batch fetch all Resi entities at once using IN clause to avoid N+1 query problem
        List<String> trackingNumbers = items.getItems()
                .stream()
                .map(ResiUpdateResult::getTrackingNumber)
                .toList();

        List<Resi> resiList = entityManager.createQuery(
                        "SELECT r FROM Resi r WHERE r.trackingNumber IN :ids", Resi.class)
                .setParameter("ids", trackingNumbers)
                .getResultList();

        Map<String, Resi> resiMap = resiList.stream()
                .collect(Collectors.toMap(Resi::getTrackingNumber, Function.identity()));

        for (ResiUpdateResult item : items) {
            Resi resi = resiMap.get(item.getTrackingNumber());
            if (resi != null) {
                resi.setLastCheckpointUpdate(item.getProcessedAt());
                resi.setCustomUpdateBy("BATCH_JOB");
            }
        }

        entityManager.flush();
        entityManager.clear(); // avoid memory buildup
    }
}
