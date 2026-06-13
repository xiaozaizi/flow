package com.example.countersign;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/countersign")
public class CountersignController {

    private final CountersignService service;

    public CountersignController(CountersignService service) { this.service = service; }

    @PostMapping("/create")
    public CountersignTask create(@RequestParam String taskId, @RequestParam String processInstanceId, @RequestParam(defaultValue = "ALL") String rule, @RequestParam(required = false) Double percent, @RequestParam(required = false) Integer expectedVotes) {
        return service.create(taskId, processInstanceId, rule, percent, expectedVotes);
    }

    @PostMapping("/{csId}/vote")
    public CountersignVote vote(@PathVariable Long csId, @RequestParam String voter, @RequestParam String vote) {
        return service.vote(csId, voter, vote);
    }

    @GetMapping("/{csId}/stats")
    public Map<String, Object> stats(@PathVariable Long csId) {
        return service.stats(csId);
    }

    @PostMapping("/{csId}/timeout")
    public Map<String, Object> timeout(@PathVariable Long csId) {
        service.timeout(csId);
        return Map.of("status", "ok");
    }
}
