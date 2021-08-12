package darkchoco.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
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
        // (여기서는 아래 RepeatStatus.FINISHED)
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
                    .on("*").to(givePackageToCustomerStep())
                .end()
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
