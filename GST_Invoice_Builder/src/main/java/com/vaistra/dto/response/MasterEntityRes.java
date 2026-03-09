package com.vaistra.dto.response;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class MasterEntityRes {
    private Boolean success;
    HttpStatus successCode;
    private EntityData data;

    @Data
    public static class EntityData{
        private Integer entityId;

        private String vid;

        private Integer userId;

        private String entityName;

        private String fullName;

        private String displayName;

        private String mobNo;

        private String email;

        private LocalDate dateOfCorporation;

        private String uniqueName;

        private Integer roleId;
        private String roleName;

        private String officeNo;

        private String landLineNo;

        private String contactPerson;

        private String contactPersonNo;

        private String addressType;

        private String addressLine1;

        private String addressLine2;

        private String addressLine3;

        private Integer countryId;
        private String countryName;

        private Integer stateId;
        private String stateName;

        private Integer districtId;
        private String districtName;

        private Integer subDistrictId;
        private String subDistrictName;

        private Integer villageId;
        private String villageName;

        private String pinCode;

        private String regiNo;

        private String businessPanNo;

        private String gstNo;

        private List<AssociateData> associates;

        @Data
        private static class AssociateData{
            private Integer associateId;
            private String associateVid;
            private String associateName;
            private String associateStatus;
            private Integer designationId;
            private String designationName;
        }

        private String approvalStatus;

        private Boolean isDeleted;

        private String approvedBy;

        private LocalDateTime approvedAt;

        private String rejectedBy;

        private LocalDateTime rejectedAt;

        private String rejectionReason;

        private String message;
    }

}
