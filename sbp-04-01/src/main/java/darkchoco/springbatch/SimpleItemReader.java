package darkchoco.springbatch;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimpleItemReader implements ItemReader<String> {

    private List<String> data = new ArrayList<>();

    private final Iterator<String> iterator;

    public SimpleItemReader() {
        this.data.add("1");
        this.data.add("2");
        this.data.add("3");
        this.data.add("4");
        this.data.add("5");
        this.iterator = this.data.iterator();
    }

    @Override
    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return iterator.hasNext() ? iterator.next() : null;
    }
}
