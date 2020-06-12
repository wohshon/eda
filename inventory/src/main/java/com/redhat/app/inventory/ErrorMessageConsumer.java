package com.redhat.app.inventory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class ErrorMessageConsumer implements Runnable, MessageListener{
    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    InventoryService inventoryService;

    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();
    
    void onStart(@Observes StartupEvent ev) {
        scheduler.submit(this);
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
    }



    Logger log = LoggerFactory.getLogger(this.getClass());


    @Override
    public void onMessage(Message message) {
        String json;
		try {
            log.info("message "+((TextMessage)message).getText());
            json = ((TextMessage)message).getText();
            this.inventoryService.processError(json);

        } 
        catch (InventoryException ex) {
            log.info("Inventory Exception "+ex);
        }         
        catch (JMSException e) {
			e.printStackTrace();
        }   
    }

    @Override
    public void run() {
        try (JMSContext context = connectionFactory.createContext(Session.SESSION_TRANSACTED)) 
        {
            JMSConsumer errorOrder = context.createConsumer(context.createQueue("order-error-inv"));

            while(true) {
                Thread.sleep(500);

            errorOrder.setMessageListener(this);
            context.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}