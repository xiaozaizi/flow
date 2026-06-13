package com.example.countersign;

import org.flowable.engine.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CountersignService {

    private final CountersignTaskRepository taskRepo;
    private final CountersignVoteRepository voteRepo;
    private final TaskService taskService;

    public CountersignService(CountersignTaskRepository taskRepo, CountersignVoteRepository voteRepo, TaskService taskService) {
        this.taskRepo = taskRepo; this.voteRepo = voteRepo; this.taskService = taskService;
    }

    public CountersignTask create(String taskId, String processInstanceId, String rule, Double percent) {
        CountersignTask t = new CountersignTask(taskId, processInstanceId, rule, percent);
        return taskRepo.save(t);
    }

    public CountersignVote vote(Long csTaskId, String voter, String vote) {
        CountersignVote v = new CountersignVote(csTaskId, voter, vote);
        voteRepo.save(v);
        evaluate(csTaskId);
        return v;
    }

    public void evaluate(Long csTaskId) {
        CountersignTask t = taskRepo.findById(csTaskId).orElseThrow();
        List<CountersignVote> votes = voteRepo.findByCountersignTaskId(csTaskId);
        long approvals = votes.stream().filter(v->"APPROVE".equals(v.vote)).count();
        long rejects = votes.stream().filter(v->"REJECT".equals(v.vote)).count();
        long total = votes.size();
        boolean complete=false;
        switch (t.getRule()) {
            case "ALL": complete = rejects==0 && total>0; break;
            case "ANY": complete = approvals>0; break;
            case "MAJORITY": complete = approvals>rejects && total>0; break;
            case "PERCENT": complete = (double)approvals/((double)total)==t.getPercent(); break;
            default: break;
        }
        if (complete) {
            // complete associated Flowable task
            var task = taskService.createTaskQuery().taskId(t.getTaskId()).singleResult();
            if (task != null) taskService.complete(task.getId());
        }
    }
}
