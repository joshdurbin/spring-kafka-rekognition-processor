package io.durbs.face.processor.repository.listener;

import io.durbs.face.processor.repository.entity.BaseResponseEntity;

import javax.persistence.PrePersist;
import java.util.Date;

public class DateSettingEntityListener {

    @PrePersist
    void prePersist(BaseResponseEntity baseResponseEntity) {
        baseResponseEntity.setCreatedDate(new Date());
    }
}
