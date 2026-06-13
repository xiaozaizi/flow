package com.example.countersign;

import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CountersignService {

    private final CountersignTaskRepository taskRepo;
    private final CountersignVoteRepository voteRepo;
    private final TaskService taskService;

    public CountersignService(CountersignTaskRepository taskRepo, CountersignVoteRepository voteRepo, TaskService taskService) {
        this.taskRepo = taskRepo; this.voteRepo = voteRepo; this.taskService = taskService;
    }

    public CountersignTask create(String taskId, String processInstanceId, String rule, Double percent, Integer expectedVotes) {
        CountersignTask t = new CountersignTask(taskId, processInstanceId, rule, percent, expectedVotes);
        return taskRepo.save(t);
    }

    public CountersignVote vote(Long csTaskId, String voter, String vote) {
        CountersignVote v = new CountersignVote(csTaskId, voter, vote);
        voteRepo.save(v);
        evaluate(csTaskId);
        return v;
    }

    @Transactional
    public void evaluate(Long csTaskId) {
        CountersignTask t = taskRepo.findById(csTaskId).orElseThrow();
        if (t.isCompleted()) return; // already completed
        List<CountersignVote> votes = voteRepo.findByCountersignTaskId(csTaskId);
        long approvals = votes.stream().filter(v->"APPROVE".equalsIgnoreCase(v.getVote())).count();
        long rejects = votes.stream().filter(v->"REJECT".equalsIgnoreCase(v.getVote())).count();
        long abstains = votes.stream().filter(v->"ABSTAIN".equalsIgnoreCase(v.getVote())).count();
        long totalVotes = votes.size();

        boolean complete=false;
        String rule = t.getRule() == null ? "ALL" : t.getRule();
        switch (rule.toUpperCase()) {
            case "ALL":
                // ALL: require no rejects and (if expectedVotes set) all participants have voted
                if (rejects==0) {
                    if (t.getExpectedVotes() == null) {
                        // cannot determine completion until explicit signal; for PoC, complete when any approval and no rejects
                        complete = approvals>0;
                    } else {
                        complete = (totalVotes >= t.getExpectedVotes());
                    }
                }
                break;
            case "ANY":
                complete = approvals>0;
                break;
            case "MAJORITY":
                if (totalVotes>0) complete = approvals>rejects;
                break;
            case "PERCENT":
                double target = t.getPercent() == null ? 1.0 : t.getPercent();
                if (t.getExpectedVotes() != null && t.getExpectedVotes()>0) {
                    complete = ((double)approvals / (double)t.getExpectedVotes()) >= target;
                } else if (totalVotes>0) {
                    complete = ((double)approvals / (double)totalVotes) >= target;
                }
                break;
            default:
                break;
        }
        if (complete) {
            // complete associated Flowable task if still present
            Task task = taskService.createTaskQuery().taskId(t.getTaskId()).singleResult();
            if (task != null) taskService.complete(task.getId());
            t.setCompleted(true);
            taskRepo.save(t);
        }
    }

    public Map<String, Object> stats(Long csTaskId) {
        CountersignTask t = taskRepo.findById(csTaskId).orElseThrow();
        List<CountersignVote> votes = voteRepo.findByCountersignTaskId(csTaskId);
        long approvals = votes.stream().filter(v->"APPROVE".equalsIgnoreCase(v.getVote())).count();
        long rejects = votes.stream().filter(v->"REJECT".equalsIgnoreCase(v.getVote())).count();
        long abstains = votes.stream().filter(v->"ABSTAIN".equalsIgnoreCase(v.getVote())).count();
        long totalVotes = votes.size();
        return Map.of(
                "countersignId", csTaskId,
                "approvals", approvals,
                "rejects", rejects,
                "abstains", abstains,
                "totalVotes", totalVotes,
                "expectedVotes", t.getExpectedVotes(),
                "completed", t.isCompleted()
        );
    }

    @Transactional
    public void timeout(Long csTaskId) {
        CountersignTask t = taskRepo.findById(csTaskId).orElseThrow();
        if (t.isCompleted()) return;
        Integer expected = t.getExpectedVotes();
        if (expected == null || expected <= 0) {
            // nothing to do: no expected count configured
            return;
        }
        List<CountersignVote> votes = voteRepo.findByCountersignTaskId(csTaskId);
        long totalVotes = votes.size();
        int missing = expected - (int)totalVotes;
        if (missing <= 0) return;
        // create ABSTAIN votes for missing participants (voter=null)
        for (int i=0;i<missing;i++) {
            CountersignVote v = new CountersignVote(csTaskId, null, "ABSTAIN");
            voteRepo.save(v);
        }
        // re-evaluate
        evaluate(csTaskId);
    }
}
