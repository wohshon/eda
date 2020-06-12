package com.redhat.app.inventory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Named;
import javax.persistence.LockModeType;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
@ApplicationScoped
@ActivateRequestContext
public class InventoryRepository implements PanacheRepository<Inventory>{
 

    
    
    public Inventory findByName(String name) {

        return find("name",name).firstResult();
    }

    Logger log = LoggerFactory.getLogger(this.getClass());
    @Transactional
    public Inventory updateStock(Order order, Integer type) throws InventoryException{
        
        Inventory i = find("name",order.getProduct()).withLock(LockModeType.PESSIMISTIC_WRITE).firstResult();
        log.info("-------before---"+order.getProduct()+":"+order.getQty()+":"+order.getStatus()+"----"+i);
        if (type < 0 && i.getStock() < order.getQty()) {
            throw new InventoryException("INVENTORY_INSUFFICIENT_STOCK");
        } 
        i.setStock(i.getStock()+(order.getQty()*type));
        this.persistAndFlush(i);
        log.info("-------after---"+order.getProduct()+":"+order.getQty()+":"+order.getStatus()+"----"+i);

        return i;

    }
}