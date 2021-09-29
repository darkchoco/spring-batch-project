package darkchoco.springbatch;

import org.springframework.batch.item.database.ItemPreparedStatementSetter;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrderItemPreparedStatementSetter implements ItemPreparedStatementSetter<Order> {

    @Override
    public void setValues(Order order, PreparedStatement ps) throws SQLException {
        ps.setLong(1, order.getOrderId());
        ps.setString(2, order.getFirstName());
        ps.setString(3, order.getLastName());
        ps.setString(4, order.getEmail());
        ps.setBigDecimal(5, order.getCost());
        ps.setString(6, order.getItemId());
        ps.setString(7, order.getItemName());
        ps.setDate(8, new Date(order.getShipDate().getTime()));
    }
}
