package darkchoco.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class Application {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public JobExecutionDecider itemCorrectnessdecider() {
        return new ItemCorrectnessDecider();
    }

    @Bean
    public JobExecutionDecider decider() {
        return new DeliveryDecider();
    }

    @Bean
    public Step thanksCustomerStep() {
        return this.stepBuilderFactory.get("thanksCustomerStep")
                .tasklet( (contribution, chunkContext) -> {
                    System.out.println("Thank you for using our service.");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step refundItemStep() {
        return this.stepBuilderFactory.get("refundItemStep")
                .tasklet( (contribution, chunkContext) -> {
                    System.out.println("Customer claims for refund.");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step leaveAtDoorStep() {
        return this.stepBuilderFactory.get("leaveAtDoorStep")
                .tasklet( (contribution, chunkContext) -> {
                    System.out.println("Leave the package at the door.");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step storePackageStep() {
        return this.stepBuilderFactory.get("storePackageStep")
                .tasklet( (contribution, chunkContext) -> {
                    System.out.println("Storing the package while the customer address is located.");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step givePackageToCustomerStep() {
        return this.stepBuilderFactory.get("givePackageToCustomerStep")
                .tasklet( (contribution, chunkContext) -> {
                    System.out.println("Given the package to the customer.");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step driveToAddressStep() {
        boolean GOT_LOST = false;

        return this.stepBuilderFactory.get("driveToAddressStep")
                .tasklet( (contribution, chunkContext) -> {
                    if (GOT_LOST)
                        throw new RuntimeException("Got lost driving to the address");
                    System.out.println("Successfully arrived at the address.");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step packageItemStep() {
        // Tasklet is a particular type of step. It has one method on its interface, execute.
        // And that method will get called over and over again until the tasklet signals that it has been completed.
        // (???????????? ?????? RepeatStatus.FINISHED)
        return this.stepBuilderFactory.get("packageItemStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                String item = chunkContext.getStepContext().getJobParameters().get("item").toString();
                String date = chunkContext.getStepContext().getJobParameters().get("run.date").toString();

                System.out.printf("The %s has been packaged on %s.%n", item, date);
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Job deliverPackageJob() {
        return this.jobBuilderFactory.get("deliverPackageJob")
                .start(packageItemStep())
                .next(driveToAddressStep())
                    .on("FAILED").to(storePackageStep())
                .from(driveToAddressStep())
                    .on("*").to(decider())
                        .on("PRESENT").to(givePackageToCustomerStep())
//                            .on("*").to(itemCorrectnessdecider())
                            .next(itemCorrectnessdecider())
                                .on("CORRECT").to(thanksCustomerStep())
                            .from(itemCorrectnessdecider())
                                .on("INCORRECT").to(refundItemStep())
                    .from(decider())
                        .on("NOT PRESENT").to(leaveAtDoorStep())
                .end()
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
