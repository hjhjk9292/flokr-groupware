package com.kh.flokrGroupware.attendance.model.vo;

import java.time.Duration;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class WeeklySummary {

    private int weekNumber;
    private LocalDate startDate;
    private LocalDate endDate;

    private Duration totalDuration = Duration.ZERO;

    private long totalSeconds = 0;       // 누적 시간(초)
    private long overtimeSeconds = 0;    // 초과 시간(초)

    public WeeklySummary(int weekNumber, LocalDate startDate, LocalDate endDate) {
        this.weekNumber = weekNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalDuration = Duration.ZERO;
    }

    public void addDuration(Duration d) {
        this.totalDuration = this.totalDuration.plus(d);
        this.totalSeconds = this.totalDuration.getSeconds();

        // 35시간 = 126000초
        if (this.totalSeconds > 126000) {
            this.overtimeSeconds = this.totalSeconds - 126000;
        } else {
            this.overtimeSeconds = 0;
        }
    }
}
