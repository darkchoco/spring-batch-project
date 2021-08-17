package darkchoco.springbatch;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

import java.util.Random;

public class ItemCorrectnessDecider implements JobExecutionDecider {
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        // https://stackoverflow.com/questions/8183840/probability-in-java
        String result = new Random().nextInt(100) <= 70 ? "CORRECT" : "INCORRECT";
        System.out.printf("Item is %s. %n", result);
        return new FlowExecutionStatus(result);
    }
}
