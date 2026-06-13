package com.example.countersign;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/countersign")
public class CountersignController {

    private final CountersignService service;

    public CountersignController(CountersignService service) { this.service = service; }

    @PostMapping("/create")
    public CountersignTask create(@RequestParam String taskId, @RequestParam String processInstanceId, @RequestParam(defaultValue = "ALL") String rule, @RequestParam(required = false) Double percent) {
        return service.create(taskId, processInstanceId, rule, percent);
    }

    @PostMapping("/{csId}/vote")
    public CountersignVote vote(@PathVariable Long csId, @RequestParam String voter, @RequestParam String vote) {
        return service.vote(csId, voter, vote);
    }
}
