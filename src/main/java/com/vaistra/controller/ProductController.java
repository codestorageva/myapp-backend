package com.vaistra.controller;

import com.vaistra.dto.ProductDto;
import com.vaistra.dto.response.DataResponse;
import com.vaistra.dto.response.HttpResponse;
import com.vaistra.dto.response.MessageResponse;
import com.vaistra.dto.update.ProductUpdateDto;
import com.vaistra.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @PostMapping
    public ResponseEntity<MessageResponse> addProduct(@Valid @RequestBody ProductDto product,@RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(productService.addProduct(product,headers), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<HttpResponse> getAllProducts(@RequestParam Integer companyId,
                                                       @RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                                       @RequestParam(value = "pageSize", defaultValue = "2147483647", required = false) Integer pageSize,
                                                       @RequestParam(value = "sortBy", defaultValue = "productId", required = false) String sortBy,
                                                       @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
                                                       @RequestParam(value = "isDeleted", defaultValue = "false", required = false) String isDeleted,
                                                       @RequestParam(value = "status", defaultValue = "true", required = false) String status,
                                                       @RequestHeader Map<String, String> headers) {

        return new ResponseEntity<>(productService.getAllProducts(companyId,keyword, pageNumber, pageSize, sortBy, sortDirection, isDeleted, status, headers), HttpStatus.OK);
    }


    @GetMapping("{productId}")
    public ResponseEntity<DataResponse> getProductById(@PathVariable int productId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(productService.getProductById(productId, headers), HttpStatus.OK);
    }



    @PutMapping("{productId}")
    public ResponseEntity<MessageResponse> updateProduct(@RequestBody ProductUpdateDto updateDto, @PathVariable int productId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(productService.updateProduct(updateDto, productId, headers), HttpStatus.OK);
    }


    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
//    @DeleteMapping("hardDelete/{productId}")
//    public ResponseEntity<MessageResponse> deleteProductById(@PathVariable int productId, @RequestHeader Map<String, String> headers) {
//        return new ResponseEntity<>(productService.deleteProductById(productId, headers), HttpStatus.OK);
//    }


    @PutMapping("softDelete/{productId}")
    public ResponseEntity<MessageResponse> softDeleteProductById(@PathVariable int productId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(productService.softDeleteProductById(productId, headers), HttpStatus.OK);
    }

    @PutMapping("restore/{productId}")
    public ResponseEntity<MessageResponse> restoreProductById(@PathVariable int productId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(productService.restoreProductById(productId, headers), HttpStatus.OK);
    }
}

