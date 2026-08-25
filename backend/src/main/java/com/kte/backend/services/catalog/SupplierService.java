package com.kte.backend.services.catalog;

import com.kte.backend.models.dto.request.SupplierRequest;
import com.kte.backend.models.dto.response.SupplierResponse;
import com.kte.backend.services.CrudServices;

public interface SupplierService extends CrudServices<SupplierRequest, SupplierResponse, String> {
}
