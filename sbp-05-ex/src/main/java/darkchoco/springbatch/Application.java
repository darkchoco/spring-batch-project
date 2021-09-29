package darkchoco.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;

@SpringBootApplication
@EnableBatchProcessing
public class Application {

    // order_id, first_name... 식이 아니다!
//    public static String[] names = new String[] {"orderId", "firstName", "lastName", "email",
//            "cost", "itemId", "itemName", "shipDate"};

    public static String INSERT_ORDER_SQL = "INSERT INTO "
            + "SHIPPED_ORDER_OUTPUT (order_id, first_name, last_name, email, cost, item_id, item_name, ship_date) "
//            + "VALUES(?,?,?,?,?,?,?,?) ";  // 이렇게 ordinal values + OrderItemPreparedStatementSetter 대신 아래같이 named parameters를 사용하자.
            + "VALUES(:orderId, :firstName, :lastName, :email, :cost, :itemId, :itemName, :shipDate)";

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    @Bean
    public ItemWriter<Order> itemWriter() {
        return new JsonFileItemWriterBuilder<Order>()
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .resource(
                        new FileSystemResource("Z:\\lab_ext\\java-spring\\spring-batch\\spring-batch-project\\data\\shipped_orders_output.json"))
                .name("jsonItemWriter")
                .build();
    }

    @Bean
    public PagingQueryProvider queryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();

        factoryBean.setSelectClause("SELECT order_id, first_name, last_name, email, cost, item_id, item_name, ship_date");
        factoryBean.setFromClause("FROM SHIPPED_ORDER");
        factoryBean.setSortKey("order_id");  // A sortKey causes the database to return the data in the specified order,
                                             // so a Job can be restarted from a particular point in the dataset.
        factoryBean.setDataSource(dataSource);

        return factoryBean.getObject();  // This factory does throw an exception. So we're going to need to propagate
                                         // that exception through our call stack, in order to be able to handle it.
                                         // 즉 이 method signature에 throw Exception 추가하고 --> itemReader() -->
                                         // chunkBasedStep() --> job() 순으로 Exception을 추가한다.
    }

    @Bean
    public ItemReader<Order> itemReader () throws Exception {
        return new JdbcPagingItemReaderBuilder<Order>()
                .dataSource(dataSource)
                .name("jdbcCursorItemReader")
                .queryProvider(queryProvider())
                .rowMapper(new OrderRowMapper())
                .pageSize(90)  // chunkSize와 똑같게 해야한다.
                .build();
    }

    @Bean
    public Step chunkBasedStep() throws Exception {
        return this.stepBuilderFactory.get("chunkBasedStep")
                .<Order, Order>chunk(90)
                .reader(itemReader())
                .writer(itemWriter()).build();
    }

    @Bean
    public Job job() throws Exception {
        return this.jobBuilderFactory.get("job")
                .start(chunkBasedStep())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
