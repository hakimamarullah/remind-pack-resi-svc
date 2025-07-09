package com.starline.resi.batch.writer;

import com.starline.resi.dto.ResiUpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@StepScope
public class ResiItemWriter implements ItemWriter<ResiUpdateResult> {

    private final JdbcTemplate jdbcTemplate;


    @Override
    public void write(Chunk<? extends ResiUpdateResult> items) {
        String sql = """
            UPDATE resi
            SET last_checkpoint = ?,
                last_checkpoint_update = ?
            WHERE tracking_number = ?
            """;


        List<Object[]> batchArgs = items.getItems().stream()
                .filter(ResiUpdateResult::isUpdated)
                .map(item -> new Object[]{
                        item.getNewCheckpoint(),
                        item.getProcessedAt(),
                        item.getTrackingNumber()
                })
                .collect(Collectors.toList());

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }


}
