package com.starline.resi.batch.writer;

import com.starline.resi.dto.ResiUpdateResult;
import com.starline.resi.model.Resi;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@StepScope
public class ResiItemJpaWriter implements ItemWriter<ResiUpdateResult> {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void write(Chunk<? extends ResiUpdateResult> items) {
        for (ResiUpdateResult item : items) {
            if (item.isUpdated()) {
                Resi resi = entityManager.find(Resi.class, item.getTrackingNumber());
                if (resi != null) {
                    resi.setLastCheckpoint(item.getNewCheckpoint());
                    resi.setLastCheckpointUpdate(item.getProcessedAt());
                }
            }
        }
        entityManager.flush();
        entityManager.clear(); // prevent memory issues
    }
}
