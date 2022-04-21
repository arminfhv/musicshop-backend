package at.fhv.ae.backend;

import at.fhv.ae.backend.application.BasketService;
import at.fhv.ae.backend.application.BroadcastService;
import at.fhv.ae.backend.application.ReleaseSearchService;
import at.fhv.ae.backend.application.SellService;
import at.fhv.ae.backend.application.impl.BasketServiceImpl;
import at.fhv.ae.backend.application.impl.BroadcastServiceImpl;
import at.fhv.ae.backend.application.impl.ReleaseServiceImpl;
import at.fhv.ae.backend.application.impl.SellServiceImpl;
import at.fhv.ae.backend.domain.repository.*;
import at.fhv.ae.backend.infrastructure.*;
import at.fhv.ae.backend.middleware.common.CredentialsService;
import at.fhv.ae.shared.repository.CustomerRepository;
import lombok.SneakyThrows;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.rmi.Naming;
import java.util.Hashtable;
import java.util.Map;

public class ServiceRegistry {



    private ServiceRegistry() { } // hide public constructor

    // repositories / persistence

    private static EntityManager entityManager;

    private static BasketRepository basketRepository;

    private static ReleaseRepository releaseRepository;

    private static SaleRepository saleRepository;

    private static WorkRepository workRepository;

    private static UserRepository userRepository;


    // application services

    private static ReleaseSearchService releaseService;

    private static BasketService basketService;

    private static SellService sellService;

    private static CredentialsService credentialsService;

    // remote customer db services

    private static CustomerRepository customerRepository;

    // messaging

    private static BroadcastService broadcastService;

    private static ConnectionFactory jmsConnectionFactory;

    private static JmsMessageProducer jmsMessageProducer;


    public static EntityManager entityManager() {
        if(entityManager == null) {
            entityManager = Persistence.createEntityManagerFactory("Test").createEntityManager();
        }
        return entityManager;
    }

    public static BasketRepository basketRepository() {
        if (basketRepository == null) {
            basketRepository = new HashMapBasketRepository();
        }
        return basketRepository;
    }

    public static ReleaseRepository releaseRepository() {
        if(releaseRepository == null) {
            releaseRepository = new HibernateReleaseRepository(entityManager());
        }
        return releaseRepository;
    }

    public static SaleRepository saleRepository() {
        if (saleRepository == null) {
            saleRepository = new HibernateSaleRepository(entityManager());
        }
        return saleRepository;
    }

    public static WorkRepository workRepository() {
        if(workRepository == null) {
            workRepository = new HibernateWorkRepository(entityManager());
        }
        return workRepository;
    }

    public static UserRepository userRepository() {
        if(userRepository == null) {
            userRepository = new HibernateUserRepository(entityManager());
        }
        return userRepository;
    }


    public static ReleaseSearchService releaseService() {
        if(releaseService == null) {
            releaseService = new ReleaseServiceImpl(releaseRepository(), workRepository());
        }
        return releaseService;
    }

    public static BasketService basketService() {
        if(basketService == null) {
            basketService = new BasketServiceImpl(basketRepository(), releaseRepository(), userRepository());
        }
        return basketService;
    }

    public static SellService sellService() {
        if(sellService == null) {
            sellService = new SellServiceImpl(saleRepository(), basketRepository(), userRepository(), entityManager());
        }
        return sellService;
    }



    public static CustomerRepository customerRepository() {
        if (customerRepository == null) {
            try {
                customerRepository = (CustomerRepository) Naming.lookup("rmi://localhost:10990/customer-repository"); // todo change url
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("Couldn't connect to Customer DB!");
            }
        }
        return customerRepository;
    }

    public static BroadcastService broadcastService() {
        if(broadcastService == null) {
            broadcastService = new BroadcastServiceImpl(jmsMessageProducer());
        }
        return broadcastService;
    }

    public static ConnectionFactory jmsConnectionFactory() {

        if(jmsConnectionFactory == null) {
            jmsConnectionFactory = new ActiveMQConnectionFactory("tcp://10.0.40.160:61616");
        }

        return jmsConnectionFactory;
    }

    @SneakyThrows
    public static JmsMessageProducer jmsMessageProducer() {
        if(jmsMessageProducer == null) {

            var env = new Hashtable<>(Map.of(
                    Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory",
                    Context.PROVIDER_URL, "vm://10.0.40.160",
                    "topic.System", "SystemTopic",
                    "topic.Pop", "PopTopic",
                    "topic.Rock", "RockTopic"
            ));

            jmsMessageProducer = new JmsMessageProducer(new InitialContext(env), jmsConnectionFactory());
        }

        return jmsMessageProducer;
    }

}
