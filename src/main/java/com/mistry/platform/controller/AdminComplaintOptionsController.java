package com.mistry.platform.controller;

import com.mistry.platform.entity.ComplaintCategory;
import com.mistry.platform.entity.ComplaintStatus;
import com.mistry.platform.entity.ResolutionDecision;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The choices the admin dispute screen shows. They come from the enums, so the screen can
 * never offer a decision the backend does not know.
 */
@RestController
@RequestMapping("/api/admin/complaint-options")
public class AdminComplaintOptionsController {

    @GetMapping
    public Map<String, List<String>> options() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        result.put("decisions", names(ResolutionDecision.values()));
        result.put("categories", names(ComplaintCategory.values()));
        result.put("statuses", names(ComplaintStatus.values()));
        return result;
    }

    private static List<String> names(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).toList();
    }
}
