package com.mistry.platform.dto;

import com.mistry.platform.entity.StockStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockStatusRequest {

    @NotNull(message = "Stock status is required (AVAILABLE or OUT_OF_STOCK)")
    private StockStatus stockStatus;
}
