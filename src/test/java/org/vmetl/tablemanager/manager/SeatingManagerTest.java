package org.vmetl.tablemanager.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vmetl.tablemanager.model.CustomerGroup;
import org.vmetl.tablemanager.model.Table;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.List.*;
import static org.junit.jupiter.api.Assertions.*;

class SeatingManagerTest {

    private SeatingManager seatingManager;

    @BeforeEach
    void setUp() {
        List<Table> tables = createRandomTables(100);
        seatingManager = new SeatingManager(tables);
    }

    @Test
    void testArrives() {
        CustomerGroup customerGroup = new CustomerGroup(3, 0);
        seatingManager.arrives(customerGroup);
        Table table = seatingManager.locate(customerGroup);

        assertEquals(0, table.getFreeSeats());
        assertTrue(table.isFull());
    }

    @Test
    void testArrivesWithNoFreePlaces() {

        seatingManager = new SeatingManager(of(new Table(5, 0)));
        CustomerGroup customerGroupOf5 = new CustomerGroup(5, 0);
        CustomerGroup customerGroupOf3 = new CustomerGroup(3, 1);

        seatingManager.arrives(customerGroupOf3);
        Table table = seatingManager.locate(customerGroupOf3);
        assertNotNull(table);

        seatingManager.arrives(customerGroupOf5);
        table = seatingManager.locate(customerGroupOf5);
        assertNull(table);

        seatingManager.leaves(customerGroupOf3); // these should trigger auto-placement of the group-of-5
        table = seatingManager.locate(customerGroupOf5);
        assertNotNull(table);

        assertTrue(table.isFull());
    }

    @Test
    void testLeaves() {
        CustomerGroup customerGroup = new CustomerGroup(3, 0);
        seatingManager.arrives(customerGroup);
        seatingManager.leaves(customerGroup);
        Table table = seatingManager.locate(customerGroup);

        assertNull(table);
    }

    @Test
    void testLocate() {

        CustomerGroup customerGroup = new CustomerGroup(3, 0);
        seatingManager.arrives(customerGroup);
        Table table = seatingManager.locate(customerGroup);

        assertNotNull(table);
        assertTrue(table.isFull());
    }

    @Test
    void testSeatingFlow() {
        List<CustomerGroup> customerGroups = createRandomCustomerGroups(150);

        int beforeCustomersSeats = seatingManager.getFreeSeats();

        customerGroups.forEach(customerGroup -> seatingManager.arrives(customerGroup));

        assertTrue(seatingManager.getWaitingCustomers().size() > 0);

        customerGroups.forEach(customerGroup -> seatingManager.leaves(customerGroup));

        assertEquals(0, seatingManager.getWaitingCustomers().size());
        assertEquals(beforeCustomersSeats, seatingManager.getFreeSeats()); // amount of seats after everybody left
    }

    private static List<CustomerGroup> createRandomCustomerGroups(int groupsNumber) {
        Random random = new Random();

        return IntStream.range(0, groupsNumber).boxed().map(
                number -> new CustomerGroup(random.nextInt(5) + 2, number)).collect(Collectors.toList());
    }


    private static List<Table> createRandomTables(int numberOfTables) {
        Random random = new Random();

        return IntStream.range(0, numberOfTables).boxed().map(
                number -> new Table(random.nextInt(5) + 2, number)).collect(Collectors.toList());
    }
}