package darkchoco.springbatch;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

import java.time.LocalDateTime;

public class DeliveryDecider implements JobExecutionDecider {

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        String result = LocalDateTime.now().getHour() < 12 ? "PRESENT" : "NOT PRESENT";  // 오후에는 부재중으로
        System.out.println("Decider result is: " + result);
        return new FlowExecutionStatus(result);
    }
}
