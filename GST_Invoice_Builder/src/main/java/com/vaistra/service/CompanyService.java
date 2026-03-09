package com.vaistra.service;



import com.vaistra.dto.response.DataResponse;
import com.vaistra.dto.response.HttpResponse;
import com.vaistra.dto.response.MessageResponse;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CompanyService {


    MessageResponse addCompany(
            String companyName,
            String ownerName,
            String password,
            MultipartFile logoFile,

            String email,
            String cinNumber,
            String lrNumber,
            String transport,
            String commissionerate,
            String range,
            String division,
            String mobileNumber,
            String alternateMobileNumber,

            String billingAddress1,
            String billingAddress2,
            String billingAddress3,
            Integer billingStateId,
            Integer billingCityId,
            String billingPincode,

            String panNumber,
            String gstNumber,
            String serviceDescription,
            String industry,
            Boolean status,
            Boolean isDeleted,

            List<String> bankName,
            List<String> ifscCode,
            List<String> branch,
            List<String> accountNumber,
            List<String> accHolderName,
            List<String> bankAddress,
            List<Boolean> bankStatus,
            List<Boolean> bankIsDeleted,

            Map<String, String> headers
    );
    HttpResponse getAllCompanies(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers);

    DataResponse getCompanyById(int companyId, Map<String, String> headers);

        MessageResponse updateCompany(
                String companyName,
                String ownerName,
                String password,
                MultipartFile logoFile,

                String email,
                String cinNumber,
                String lrNumber,
                String transport,
                String commissionerate,
                String range,
                String division,

                String mobileNumber,
                String alternateMobileNumber,

                String billingAddress1,
                String billingAddress2,
                String billingAddress3,
                Integer billingStateId,
                Integer billingCityId,
                String billingPincode,

                String panNumber,
                String gstNumber,
                String serviceDescription,
                String industry,
                Boolean status,
                Boolean isDeleted,

                List<String> bankName,
                List<String> ifscCode,
                List<String> branch,
                List<String> accountNumber,
                List<String> accHolderName,
                List<String> bankAddress,
                List<Boolean> bankStatus,
                List<Boolean> bankIsDeleted,

                int companyId,
                Map<String, String> headers
        );
    MessageResponse softDeleteCompanyById(int companyId, Map<String, String> headers);

    MessageResponse restoreCompanyById(int companyId, Map<String, String> headers);

    MessageResponse uploadLogoPicture(MultipartFile file, int companyId, Map<String, String> headers) throws IOException;

    DataResponse getCompanyPassword(int companyId, Map<String, String> headers);

//    MessageResponse uploadLogoPicture(MultipartFile file,Map<String,String> headers) throws IOException;


}
