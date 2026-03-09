package com.vaistra.service;

import com.vaistra.dto.ProductDto;
import com.vaistra.dto.response.DataResponse;
import com.vaistra.dto.response.HttpResponse;
import com.vaistra.dto.response.MessageResponse;
import com.vaistra.dto.update.ProductUpdateDto;
import org.springframework.web.bind.annotation.RequestHeader;


import java.util.Map;

public interface ProductService {

    MessageResponse addProduct( ProductDto product,Map<String, String> headers);

    HttpResponse getAllProducts( Integer companyId,String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers);

    DataResponse getProductById(int productId, Map<String, String> headers);

    MessageResponse updateProduct(ProductUpdateDto updateDto, int productId, Map<String, String> headers);

    MessageResponse softDeleteProductById(int productId, Map<String, String> headers);

    MessageResponse restoreProductById(int productId, Map<String, String> headers);
}
