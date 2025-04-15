package com.example.software_management.DTO;

import com.example.software_management.Model.Alert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer alertId;
    private String deviceName;
    private Integer status;
    private String alertDescription;
    private LocalDateTime alertTime;
    private boolean isConfirmed;

    // Additional fields for detailed views
    private Integer componentId;
    private LocalDateTime confirmedTime;
    private String confirmedBy;

    // Fields for Excel export
    private Double hptEffMod;
    private Double nf;
    private Double smFan;
    private Double t24;
    private Double wf;
    private Double t48;
    private Double nc;
    private Double smHPC;

    // Constructor from Alert entity
    public AlertDTO(Alert alert) {
        this.alertId = alert.getId();
        this.status = alert.getStatus().getValue();
        this.alertDescription = alert.getAlertDescription();
        this.alertTime = alert.getAlertTime();
        this.isConfirmed = alert.getIsConfirmed();

        if (alert.getComponent() != null) {
            this.componentId = alert.getComponent().getId();
            this.deviceName = alert.getComponent().getName();
        }

        this.confirmedTime = alert.getConfirmedTime();
        if (alert.getConfirmedBy() != null) {
            this.confirmedBy = alert.getConfirmedBy().getUsername();
        }
    }
}