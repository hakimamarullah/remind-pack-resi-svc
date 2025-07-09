package com.starline.resi.batch.reader;

import com.starline.resi.dto.ResiProjectionImpl;
import com.starline.resi.dto.projection.ResiProjection;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
@StepScope
public class ResiItemReader extends AbstractItemCountingItemStreamItemReader<ResiProjection> {

    private final JdbcTemplate jdbcTemplate;
    private Iterator<ResiProjection> resiIterator;

    @Value("#{jobParameters['cutoffTime']}")
    private String cutoffTime;

    @Value("#{jobParameters['chunkSize'] ?: 1000}")
    private Integer chunkSize;

    public ResiItemReader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        setName("resiItemReader");
    }

    @Override
    protected void doOpen() {
        if (resiIterator == null) {
            List<ResiProjection> resiList = fetchResiData();
            resiIterator = resiList.iterator();
        }
    }

    @Override
    protected ResiProjection doRead() {
        if (resiIterator != null && resiIterator.hasNext()) {
            return resiIterator.next();
        }
        return null;
    }

    @Override
    protected void doClose() {
        resiIterator = null;
    }

    private List<ResiProjection> fetchResiData() {
        String sql = """
                SELECT r.tracking_number, r.user_id, r.last_checkpoint,
                       r.additional_value_1, c.code as courier_id,
                       r.last_checkpoint_update
                FROM resi r
                LEFT JOIN courier c ON r.courier_id = c.id
                WHERE r.last_checkpoint_update IS NULL
                   OR r.last_checkpoint_update < ?::timestamp
                ORDER BY r.last_checkpoint_update NULLS FIRST, r.tracking_number
                """;


        return jdbcTemplate.query(sql,
                (rs, rowNum) ->
                        ResiProjectionImpl.builder()
                                .trackingNumber(rs.getString("tracking_number"))
                                .userId(rs.getLong("user_id"))
                                .lastCheckpoint(rs.getString("last_checkpoint"))
                                .additionalValue1(rs.getString("additional_value_1"))
                                .courierCode(rs.getString("courier_id"))
                                .lastCheckpointUpdate(rs.getTimestamp("last_checkpoint_update") != null ?
                                        rs.getTimestamp("last_checkpoint_update").toLocalDateTime() : null)
                                .build(),
                cutoffTime

        );
    }

}