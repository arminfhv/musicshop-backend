package at.fhv.ae.backend.application;

import at.fhv.ae.backend.application.dto.SaleItemsDTO;
import at.fhv.ae.backend.application.exceptions.OutOfStockException;
import org.bson.types.ObjectId;

import javax.ejb.Local;
import java.util.List;

@Local
public interface SellService {

    /**
     * @param customerId can be null if sale is anonymous
     */
    void sellItemsInBasket(String userId, ObjectId customerId) throws OutOfStockException;

    List<SaleItemsDTO> allSales();

}
