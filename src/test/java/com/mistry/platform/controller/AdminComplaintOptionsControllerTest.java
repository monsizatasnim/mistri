package com.mistry.platform.controller;

import com.mistry.platform.entity.ComplaintCategory;
import com.mistry.platform.entity.ComplaintStatus;
import com.mistry.platform.entity.ResolutionDecision;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminComplaintOptionsControllerTest {

    private final Map<String, List<String>> options = new AdminComplaintOptionsController().options();

    @Test
    void listsEveryResolutionDecision() {
        assertEquals(Arrays.stream(ResolutionDecision.values()).map(Enum::name).toList(), options.get("decisions"));
        assertTrue(options.get("decisions").contains("REFUND_ISSUED"));
    }

    @Test
    void listsEveryComplaintCategory() {
        assertEquals(Arrays.stream(ComplaintCategory.values()).map(Enum::name).toList(), options.get("categories"));
        assertTrue(options.get("categories").contains("NO_SHOW"));
    }

    @Test
    void listsEveryComplaintStatusInTheirOrder() {
        assertEquals(List.of("SUBMITTED", "UNDER_REVIEW", "RESOLVED", "CLOSED"), options.get("statuses"));
    }
}
